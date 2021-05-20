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

import mu.KLogging
import org.camunda.bpm.engine.delegate.JavaDelegate
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Instant
import java.util.*

/**
 * Configure delegates for the process service tasks.
 */
@Configuration
class ProcessDelegatesConfiguration {

  companion object : KLogging()

  /**
   * Logging delegate.
   */
  @Bean
  fun loggingDelegate() = JavaDelegate {
    logger.info { "PROCESS-LOGGER-001: ${it.eventName.toGerund()} '${it.currentActivityName.toSinglePrettyString()}, payload: ${it.variables}" }
  }

  /**
   * Value setting delegate.
   */
  @Bean
  fun variableSetter() = JavaDelegate {
    // setting untyped variable
    it.setVariable("MY_UNTYPED_STRING", "Example Value")
    it.setVariable("MY_UNTYPED_OBJ", Payload())
  }
}

/**
 * Payload class.
 */
data class Payload(val time: Instant = Instant.now(), val field: String = UUID.randomUUID().toString())

/**
 * String formatting.
 */
private fun String.toSinglePrettyString() = this
  .replace("\n", " ")
  .replace("\t", " ")
  .replace("  ", " ")

/**
 * task name formatting.
 */
private fun String.toGerund() = when (this.length) {
  0 -> ""
  1 -> "${this.uppercase(Locale.getDefault())}ing"
  else -> "${this.substring(0, 1).uppercase(Locale.getDefault())}${this.substring(1, this.length)}ing"
}

