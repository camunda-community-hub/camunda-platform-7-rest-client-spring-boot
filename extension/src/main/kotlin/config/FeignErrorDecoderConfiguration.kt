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
import org.camunda.bpm.engine.ProcessEngineException
import org.camunda.bpm.extension.rest.config.CamundaHttpExceptionReason.Companion.fromMessage
import org.camunda.bpm.extension.rest.exception.RemoteProcessEngineException
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.io.IOException


@Configuration
class FeignErrorDecoderConfiguration {

  companion object : KLogging()

  // FIXME: move to configuration properties
  val wrapHttpCodes = listOf(400, 500)

  @Bean
  fun errorDecoder(): ErrorDecoder {

    val default = ErrorDecoder.Default()

    return ErrorDecoder { methodKey, response ->
      when {
        wrapHttpCodes.contains(response.status()) -> CamundaFeignExceptionDecoder(response).decodeException()
          ?: RemoteProcessEngineException(
            message = "REST-CLIENT-001 Error during remote Camunda engine invocation of $methodKey: ${response.reason()}"
          )
        else -> default.decode(methodKey, response)
      }
    }
  }
}

data class CamundaFeignExceptionDecoder(val response: Response) {

  fun decodeException(): Exception? {
    return try {
      val response = jacksonObjectMapper().readValue<CamundaHttpExceptionReason>(response.body().asInputStream(), CamundaHttpExceptionReason::class.java)
      constructExceptionInstance(response)
        ?: fromMessage(response.message)?.let {
          constructExceptionInstance(it)
        }
    } catch (e: IOException) {
      null
    }
  }

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

internal data class CamundaHttpExceptionReason(
  @JsonProperty("type")
  val clazz: String,
  @JsonProperty("message")
  val message: String
) {
  companion object : KLogging() {
    fun fromMessage(message: String): CamundaHttpExceptionReason? {
      val match = "(([a-zA-Z_\$][a-zA-Z\\d_\$]*\\.)*[a-zA-Z_\$][a-zA-Z\\d_\$]*): (.*)".toRegex().find(message)

      return if (match != null) {
        val (clazz, separator, remaining) = match.destructured
        CamundaHttpExceptionReason(clazz = clazz, message = remaining)
      } else {
        logger.error { "REST-CLIENT-003 Could not parse Camunda exception from server response: \n$message" }
        null
      }
    }
  }
}


