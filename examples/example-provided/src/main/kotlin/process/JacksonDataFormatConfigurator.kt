/*-
 * #%L
 * camunda-rest-client-spring-boot-example
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
package org.camunda.bpm.extension.rest.example.processapplication.process

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.camunda.spin.impl.json.jackson.format.JacksonJsonDataFormat
import org.camunda.spin.spi.DataFormatConfigurator
import java.text.SimpleDateFormat


/**
 * Configured SPIN Jackson Mapper.
 */
class JacksonDataFormatConfigurator : DataFormatConfigurator<JacksonJsonDataFormat> {

  companion object {
    /**
     * Factory configuring object mapper.
     */
    fun configureObjectMapper(objectMapper: ObjectMapper) = objectMapper.apply {
      registerModule(KotlinModule())
      registerModule(Jdk8Module())
      registerModule(JavaTimeModule())
      dateFormat = SimpleDateFormat("yyyy-MM-dd'T'hh:MM:ss.SSSz")
      disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
      disable(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS)
      disable(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS)
    }
  }

  override fun configure(dataFormat: JacksonJsonDataFormat) {
    configureObjectMapper(dataFormat.objectMapper)
  }

  override fun getDataFormatClass(): Class<JacksonJsonDataFormat> {
    return JacksonJsonDataFormat::class.java
  }
}

