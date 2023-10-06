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
package org.camunda.community.rest.client

import feign.Retryer
import feign.codec.Encoder
import org.camunda.community.rest.client.KeyChangingSpringManyMultipartFilesWriter.Companion.camundaMultipartFormEncoder
import org.springframework.beans.factory.ObjectFactory
import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.autoconfigure.http.HttpMessageConverters
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.cloud.openfeign.support.FeignEncoderProperties
import org.springframework.cloud.openfeign.support.HttpMessageConverterCustomizer
import org.springframework.cloud.openfeign.support.SpringEncoder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


/**
 * Basic configuration of the feign client.
 */
@Configuration
@EnableFeignClients
class FeignClientConfiguration {

  @Bean
  fun feignEncoder(
    messageConverters: ObjectFactory<HttpMessageConverters>,
    feignEncoderProperties: FeignEncoderProperties,
    customizers: ObjectProvider<HttpMessageConverterCustomizer>
  ): Encoder {
    return SpringEncoder(camundaMultipartFormEncoder(), messageConverters, feignEncoderProperties, customizers)
  }

  @Bean
  fun retryer() = Retryer.Default()

}

