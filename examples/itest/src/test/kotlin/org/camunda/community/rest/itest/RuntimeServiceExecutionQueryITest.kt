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
import org.springframework.test.annotation.DirtiesContext

@As("Creates execution query")
@DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
class RuntimeServiceExecutionQueryITest :
  CamundaRestClientITestBase<RuntimeService, RuntimeServiceActionStage, RuntimeServiceAssertStage>() {

  @Test
  fun `find execution by process started by id`() {
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
      .execution_query_succeeds { query, _ ->
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

}
