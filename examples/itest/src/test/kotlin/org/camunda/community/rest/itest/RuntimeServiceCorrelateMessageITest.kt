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
import org.camunda.bpm.engine.MismatchingMessageCorrelationException
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.variable.Variables.createVariables
import org.camunda.community.rest.itest.stages.CamundaRestClientITestBase
import org.camunda.community.rest.itest.stages.RuntimeServiceActionStage
import org.camunda.community.rest.itest.stages.RuntimeServiceAssertStage
import org.junit.Test

@As("Correlate Message")
class RuntimeServiceCorrelateMessageITest :
  CamundaRestClientITestBase<RuntimeService, RuntimeServiceActionStage, RuntimeServiceAssertStage>() {

  @Test
  fun `should correlate message with definition`() {
    val processDefinitionKey = processDefinitionKey()
    val messageName = "myStartMessage"
    val userTaskId = "user-task-in-process-started-by-message"

    GIVEN
      .process_with_start_by_message_event_is_deployed(processDefinitionKey, userTaskId, messageName)

    WHEN
      .remoteService
      .correlateMessage(messageName)

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
  fun `should correlate message with waiting instance`() {
    val processDefinitionKey = processDefinitionKey()
    val messageName = "myEventMessage"
    val userTaskId = "user-task"
    GIVEN
      .process_with_intermediate_message_catch_event_is_deployed(processDefinitionKey, userTaskId, messageName)
      .AND
      .process_is_started_by_key(processDefinitionKey)
      .AND

    WHEN
      .remoteService
      .correlateMessage(messageName)

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
  fun `should fail to correlate message with waiting instance`() {
    val processDefinitionKey = processDefinitionKey()
    val messageName = "myEventMessage"
    val userTaskId = "user-task"
    GIVEN
      .process_with_intermediate_message_catch_event_is_deployed(processDefinitionKey, userTaskId, messageName)
      .AND
      .process_is_started_by_key(processDefinitionKey)
      .AND

    THEN
      .process_engine_exception_is_thrown_caused_by(
        clazz = MismatchingMessageCorrelationException::class.java,
        reason = "Cannot correlate message 'wrong-message': No process definition or execution matches the parameters"
      ) {
        WHEN
          .remoteService
          .correlateMessage("wrong-message")
      }
  }

  @Test
  fun `should correlate message and business key with waiting instance`() {
    val processDefinitionKey = processDefinitionKey()
    val messageName = "myEventMessage"
    val userTaskId = "user-task"
    GIVEN
      .process_with_intermediate_message_catch_event_is_deployed(processDefinitionKey, userTaskId, messageName)
      .AND
      .process_is_started_by_key(processDefinitionKey, "my-business-key1")

    WHEN
      .remoteService
      .correlateMessage(messageName, "my-business-key1")

    THEN
      .process_instance_exists(processDefinitionKey) { instance, stage ->
        assertThat(instance.businessKey).isEqualTo("my-business-key1")
        assertThat(
          stage.localService.createProcessInstanceQuery()
            .processInstanceId(instance.id)
            .activityIdIn(userTaskId)
            .singleResult()
        ).isNotNull
      }
  }

  @Test
  fun `should correlate message, business key and with waiting instance and set variables`() {
    val processDefinitionKey = processDefinitionKey()
    val messageName = "myEventMessage2"
    val userTaskId = "user-task"
    GIVEN
      .process_with_intermediate_message_catch_event_is_deployed(processDefinitionKey, userTaskId, messageName)
      .AND
      .process_is_started_by_key(processDefinitionKey, "my-business-key1", "caseInstanceId1", createVariables().putValue("VAR1", "VAL1"))

    WHEN
      .remoteService
      .correlateMessage(messageName, "my-business-key1", createVariables().putValue("VAR2", "VAL2"))

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
  fun `should correlate message and correlation keys with waiting instance`() {
    val processDefinitionKey = processDefinitionKey()
    val messageName = "myEventMessage8"
    val userTaskId = "user-task"
    GIVEN
      .process_with_intermediate_message_catch_event_is_deployed(processDefinitionKey, userTaskId, messageName)
      .AND
      .process_is_started_by_key(processDefinitionKey, "my-business-key1", "caseInstanceId1", createVariables().putValue("VAR9", "VAL9"))

    WHEN
      .remoteService
      .correlateMessage(messageName, createVariables().putValue("VAR9", "VAL9"))

    THEN
      .process_instance_exists(processDefinitionKey) { instance, stage ->
        assertThat(instance.caseInstanceId).isEqualTo("caseInstanceId1")
        assertThat(
          stage.localService.createProcessInstanceQuery()
            .processInstanceId(instance.id)
            .activityIdIn(userTaskId)
            .singleResult()
        ).isNotNull
        assertThat(
          stage.localService.getVariables(instance.id, listOf("VAR9"))
        ).containsValues("VAL9")

      }
  }

  @Test
  fun `should correlate message, and correlation keys with waiting instance and set variables`() {
    val processDefinitionKey = processDefinitionKey()
    val messageName = "myEventMessage6"
    val userTaskId = "user-task"
    val businessKey = "my-business-key6"
    val caseInstanceId = "caseInstanceId6"
    GIVEN
      .process_with_intermediate_message_catch_event_is_deployed(processDefinitionKey, userTaskId, messageName)
      .AND
      .process_is_started_by_key(processDefinitionKey, businessKey, caseInstanceId, createVariables().putValue("VAR1", "VAL1"))
      .AND
      .process_is_started_by_key(processDefinitionKey, businessKey, caseInstanceId, createVariables().putValue("VAR2", "VAL2"))

    WHEN
      .remoteService
      .correlateMessage(messageName, createVariables().putValue("VAR2", "VAL2"), createVariables().putValue("VAR-NEW", "VAL-NEW"))

    THEN
      .process_instance_exists(
        processDefinitionKey,
        containingSimpleProcessVariables = createVariables().putValue("VAR-NEW", "VAL-NEW")
      ) { instance, stage ->
        assertThat(instance.businessKey).isEqualTo(businessKey)
        assertThat(instance.caseInstanceId).isEqualTo(caseInstanceId)
        assertThat(
          stage.localService.createProcessInstanceQuery()
            .processInstanceId(instance.id)
            .activityIdIn(userTaskId)
            .singleResult()
        ).isNotNull
        assertThat(
          stage.localService.getVariables(instance.id, listOf("VAR2", "VAR-NEW"))
        ).containsValues("VAL2", "VAL-NEW")

      }
  }


  @Test
  fun `should correlate message, business key and correlation keys with waiting instance and set variables`() {
    val processDefinitionKey = processDefinitionKey()
    val messageName = "myEventMessage3"
    val userTaskId = "user-task"
    val businessKey = "my-business-key3"
    val caseInstanceId = "caseInstanceId3"
    GIVEN
      .process_with_intermediate_message_catch_event_is_deployed(processDefinitionKey, userTaskId, messageName)
      .AND
      .process_is_started_by_key(processDefinitionKey, businessKey, caseInstanceId, createVariables().putValue("VAR1", "VAL1"))
      .AND
      .process_is_started_by_key(processDefinitionKey, businessKey, caseInstanceId, createVariables().putValue("VAR2", "VAL2"))

    WHEN
      .remoteService
      .correlateMessage(
        messageName,
        businessKey,
        createVariables().putValue("VAR2", "VAL2"),
        createVariables().putValue("VAR-NEW", "VAL-NEW")
      )

    THEN
      .process_instance_exists(
        processDefinitionKey,
        containingSimpleProcessVariables = createVariables().putValue("VAR-NEW", "VAL-NEW")
      ) { instance, stage ->
        assertThat(instance.businessKey).isEqualTo(businessKey)
        assertThat(instance.caseInstanceId).isEqualTo(caseInstanceId)
        assertThat(
          stage.localService.createProcessInstanceQuery()
            .processInstanceId(instance.id)
            .activityIdIn(userTaskId)
            .singleResult()
        ).isNotNull
        assertThat(
          stage.localService.getVariables(instance.id, listOf("VAR2", "VAR-NEW"))
        ).containsValues("VAL2", "VAL-NEW")

      }
  }

  @Test
  fun `should correlate message created by correlation builder with waiting instance and set variables`() {
    val processDefinitionKey = processDefinitionKey()
    val messageName = "myEventMessage9"
    val userTaskId = "user-task"
    GIVEN
      .process_with_intermediate_message_catch_event_is_deployed(processDefinitionKey, userTaskId, messageName)
      .AND
      .process_is_started_by_key(processDefinitionKey, "my-business-key9", "caseInstanceId1", createVariables().putValue("VAR9", "VAL9"))

    WHEN
      .remoteService
      .createMessageCorrelation(messageName)
      .setVariables(createVariables().putValue("NEW-VAR", "NEW-VAL"))
      .setVariable("NEW-VAR2", "NEW-VAL2")
      .setVariablesLocal(createVariables().putValue("LOCAL-VAR", "LOCAL-VALUE"))
      .setVariableLocal("LOCAL-VAR2", "LOCAL-VALUE2")
      .processInstanceVariablesEqual(createVariables().putValue("VAR9", "VAL9"))
      .processInstanceId(GIVEN.processInstance.id)
      .processDefinitionId(GIVEN.processDefinition.id)
      .processInstanceBusinessKey("my-business-key9")
      .correlate()

    THEN
      .process_instance_exists(processDefinitionKey) { instance, stage ->
        assertThat(instance.caseInstanceId).isEqualTo("caseInstanceId1")
        assertThat(instance.businessKey).isEqualTo("my-business-key9")
        assertThat(
          stage.localService.createProcessInstanceQuery()
            .processInstanceId(instance.id)
            .activityIdIn(userTaskId)
            .singleResult()
        ).isNotNull
        assertThat(
          stage.localService.getVariables(instance.id, listOf("VAR9", "NEW-VAR", "NEW-VAR2", "LOCAL-VAR", "LOCAL-VAR2"))
        ).containsValues("VAL9", "NEW-VAL", "NEW-VAL2") // no locals
      }
  }
}
