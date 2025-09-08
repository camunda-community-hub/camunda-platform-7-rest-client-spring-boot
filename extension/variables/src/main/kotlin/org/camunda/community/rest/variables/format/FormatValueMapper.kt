package org.camunda.community.rest.variables.format

import org.camunda.bpm.engine.variable.Variables.SerializationDataFormats
import org.camunda.community.rest.variables.IValueMapper

/**
 * A special type of [IValueMapper] that is used for formatting values
 * into a specific serialization format.
 */
interface FormatValueMapper : IValueMapper {

  val serializationDataFormat: SerializationDataFormats

}
