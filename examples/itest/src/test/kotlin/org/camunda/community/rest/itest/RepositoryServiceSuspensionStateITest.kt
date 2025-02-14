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
import org.camunda.bpm.engine.RepositoryService
import org.camunda.community.rest.itest.stages.*
import org.junit.jupiter.api.Test
import java.util.concurrent.TimeUnit

@As("Update Suspension State")
class RepositoryServiceSuspensionStateITest : CamundaRestClientITestBase<RepositoryService, RepositoryServiceActionStage, RepositoryServiceAssertStage>() {

  @Test
  fun `should suspend and activate process definition by key`() {
    val processDefinitionKey = processDefinitionKey()

    GIVEN
      .process_is_deployed(processDefinitionKey)

    WHEN
      .process_definition_is_suspended_by_key(processDefinitionKey)

    THEN
      .process_definition_query_succeeds { processDefinitionQuery, _ ->
        assertThat(
          processDefinitionQuery
            .processDefinitionKey(processDefinitionKey)
            .singleResult()
            .isSuspended
        ).isTrue()
      }
      .process_definition_is_activated_by_key()
      .AND
      .process_definition_query_succeeds { processInstanceQuery, _ ->
        assertThat(
          processInstanceQuery
            .processDefinitionKey(processDefinitionKey)
            .singleResult()
            .isSuspended
        ).isFalse()
      }

  }

  @Test
  fun `should suspend and activate process instance by process definition key`() {
    val processDefinitionKey = processDefinitionKey()

    GIVEN
      .process_is_deployed(processDefinitionKey)

    WHEN
      .process_definition_is_suspended_by_id()

    THEN
      .process_definition_query_succeeds { processDefinitionQuery, _ ->
        assertThat(
          processDefinitionQuery
            .processDefinitionKey(processDefinitionKey)
            .singleResult()
            .isSuspended
        ).isTrue()
      }
      .process_definition_is_activated_by_id()
      .AND
      .process_definition_query_succeeds { processDefinitionQuery, _ ->
        assertThat(
          processDefinitionQuery
            .processDefinitionKey(processDefinitionKey)
            .singleResult()
            .isSuspended
        ).isFalse()
      }

  }
}
