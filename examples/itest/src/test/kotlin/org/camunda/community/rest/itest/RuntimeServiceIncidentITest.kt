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

@As("Incident")
class RuntimeServiceIncidentITest : CamundaRestClientITestBase<RuntimeService, RuntimeServiceActionStage, RuntimeServiceAssertStage>() {

  @Test
  fun `should create incident on running execution`() {
    val processDefinitionKey = processDefinitionKey()
    val businessKey = "$processDefinitionKey.businessKey1"
    GIVEN
      .process_with_user_task_is_deployed(processDefinitionKey)
      .AND
      .process_is_started_by_key(processDefinitionKey, businessKey)

    WHEN
      .incident_is_created("incidentType", "configuration", "message")

    THEN
      .process_instance_exists(processDefinitionKey) { instance, stage ->
        assertThat(instance.businessKey).isEqualTo(businessKey)
        val incident = stage.localService.createIncidentQuery()
          .processInstanceId(instance.id)
          .singleResult()
        assertThat(incident).isNotNull
        assertThat(incident.incidentType).isEqualTo("incidentType")
        assertThat(incident.configuration).isEqualTo("configuration")
        assertThat(incident.incidentMessage).isEqualTo("message")
        assertThat(incident.incidentTimestamp).isNotNull()
      }
  }

  @Test
  fun `should resolve incident on running execution`() {
    val processDefinitionKey = processDefinitionKey()
    val businessKey = "$processDefinitionKey.businessKey1"

    GIVEN
      .process_with_user_task_is_deployed(processDefinitionKey)
      .AND
      .process_is_started_by_key(processDefinitionKey, businessKey)
      .AND
      .incident_is_created_locally("incidentType", "configuration")

    WHEN
      .incident_is_resolved()

    THEN
      .process_instance_exists(processDefinitionKey) { instance, stage ->
        assertThat(instance.businessKey).isEqualTo(businessKey)
        assertThat(
          stage.remoteService.createIncidentQuery()
            .processInstanceId(instance.id)
            .count()
        ).isEqualTo(0)
      }
  }

  @Test
  fun `should find incidents on running execution`() {
    val processDefinitionKey = processDefinitionKey()
    val businessKey = "$processDefinitionKey.businessKey1"
    GIVEN
      .process_with_user_task_is_deployed(processDefinitionKey)
      .AND
      .process_is_started_by_key(processDefinitionKey, businessKey)

    WHEN
      .incident_is_created_locally("incidentType", "configuration")
      .AND
      .incident_is_created_locally("incidentType2", "configuration2", "with_message")

    THEN
      .incident_query_succeeds { incidentQuery, _ ->
        assertThat(
          incidentQuery
            .processDefinitionKeyIn(processDefinitionKey)
            .count()
        ).isEqualTo(2)
      }
  }

  @Test
  fun `should sort incidents in query`() {
    val processDefinitionKey = processDefinitionKey()
    val businessKey = "$processDefinitionKey.businessKey1"
    GIVEN
      .process_with_user_task_is_deployed(processDefinitionKey)
      .AND
      .process_is_started_by_key(processDefinitionKey, businessKey)

    WHEN
      .incident_is_created_locally("incidentType", "ZZZ")
      .AND
      .incident_is_created_locally("incidentType2", "AAA", "with_message")

    THEN
      .incident_query_succeeds { incidentQuery, _ ->
        assertThat(
          incidentQuery
            .processDefinitionKeyIn(processDefinitionKey)
            .orderByConfiguration().asc()
            .list()
            .map { it.incidentType }
        ).containsExactly("incidentType2", "incidentType")
      }
  }

  @Test
  fun `should set and clear annotation on incident`() {
    val processDefinitionKey = processDefinitionKey()
    val businessKey = "$processDefinitionKey.businessKey1"
    GIVEN
      .process_with_user_task_is_deployed(processDefinitionKey)
      .AND
      .process_is_started_by_key(processDefinitionKey, businessKey)
      .AND
      .incident_is_created_locally("incidentType", "configuration", "message")

    WHEN
      .annotation_is_set_on_incident("annotation")

    THEN
      .incident_query_succeeds { incidentQuery, _ ->
        assertThat(
          incidentQuery
            .processDefinitionKeyIn(processDefinitionKey)
            .singleResult().annotation
        ).isEqualTo("annotation")
      }

    WHEN
      .annotation_is_cleared_on_incident()

    THEN
      .incident_query_succeeds { incidentQuery, _ ->
        assertThat(
          incidentQuery
            .processDefinitionKeyIn(processDefinitionKey)
            .singleResult().annotation
        ).isNull()
      }

  }

}
