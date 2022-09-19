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
import org.camunda.community.rest.itest.stages.CamundaRestClientITestBase
import org.camunda.community.rest.itest.stages.RuntimeServiceActionStage
import org.camunda.community.rest.itest.stages.RuntimeServiceAssertStage
import org.junit.Test
import org.springframework.test.annotation.DirtiesContext

@As("Delete process instance")
@DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
class RuntimeServiceDeleteProcessInstanceITest :
  CamundaRestClientITestBase<RuntimeService, RuntimeServiceActionStage, RuntimeServiceAssertStage>() {

  @Test
  fun `delete process instance by id`() {
    val processDefinitionKey = processDefinitionKey()

    GIVEN
      .no_deployment_exists()
      .and()
      .process_with_user_task_is_deployed(processDefinitionKey)

    WHEN
      .process_is_started_by_key(processDefinitionKey)
      .AND
      .execution_is_waiting_in_user_task()
      .AND
      .process_instance_is_deleted(GIVEN.processInstance.id)

    THEN
      .process_instance_query_succeeds { query, _ ->
        assertThat(
          query
            .processDefinitionKey(processDefinitionKey)
            .count()
        ).isEqualTo(0)

      }
  }

  @Test
  fun `delete process instances by id`() {
    val processDefinitionKey = processDefinitionKey()

    GIVEN
      .no_deployment_exists()
      .and()
      .process_with_user_task_is_deployed(processDefinitionKey)

    WHEN
      .process_is_started_by_key(processDefinitionKey)

    val processInstanceId1 = GIVEN.processInstance.id
    WHEN
      .process_is_started_by_key(processDefinitionKey)
      .AND
      .execution_is_waiting_in_user_task()
      .AND
      .process_instances_are_deleted(processInstanceId1, GIVEN.processInstance.id)

    THEN
      .process_instance_query_succeeds { query, _ ->
        assertThat(
          query
            .processDefinitionKey(processDefinitionKey)
            .count()
        ).isEqualTo(0)

      }
  }

  @Test
  fun `delete process instance if exists should not fail`() {
    val processDefinitionKey = processDefinitionKey()

    GIVEN
      .no_deployment_exists()
      .and()
      .process_with_user_task_is_deployed(processDefinitionKey)

    WHEN
      .process_is_started_by_key(processDefinitionKey)
      .AND
      .execution_is_waiting_in_user_task()
      .AND
      .process_instance_is_deleted_if_exists("wrong process instance id")

    THEN
      .process_instance_query_succeeds { query, _ ->
        assertThat(
          query
            .processDefinitionKey(processDefinitionKey)
            .count()
        ).isEqualTo(1)

      }
  }

  @Test
  fun `delete process instances if exists should not fail`() {
    val processDefinitionKey = processDefinitionKey()

    GIVEN
      .no_deployment_exists()
      .and()
      .process_with_user_task_is_deployed(processDefinitionKey)

    WHEN
      .process_is_started_by_key(processDefinitionKey)

    val processInstanceId1 = GIVEN.processInstance.id
    WHEN
      .process_is_started_by_key(processDefinitionKey)
      .AND
      .execution_is_waiting_in_user_task()
      .AND
      .process_instances_are_deleted_if_existing(processInstanceId1, "wrong process instance id")

    THEN
      .process_instance_query_succeeds { query, _ ->
        assertThat(
          query
            .processDefinitionKey(processDefinitionKey)
            .count()
        ).isEqualTo(1)

      }
  }

  @Test
  fun `delete process instance async`() {
    val processDefinitionKey = processDefinitionKey()

    GIVEN
      .no_deployment_exists()
      .and()
      .process_with_user_task_is_deployed(processDefinitionKey)

    WHEN
      .process_is_started_by_key(processDefinitionKey)
      .AND
      .process_instance_is_deleted_async(GIVEN.processInstance.id)

    THEN
      .batch_has_jobs(1)
      .AND
      .wait_for_batch()
      .AND
      .process_instance_query_succeeds { query, _ ->
        assertThat(
          query
            .processDefinitionKey(processDefinitionKey)
            .count()
        ).isEqualTo(0)

      }
  }

  @Test
  fun `delete process instances async by process definition key`() {
    val processDefinitionKey = processDefinitionKey()

    GIVEN
      .no_deployment_exists()
      .and()
      .process_with_user_task_is_deployed(processDefinitionKey)

    WHEN
      .process_is_started_by_key(processDefinitionKey)
      .AND
      .process_is_started_by_key(processDefinitionKey)
      .AND
      .execution_is_waiting_in_user_task()
      .AND
      .process_instances_are_deleted_async_by_process_definition_key(processDefinitionKey)

    THEN
      .batch_has_jobs(2)
      .AND
      .wait_for_batch()
      .AND
      .process_instance_query_succeeds { query, _ ->
        assertThat(
          query
            .processDefinitionKey(processDefinitionKey)
            .count()
        ).isEqualTo(0)
      }
  }

}
