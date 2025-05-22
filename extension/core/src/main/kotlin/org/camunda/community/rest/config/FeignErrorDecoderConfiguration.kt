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
import org.camunda.community.rest.exception.CamundaHttpFeignErrorDecoder
import org.camunda.community.rest.exception.ClientExceptionFactory
import org.camunda.community.rest.exception.RemoteProcessEngineException
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Configures error decoding.
 */
@Configuration
@ConditionalOnProperty(
  name = [
    "camunda.rest.client.error-encoding.enabled", // is still listed due to backwards compatibility
    "camunda.rest.client.error-decoding.enabled"
  ],
  matchIfMissing = true
)
class FeignErrorDecoderConfiguration {

  /**
   * Provides an error decoder bean for feign.
   * @param camundaRestClientProperties properties for configuration.
   */
  @Bean
  fun errorDecoder(camundaRestClientProperties: CamundaRestClientProperties): ErrorDecoder {
    return CamundaHttpFeignErrorDecoder(
      httpCodes = camundaRestClientProperties.errorDecoding.httpCodes,
      defaultDecoder = ErrorDecoder.Default(),
      wrapExceptions = camundaRestClientProperties.errorDecoding.wrapExceptions,
      targetExceptionType = RemoteProcessEngineException::class.java,
      exceptionFactory = object : ClientExceptionFactory<RemoteProcessEngineException> {
        override fun create(message: String, cause: Throwable?) = RemoteProcessEngineException(message, cause)
      }
    )
  }
}



