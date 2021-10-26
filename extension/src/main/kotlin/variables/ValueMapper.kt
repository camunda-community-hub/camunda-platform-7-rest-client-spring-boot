/*-
 * #%L
 * camunda-rest-client-spring-boot
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

package org.camunda.bpm.extension.rest.variables

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.camunda.bpm.engine.ProcessEngine
import org.camunda.bpm.engine.ProcessEngines
import org.camunda.bpm.engine.impl.QueryOperator
import org.camunda.bpm.engine.impl.QueryVariableValue
import org.camunda.bpm.engine.impl.digest._apacheCommonsCodec.Base64
import org.camunda.bpm.engine.variable.VariableMap
import org.camunda.bpm.engine.variable.Variables
import org.camunda.bpm.engine.variable.Variables.untypedNullValue
import org.camunda.bpm.engine.variable.Variables.untypedValue
import org.camunda.bpm.engine.variable.impl.value.ObjectValueImpl
import org.camunda.bpm.engine.variable.type.*
import org.camunda.bpm.engine.variable.value.FileValue
import org.camunda.bpm.engine.variable.value.SerializableValue
import org.camunda.bpm.engine.variable.value.TypedValue
import org.camunda.bpm.extension.rest.client.model.VariableQueryParameterDto
import org.camunda.bpm.extension.rest.client.model.VariableValueDto
import java.util.*


/**
 * Class responsible for mapping variables from and to DTO representations.
 */
class ValueMapper(
  private val processEngine: ProcessEngine = ProcessEngines.getDefaultProcessEngine(),
  private val objectMapper: ObjectMapper = jacksonObjectMapper()
) {
  /**
   * Creates a variable value DTO out of variable value.
   */
  fun mapValue(variableValue: Any?, isTransient: Boolean = false): VariableValueDto {
    return if (variableValue == null) {
      mapValue(untypedNullValue(isTransient))
    } else {
      mapValue(untypedValue(variableValue, isTransient))
    }
  }

  /**
   * Create a variable value DTO out of typed variable value.
   */
  fun mapValue(variableValue: TypedValue): VariableValueDto {
    /*
     * preferSerializedValue MUST be set to true, in order to be able to serialize ObjectValues
     */
    if (variableValue is SerializableValue) {
      serializeValue(variableValue)
    }
    return variableValue.toDto()
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
      }.toTypedValue(processEngine, objectMapper)
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
      deserializeObjectValue(restoreObjectJsonIfNeeded(dto).toTypedValue(processEngine, objectMapper))
    } else {
      dto.toTypedValue(processEngine, objectMapper)
    } as T
  }

  private fun VariableValueDto.toTypedValue(processEngine: ProcessEngine, objectMapper: ObjectMapper): TypedValue {
    val valueTypeResolver = processEngine.processEngineConfiguration.valueTypeResolver
    return if (type == null) {
      if (valueInfo != null && valueInfo["transient"] is Boolean) untypedValue(value, valueInfo["transient"] as Boolean) else untypedValue(value)
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
            value = Base64.decodeBase64(value as String)
          }
          valueType.createValue(value, valueInfo)
        }
        else -> if (valueType == null) throw IllegalArgumentException("Unsupported value type '$type'") else valueType.createValue(value, valueInfo)
      }
    }

  }

  /**
   * In case of object values, Jackson serializes any JSON to a map of String -> Object.
   * We want to make use of type information provided by and therefor restore the original JSON.
   */
  private fun restoreObjectJsonIfNeeded(dto: VariableValueDto): VariableValueDto {
    val valueTypeResolver: ValueTypeResolver = processEngine.processEngineConfiguration.valueTypeResolver
    val valueType: ValueType = valueTypeResolver.typeForName(fromRestApiTypeName(dto.type))

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
      // try it for application/json or unspecified
      if (variableValue.serializationDataFormat == Variables.SerializationDataFormats.JSON.getName()
        || variableValue.serializationDataFormat == null) {
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
          throw UnsupportedOperationException("Only serialization of object values is supported. Please provide serialized value for $variableValue")
        }
      } else {
        throw IllegalArgumentException("Object value could not be serialized into '${variableValue.serializationDataFormat}' and no serialized value has been provided for $variableValue")
      }
    }
  }


  /**
   *
   * Takes existing TypedValue and tries to create one with deserialized value.
   */
  private fun deserializeObjectValue(value: TypedValue): TypedValue {
    return if (value is SerializableValue && !value.isDeserialized) {
      if (value.serializationDataFormat == Variables.SerializationDataFormats.JSON.getName()
        || value.serializationDataFormat == null) {
        if (value is ObjectValueImpl) {
          val deserializedValue: Any = try {
            val clazz = Class.forName(value.objectTypeName)
            objectMapper.readValue(value.valueSerialized, clazz)
          } catch (e: Exception) {
            throw IllegalStateException("Error deserializing value $value", e)
          }
          return ObjectValueImpl(deserializedValue, value.valueSerialized, value.serializationDataFormat, value.objectTypeName, true)
        } else {
          throw IllegalStateException("Could not deserialize value $value")
        }
      } else {
        throw IllegalStateException("Could not deserialize value $value, only application/json de-serialization is supported.")
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
  QueryOperator.NOT_LIKE -> throw IllegalArgumentException()
  else -> throw IllegalArgumentException()
}

fun List<QueryVariableValue>.toDto() = if (this.isEmpty()) null else this.map { it.toDto() }

fun QueryVariableValue.toDto(): VariableQueryParameterDto = VariableQueryParameterDto()
  .name(this.name)
  .value(this.value)
  .operator(this.operator.toRestOperator())

