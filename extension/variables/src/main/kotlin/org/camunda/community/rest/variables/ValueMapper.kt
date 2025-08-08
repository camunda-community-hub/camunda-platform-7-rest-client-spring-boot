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
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.type.TypeFactory
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.camunda.bpm.engine.variable.VariableMap
import org.camunda.bpm.engine.variable.Variables
import org.camunda.bpm.engine.variable.Variables.SerializationDataFormats
import org.camunda.bpm.engine.variable.Variables.untypedValue
import org.camunda.bpm.engine.variable.impl.value.ObjectValueImpl
import org.camunda.bpm.engine.variable.type.*
import org.camunda.bpm.engine.variable.value.FileValue
import org.camunda.bpm.engine.variable.value.SerializableValue
import org.camunda.bpm.engine.variable.value.TypedValue
import org.camunda.community.rest.client.model.VariableInstanceDto
import org.camunda.community.rest.client.model.VariableValueDto
import org.camunda.community.rest.variables.ext.resolveValueType
import org.springframework.stereotype.Component
import java.io.ObjectInputStream
import java.util.*

/**
 * Class responsible for mapping variables from and to DTO representations.
 */
@Component
class ValueMapper(
  private val objectMapper: ObjectMapper = jacksonObjectMapper(),
  private val valueTypeResolver: ValueTypeResolver = ValueTypeResolverImpl(),
  private val customValueMapper: List<CustomValueMapper> = emptyList(),
  private val defaultSerializationFormat: SerializationDataFormats = SerializationDataFormats.JSON,
) {
  companion object {
    private fun toRestApiTypeName(name: String): String = name.replaceFirstChar { it.uppercase(Locale.getDefault()) }
    private fun fromRestApiTypeName(name: String): String = name.replaceFirstChar { it.lowercase(Locale.getDefault()) }
  }

  /**
   * Converts variable map to its REST representation.
   */
  fun mapValues(variables: Map<String, Any?>): Map<String, VariableValueDto> {
    return if (variables is VariableMap) {
      variables.mapValues {
        mapValue(variables.getValueTyped(it.key))
      }
    } else {
      variables.mapValues {
        mapValue(it.value)
      }
    }
  }

  /**
   * Convert variable REST implementation to variable map.
   */
  @JvmOverloads
  fun mapDtos(variables: Map<String, VariableValueDto>, deserializeValues: Boolean = true): VariableMap {
    val result: VariableMap = Variables.createVariables()
    variables.mapValues {
      result[it.key] = mapDto<Any>(it.value, deserializeValues)
    }
    return result
  }

  /**
   * Creates a variable value DTO out of variable value.
   */
  fun mapValue(variableValue: Any?, isTransient: Boolean = false): VariableValueDto {
    return mapValue(convertToTypedValue(variableValue, isTransient))
  }

  private fun convertToTypedValue(variableValue: Any?, isTransient: Boolean) =
    resolveValueType(variableValue).createValue(variableValue, mapOf(ValueType.VALUE_INFO_TRANSIENT to isTransient))

  /**
   * Create a variable value DTO out of typed variable value.
   */
  fun mapValue(variableValue: TypedValue): VariableValueDto {
    var variable = customValueMapper.firstOrNull {
      it.canMapValue(variableValue.value)
    }?.mapValue(variableValue.value)
      ?: variableValue

    if (variable is SerializableValue) {
      variable = serializeValue(variable)
    }
    return variable.toDto()
  }

  private fun TypedValue.toDto() = VariableValueDto().apply {
    this@toDto.type?.let {
      type = toRestApiTypeName(it.name)
      valueInfo = it.getValueInfo(this@toDto)
    } ?: let {
      type = toRestApiTypeName(resolveValueType(this@toDto.value).name)
    }

    value = when (this@toDto) {
      is SerializableValue -> this@toDto.valueSerialized
      is FileValue -> null //do not set the value for FileValues since we don't want to send megabytes over the network without explicit request
      else -> this@toDto.value
    }
  }


  /**
   * Maps DTO to its value.
   */
  @Suppress("UNCHECKED_CAST")
  @JvmOverloads
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
  @JvmOverloads
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
   * Serialize value, if not already serialized.
   * @param variableValue value to modify.
   */
  private fun serializeValue(variableValue: TypedValue): SerializableValue {
    // FIXME open type
    require(variableValue is SerializableValue) { "Variable value must be a SerializableValue to be serialized, but was: $variableValue" }
    if (variableValue.valueSerialized == null) {
      return customValueMapper.firstOrNull { it.canSerializeValue(variableValue) }
        ?.serializeValue(variableValue)
        ?: run {
          if (variableValue.serializationDataFormat == SerializationDataFormats.JSON.getName()
            // try it for application/json or unspecified
            || variableValue.serializationDataFormat == null
          ) {
            if (variableValue is ObjectValueImpl) {
              try {
                val serializedValue = objectMapper.writeValueAsString(variableValue.value)
                variableValue.setSerializedValue(serializedValue)
                // fix format if missing
                if (variableValue.serializationDataFormat == null) {
                  variableValue.serializationDataFormat = SerializationDataFormats.JSON.getName()
                }
                // this allows to detect native types hidden in objectValue
                variableValue.objectTypeName = constructType(variableValue.value).toCanonical()

                return variableValue
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
    return variableValue
  }

  private fun constructType(value: Any): JavaType =
    if (value is Collection<*> && value.javaClass.typeParameters.size == 1 && value.isNotEmpty()) {
      TypeFactory.defaultInstance().constructCollectionType(value.javaClass, value.first()!!.javaClass)
    } else if (value is Array<*> && value.javaClass.typeParameters.size == 1 && value.isNotEmpty()) {
      TypeFactory.defaultInstance().constructArrayType(value.first()!!.javaClass)
    } else {
      TypeFactory.defaultInstance().constructType(value.javaClass)
    }

  /**
   *
   * Takes existing TypedValue and tries to create one with deserialized value.
   */
  private fun deserializeObjectValue(value: TypedValue): TypedValue {
    return if (value is SerializableValue && !value.isDeserialized) {
      return customValueMapper.firstOrNull { it.canHandle(value) }?.deserializeValue(value)
        ?: if (value.serializationDataFormat == SerializationDataFormats.JSON.getName()
          || value.serializationDataFormat == null
        ) {
          return when (value) {
            is ObjectValueImpl -> {
              val deserializedValue: Any = try {
                val clazz = TypeFactory.defaultInstance().constructFromCanonical(value.objectTypeName)
                objectMapper.readValue(value.valueSerialized, clazz) as Any
              } catch (e: Exception) {
                throw IllegalStateException("Error deserializing value $value", e)
              }
              ObjectValueImpl(deserializedValue, value.valueSerialized, value.serializationDataFormat, value.objectTypeName, true)
            }

            else -> throw IllegalStateException("Could not deserialize value $value")
          }
        } else if (value.serializationDataFormat == SerializationDataFormats.JAVA.getName()) {
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
