/**
 * Extensions for Camunda BPM variables.
 */
package org.camunda.community.rest.variables.ext

import org.camunda.bpm.engine.variable.Variables
import org.camunda.bpm.engine.variable.value.SerializationDataFormat
import org.camunda.bpm.engine.variable.value.builder.ObjectValueBuilder

// Serialization data formats overloads name() and getName() in kotlin, so we use the format alias.
val SerializationDataFormat.format: String get() = this.name

fun ObjectValueBuilder.serializationDataFormat(format: Variables.SerializationDataFormats) = apply {
  serializationDataFormat(format.format)
}
