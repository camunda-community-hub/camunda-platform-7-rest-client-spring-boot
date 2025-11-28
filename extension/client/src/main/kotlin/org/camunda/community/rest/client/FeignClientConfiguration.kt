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
import feign.Logger
import feign.Retryer
import feign.codec.Encoder
import org.camunda.community.rest.client.KeyChangingSpringManyMultipartFilesWriter.Companion.camundaMultipartFormEncoder
import org.springframework.beans.factory.ObjectProvider
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.cloud.openfeign.support.FeignEncoderProperties
import org.springframework.cloud.openfeign.support.FeignHttpMessageConverters
import org.springframework.cloud.openfeign.support.SpringEncoder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter
import tools.jackson.databind.DeserializationFeature
import tools.jackson.databind.SerializationFeature
import tools.jackson.databind.cfg.DateTimeFeature
import tools.jackson.databind.ext.javatime.ser.OffsetDateTimeSerializer
import tools.jackson.databind.json.JsonMapper
import tools.jackson.databind.module.SimpleModule
import tools.jackson.databind.ser.jdk.JavaUtilDateSerializer
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter

/**
 * Basic configuration of the feign client.
 */
@Configuration
@EnableFeignClients
class FeignClientConfiguration {

  object CustomOffsetDateTimeSerializer : OffsetDateTimeSerializer(
    OffsetDateTimeSerializer.INSTANCE,
    false,
    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ"),
    JsonFormat.Shape.STRING
  )

  @Bean
  @ConditionalOnMissingBean
  fun camundaFeignEncoder(
    feignEncoderProperties: FeignEncoderProperties,
    converters: ObjectProvider<FeignHttpMessageConverters>
  ): Encoder {
    return SpringEncoder(camundaMultipartFormEncoder(), feignEncoderProperties, converters)
  }

  @Bean
  fun camundaRetryer() = Retryer.Default()

  /**
   * Configure feign logger level if corresponding property is set.
   */
  @Bean
  @ConditionalOnProperty("feign.client.config.default.loggerLevel")
  fun feignLoggerLevel(@Value("\${feign.client.config.default.loggerLevel}") defaultLogLevel: String) =
    Logger.Level.valueOf(defaultLogLevel)

  @Bean
  fun jsonHttpMessageConverter(): HttpMessageConverter<*> {
    val module = SimpleModule()
    module.addSerializer(CustomOffsetDateTimeSerializer)
    module.addSerializer(
      JavaUtilDateSerializer(
        false,
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
      )
    )
    val builder = JsonMapper.builder()
      .disable(DateTimeFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
      .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
      .addModule(module)
    return JacksonJsonHttpMessageConverter(builder)
  }

}

