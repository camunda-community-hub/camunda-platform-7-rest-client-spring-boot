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

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.ser.OffsetDateTimeSerializer
import feign.Logger
import feign.Retryer
import feign.codec.Encoder
import org.camunda.community.rest.client.KeyChangingSpringManyMultipartFilesWriter.Companion.camundaMultipartFormEncoder
import org.springframework.beans.factory.ObjectFactory
import org.springframework.beans.factory.ObjectProvider
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.http.HttpMessageConverters
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.cloud.openfeign.support.FeignEncoderProperties
import org.springframework.cloud.openfeign.support.HttpMessageConverterCustomizer
import org.springframework.cloud.openfeign.support.SpringEncoder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import java.time.format.DateTimeFormatter


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

  @Bean
  @ConditionalOnProperty("feign.client.config.default.loggerLevel")
  fun feignLoggerLevel(@Value("\${feign.client.config.default.loggerLevel}") defaultLogLevel: String) = Logger.Level.valueOf(defaultLogLevel)

  @Bean
  fun jsonCustomizer(): Jackson2ObjectMapperBuilder {
    return Jackson2ObjectMapperBuilder.json()
      .featuresToDisable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE, SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
      .serializers(
        OffsetDateTimeSerializer(
          OffsetDateTimeSerializer.INSTANCE,
          false,
          DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXX"), 
          JsonFormat.Shape.STRING
        )
      )
  }

  @Bean
  fun mappingJackson2HttpMessageConverter(jackson2ObjectMapperBuilder: Jackson2ObjectMapperBuilder): MappingJackson2HttpMessageConverter {
    return MappingJackson2HttpMessageConverter(jackson2ObjectMapperBuilder.build())
  }

}

