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

import com.fasterxml.jackson.databind.ObjectMapper
import org.camunda.bpm.engine.variable.VariableMap
import org.camunda.bpm.engine.variable.Variables
import org.camunda.bpm.engine.variable.Variables.untypedValue
import org.camunda.bpm.engine.variable.type.*
import org.camunda.bpm.engine.variable.value.FileValue
import org.camunda.bpm.engine.variable.value.SerializableValue
import org.camunda.bpm.engine.variable.value.TypedValue
import org.camunda.community.rest.client.model.VariableInstanceDto
import org.camunda.community.rest.client.model.VariableValueDto
import org.camunda.community.rest.variables.ext.resolveValueType
import org.camunda.community.rest.variables.format.FormatValueMapper
import java.util.*

/**
 * Class responsible for mapping variables from and to DTO representations.
 */
open class ValueMapper(
  private val objectMapper: ObjectMapper,
  private val valueTypeResolver: ValueTypeResolver,
  private val valueMappers: List<IValueMapper>,
  private val serializationFormat: Variables.SerializationDataFormats
) {
  companion object {
    fun toRestApiTypeName(name: String): String = name.replaceFirstChar { it.uppercase(Locale.getDefault()) }
    fun fromRestApiTypeName(name: String): String = name.replaceFirstChar { it.lowercase(Locale.getDefault()) }
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

  /**
   * Create a variable value DTO out of typed variable value.
   */
  fun mapValue(variableValue: TypedValue): VariableValueDto {
    var variable = findMapFunction(variableValue.value)?.mapValue(variableValue.value)
      ?: variableValue

    if (variable is SerializableValue) {
      // throws exception if serialization is not supported
      variable = findSerializeFunction(variable).serializeValue(variable)
    }
    return variable.toDto()
  }

  /**
   * Maps DTO to its value.
   */
  @Suppress("UNCHECKED_CAST")
  @JvmOverloads
  fun <T> mapDto(dto: VariableValueDto, deserializeValues: Boolean = true): T? {
    val typedValue = restoreObjectJsonIfNeeded(dto).toTypedValue()
    return if (deserializeValues && typedValue is SerializableValue) {
      findDeserializeFunction(typedValue).deserializeValue(typedValue)
    } else {
      dto.toTypedValue()
    } as T
  }

  /**
   * Maps DTO to its value.
   */
  @Suppress("UNCHECKED_CAST")
  @JvmOverloads
  fun <T> mapDto(dto: VariableInstanceDto, deserializeValues: Boolean = true): T? =
    mapDto(dto = VariableValueDto().type(dto.type).value(dto.value).valueInfo(dto.valueInfo), deserializeValues = deserializeValues)


  private fun convertToTypedValue(variableValue: Any?, isTransient: Boolean) =
    resolveValueType(variableValue).createValue(variableValue, mapOf(ValueType.VALUE_INFO_TRANSIENT to isTransient))


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


  private fun VariableValueDto.toTypedValue(): TypedValue {
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

  private fun findMapFunction(value: Any?) =
    valueMappers.firstOrNull {
      when (it) {
        is FormatValueMapper -> it.canMapValue(value) && it.serializationDataFormat == this.serializationFormat
        else -> it.canMapValue(value)
      }
    }

  private fun findSerializeFunction(value: TypedValue) = valueMappers.firstOrNull { it.canSerializeValue(value) }
    ?: throw IllegalArgumentException("No custom serializeValue() function configured for value type: ${value.javaClass.name}")

  private fun findDeserializeFunction(value: SerializableValue) = valueMappers.firstOrNull { it.canDeserializeValue(value) }
    ?: throw IllegalArgumentException("No custom deserializeValue() function configured for value type: ${value.javaClass.name}")

}
