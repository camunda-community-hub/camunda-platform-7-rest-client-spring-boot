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
import org.assertj.core.api.Assertions.assertThat
import org.camunda.bpm.engine.RepositoryService
import org.junit.Test

@RepositoryServiceCategory
@As("Creates process definition query")
class RepositoryServiceProcessDefinitionQueryITest : CamundaRestClientITestBase<RepositoryService, RepositoryServiceActionStage, RepositoryServiceAssertStage>() {

  @Test
  fun `should find deployed processes by process definition key`() {
    val processDefinitionKey = processDefinitionKey()
    val another = processDefinitionKey()

    given()
      .process_is_deployed(processDefinitionKey)
      .and()
      .process_is_deployed(processDefinitionKey)
      .and()
      .process_is_deployed(another)

    then()
      .process_query_succeds { query, _ ->
        assertThat(
          query
            .processDefinitionKey(processDefinitionKey)
            .count()
        ).isEqualTo(2)
      }
  }

  @Test
  fun `should find latest deployed process by process definition key`() {
    val processDefinitionKey = processDefinitionKey()
    val another = processDefinitionKey()

    given()
      .process_is_deployed(processDefinitionKey)
      .and()
      .process_is_deployed(processDefinitionKey)
      .and()
      .process_is_deployed(another)

    then()
      .process_query_succeds { query, _ ->
        assertThat(
          query
            .processDefinitionKey(processDefinitionKey)
            .latestVersion()
            .count()
        ).isEqualTo(1)
      }
  }
}
