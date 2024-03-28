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
import org.camunda.bpm.engine.ExternalTaskService
import org.camunda.bpm.engine.variable.Variables
import org.camunda.bpm.engine.variable.Variables.createVariables
import org.camunda.community.rest.itest.stages.CamundaRestClientITestBase
import org.camunda.community.rest.itest.stages.ExternalTaskServiceActionStage
import org.camunda.community.rest.itest.stages.ExternalTaskServiceAssertStage
import org.junit.jupiter.api.Test
import org.springframework.test.annotation.DirtiesContext

@As("Creates process instance query")
@DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
class ExternalTaskQueryITest :
  CamundaRestClientITestBase<ExternalTaskService, ExternalTaskServiceActionStage, ExternalTaskServiceAssertStage>() {

  @Test
  fun `find external task by process instance id`() {

    GIVEN
      .process_from_a_resource_is_deployed("test_external_task.bpmn")
      .AND
      .process_is_started_by_key(
        "test_external_task", "my-business-key2", "caseInstanceId2",
        createVariables()
          .putValue("VAR1", "VAL1")
          .putValueTyped("VAR4", Variables.objectValue("My object value").create())
      )
      .AND
      .process_is_started_by_key(
        "test_external_task", "my-business-key3", "caseInstanceId2",
        createVariables()
          .putValue("VAR1", "VAL1")
          .putValueTyped("VAR4", Variables.objectValue("My object value").create())
      )


    WHEN
      .process_waits_in_external_task("topic")

    THEN
      .external_task_query_succeeds { query, _ ->
        assertThat(
          query
            .processInstanceId(THEN.processInstance.id)
            .count()
        ).isEqualTo(1)

       }

  }

  @Test
  fun `find external task by topic name`() {

    GIVEN
      .process_from_a_resource_is_deployed("test_external_task.bpmn")
      .AND
      .process_is_started_by_key(
        "test_external_task", "my-business-key2", "caseInstanceId2",
        createVariables()
          .putValue("VAR1", "VAL1")
          .putValueTyped("VAR4", Variables.objectValue("My object value").create())
      )
      .AND
      .process_is_started_by_key(
        "test_external_task", "my-business-key3", "caseInstanceId2",
        createVariables()
          .putValue("VAR1", "VAL1")
          .putValueTyped("VAR4", Variables.objectValue("My object value").create())
      )


    WHEN
      .process_waits_in_external_task("topic")

    THEN
      .external_task_query_succeeds { query, _ ->

        assertThat(
          query
            .topicName("topic")
            .count()
        ).isEqualTo(2)

        assertThat(
          query
            .active()
            .count()
        ).isEqualTo(2)

        assertThat(
          query
            .notLocked()
            .count()
        ).isEqualTo(2)

        assertThat(
          query
            .withRetriesLeft()
            .list()
        ).hasSize(2)

        assertThat(
          query
            .withRetriesLeft()
            .list()
            .map { it.businessKey }
        ).contains("my-business-key2", "my-business-key3")

      }

  }

  @Test
  fun `find external task sort by id`() {

    GIVEN
      .process_from_a_resource_is_deployed("test_external_task.bpmn")
      .AND
      .process_is_started_by_key(
        "test_external_task", "my-business-key2", "caseInstanceId2",
        createVariables()
          .putValue("VAR1", "VAL1")
          .putValueTyped("VAR4", Variables.objectValue("My object value").create())
      )
      .AND
      .process_is_started_by_key(
        "test_external_task", "my-business-key3", "caseInstanceId2",
        createVariables()
          .putValue("VAR1", "VAL1")
          .putValueTyped("VAR4", Variables.objectValue("My object value").create())
      )


    WHEN
      .process_waits_in_external_task("topic")

    THEN
      .external_task_query_succeeds { query, _ ->

        assertThat(
          query
            .topicName("topic")
            .orderById()
            .desc()
            .count()
        ).isEqualTo(2)

      }

  }

}
