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
import org.camunda.spin.Spin.JSON
import org.camunda.spin.plugin.variable.SpinValues.jsonValue
import org.camunda.spin.plugin.variable.value.JsonValue
import org.junit.jupiter.api.Test

@As("Variables")
class RuntimeServiceVariablesSpinITest : CamundaRestClientITestBase<RuntimeService, RuntimeServiceActionStage, RuntimeServiceAssertStage>() {

  @Test
  fun `should add and read json spin variables`() {
    val processDefinitionKey = processDefinitionKey()
    val signalName = "var_process_blocker_1"
    val userTaskId = "user-task"
    val structure = ComplexDataStructure("string", 17)

    GIVEN
      .process_with_intermediate_signal_catch_event_is_deployed(processDefinitionKey, userTaskId, signalName)
      .AND
      .process_is_started_by_key(
        processDefinitionKey, "my-business-key1", "caseInstanceId1",
        createVariables()
          .putValue("VAR1", JSON(ComplexDataStructure("string", 42)))
          .putValueTyped("VAR2", jsonValue(JSON(ComplexDataStructure("string", 42))).create())
      )


    WHEN
      .remoteService
      .setVariables(GIVEN.processInstance.id, createVariables().putValue("VAR1", JSON(structure)))


    THEN
      .process_instance_exists(processDefinitionKey) { instance, stage ->
        assertThat(instance.businessKey).isEqualTo("my-business-key1")
        assertThat(instance.caseInstanceId).isEqualTo("caseInstanceId1")

        assertThat(stage.remoteService.getVariables(instance.id)).containsKeys("VAR1")

        assertThat(stage.remoteService.getVariableTyped<JsonValue>(instance.id, "VAR1").value.mapTo(ComplexDataStructure::class.java))
          .usingRecursiveComparison().isEqualTo(structure)

        assertThat(stage.remoteService.getVariable(GIVEN.processInstance.id, "VAR1")).usingRecursiveComparison().isEqualTo(
          stage.localService.getVariable(GIVEN.processInstance.id, "VAR1"))
        assertThat(stage.remoteService.getVariable(GIVEN.processInstance.id, "VAR2")).usingRecursiveComparison().isEqualTo(
          stage.localService.getVariable(GIVEN.processInstance.id, "VAR2"))
        assertThat(stage.remoteService.getVariableTyped<JsonValue>(GIVEN.processInstance.id, "VAR1")).usingRecursiveComparison().isEqualTo(
          stage.localService.getVariableTyped(GIVEN.processInstance.id, "VAR1"))
        assertThat(stage.remoteService.getVariableTyped<JsonValue>(GIVEN.processInstance.id, "VAR2")).usingRecursiveComparison().isEqualTo(
          stage.localService.getVariableTyped(GIVEN.processInstance.id, "VAR2"))
        assertThat(stage.remoteService.getVariableTyped<JsonValue>(GIVEN.processInstance.id, "VAR1", false)).usingRecursiveComparison().isEqualTo(
          stage.localService.getVariableTyped(GIVEN.processInstance.id, "VAR1", false))
        assertThat(stage.remoteService.getVariableTyped<JsonValue>(GIVEN.processInstance.id, "VAR2", false)).usingRecursiveComparison().isEqualTo(
          stage.localService.getVariableTyped(GIVEN.processInstance.id, "VAR2", false))
      }
  }
}
