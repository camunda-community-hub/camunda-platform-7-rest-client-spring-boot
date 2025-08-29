package org.camunda.community.rest.variables.format

import org.camunda.bpm.engine.variable.Variables.SerializationDataFormats
import org.camunda.community.rest.variables.CustomValueMapper

/**
 * A special type of CustomValueMapper that is used for formatting values
 * into a specific serialization format.
 */
interface FormatValueMapper : CustomValueMapper {

  val serializationDataFormat: SerializationDataFormats

}
