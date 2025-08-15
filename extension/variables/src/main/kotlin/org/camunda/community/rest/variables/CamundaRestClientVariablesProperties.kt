package org.camunda.community.rest.variables

import org.camunda.bpm.engine.variable.Variables.SerializationDataFormats
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("camunda.rest.client.variables")
data class CamundaRestClientVariablesProperties(

  /**
   * Default serialization format for variables.
   * This is used when no serialization format is specified in the variable.
   */
  val defaultSerializationFormat: SerializationDataFormats = SerializationDataFormats.JSON,
) {
  init {
    require(SerializationDataFormats.XML != defaultSerializationFormat) {
      "XML serialization format is not supported. Use JSON or JAVA."
    }
  }
}
