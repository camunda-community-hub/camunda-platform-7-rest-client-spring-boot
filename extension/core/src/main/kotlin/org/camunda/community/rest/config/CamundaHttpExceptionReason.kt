package org.camunda.community.rest.config

import com.fasterxml.jackson.annotation.JsonProperty
import mu.KLogging

/**
 * Exception reason.
 * @constructor constructs the reason.
 * @param clazz class name for exception reason.
 * @param message text.
 */
internal data class CamundaHttpExceptionReason(
  @JsonProperty("type")
  val clazz: String,
  @JsonProperty("message")
  val message: String,
  @JsonProperty("code")
  val code: String?
) {
  companion object : KLogging() {
    private const val FQCN = "(([a-zA-Z_\$][a-zA-Z\\d_\$]*\\.)*[a-zA-Z_\$][a-zA-Z\\d_\$]*): (.*)"

    /**
     * Factory method to construct a reason from string response of the server.
     * @param string response.
     * @return instance or <code>null</code>.
     */
    fun fromMessage(message: String): CamundaHttpExceptionReason? {
      val match = FQCN.toRegex().find(message)

      return if (match != null) {
        val (clazz, _, remaining) = match.destructured
          CamundaHttpExceptionReason(clazz = clazz, message = remaining, code = null)
      } else {
        logger.debug { "REST-CLIENT-003 Could not parse Camunda exception from server response: \n$message" }
        null
      }
    }
  }
}
