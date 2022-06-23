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
import io.toolisticon.testing.jgiven.GIVEN
import io.toolisticon.testing.jgiven.THEN
import io.toolisticon.testing.jgiven.WHEN
import org.assertj.core.api.Assertions.assertThat
import org.camunda.bpm.engine.RuntimeService
import org.camunda.community.rest.itest.stages.CamundaRestClientITestBase
import org.camunda.community.rest.itest.stages.RuntimeServiceActionStage
import org.camunda.community.rest.itest.stages.RuntimeServiceAssertStage
import org.junit.Test

@As("Start Process By Id")
class RuntimeServiceStartProcessByMessageTest :
  CamundaRestClientITestBase<RuntimeService, RuntimeServiceActionStage, RuntimeServiceAssertStage>() {

  @Test
  fun `should start process with message`() {
    val processDefinitionKey = processDefinitionKey()
    val messageName = "myStartMessage"
    val userTaskId = "user-task-in-process-started-by-message"

    GIVEN
      .process_with_start_by_message_event_is_deployed(processDefinitionKey, userTaskId, messageName)

    WHEN
      .remoteService
      .startProcessInstanceByMessage(messageName)

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
  fun `should start process with message and business key`() {
    val processDefinitionKey = processDefinitionKey()
    val messageName = "myStartMessageWithBusinessKey"
    val userTaskId = "user-task-in-process-started-by-message"
    val businessKey = "my-business-key"

    GIVEN
      .process_with_start_by_message_event_is_deployed(processDefinitionKey, userTaskId, messageName)

    WHEN
      .remoteService
      .startProcessInstanceByMessage(messageName, businessKey)

    THEN
      .process_instance_exists(processDefinitionKey) { instance, stage ->
        assertThat(instance.businessKey).isEqualTo(businessKey)
        assertThat(
          stage.localService.createProcessInstanceQuery()
            .processInstanceId(instance.id)
            .activityIdIn(userTaskId)
            .singleResult()
        ).isNotNull
      }
  }

  @Test
  fun `should start process with message and variables`() {
    val processDefinitionKey = processDefinitionKey()
    val messageName = "myStartMessageWithVariables"
    val userTaskId = "user-task-in-process-started-by-message"

    GIVEN
      .process_with_start_by_message_event_is_deployed(processDefinitionKey, userTaskId, messageName)

    WHEN
      .remoteService
      .startProcessInstanceByMessage(messageName, mutableMapOf(Pair("int", 1), Pair("string", "String")) as Map<String, Any>)

    THEN
      .process_instance_exists(processDefinitionKey) { instance, stage ->
        assertThat(instance.businessKey).isNull()
        assertThat(
          stage.localService.createProcessInstanceQuery()
            .processInstanceId(instance.id)
            .activityIdIn(userTaskId)
            .singleResult()
        ).isNotNull
        assertThat(
          stage.localService.createVariableInstanceQuery()
            .processInstanceIdIn(instance.id)
            .variableName("int")
            .singleResult()
            .value
        ).isEqualTo(1)
      }
  }


}
