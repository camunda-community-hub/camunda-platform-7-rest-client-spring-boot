package org.camunda.bpm.extension.restclient.variables

import org.camunda.bpm.engine.rest.dto.VariableValueDto
import java.util.*

/**
 * Maps key/value entry from map with variables to a variable value dto.
 */
fun fromUntypedValueEntry(entry: Map.Entry<String, Any>): VariableValueDto =
  VariableValueDto().apply {
    @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
    type = when (entry.value) {
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
    value = entry.value
  }
