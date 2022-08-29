/*-
 * #%L
 * camunda-platform-7-rest-client-spring-boot
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
package org.camunda.community.rest.config

import feign.codec.ErrorDecoder
import mu.KLogging
import org.camunda.community.rest.exception.RemoteProcessEngineException
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


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
    val errorDecoding = camundaRestClientProperties.errorDecoding

    return ErrorDecoder { methodKey, response ->
      when {
        errorDecoding.httpCodes.contains(response.status()) -> {
          CamundaFeignExceptionDecoder(response).decodeException()?.let {
            if (errorDecoding.wrapExceptions && it !is RemoteProcessEngineException) {
              RemoteProcessEngineException("REST-CLIENT-002 Error during remote Camunda engine invocation", it)
            } else {
              it
            }
          } ?: RemoteProcessEngineException(message = "REST-CLIENT-001 Error during remote Camunda engine invocation of $methodKey: ${response.reason()}")
        }
        else -> default.decode(methodKey, response)
      }
    }
  }
}


