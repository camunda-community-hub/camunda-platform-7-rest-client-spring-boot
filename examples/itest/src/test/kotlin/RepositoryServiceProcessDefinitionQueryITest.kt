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
import org.assertj.core.api.Assertions.assertThat
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.extension.rest.itest.stages.CamundaRestClientITestBase
import org.camunda.bpm.extension.rest.itest.stages.RepositoryServiceActionStage
import org.camunda.bpm.extension.rest.itest.stages.RepositoryServiceAssertStage
import org.camunda.bpm.extension.rest.itest.stages.RepositoryServiceCategory
import org.junit.Test
import org.springframework.test.annotation.DirtiesContext

@RepositoryServiceCategory
@As("Creates process definition query")
@DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
class RepositoryServiceProcessDefinitionQueryITest :
  CamundaRestClientITestBase<RepositoryService, RepositoryServiceActionStage, RepositoryServiceAssertStage>() {

  companion object {
    const val OFFSET_AUTO_DEPLOYED_DEFINITION = 2L
  }

  @Test
  fun `should find no processes`() {
    THEN
      .process_query_succeeds { query, _ ->
        assertThat(
          query
            .count()
        ).isEqualTo(OFFSET_AUTO_DEPLOYED_DEFINITION)
      }
  }

  @Test
  fun `should find deployed processes by process definition key`() {
    val processDefinitionKey = processDefinitionKey()
    val another = processDefinitionKey()

    GIVEN
      .process_is_deployed(processDefinitionKey)
      .AND
      .process_is_deployed(processDefinitionKey)
      .AND
      .process_is_deployed(another)

    THEN
      .process_query_succeeds { query, _ ->
        assertThat(
          query
            .processDefinitionKey(processDefinitionKey)
            .count()
        ).isEqualTo(OFFSET_AUTO_DEPLOYED_DEFINITION + 3)
      }
  }
}
