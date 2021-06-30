package org.camunda.bpm.extension.rest.itest

import com.tngtech.jgiven.annotation.As
import io.toolisticon.testing.jgiven.AND
import io.toolisticon.testing.jgiven.GIVEN
import io.toolisticon.testing.jgiven.THEN
import io.toolisticon.testing.jgiven.WHEN
import org.assertj.core.api.Assertions
import org.camunda.bpm.engine.TaskService
import org.camunda.bpm.engine.variable.Variables
import org.camunda.bpm.extension.rest.itest.stages.*
import org.junit.Test
import org.springframework.test.annotation.DirtiesContext

@TaskServiceCategory
@As("Creates task query")
@DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
class TaskServiceITest
  : CamundaRestClientITestBase<TaskService, TaskServiceActionStage, TaskServiceAssertStage>() {

  @Test
  fun `find process started by id`() {
    val processDefinitionKey = processDefinitionKey()
    val key1 = "businessKey1"
    val vars1 = Variables.createVariables().putValue("VAR1", "value1")
    val key2 = "businessKey2"
    val vars2 = Variables.createVariables().putValue("VAR1", "value2")

    GIVEN
      .process_with_user_task_is_deployed(processDefinitionKey)

    WHEN
      .process_is_started_by_key(processDefinitionKey, key1, null, vars1)
      .AND
      .process_is_started_by_key(processDefinitionKey, key2, null, vars2)

    THEN
      .task_query_succeeds { query, _ ->
        Assertions.assertThat(
          query
            .processDefinitionKey(processDefinitionKey)
            .count()
        ).isEqualTo(2)

        Assertions.assertThat(
          query
            .processInstanceBusinessKey(key1)
            .count()
        ).isEqualTo(1)
      }
  }
}
