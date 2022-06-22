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
import org.camunda.bpm.engine.variable.Variables.createVariables
import org.camunda.community.rest.itest.stages.CamundaRestClientITestBase
import org.camunda.community.rest.itest.stages.RuntimeServiceActionStage
import org.camunda.community.rest.itest.stages.RuntimeServiceAssertStage
import org.junit.Test

@As("Start Process By Id")
class RuntimeServiceStartProcessByIdITest :
  CamundaRestClientITestBase<RuntimeService, RuntimeServiceActionStage, RuntimeServiceAssertStage>() {

  @Test
  fun `should start process by id`() {
    val processDefinitionKey = processDefinitionKey()
    GIVEN
      .process_with_user_task_is_deployed(processDefinitionKey)

    WHEN
      .remoteService
      .startProcessInstanceById(WHEN.processDefinition.id)

    THEN
      .process_instance_exists(processDefinitionId = WHEN.processDefinition.id) { instance, stage ->
        assertThat(instance.businessKey).isNull()
        assertThat(stage.localService.getVariables(instance.id)).isEmpty()
      }
  }

  @Test
  fun `should start process by id with business key`() {
    val processDefinitionKey = processDefinitionKey()
    GIVEN
      .process_with_user_task_is_deployed(processDefinitionKey)

    WHEN
      .remoteService
      .startProcessInstanceById(WHEN.processDefinition.id, "businessKey")

    THEN
      .process_instance_exists(processDefinitionId = WHEN.processDefinition.id) { instance, stage ->
        assertThat(instance.businessKey).isEqualTo("businessKey")
        assertThat(stage.localService.getVariables(instance.id)).isEmpty()
      }
  }

  @Test
  fun `should start process by id with business id and variables`() {
    val processDefinitionKey = processDefinitionKey()
    GIVEN
      .process_with_user_task_is_deployed(processDefinitionKey)

    WHEN
      .remoteService
      .startProcessInstanceById(WHEN.processDefinition.id, "businessKey", createVariables().putValue("VAR_NAME", "var value"))

    THEN
      .process_instance_exists(processDefinitionId = WHEN.processDefinition.id) { instance, stage ->
        assertThat(instance.businessKey).isEqualTo("businessKey")
        assertThat(stage.localService.getVariables(instance.id)).isNotEmpty
        assertThat(stage.localService.getVariable(instance.id, "VAR_NAME")).isEqualTo("var value")
      }
  }

  @Test
  fun `should start process by id with business id and case instance id`() {
    val processDefinitionKey = processDefinitionKey()
    GIVEN
      .process_with_user_task_is_deployed(processDefinitionKey)

    WHEN
      .remoteService
      .startProcessInstanceById(WHEN.processDefinition.id, "businessKey", "caseInstanceId")

    THEN
      .process_instance_exists(processDefinitionId = WHEN.processDefinition.id) { instance, stage ->
        assertThat(instance.businessKey).isEqualTo("businessKey")
        assertThat(instance.caseInstanceId).isEqualTo("caseInstanceId")
        assertThat(stage.localService.getVariables(instance.id)).isEmpty()
      }
  }


  @Test
  fun `should start process by id with business key, case instance id and variables`() {
    val processDefinitionKey = processDefinitionKey()

    GIVEN
      .process_with_user_task_is_deployed(processDefinitionKey)

    WHEN
      .remoteService
      .startProcessInstanceById(
        WHEN.processDefinition.id,
        "businessKey",
        "caseInstanceId",
        createVariables().putValue("VAR_NAME", "var value")
      )

    THEN
      .process_instance_exists(processDefinitionId = WHEN.processDefinition.id) { instance, stage ->
        assertThat(instance.businessKey).isEqualTo("businessKey")
        assertThat(instance.caseInstanceId).isEqualTo("caseInstanceId")
        assertThat(stage.localService.getVariables(instance.id)).isNotEmpty
        assertThat(stage.localService.getVariable(instance.id, "VAR_NAME")).isEqualTo("var value")
      }
  }

  @Test
  fun `should start process by id with variables`() {
    val processDefinitionKey = processDefinitionKey()

    GIVEN
      .process_with_user_task_is_deployed(processDefinitionKey)

    WHEN
      .remoteService
      .startProcessInstanceById(WHEN.processDefinition.id, createVariables().putValue("VAR_NAME", "var value"))

    THEN
      .process_instance_exists(processDefinitionId = WHEN.processDefinition.id) { instance, stage ->
        assertThat(instance.businessKey).isNull()
        assertThat(stage.localService.getVariables(instance.id)).isNotEmpty
        assertThat(stage.localService.getVariable(instance.id, "VAR_NAME")).isEqualTo("var value")
      }
  }
}
