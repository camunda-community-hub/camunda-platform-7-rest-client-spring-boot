/*-
 * #%L
 * camunda-platform-7-rest-client-spring-boot
 * %%
 * Copyright (C) 2019 Camunda Services GmbH
 * %%
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 *  under one or more contributor license agreements. See the NOTICE file
 *  distributed with this work for additional information regarding copyright
 *  ownership. Camunda licenses this file to you under the Apache License,
 *  Version 2.0; you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * #L%
 */

package org.camunda.community.rest.variables

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.camunda.bpm.engine.variable.VariableMap
import org.camunda.bpm.engine.variable.Variables
import org.camunda.bpm.engine.variable.Variables.untypedNullValue
import org.camunda.bpm.engine.variable.Variables.untypedValue
import org.camunda.bpm.engine.variable.impl.value.ObjectValueImpl
import org.camunda.bpm.engine.variable.type.FileValueType
import org.camunda.bpm.engine.variable.type.PrimitiveValueType
import org.camunda.bpm.engine.variable.type.SerializableValueType
import org.camunda.bpm.engine.variable.type.ValueType
import org.camunda.bpm.engine.variable.type.ValueTypeResolver
import org.camunda.bpm.engine.variable.value.FileValue
import org.camunda.bpm.engine.variable.value.SerializableValue
import org.camunda.bpm.engine.variable.value.TypedValue
import org.camunda.community.rest.client.model.VariableInstanceDto
import org.camunda.community.rest.client.model.VariableQueryParameterDto
import org.camunda.community.rest.client.model.VariableValueDto
import org.camunda.community.rest.impl.query.QueryOperator
import org.camunda.community.rest.impl.query.QueryVariableValue
import java.io.ObjectInputStream
import java.util.*

interface CustomValueMapper {

  fun mapValue(variableValue: Any): TypedValue

  fun canHandle(variableValue: Any): Boolean

  fun serializeValue(variableValue: SerializableValue): SerializableValue

  fun deserializeValue(variableValue: SerializableValue): TypedValue

}

/**
 * Class responsible for mapping variables from and to DTO representations.
 */
class ValueMapper(
  private val objectMapper: ObjectMapper = jacksonObjectMapper(),
  private val valueTypeResolver: ValueTypeResolver,
  private val customValueMapper: List<CustomValueMapper> = emptyList()
) {
  /**
   * Creates a variable value DTO out of variable value.
   */
  fun mapValue(variableValue: Any?, isTransient: Boolean = false): VariableValueDto {
    return mapValue(
      when (variableValue) {
        null -> untypedNullValue(isTransient)
        else -> untypedValue(variableValue, isTransient)
      }
    )
  }

  /**
   * Create a variable value DTO out of typed variable value.
   */
  private fun mapValue(variableValue: TypedValue): VariableValueDto {
    val variable = customValueMapper.firstOrNull { it.canHandle(variableValue.value) }?.mapValue(variableValue.value) ?: variableValue
    /*
     * preferSerializedValue MUST be set to true, in order to be able to serialize ObjectValues
     */
    if (variable is SerializableValue) {
      serializeValue(variable)
    }
    return variable.toDto()
  }

  private fun TypedValue.toDto() = VariableValueDto().apply {
    this@toDto.type?.let {
      type = toRestApiTypeName(it.name)
      valueInfo = it.getValueInfo(this@toDto)
    }

    value = when (this@toDto) {
      is SerializableValue -> this@toDto.valueSerialized
      is FileValue -> null //do not set the value for FileValues since we don't want to send megabytes over the network without explicit request
      else -> this@toDto.value
    }
  }

  fun toRestApiTypeName(name: String): String {
    return name.substring(0, 1).uppercase(Locale.getDefault()) + name.substring(1)
  }

  fun fromRestApiTypeName(name: String): String {
    return name.substring(0, 1).lowercase(Locale.getDefault()) + name.substring(1)
  }

  /**
   * Converts variable map to its REST representation.
   */
  fun mapValues(variables: MutableMap<String, out Any>): Map<String, VariableValueDto> {
    return if (variables is VariableMap) {
      variables.map { it.key to mapValue(variables.getValueTyped(it.key)) }.toMap()
    } else {
      variables.mapValues {
        mapValue(it.value)
      }
    }
  }

  /**
   * Convert variable REST implementation to variable map.
   */
  fun mapDtos(variables: Map<String, VariableValueDto>, deserializeValues: Boolean = true): VariableMap {
    // Not using VariableValueDto#toMap() since it ignores de-serialization.
    val result: VariableMap = Variables.createVariables()
    variables.mapValues {
      val value = if (deserializeValues) {
        restoreObjectJsonIfNeeded(it.value)
      } else {
        it.value
      }.toTypedValue(objectMapper)
      result[it.key] = value
    }
    return result
  }

  /**
   * Maps DTO to its value.
   */
  @Suppress("UNCHECKED_CAST")
  fun <T> mapDto(dto: VariableValueDto, deserializeValues: Boolean = true): T? {
    return if (deserializeValues) {
      deserializeObjectValue(restoreObjectJsonIfNeeded(dto).toTypedValue(objectMapper))
    } else {
      dto.toTypedValue(objectMapper)
    } as T
  }

  /**
   * Maps DTO to its value.
   */
  @Suppress("UNCHECKED_CAST")
  fun <T> mapDto(dto: VariableInstanceDto, deserializeValues: Boolean = true): T? {
    val valueDto = VariableValueDto().type(dto.type).value(dto.value).valueInfo(dto.valueInfo)
    return if (deserializeValues) {
      deserializeObjectValue(restoreObjectJsonIfNeeded(valueDto).toTypedValue(objectMapper))
    } else {
      valueDto.toTypedValue(objectMapper)
    } as T
  }

  private fun VariableValueDto.toTypedValue(objectMapper: ObjectMapper): TypedValue {
    return if (type == null) {
      if (valueInfo != null && valueInfo["transient"] is Boolean) untypedValue(value, valueInfo["transient"] as Boolean) else untypedValue(
        value
      )
    } else {
      when (val valueType = valueTypeResolver.typeForName(fromRestApiTypeName(type))) {
        is PrimitiveValueType -> {
          val javaType = valueType.javaType
          var mappedValue: Any? = null
          if (value != null) {
            mappedValue = if (javaType.isAssignableFrom(value.javaClass)) {
              value
            } else {
              objectMapper.readValue("\"" + value + "\"", javaType)
            }
          }
          valueType.createValue(mappedValue, valueInfo)
        }

        is SerializableValueType -> {
          if (value != null && value !is String) {
            throw IllegalArgumentException("Must provide 'null' or String value for value of SerializableValue type '$type'.")
          } else {
            valueType.createValueFromSerialized(value as String, valueInfo)
          }
        }

        is FileValueType -> {
          if (value is String) {
            value = Base64.getDecoder().decode(value as String)
          }
          valueType.createValue(value, valueInfo)
        }

        else -> if (valueType == null) throw IllegalArgumentException("Unsupported value type '$type'") else valueType.createValue(
          value,
          valueInfo
        )
      }
    }

  }

  /**
   * In case of object values, Jackson serializes any JSON to a map of String -> Object.
   * We want to make use of type information provided by and therefor restore the original JSON.
   */
  private fun restoreObjectJsonIfNeeded(dto: VariableValueDto): VariableValueDto {
    val valueType: ValueType? = valueTypeResolver.typeForName(fromRestApiTypeName(dto.type))

    if (valueType is SerializableValueType) {
      if (dto.value != null && dto.value !is String && dto.value is Map<*, *>) {
        // recover json in order to avoid "Must provide 'null' or String value for value of SerializableValue type '$type'." exception
        val attributes: Map<*, *> = dto.value as Map<*, *>
        dto.value = objectMapper.writeValueAsString(attributes)
      }
    }

    return dto
  }

  /**
   * Maps untyped value to DTO, guessing the type.
   * See {@link VariableValueDto} for more static functions.
   */
  private fun fromUntypedValue(value: Any): VariableValueDto =
    VariableValueDto().apply {
      this.type = getTypeName(value)
      this.value = value
    }

  @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
  private fun getTypeName(value: Any): String = when (value) {
    is Boolean, is Date, is Double, is Integer, is Long, is Short, is String -> value::class.simpleName!!
    is ByteArray -> "Bytes"
    else -> "Object"
  }

  /**
   * Serialize value, if not already serialized.
   * @param variableValue value to modify.
   */
  private fun serializeValue(variableValue: SerializableValue) {
    if (variableValue.valueSerialized == null) {
      customValueMapper.firstOrNull { it.canHandle(variableValue) }?.serializeValue(variableValue) ?: run {
        if (variableValue.serializationDataFormat == Variables.SerializationDataFormats.JSON.getName()
          // try it for application/json or unspecified
          || variableValue.serializationDataFormat == null
        ) {
          if (variableValue is ObjectValueImpl) {
            try {
              val serializedValue = objectMapper.writeValueAsString(variableValue.value)
              variableValue.setSerializedValue(serializedValue)
              // fix format if missing
              if (variableValue.serializationDataFormat == null) {
                variableValue.serializationDataFormat = Variables.SerializationDataFormats.JSON.getName()
              }
              // this allows to detect native types hidden in objectValue
              variableValue.objectTypeName = getTypeName(variableValue.value)
            } catch (e: JsonProcessingException) {
              throw IllegalArgumentException("Object value could not be serialized into '${variableValue.serializationDataFormat}'", e)
            }
          } else {
            throw UnsupportedOperationException("Serialization not supported for $variableValue")
          }
        } else {
          throw IllegalArgumentException("Object value could not be serialized into '${variableValue.serializationDataFormat}' and no serialized value has been provided for $variableValue")
        }
      }
    }
  }


  /**
   *
   * Takes existing TypedValue and tries to create one with deserialized value.
   */
  private fun deserializeObjectValue(value: TypedValue): TypedValue {
    return if (value is SerializableValue && !value.isDeserialized) {
      return customValueMapper.firstOrNull { it.canHandle(value) }?.deserializeValue(value)
        ?: if (value.serializationDataFormat == Variables.SerializationDataFormats.JSON.getName()
          || value.serializationDataFormat == null
        ) {
          return when (value) {
            is ObjectValueImpl -> {
              val deserializedValue: Any = try {
                val clazz = Class.forName(value.objectTypeName)
                objectMapper.readValue(value.valueSerialized, clazz)
              } catch (e: Exception) {
                throw IllegalStateException("Error deserializing value $value", e)
              }
              ObjectValueImpl(deserializedValue, value.valueSerialized, value.serializationDataFormat, value.objectTypeName, true)
            }

            else -> throw IllegalStateException("Could not deserialize value $value")
          }
        } else if (value.serializationDataFormat == Variables.SerializationDataFormats.JAVA.getName()) {
          if (value is ObjectValueImpl) {
            val deserializedValue: Any = try {
              ObjectInputStream(Base64.getDecoder().decode(value.valueSerialized).inputStream()).use { it.readObject() }
            } catch (e: Exception) {
              throw IllegalStateException("Error deserializing value $value", e)
            }
            return ObjectValueImpl(deserializedValue, value.valueSerialized, value.serializationDataFormat, value.objectTypeName, true)
          } else {
            throw IllegalStateException("Could not deserialize value $value")
          }
        } else {
          throw IllegalStateException("Could not deserialize value $value, ${value.serializationDataFormat} is not supported.")
        }
    } else {
      value
    }
  }
}

fun QueryOperator.toRestOperator() = when (this) {
  QueryOperator.EQUALS -> VariableQueryParameterDto.OperatorEnum.EQ
  QueryOperator.GREATER_THAN -> VariableQueryParameterDto.OperatorEnum.GT
  QueryOperator.GREATER_THAN_OR_EQUAL -> VariableQueryParameterDto.OperatorEnum.GTEQ
  QueryOperator.LESS_THAN -> VariableQueryParameterDto.OperatorEnum.LT
  QueryOperator.LESS_THAN_OR_EQUAL -> VariableQueryParameterDto.OperatorEnum.LTEQ
  QueryOperator.LIKE -> VariableQueryParameterDto.OperatorEnum.LIKE
  QueryOperator.NOT_EQUALS -> VariableQueryParameterDto.OperatorEnum.NEQ
  QueryOperator.NOT_LIKE -> VariableQueryParameterDto.OperatorEnum.NOTLIKE
}

fun List<QueryVariableValue>.toDto() = if (this.isEmpty()) null else this.map { it.toDto() }

fun QueryVariableValue.toDto(): VariableQueryParameterDto = VariableQueryParameterDto()
  .name(this.name)
  .value(this.value)
  .operator(this.operator.toRestOperator())

