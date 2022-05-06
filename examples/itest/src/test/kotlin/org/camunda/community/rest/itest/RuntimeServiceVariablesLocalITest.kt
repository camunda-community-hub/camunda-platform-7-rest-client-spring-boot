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
import org.camunda.bpm.engine.variable.Variables.*
import org.camunda.bpm.engine.variable.value.StringValue
import org.camunda.community.rest.itest.stages.CamundaRestClientITestBase
import org.camunda.community.rest.itest.stages.RuntimeServiceActionStage
import org.camunda.community.rest.itest.stages.RuntimeServiceAssertStage
import org.camunda.community.rest.itest.stages.RuntimeServiceCategory
import org.junit.Test

@RuntimeServiceCategory
@As("Variables")
class RuntimeServiceVariablesLocalITest :
  CamundaRestClientITestBase<RuntimeService, RuntimeServiceActionStage, RuntimeServiceAssertStage>() {

  @Test
  fun `should add new, update and delete existing local variables`() {
    val processDefinitionKey = processDefinitionKey()
    val signalName = "var_process_blocker_2"
    val userTaskId = "user-task"
    GIVEN
      .process_with_intermediate_signal_catch_event_is_deployed(processDefinitionKey, userTaskId, signalName)
      .AND
      .process_is_started_by_key(
        processDefinitionKey, "my-business-key2", "caseInstanceId2",
        createVariables()
          .putValue("VAR1", "VAL1")
          .putValue("VAR2", "VAL2")
          .putValueTyped("VAR3", stringValue("VAL3"))
          .putValueTyped("VAR4", objectValue("My object value").create())
      )

    WHEN
      .remoteService
      .removeVariableLocal(GIVEN.processInstance.id, "VAR2")

    WHEN
      .remoteService
      .setVariableLocal(GIVEN.processInstance.id, "VAR1", "NEW VALUE")

    WHEN
      .remoteService
      .setVariableLocal(GIVEN.processInstance.id, "TO_REMOVE", "TO_REMOVE")

    WHEN
      .remoteService
      .removeVariablesLocal(GIVEN.processInstance.id, listOf("TO_REMOVE"))


    WHEN
      .remoteService
      .setVariableLocal(GIVEN.processInstance.id, "VAR5", "untyped")

    WHEN
      .remoteService
      .setVariablesLocal(GIVEN.processInstance.id, createVariables().putValueTyped("VAR6", stringValue("typed")))


    THEN
      .process_instance_exists(processDefinitionKey) { instance, stage ->
        assertThat(instance.businessKey).isEqualTo("my-business-key2")
        assertThat(instance.caseInstanceId).isEqualTo("caseInstanceId2")

        assertThat(stage.remoteService.getVariablesLocal(instance.id)).containsKeys("VAR1", "VAR3", "VAR4", "VAR5", "VAR6")
        assertThat(stage.remoteService.getVariablesLocal(instance.id, listOf("VAR1", "VAR2", "VAR6"))).containsKeys("VAR1", "VAR6")
        assertThat(stage.remoteService.getVariableLocal(instance.id, "VAR1")).isEqualTo("NEW VALUE")

        assertThat(stage.remoteService.getVariablesLocalTyped(instance.id)).containsKeys("VAR1", "VAR3", "VAR4", "VAR5", "VAR6")
        assertThat(stage.remoteService.getVariablesLocalTyped(instance.id, listOf("VAR1", "VAR2", "VAR6"), true)).containsKeys(
          "VAR1",
          "VAR6"
        )

        assertThat(stage.remoteService.getVariableLocalTyped<StringValue>(instance.id, "VAR6")).isEqualTo(stringValue("typed"))

        assertThat(stage.localService.getVariablesLocal(instance.id)).containsKeys("VAR1", "VAR3", "VAR4", "VAR5", "VAR6")
        assertThat(stage.localService.getVariableLocal(instance.id, "VAR1")).isEqualTo("NEW VALUE")
        assertThat(stage.localService.getVariableLocal(instance.id, "VAR3")).isEqualTo("VAL3")
        assertThat(stage.localService.getVariableLocal(instance.id, "VAR4")).isEqualTo("My object value")
        assertThat(stage.localService.getVariableLocal(instance.id, "VAR5")).isEqualTo("untyped")
        assertThat(stage.localService.getVariableLocalTyped<StringValue>(instance.id, "VAR6")).isEqualTo(stringValue("typed"))
      }
  }
}
