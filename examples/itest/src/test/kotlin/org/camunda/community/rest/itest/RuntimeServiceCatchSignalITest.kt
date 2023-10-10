/*-
 * #%L
 * camunda-platform-7-rest-client-spring-boot-itest
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
package org.camunda.community.rest.itest

import com.tngtech.jgiven.annotation.As
import io.toolisticon.testing.jgiven.AND
import io.toolisticon.testing.jgiven.GIVEN
import io.toolisticon.testing.jgiven.THEN
import io.toolisticon.testing.jgiven.WHEN
import org.assertj.core.api.Assertions.assertThat
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.variable.Variables.createVariables
import org.camunda.community.rest.itest.stages.CamundaRestClientITestBase
import org.camunda.community.rest.itest.stages.RuntimeServiceActionStage
import org.camunda.community.rest.itest.stages.RuntimeServiceAssertStage
import org.junit.jupiter.api.Test

@As("Catch Signal")
class RuntimeServiceCatchSignalITest : CamundaRestClientITestBase<RuntimeService, RuntimeServiceActionStage, RuntimeServiceAssertStage>() {

  @Test
  fun `should signal waiting instance`() {
    val processDefinitionKey = processDefinitionKey()
    val signalName = "mySignal1"
    val userTaskId = "user-task"
    GIVEN
      .process_with_intermediate_signal_catch_event_is_deployed(processDefinitionKey, userTaskId, signalName)
      .AND
      .process_is_started_by_key(processDefinitionKey)

    WHEN
      .remoteService
      .signalEventReceived(signalName)

    THEN
      .process_instance_exists(processDefinitionKey) { instance, stage ->
        assertThat(instance.businessKey).isNull()
        assertThat(
          stage.localService.createProcessInstanceQuery()
            .processInstanceId(instance.id)
            .activityIdIn(userTaskId)
            .singleResult()
        ).isNotNull
      }
  }

  @Test
  fun `should signal waiting instance and set variables`() {
    val processDefinitionKey = processDefinitionKey()
    val signalName = "mySignal2"
    val userTaskId = "user-task"
    GIVEN
      .process_with_intermediate_signal_catch_event_is_deployed(processDefinitionKey, userTaskId, signalName)
      .AND
      .process_is_started_by_key(processDefinitionKey, "my-business-key1", "caseInstanceId1", createVariables().putValue("VAR1", "VAL1"))

    WHEN
      .remoteService
      .signalEventReceived(signalName, createVariables().putValue("VAR2", "VAL2"))

    THEN
      .process_instance_exists(processDefinitionKey) { instance, stage ->
        assertThat(instance.businessKey).isEqualTo("my-business-key1")
        assertThat(instance.caseInstanceId).isEqualTo("caseInstanceId1")
        assertThat(
          stage.localService.createProcessInstanceQuery()
            .processInstanceId(instance.id)
            .activityIdIn(userTaskId)
            .singleResult()
        ).isNotNull
        assertThat(
          stage.localService.getVariables(instance.id, listOf("VAR1", "VAR2"))
        ).containsValues("VAL1", "VAL2")

      }
  }

  @Test
  fun `should signal waiting instance by execution id and signal name`() {
    val processDefinitionKey = processDefinitionKey()
    val signalName = "mySignal3"
    val userTaskId = "user-task"
    GIVEN
      .process_with_intermediate_signal_catch_event_is_deployed(processDefinitionKey, userTaskId, signalName)
      .AND
      .process_is_started_by_key(processDefinitionKey, "my-business-key1", "caseInstanceId1", createVariables().putValue("VAR1", "VAL1"))
      .AND
      .execution_is_waiting_for_signal()

    WHEN
      .remoteService
      .signalEventReceived(signalName, GIVEN.execution.id)

    THEN
      .process_instance_exists(processDefinitionKey) { instance, stage ->
        assertThat(instance.businessKey).isEqualTo("my-business-key1")
        assertThat(instance.caseInstanceId).isEqualTo("caseInstanceId1")
        assertThat(
          stage.localService.createProcessInstanceQuery()
            .processInstanceId(instance.id)
            .activityIdIn(userTaskId)
            .singleResult()
        ).isNotNull
        assertThat(
          stage.localService.getVariables(instance.id, listOf("VAR1"))
        ).containsValues("VAL1")
      }
  }

  @Test
  fun `should signal waiting instance by execution id and signal name and set variables`() {
    val processDefinitionKey = processDefinitionKey()
    val signalName = "mySignal4"
    val userTaskId = "user-task"
    GIVEN
      .process_with_intermediate_signal_catch_event_is_deployed(processDefinitionKey, userTaskId, signalName)
      .AND
      .process_is_started_by_key(processDefinitionKey, "my-business-key1", "caseInstanceId1", createVariables().putValue("VAR1", "VAL1"))
      .AND
      .execution_is_waiting_for_signal()

    WHEN
      .remoteService
      .signalEventReceived(signalName, GIVEN.execution.id, createVariables().putValue("VAR2", "VAL2"))

    THEN
      .process_instance_exists(processDefinitionKey) { instance, stage ->
        assertThat(instance.businessKey).isEqualTo("my-business-key1")
        assertThat(instance.caseInstanceId).isEqualTo("caseInstanceId1")
        assertThat(
          stage.localService.createProcessInstanceQuery()
            .processInstanceId(instance.id)
            .activityIdIn(userTaskId)
            .singleResult()
        ).isNotNull
        assertThat(
          stage.localService.getVariables(instance.id, listOf("VAR1", "VAR2"))
        ).containsValues("VAL1", "VAL2")
      }
  }

  @Test
  fun `should signal waiting instance created by signal builder and set variables`() {
    val processDefinitionKey = processDefinitionKey()
    val signalName = "mySignal7"
    val userTaskId = "user-task"
    GIVEN
      .process_with_intermediate_signal_catch_event_is_deployed(processDefinitionKey, userTaskId, signalName)
      .AND
      .process_is_started_by_key(processDefinitionKey, "my-business-key1", "caseInstanceId1", createVariables().putValue("VAR1", "VAL1"))
      .AND
      .execution_is_waiting_for_signal()

    WHEN
      .remoteService
      .createSignalEvent(signalName)
      .executionId(GIVEN.execution.id)
      .setVariables(createVariables().putValue("VAR2", "VAL2"))
      .send()

    THEN
      .process_instance_exists(processDefinitionKey) { instance, stage ->
        assertThat(instance.businessKey).isEqualTo("my-business-key1")
        assertThat(instance.caseInstanceId).isEqualTo("caseInstanceId1")
        assertThat(
          stage.localService.createProcessInstanceQuery()
            .processInstanceId(instance.id)
            .activityIdIn(userTaskId)
            .singleResult()
        ).isNotNull
        assertThat(
          stage.localService.getVariables(instance.id, listOf("VAR1", "VAR2"))
        ).containsValues("VAL1", "VAL2")
      }
  }
}
