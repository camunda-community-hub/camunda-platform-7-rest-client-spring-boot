/*-
 * #%L
 * camunda-platform-7-rest-client-spring-boot-example
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

package org.camunda.community.rest.example.processapplication.client

import io.github.oshai.kotlinlogging.KotlinLogging
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.variable.Variables.*
import org.camunda.community.rest.variables.toPrettyString
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.scheduling.annotation.Scheduled
import java.time.Instant
import java.util.*

private val logger = KotlinLogging.logger {}

/**
 * This client could be a spring component, but is built using configuration
 * class [ProcessClientConfiguration] in order to control its activation via property.
 */
class ProcessClient(
  @Qualifier("remote") private val runtimeService: RuntimeService,
  @Qualifier("remote") private val repositoryService: RepositoryService
) {

  companion object {
    const val RATE = 5_000L // loop with rate of five seconds
  }

  /**
   * Periodically retrieve process definitions and log them.
   */
  @Scheduled(initialDelay = 8_000, fixedRate = Integer.MAX_VALUE.toLong())
  fun retrieveProcessDefinition() {

    logger.info { "CLIENT-90: Retrieving process definition" }
    val count = repositoryService.createProcessDefinitionQuery().count()
    logger.info { "CLIENT-91: Found $count deployed processes" }
    val processDefinitions = repositoryService.createProcessDefinitionQuery().list()
    logger.info { "CLIENT-92: Deployed process definitions are ${processDefinitions.map{it.toPrettyString()}}" }
  }


  /**
   * Periodically start process.
   */
  @Scheduled(initialDelay = 10_000, fixedDelay = RATE)
  fun startProcess() {

    logger.trace { "CLIENT-100: Starting a process instance remote" }

    val variables = createVariables()
      .putValueTyped("ID", stringValue("MESSAGING-${UUID.randomUUID()}"))
    val instance = runtimeService.startProcessInstanceByKey("process_messaging", "WAIT_FOR_MESSAGE", variables)

    logger.trace { "CLIENT-101: Started instance ${instance.id} - ${instance.businessKey}" }
  }


  /**
   * Periodically fire signals.
   */
  @Scheduled(initialDelay = 12_500, fixedDelay = RATE)
  fun fireSignal() {

    logger.info { "CLIENT-200: Sending signal" }

    val variables = createVariables()
    variables.putValueTyped("BYTES", byteArrayValue("World".toByteArray()))

    runtimeService
      .createSignalEvent("signal_received")
      .setVariables(variables)
      .send()
  }

  /**
   * Periodically correlate messages.
   */
  @Scheduled(initialDelay = 13_500, fixedDelay = RATE)
  fun correlateMessage() {

    logger.info { "CLIENT-300: Correlating message" }

    val variables = createVariables()
    variables.putValueTyped("STRING", stringValue("my string"))
    variables.putValueTyped("CORRELATION_DATE", dateValue(Date.from(Instant.now())))
    variables.putValueTyped("SHORT", shortValue(1))
    variables.putValueTyped("DOUBLE", doubleValue(1.0))
    variables.putValueTyped("INTEGER", integerValue(65800))
    variables.putValueTyped("LONG", longValue(1L + Integer.MAX_VALUE))
    variables.putValueTyped("BYTES", byteArrayValue("Hello!".toByteArray()))
    variables.putValueTyped("OBJECT", objectValue(MyDataStructure("string", 100)).create())

    val result = runtimeService
      .createMessageCorrelation("message_received")
      .processInstanceBusinessKey("WAIT_FOR_MESSAGE")
      .setVariables(variables)
      .correlateAllWithResultAndVariables(true)

    result.forEach {
      logger.info { "CLIENT-301: ${it.toPrettyString()}" }
    }
  }

}

/**
 * Random structure.
 */
data class MyDataStructure(val string: String, val integer: Int)
