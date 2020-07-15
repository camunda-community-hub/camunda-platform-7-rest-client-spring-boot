package org.camunda.bpm.extension.rest.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.NestedConfigurationProperty

@ConfigurationProperties("camunda.rest.client")
@ConstructorBinding
data class CamundaRestClientProperties(
  /**
   * Controls error decoding from HTTP response codes.
   */
  @NestedConfigurationProperty
  val errorDecoding: ErrorDecoding = ErrorDecoding()
)

/**
 * Controls decoding of HTTP status response to Camunda Exceptions.
 */
data class ErrorDecoding(
  /**
   * Enable decoding.
   */
  val enabled: Boolean = true,
  /**
   * List of HTTP codes to decode. Defaults to HTTP status 400 and 500.
   */
  val httpCodes: List<Int> = listOf(400, 500)
)
