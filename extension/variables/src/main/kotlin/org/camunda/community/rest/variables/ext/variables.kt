/**
 * Extensions for Camunda BPM variables.
 */
package org.camunda.community.rest.variables.ext

import org.camunda.bpm.engine.variable.Variables
import org.camunda.bpm.engine.variable.type.ValueType
import org.camunda.bpm.engine.variable.value.ObjectValue
import org.camunda.bpm.engine.variable.value.builder.ObjectValueBuilder
import java.util.*

// Serialization data formats overloads name() and getName() in kotlin, so we use the format alias.
val Variables.SerializationDataFormats.format: String get() = this.getName()

fun ObjectValueBuilder.serializationDataFormat(format: Variables.SerializationDataFormats) = apply {
  serializationDataFormat(format.format)
}

fun ObjectValue.hasSerializationDataFormat(format: Variables.SerializationDataFormats): Boolean =
  this.serializationDataFormat?.let { it == format.format } ?: false

/**
 * Tries to guess the type from the passed value.
 */
@Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
fun resolveValueType(value: Any?): ValueType = when (value) {
  null -> ValueType.NULL
  is Boolean -> ValueType.BOOLEAN
  is Date -> ValueType.DATE
  is Double -> ValueType.DOUBLE
  is Integer -> ValueType.INTEGER
  is Long -> ValueType.LONG
  is Short -> ValueType.SHORT
  is String -> ValueType.STRING
  is ByteArray -> ValueType.BYTES
  is Number -> ValueType.NUMBER
  else -> ValueType.OBJECT
}
