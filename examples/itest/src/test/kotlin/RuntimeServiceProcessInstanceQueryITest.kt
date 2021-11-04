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
import io.toolisticon.testing.jgiven.GIVEN
import io.toolisticon.testing.jgiven.THEN
import io.toolisticon.testing.jgiven.WHEN
import org.assertj.core.api.Assertions.assertThat
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.variable.Variables.createVariables
import org.camunda.bpm.extension.rest.itest.stages.CamundaRestClientITestBase
import org.camunda.bpm.extension.rest.itest.stages.RuntimeServiceActionStage
import org.camunda.bpm.extension.rest.itest.stages.RuntimeServiceAssertStage
import org.camunda.bpm.extension.rest.itest.stages.RuntimeServiceCategory
import org.junit.Test
import org.springframework.test.annotation.DirtiesContext

@RuntimeServiceCategory
@As("Creates process instance query")
@DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
class RuntimeServiceProcessInstanceQueryITest :
  CamundaRestClientITestBase<RuntimeService, RuntimeServiceActionStage, RuntimeServiceAssertStage>() {

  @Test
  fun `find process started by id`() {
    val processDefinitionKey = processDefinitionKey()
    val key1 = "businessKey1"
    val vars1 = createVariables().putValue("VAR1", "value1")
    val key2 = "businessKey2"
    val vars2 = createVariables().putValue("VAR1", "value2")

    GIVEN
      .no_deployment_exists()
      .and()
      .process_with_user_task_is_deployed(processDefinitionKey)

    WHEN
      .apply {
        localService.startProcessInstanceById(GIVEN.processDefinition.id, key1, vars1)
        localService.startProcessInstanceById(GIVEN.processDefinition.id, key2, vars2)
      }

    THEN
      .process_instance_query_succeeds { query, _ ->
        assertThat(
          query
            .processDefinitionKey(processDefinitionKey)
            .count()
        ).isEqualTo(2)

        assertThat(
          query
            .processInstanceBusinessKey(key1)
            .count()
        ).isEqualTo(1)

        assertThat(
          query
            .processInstanceBusinessKey(key1)
            .singleResult()
            .businessKey
        ).isEqualTo(key1)

        assertThat(
          query
            .variableValueEquals("VAR1", "value1")
            .singleResult()
            .businessKey
        ).isEqualTo(key1)

        assertThat(
          query
            .matchVariableNamesIgnoreCase()
            .variableValueEquals("var1", "value1")
            .singleResult()
            .businessKey
        ).isEqualTo(key1)

        assertThat(
          query
            .matchVariableNamesIgnoreCase()
            .variableValueEquals("var1", "value1")
            .singleResult().id
        ).isEqualTo(
          query
            .matchVariableNamesIgnoreCase()
            .variableValueEquals("var1", "value1")
            .list()[0].id
        )

      }

  }

  @Test
  fun `find process sorted by business key`() {
    val processDefinitionKey = processDefinitionKey()
    val key1 = "businessKey1"
    val vars1 = createVariables().putValue("VAR1", "value1")
    val key2 = "businessKey2"
    val vars2 = createVariables().putValue("VAR1", "value2")
    val key3 = "businessKey3"

    GIVEN
      .no_deployment_exists()
      .and()
      .process_with_user_task_is_deployed(processDefinitionKey)

    WHEN
      .apply {
        localService.startProcessInstanceById(GIVEN.processDefinition.id, key3, vars1)
        localService.startProcessInstanceById(GIVEN.processDefinition.id, key2, vars2)
        localService.startProcessInstanceById(GIVEN.processDefinition.id, key1, vars2)
        localService.startProcessInstanceById(GIVEN.processDefinition.id, key2, vars2)
      }

    THEN
      .process_instance_query_succeeds { query, _ ->
        assertThat(
          query
            .orderByBusinessKey().asc()
            .list()
            .map { it.businessKey }
        ).containsExactly(key1, key2, key2, key3)
      }
      .and()
      .process_instance_query_succeeds { query, _ ->
        assertThat(
          query
            .orderByBusinessKey().desc()
            .list()
            .map { it.businessKey }
        ).containsExactly(key3, key2, key2, key1)
      }
  }

  @Test
  fun `find process sorted by process definition key and business key`() {
    val processDefinitionKey1 = "processDefinitionKey1"
    val processDefinitionKey2 = "processDefinitionKey2"
    val key1 = "businessKey1"
    val key2 = "businessKey2"
    val key3 = "businessKey3"

    GIVEN.no_deployment_exists()

    val processDefinition1 = GIVEN
      .process_with_user_task_is_deployed(processDefinitionKey1)
      .processDefinition.id

    val processDefinition2 = GIVEN
      .process_with_user_task_is_deployed(processDefinitionKey2)
      .processDefinition.id

    WHEN
      .apply {
        localService.startProcessInstanceByKey(processDefinitionKey1, key3)
        localService.startProcessInstanceByKey(processDefinitionKey2, key3)
        localService.startProcessInstanceByKey(processDefinitionKey1, key1)
        localService.startProcessInstanceByKey(processDefinitionKey1, key2)
        localService.startProcessInstanceByKey(processDefinitionKey2, key2)
        localService.startProcessInstanceByKey(processDefinitionKey2, key1)
        localService.startProcessInstanceByKey(processDefinitionKey2, key3)
      }

    THEN
      .process_instance_query_succeeds { query, _ ->
        val result = query
          .orderByBusinessKey().asc()
          .orderByProcessDefinitionKey().desc()
          .list()
        assertThat(
          result.map { it.businessKey }
        ).containsExactly(key1, key1, key2, key2, key3, key3, key3)
        assertThat(
          result.map { it.processDefinitionId }
        ).containsExactly(processDefinition2, processDefinition1, processDefinition2, processDefinition1, processDefinition2, processDefinition2, processDefinition1)
      }
  }


}
