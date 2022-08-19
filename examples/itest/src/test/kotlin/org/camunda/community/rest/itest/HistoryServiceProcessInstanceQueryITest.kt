/*-
 * #%L
 * camunda-platform-7-rest-client-spring-boot-itest
 * %%
 * Copyright (C) 2021 Camunda Services GmbH
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
import org.assertj.core.api.Assertions.assertThat
import org.camunda.bpm.engine.HistoryService
import org.camunda.community.rest.itest.stages.CamundaRestClientITestBase
import org.camunda.community.rest.itest.stages.HistoryServiceActionStage
import org.camunda.community.rest.itest.stages.HistoryServiceAssertStage
import org.junit.Test
import org.springframework.test.annotation.DirtiesContext

@As("Creates historic process instance query")
@DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
class HistoryServiceProcessInstanceQueryITest :
  CamundaRestClientITestBase<HistoryService, HistoryServiceActionStage, HistoryServiceAssertStage>() {

  @Test
  fun `should find historic process instance by process definition key`() {
    val processDefinitionKey = processDefinitionKey()
    GIVEN
      .no_deployment_exists()
      .AND
      .process_is_deployed(processDefinitionKey)
      .AND
      .process_is_started_by_key(processDefinitionKey)
      .AND
      .task_is_completed()
    THEN
      .historic_process_instance_query_succeeds { query, _ ->
        assertThat(
          query.processDefinitionKey(processDefinitionKey).count()
        ).isEqualTo(1)
        assertThat(
          query.processDefinitionKey(processDefinitionKey).singleResult()?.processDefinitionKey
        ).isEqualTo(processDefinitionKey)
      }
  }

  @Test
  fun `should find historic process instance by process definition key if process still active`() {
    val processDefinitionKey = processDefinitionKey()
    GIVEN
      .no_deployment_exists()
      .AND
      .process_is_deployed(processDefinitionKey)
      .AND
      .process_is_started_by_key(processDefinitionKey)
    THEN
      .historic_process_instance_query_succeeds { query, _ ->
        assertThat(
          query.processDefinitionKey(processDefinitionKey).count()
        ).isEqualTo(1)
        assertThat(
          query.processDefinitionKey(processDefinitionKey).singleResult()?.processDefinitionKey
        ).isEqualTo(processDefinitionKey)
      }
      .AND
      .runtimeService.deleteProcessInstance(GIVEN.processInstance.id, "itest")
  }

  @Test
  fun `should find historic process instance by process instance id`() {
    val processDefinitionKey = processDefinitionKey()
    GIVEN
      .no_deployment_exists()
      .AND
      .process_is_deployed(processDefinitionKey)
      .AND
      .process_is_started_by_key(processDefinitionKey)
      .AND
      .task_is_completed()
    THEN
      .historic_process_instance_query_succeeds { query, _ ->
        assertThat(
          query.processInstanceId(GIVEN.processInstance.id).completed().count()
        ).isEqualTo(1)
      }
      .AND
      .historic_process_instance_query_succeeds { query, _ ->
        assertThat(
          query.processInstanceId(GIVEN.processInstance.id).finished().count()
        ).isEqualTo(1)
      }
      .AND
      .historic_process_instance_query_succeeds { query, _ ->
        assertThat(
          query.processInstanceId(GIVEN.processInstance.id).unfinished().count()
        ).isEqualTo(0)
      }
      .AND
      .historic_process_instance_query_succeeds { query, _ ->
        assertThat(
          query.processInstanceId(GIVEN.processInstance.id).completed().singleResult()?.processDefinitionKey
        ).isEqualTo(processDefinitionKey)
      }
  }

  @Test
  fun `should find historic process instance by executed activity`() {
    val processDefinitionKey = processDefinitionKey()
    GIVEN
      .no_deployment_exists()
      .AND
      .process_is_deployed(processDefinitionKey)
      .AND
      .process_is_started_by_key(processDefinitionKey)
      .AND
      .task_is_completed()
    THEN
      .historic_process_instance_query_succeeds { query, _ ->
        assertThat(
          query.executedActivityIdIn("task").count()
        ).isEqualTo(1)
        assertThat(
          query.executedActivityIdIn("task").singleResult()?.processDefinitionKey
        ).isEqualTo(processDefinitionKey)
      }
  }

  @Test
  fun `should find historic process instance by active activity`() {
    val processDefinitionKey = processDefinitionKey()
    GIVEN
      .no_deployment_exists()
      .AND
      .process_is_deployed(processDefinitionKey)
      .AND
      .process_is_started_by_key(processDefinitionKey)
    THEN
      .historic_process_instance_query_succeeds { query, _ ->
        assertThat(
          query.activeActivityIdIn("task").count()
        ).isEqualTo(1)
        assertThat(
          query.activeActivityIdIn("task").singleResult()?.processDefinitionKey
        ).isEqualTo(processDefinitionKey)
      }
  }


}
