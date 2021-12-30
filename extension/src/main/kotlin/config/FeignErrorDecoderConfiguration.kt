/*-
 * #%L
 * camunda-rest-client-spring-boot
 * %%
 * Copyright (C) 2019 Camunda Services GmbH
 * %%
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 *  under one or more contributor license agreements. See the NOTICE file
 *  distributed with this work for additional information regarding copyright
 *  ownership. Camunda licenses this file to you under the Apache License,
 *  Version 2.0; you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * #L%
 */
package org.camunda.bpm.extension.rest.config

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import feign.Response
import feign.codec.ErrorDecoder
import mu.KLogging
import org.camunda.bpm.extension.rest.config.CamundaHttpExceptionReason.Companion.fromMessage
import org.camunda.bpm.extension.rest.exception.RemoteProcessEngineException
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.io.IOException


/**
 * Configures error decoding.
 * @constructor creates the configuration.
 * @param camundaRestClientProperties properties for configuration.
 */
@Configuration
@ConditionalOnProperty("camunda.rest.client.error-encoding.enabled", matchIfMissing = true)
class FeignErrorDecoderConfiguration(
  val camundaRestClientProperties: CamundaRestClientProperties
) {

  companion object : KLogging()

  /**
   * Provides an error decoder bean for feign.
   */
  @Bean
  fun errorDecoder(): ErrorDecoder {

    val default = ErrorDecoder.Default()

    return ErrorDecoder { methodKey, response ->
      when {
        camundaRestClientProperties.errorDecoding.httpCodes.contains(response.status()) -> CamundaFeignExceptionDecoder(response).decodeException()
          ?: RemoteProcessEngineException(
            message = "REST-CLIENT-001 Error during remote Camunda engine invocation of $methodKey: ${response.reason()}"
          )
        else -> default.decode(methodKey, response)
      }
    }
  }
}

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
        ?: fromMessage(response.message)?.let {
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
        RemoteProcessEngineException("REST-CLIENT-002 Error during remote Camunda engine invocation", constructor.newInstance(reason.message) as Exception)
      } else {
        null
      }
    } catch (e: Exception) {
      null
    }
  }
}

/**
 * Exception reason.
 * @constructor constructs the reason.
 * @param class name for exception reason.
 * @param message text.
 */
internal data class CamundaHttpExceptionReason(
  @JsonProperty("type")
  val clazz: String,
  @JsonProperty("message")
  val message: String
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
        CamundaHttpExceptionReason(clazz = clazz, message = remaining)
      } else {
        logger.debug { "REST-CLIENT-003 Could not parse Camunda exception from server response: \n$message" }
        null
      }
    }
  }
}


