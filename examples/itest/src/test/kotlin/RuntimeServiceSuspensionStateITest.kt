/*-
 * #%L
 * camunda-rest-client-spring-boot-itest
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
package org.camunda.bpm.extension.rest.itest

import com.tngtech.jgiven.annotation.As
import io.toolisticon.testing.jgiven.AND
import io.toolisticon.testing.jgiven.GIVEN
import io.toolisticon.testing.jgiven.THEN
import io.toolisticon.testing.jgiven.WHEN
import org.assertj.core.api.Assertions.assertThat
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.extension.rest.itest.stages.CamundaRestClientITestBase
import org.camunda.bpm.extension.rest.itest.stages.RuntimeServiceActionStage
import org.camunda.bpm.extension.rest.itest.stages.RuntimeServiceAssertStage
import org.camunda.bpm.extension.rest.itest.stages.RuntimeServiceCategory
import org.junit.Test
import java.util.concurrent.TimeUnit

@RuntimeServiceCategory
@As("Update Suspension State")
class RuntimeServiceSuspensionStateITest : CamundaRestClientITestBase<RuntimeService, RuntimeServiceActionStage, RuntimeServiceAssertStage>() {

  @Test
  fun `should suspend and activate process instance`() {
    val processDefinitionKey = processDefinitionKey()
    GIVEN
      .process_with_user_task_is_deployed(processDefinitionKey)

    WHEN
      .process_is_started_by_key(processDefinitionKey)

    THEN
      .process_instance_is_suspended()
      .AND
      .process_instance_query_succeeds { processInstanceQuery, _ ->
        assertThat(
          processInstanceQuery.processDefinitionKey(processDefinitionKey)
            .singleResult().isSuspended
        ).isTrue()
      }
      .process_instance_is_activated()
      .AND
      .process_instance_query_succeeds { processInstanceQuery, _ ->
        assertThat(
          processInstanceQuery.processDefinitionKey(processDefinitionKey)
            .singleResult().isSuspended
        ).isFalse()
      }

  }

  @Test
  fun `should suspend and activate process instance by process definition key`() {
    val processDefinitionKey = processDefinitionKey()
    GIVEN
      .process_with_user_task_is_deployed(processDefinitionKey)

    WHEN
      .process_is_started_by_key(processDefinitionKey)

    THEN
      .process_instance_is_suspended_by_process_definition_key(processDefinitionKey)
      .AND
      .process_instance_query_succeeds { processInstanceQuery, _ ->
        assertThat(
          processInstanceQuery.processDefinitionKey(processDefinitionKey)
            .singleResult().isSuspended
        ).isTrue()
      }
      .process_instance_is_activated_by_process_definition_key(processDefinitionKey)
      .AND
      .process_instance_query_succeeds { processInstanceQuery, _ ->
        assertThat(
          processInstanceQuery.processDefinitionKey(processDefinitionKey)
            .singleResult().isSuspended
        ).isFalse()
      }

  }

  @Test
  fun `should suspend and activate with update of suspension state`() {
    val processDefinitionKey = processDefinitionKey()
    GIVEN
      .process_with_user_task_is_deployed(processDefinitionKey)

    WHEN
      .process_is_started_by_key(processDefinitionKey)

    THEN
      .remoteService.updateProcessInstanceSuspensionState().byProcessDefinitionKey(processDefinitionKey).suspend()

    THEN
      .process_instance_query_succeeds { processInstanceQuery, _ ->
        assertThat(
          processInstanceQuery.processDefinitionKey(processDefinitionKey)
            .singleResult().isSuspended
        ).isTrue()
      }

    THEN
      .remoteService.updateProcessInstanceSuspensionState().byProcessInstanceIds(THEN.processInstance!!.id).activate()

    THEN
      .process_instance_query_succeeds { processInstanceQuery, _ ->
        assertThat(
          processInstanceQuery.processDefinitionKey(processDefinitionKey)
            .singleResult().isSuspended
        ).isFalse()
      }

  }

  @Test
  fun `should suspend and activate with update of suspension state async`() {
    val processDefinitionKey = processDefinitionKey()
    GIVEN
      .process_with_user_task_is_deployed(processDefinitionKey)

    WHEN
      .process_is_started_by_key(processDefinitionKey)

    val batchSuspend = THEN
      .remoteService.updateProcessInstanceSuspensionState().byProcessInstanceIds(THEN.processInstance!!.id).suspendAsync()

    TimeUnit.SECONDS.sleep(1)

    assertThat(batchSuspend).isNotNull
    assertThat(batchSuspend.totalJobs).isEqualTo(1)
    THEN
      .process_instance_query_succeeds { processInstanceQuery, _ ->
        assertThat(
          processInstanceQuery.processDefinitionKey(processDefinitionKey)
            .singleResult().isSuspended
        ).isTrue()
      }

    val batchActivate = THEN
      .remoteService.updateProcessInstanceSuspensionState().byProcessInstanceIds(listOf(THEN.processInstance!!.id)).activateAsync()
    assertThat(batchActivate).isNotNull
    assertThat(batchActivate.totalJobs).isEqualTo(1)

    TimeUnit.SECONDS.sleep(1)

    THEN
      .process_instance_query_succeeds { processInstanceQuery, _ ->
        assertThat(
          processInstanceQuery.processDefinitionKey(processDefinitionKey)
            .singleResult().isSuspended
        ).isFalse()
      }

  }

  @Test
  fun `should succeed with process instance query`() {
    val processDefinitionKey = processDefinitionKey()
    GIVEN
      .process_with_user_task_is_deployed(processDefinitionKey)

    WHEN
      .process_is_started_by_key(processDefinitionKey)

    val query = THEN
      .remoteService.createProcessInstanceQuery().processDefinitionKeyIn(processDefinitionKey)

    THEN
      .remoteService.updateProcessInstanceSuspensionState().byProcessInstanceQuery(query).suspend()

    THEN
      .process_instance_query_succeeds { processInstanceQuery, _ ->
        assertThat(
          processInstanceQuery.processDefinitionKey(processDefinitionKey)
            .singleResult().isSuspended
        ).isTrue()
      }

  }


  @Test
  fun `should succeed with historic process instance query`() {
    val processDefinitionKey = processDefinitionKey()
    GIVEN
      .process_with_user_task_is_deployed(processDefinitionKey)

    WHEN
      .process_is_started_by_key(processDefinitionKey)

    val query = THEN
      .historyService.createHistoricProcessInstanceQuery().processDefinitionKeyIn(processDefinitionKey)

    THEN
      .remoteService.updateProcessInstanceSuspensionState().byHistoricProcessInstanceQuery(query).suspend()

    THEN
      .process_instance_query_succeeds { processInstanceQuery, _ ->
        assertThat(
          processInstanceQuery.processDefinitionKey(processDefinitionKey)
            .singleResult().isSuspended
        ).isTrue()
      }

  }

}
