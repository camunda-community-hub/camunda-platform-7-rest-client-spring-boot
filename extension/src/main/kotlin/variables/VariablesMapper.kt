package org.camunda.bpm.extension.feign.variables

import org.camunda.bpm.engine.rest.dto.VariableValueDto
import org.camunda.bpm.engine.variable.value.TypedValue
import java.util.*

/**
 * Converts variable map to its REST representation.
 */
fun MutableMap<String, out Any>.toVariableValueDtoMap(): Map<String, VariableValueDto> {
  return this.mapValues {
    if (it.value is TypedValue) {
      VariableValueDto.fromTypedValue(it.value as TypedValue)
    } else {
      fromUntypedValue(it.value)
    }
  }
}

/**
 * Maps untyped value to DTO, guessing the type.
 * See {@link VariableValueDto} for more static functions.
 */
fun fromUntypedValue(value: Any): VariableValueDto =
  VariableValueDto().apply {
    @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
    this.type = when (value) {
      is Boolean -> "Boolean"
      is ByteArray -> "Bytes"
      is Short -> "Short"
      is Integer -> "Integer"
      is Long -> "Long"
      is Double -> "Double"
      is Date -> "Date"
      is String -> "String"
      else -> "Object"
    }
    this.value = value
  }

