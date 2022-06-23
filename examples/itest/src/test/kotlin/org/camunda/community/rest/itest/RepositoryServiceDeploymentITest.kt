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
import org.camunda.bpm.engine.RepositoryService
import org.camunda.community.rest.itest.stages.CamundaRestClientITestBase
import org.camunda.community.rest.itest.stages.RepositoryServiceActionStage
import org.camunda.community.rest.itest.stages.RepositoryServiceAssertStage
import org.junit.Test
import org.springframework.test.annotation.DirtiesContext

@As("Creates process definition query")
@DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
class RepositoryServiceDeploymentITest :
  CamundaRestClientITestBase<RepositoryService, RepositoryServiceActionStage, RepositoryServiceAssertStage>() {

  @Test
  fun `should deploy processes successfully`() {
    GIVEN
      .no_deployment_exists()
    WHEN
      .process_definitions_are_deployed("testdeployment", "test")
    THEN
      .process_definition_query_succeeds { query, _ ->
        assertThat(
          query
            .count()
        ).isEqualTo(2)
        assertThat(
          query.list().map { it.key }
        ).containsAll(listOf("test", "process_messaging"))
      }
  }


  @Test
  fun `should find deployments with query`() {
    GIVEN
      .no_deployment_exists()
    WHEN
      .process_is_deployed("test", deploymentName = "deployment1")
      .process_is_deployed("test2", deploymentName = "deployment2")
    THEN
      .deployment_query_succeeds { query, _ ->
        assertThat(
          query
            .count()
        ).isEqualTo(2)
        assertThat(
          query.list().map { it.name }
        ).containsAll(listOf("deployment1", "deployment2"))
      }
  }

  @Test
  fun `should sort deployments by name`() {
    GIVEN
      .no_deployment_exists()
    WHEN
      .process_is_deployed("test", deploymentName = "BBB")
      .process_is_deployed("test2", deploymentName = "AAA")
      .process_is_deployed("test3", deploymentName = "CCC")
    THEN
      .deployment_query_succeeds { query, _ ->
        assertThat(
          query
            .orderByDeploymentName().asc()
            .list()
            .map { it.name }
        ).containsExactly("AAA", "BBB", "CCC")
      }
  }

  @Test
  fun `should suspend process definition`() {
    GIVEN
      .no_deployment_exists()
      .and()
      .process_is_deployed("test")
    WHEN
      .process_definition_is_suspended("test")
    THEN
      .process_definition_query_succeeds { query, _ ->
        assertThat(
          query
            .count()
        ).isEqualTo(1)
        assertThat(
          query.singleResult().isSuspended
        ).isTrue()
      }
  }

}
