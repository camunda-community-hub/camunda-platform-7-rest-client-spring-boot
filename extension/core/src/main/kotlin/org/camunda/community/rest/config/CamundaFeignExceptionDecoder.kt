package org.camunda.community.rest.config

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import feign.Response
import org.camunda.community.rest.exception.RemoteProcessEngineException
import java.io.IOException

/**
 * Decoder responsible for reading the exception out of HTTP response.
 * @constructor creates the decoder.
 * @param response feign response.
 */
internal data class CamundaFeignExceptionDecoder(val response: Response) {

  /**
   * Tries to create an instance of exception deduced from status code.
   * @return exception or <code>null</code> if decoding was not possible.
   */
  fun decodeException(): Exception? {
    return try {
      val response = jacksonObjectMapper().readValue<CamundaHttpExceptionReason>(response.body().asInputStream(), CamundaHttpExceptionReason::class.java)
      constructExceptionInstance(response)
        ?: CamundaHttpExceptionReason.fromMessage(response.message)?.let {
          constructExceptionInstance(it)
        }
        ?: RemoteProcessEngineException("REST-CLIENT-002 Error during remote Camunda engine invocation with ${response.clazz}: ${response.message}")
    } catch (e: IOException) {
      null
    }
  }

  /**
   * Constructs exception.
   * @param exception reason wrapper.
   * @return exception or <code>null</code>.
   */
  private fun constructExceptionInstance(reason: CamundaHttpExceptionReason): Exception? {
    return try {
      val exceptionClass = Class.forName(reason.clazz)
      if (Throwable::class.java.isAssignableFrom(exceptionClass)) {
        val constructor = exceptionClass.getConstructor(String::class.java)
          RemoteProcessEngineException(
              "REST-CLIENT-002 Error during remote Camunda engine invocation",
              constructor.newInstance(reason.message) as Exception
          )
      } else {
        null
      }
    } catch (e: Exception) {
      null
    }
  }
}
