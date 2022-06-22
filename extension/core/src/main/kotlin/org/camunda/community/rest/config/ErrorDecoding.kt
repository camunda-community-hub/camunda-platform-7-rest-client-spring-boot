package org.camunda.community.rest.config

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
