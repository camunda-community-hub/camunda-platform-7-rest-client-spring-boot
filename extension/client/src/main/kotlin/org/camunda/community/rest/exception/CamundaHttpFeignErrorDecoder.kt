package org.camunda.community.rest.exception

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import feign.Response
import feign.codec.ErrorDecoder
import java.io.IOException

/**
 * Special Feign error decoder responsible for detecting reasons for error codes delivered by invocation
 * of Camunda REST API. It tries to tackle all different styles of errors and wrap them into
 * a target exception thrown on the client side.
 */
class CamundaHttpFeignErrorDecoder<T : Exception>(
  private val httpCodes: List<Int>,
  private val defaultDecoder: ErrorDecoder = ErrorDecoder.Default(),
  private val wrapExceptions: Boolean,
  private val targetExceptionType: Class<T>,
  private val exceptionFactory: ClientExceptionFactory<T>
) : ErrorDecoder {

  override fun decode(methodKey: String, response: Response): Exception {
    return when {
      httpCodes.contains(response.status()) -> {
        response.decodeException()?.let {
          if (wrapExceptions && it.javaClass != targetExceptionType) {
            exceptionFactory.create(
              "REST-CLIENT-002 Error during remote Camunda engine invocation",
              it
            )
          } else {
            it
          }
        } ?: exceptionFactory.create(
          "REST-CLIENT-001 Error during remote Camunda engine invocation of $methodKey: ${response.reason()}"
        )
      }

      else -> defaultDecoder.decode(methodKey, response)
    }
  }

  /**
   * Tries to create an instance of exception deduced from status code.
   * @return exception or <code>null</code> if decoding was not possible.
   */
  private fun Response.decodeException(): Exception? {
    return try {
      val reason = jacksonObjectMapper().readValue(this.body().asInputStream(), CamundaHttpExceptionReason::class.java)
      reason.constructExceptionInstance()
        ?: CamundaHttpExceptionReason.fromMessage(reason.message)?.constructExceptionInstance()
        ?: exceptionFactory.create(
          "REST-CLIENT-002 Error during remote Camunda engine invocation with ${reason.clazz}: ${reason.message}"
        )
    } catch (e: IOException) {
      null
    }
  }

  /**
   * Try to construct exception from reason.
   * @return exception or <code>null</code>.
   */
  private fun CamundaHttpExceptionReason.constructExceptionInstance(): Exception? {
    return try {
      val exceptionClass = Class.forName(this.clazz)
      if (Throwable::class.java.isAssignableFrom(exceptionClass)) {
        exceptionClass.getConstructor(String::class.java).newInstance(this.message) as Exception
      } else {
        null
      }
    } catch (e: Exception) {
      null
    }
  }

}

