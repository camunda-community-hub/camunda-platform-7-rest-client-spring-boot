package org.camunda.bpm.extension.restclient.variables

import org.camunda.bpm.engine.rest.dto.VariableValueDto
import java.util.*

/**
 * Maps value to DTO.
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


