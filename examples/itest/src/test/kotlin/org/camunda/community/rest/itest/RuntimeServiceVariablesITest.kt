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
import io.toolisticon.testing.jgiven.WHEN
import io.toolisticon.testing.jgiven.THEN
import io.toolisticon.testing.jgiven.AND
import org.assertj.core.api.Assertions.assertThat
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.variable.Variables.*
import org.camunda.bpm.engine.variable.value.ObjectValue
import org.camunda.bpm.engine.variable.value.StringValue
import org.camunda.community.rest.itest.stages.CamundaRestClientITestBase
import org.camunda.community.rest.itest.stages.RuntimeServiceActionStage
import org.camunda.community.rest.itest.stages.RuntimeServiceAssertStage
import org.camunda.community.rest.itest.stages.RuntimeServiceCategory
import org.junit.Test

@RuntimeServiceCategory
@As("Variables")
class RuntimeServiceVariablesITest : CamundaRestClientITestBase<RuntimeService, RuntimeServiceActionStage, RuntimeServiceAssertStage>() {

  @Test
  fun `should add new, update and delete existing variables`() {
    val processDefinitionKey = processDefinitionKey()
    val signalName = "var_process_blocker_1"
    val userTaskId = "user-task"
    val structure = ComplexDataStructure("string", 17)

    GIVEN
      .process_with_intermediate_signal_catch_event_is_deployed(processDefinitionKey, userTaskId, signalName)
      .AND
      .process_is_started_by_key(processDefinitionKey, "my-business-key1", "caseInstanceId1",
        createVariables()
          .putValue("VAR1", "VAL1")
          .putValue("VAR2", "VAL2")
          .putValue("TO_REMOVE1", "TO_REMOVE")
          .putValue("TO_REMOVE2", "TO_REMOVE")
          .putValueTyped("VAR3", stringValue("VAL3"))
          .putValueTyped("VAR4", stringValue("My object value"))
          .putValueTyped("VAR7",
            objectValue(structure)
              .serializationDataFormat("application/json")
              .create()
          )
      )

    WHEN
      .remoteService
      .removeVariable(GIVEN.processInstance.id, "VAR2")

    WHEN
      .remoteService
      .removeVariables(GIVEN.processInstance.id, listOf("TO_REMOVE1", "TO_REMOVE2"))

    WHEN
      .remoteService
      .setVariable(GIVEN.processInstance.id, "VAR1", "NEW VALUE")

    WHEN
      .remoteService
      .setVariable(GIVEN.processInstance.id, "VAR5", "untyped")

    WHEN
      .remoteService
      .setVariables(GIVEN.processInstance.id, createVariables().putValueTyped("VAR6", stringValue("typed")))


    THEN
      .process_instance_exists(processDefinitionKey) { instance, stage ->
        assertThat(instance.businessKey).isEqualTo("my-business-key1")
        assertThat(instance.caseInstanceId).isEqualTo("caseInstanceId1")

        assertThat(stage.remoteService.getVariables(instance.id)).containsKeys("VAR1", "VAR3", "VAR4", "VAR5", "VAR6")
        assertThat(stage.remoteService.getVariables(instance.id, listOf("VAR1", "VAR2", "VAR6"))).containsKeys("VAR1", "VAR6")
        assertThat(stage.remoteService.getVariable(instance.id, "VAR1")).isEqualTo("NEW VALUE")

        assertThat(stage.remoteService.getVariableTyped<ObjectValue>(instance.id, "VAR7").value).isEqualTo(structure)
        assertThat(stage.remoteService.getVariableTyped<StringValue>(instance.id, "VAR4").value).isEqualTo("My object value")
        assertThat(stage.remoteService.getVariableTyped<StringValue>(instance.id, "VAR6")).isEqualTo(stringValue("typed"))
        assertThat(stage.remoteService.getVariableTyped<StringValue>(instance.id, "VAR6")).isEqualTo(stringValue("typed"))
        assertThat(stage.remoteService.getVariableTyped<StringValue>(instance.id, "VAR6", true)).isEqualTo(stringValue("typed"))

        assertThat(stage.remoteService.getVariablesTyped(instance.id)).containsKeys("VAR1", "VAR3", "VAR4", "VAR5", "VAR6")
        assertThat(stage.remoteService.getVariablesTyped(instance.id, true)).containsValues("NEW VALUE", "VAL3", "My object value", "untyped", "typed")
        assertThat(stage.remoteService.getVariablesTyped(instance.id, listOf("VAR1", "VAR6"), true)).containsValues("NEW VALUE", "typed")

        assertThat(stage.localService.getVariables(instance.id)).containsKeys("VAR1", "VAR3", "VAR4", "VAR5", "VAR6")
        assertThat(stage.localService.getVariable(instance.id, "VAR1")).isEqualTo("NEW VALUE")
        assertThat(stage.localService.getVariable(instance.id, "VAR3")).isEqualTo("VAL3")
        assertThat(stage.localService.getVariable(instance.id, "VAR4")).isEqualTo("My object value")
        assertThat(stage.localService.getVariable(instance.id, "VAR5")).isEqualTo("untyped")
        assertThat(stage.localService.getVariableTyped<StringValue>(instance.id, "VAR6")).isEqualTo(stringValue("typed"))
      }
  }
}

data class ComplexDataStructure(val string: String, val integer: Int)
