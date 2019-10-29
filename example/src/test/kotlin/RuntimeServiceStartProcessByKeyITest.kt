/*-
 * #%L
 * camunda-bpm-feign-example
 * %%
 * Copyright (C) 2019 Camunda Services GmbH
 * %%
 * /*
 *  * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 *  * under one or more contributor license agreements. See the NOTICE file
 *  * distributed with this work for additional information regarding copyright
 *  * ownership. Camunda licenses this file to you under the Apache License,
 *  * Version 2.0; you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  */
 * #L%
 */
package org.camunda.bpm.extension.feign.itest

import com.tngtech.jgiven.annotation.As
import org.assertj.core.api.Assertions.assertThat
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.variable.Variables.createVariables
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import java.util.*

@RuntimeServiceCategory
@As("Start Process By Key")
class RuntimeServiceStartProcessByKeyITest : CamundaBpmFeignITestBase<RuntimeService, RuntimeServiceActionStage, RuntimeServiceAssertStage>() {

  @Test
  fun `should start process by key`() {
    val processDefinitionKey = processDefinitionKey()
    given()
      .process_with_user_task_is_deployed(processDefinitionKey)
    whenever()
      .remoteService
      .startProcessInstanceByKey(processDefinitionKey)

    then()
      .process_instance_exists(processDefinitionKey = processDefinitionKey) { instance, stage ->
        assertThat(instance.businessKey).isNull()
        assertThat(stage.localService.getVariables(instance.id)).isEmpty()
      }
  }

  @Test
  fun `should start process by key with business key`() {
    val processDefinitionKey = processDefinitionKey()
    given()
      .process_with_user_task_is_deployed(processDefinitionKey)

    whenever()
      .remoteService
      .startProcessInstanceByKey(processDefinitionKey, "businessKey")

    then()
      .process_instance_exists(processDefinitionKey = processDefinitionKey) { instance, stage ->
        assertThat(instance.businessKey).isEqualTo("businessKey")
        assertThat(stage.localService.getVariables(instance.id)).isEmpty()
      }
  }

  @Test
  fun `should start process by key with business key and variables`() {
    val processDefinitionKey = processDefinitionKey()
    given()
      .process_with_user_task_is_deployed(processDefinitionKey)

    whenever()
      .remoteService
      .startProcessInstanceByKey(processDefinitionKey, "businessKey", createVariables().putValue("VAR_NAME", "var value"))

    then()
      .process_instance_exists(processDefinitionKey = processDefinitionKey) { instance, stage ->
        assertThat(instance.businessKey).isEqualTo("businessKey")
        assertThat(stage.localService.getVariables(instance.id)).isNotEmpty
        assertThat(stage.localService.getVariable(instance.id, "VAR_NAME")).isEqualTo("var value")
      }
  }

  @Test
  fun `should start process by key with business key and case instance id`() {
    val processDefinitionKey = processDefinitionKey()
    given()
      .process_with_user_task_is_deployed(processDefinitionKey)

    whenever()
      .remoteService
      .startProcessInstanceByKey(processDefinitionKey, "businessKey", "caseInstanceId")

    then()
      .process_instance_exists(processDefinitionKey = processDefinitionKey) { instance, stage ->
        assertThat(instance.businessKey).isEqualTo("businessKey")
        assertThat(instance.caseInstanceId).isEqualTo("caseInstanceId")
        assertThat(stage.localService.getVariables(instance.id)).isEmpty()
      }
  }


  @Test
  fun `should start process by key with business key, case instance id and variables`() {
    val processDefinitionKey = processDefinitionKey()
    given()
      .process_with_user_task_is_deployed(processDefinitionKey)

    whenever()
      .remoteService
      .startProcessInstanceByKey(processDefinitionKey, "businessKey", "caseInstanceId", createVariables().putValue("VAR_NAME", "var value"))

    then()
      .process_instance_exists(processDefinitionKey = processDefinitionKey) { instance, stage ->
        assertThat(instance.businessKey).isEqualTo("businessKey")
        assertThat(instance.caseInstanceId).isEqualTo("caseInstanceId")
        assertThat(stage.localService.getVariables(instance.id)).isNotEmpty
        assertThat(stage.localService.getVariable(instance.id, "VAR_NAME")).isEqualTo("var value")
      }
  }

  @Test
  fun `should start process by key with variables`() {
    val processDefinitionKey = processDefinitionKey()
    given()
      .process_with_user_task_is_deployed(processDefinitionKey)

    whenever()
      .remoteService
      .startProcessInstanceByKey(processDefinitionKey, createVariables().putValue("VAR_NAME", "var value"))

    then()
      .process_instance_exists(processDefinitionKey = processDefinitionKey) { instance, stage ->
        assertThat(instance.businessKey).isNull()
        assertThat(stage.localService.getVariables(instance.id)).isNotEmpty
        assertThat(stage.localService.getVariable(instance.id, "VAR_NAME")).isEqualTo("var value")
      }
  }
}
