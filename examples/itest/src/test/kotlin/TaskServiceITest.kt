package org.camunda.bpm.extension.rest.itest

import com.tngtech.jgiven.annotation.AfterStage
import com.tngtech.jgiven.annotation.As
import io.toolisticon.testing.jgiven.AND
import io.toolisticon.testing.jgiven.GIVEN
import io.toolisticon.testing.jgiven.THEN
import io.toolisticon.testing.jgiven.WHEN
import org.assertj.core.api.Assertions.assertThat
import org.camunda.bpm.engine.TaskService
import org.camunda.bpm.engine.task.DelegationState
import org.camunda.bpm.engine.variable.Variables
import org.camunda.bpm.extension.rest.itest.stages.CamundaRestClientITestBase
import org.camunda.bpm.extension.rest.itest.stages.TaskServiceActionStage
import org.camunda.bpm.extension.rest.itest.stages.TaskServiceAssertStage
import org.camunda.bpm.extension.rest.itest.stages.TaskServiceCategory
import org.junit.Test
import org.springframework.test.annotation.DirtiesContext

@TaskServiceCategory
@As("Operations on tasks and task queries")
@DirtiesContext
class TaskServiceITest
  : CamundaRestClientITestBase<TaskService, TaskServiceActionStage, TaskServiceAssertStage>() {

  @Test
  fun `find tasks`() {
    val processDefinitionKey = processDefinitionKey()
    val taskDefinitionKey = taskDefinitionKey()
    val key1 = "businessKey1"
    val vars1 = Variables.createVariables().putValue("VAR1", "value1")
    val key2 = "businessKey2"
    val vars2 = Variables.createVariables().putValue("VAR1", "value2")

    GIVEN
      .process_with_user_task_is_deployed(processDefinitionKey, taskDefinitionKey)

    WHEN
      .process_is_started_by_key(processDefinitionKey, key1, vars1)
      .AND
      .process_is_started_by_key(processDefinitionKey, key2, vars2)

    THEN
      .task_query_succeeds { query, _ ->
        assertThat(
          query
            .processDefinitionKey(processDefinitionKey)
            .count()
        ).isEqualTo(2)
      }
      .AND
      .task_query_succeeds { query, _ ->
        assertThat(
          query
            .processInstanceBusinessKey(key1)
            .count()
        ).isEqualTo(1)
      }
      .AND
      .task_query_succeeds { query, _ ->
        assertThat(
          query
            .processInstanceBusinessKey(key2)
            .count()
        ).isEqualTo(1)
      }
      .AND
      .task_query_succeeds { query, _ ->
        assertThat(
          query
            .processVariableValueEquals("VAR1", "value1")
            .count()
        ).isEqualTo(1)
      }
      .AND
      .task_query_succeeds { query, _ ->
        assertThat(
          query
            .processVariableValueEquals("VAR1", "value2")
            .count()
        ).isEqualTo(1)
      }
  }

  @Test
  fun `delegate task`() {
    val processDefinitionKey = processDefinitionKey()
    val taskDefinitionKey = taskDefinitionKey()
    val key1 = "businessKey1"
    val vars1 = Variables.createVariables().putValue("VAR1", "value1")

    GIVEN
      .process_with_user_task_is_deployed(processDefinitionKey, taskDefinitionKey)
      .AND
      .process_is_started_by_key(processDefinitionKey, key1, vars1)
      .AND
      .process_waits_in_task(processDefinitionKey, taskDefinitionKey)
      .AND
      .remoteService.apply {
        this.claim(GIVEN.task.id, "firstUser")
      }

    WHEN
      .remoteService.delegateTask(GIVEN.task.id, "delegateUser")

    THEN
      .task_query_succeeds { query, _ ->
        assertThat(
          query
            .processDefinitionKey(processDefinitionKey)
            .singleResult()
        ).apply {
          this.extracting { task -> task.assignee }.isEqualTo("delegateUser")
          this.extracting { task -> task.delegationState }.isEqualTo(DelegationState.PENDING)
        }
      }
  }

  @Test
  fun `delegate task and resolve`() {
    val processDefinitionKey = processDefinitionKey()
    val taskDefinitionKey = taskDefinitionKey()
    val key1 = "businessKey1"
    val vars1 = Variables.createVariables().putValue("VAR1", "value1")

    GIVEN
      .process_with_user_task_is_deployed(processDefinitionKey, taskDefinitionKey)
      .AND
      .process_is_started_by_key(processDefinitionKey, key1, vars1)
      .AND
      .process_waits_in_task(processDefinitionKey, taskDefinitionKey)
      .AND
      .localService.apply {
        this.claim(GIVEN.task.id, "firstUser")
        delegateTask(GIVEN.task.id, "delegateUser")
      }

    WHEN
      .remoteService.resolveTask(GIVEN.task.id)

    THEN
      .task_query_succeeds { query, _ ->
        assertThat(
          query
            .processDefinitionKey(processDefinitionKey)
            .singleResult()
        ).apply {
          this.extracting { task -> task.assignee }.isEqualTo("firstUser")
          this.extracting { task -> task.delegationState }.isEqualTo(DelegationState.RESOLVED)
        }
      }
  }

  @Test
  fun claim() {
    val taskDefinitionKey = taskDefinitionKey()
    val processDefinitionKey = processDefinitionKey()
    val key1 = "businessKey1"
    val vars1 = Variables.createVariables().putValue("VAR1", "value1")

    GIVEN
      .process_with_user_task_is_deployed(processDefinitionKey, taskDefinitionKey)
      .AND
      .process_is_started_by_key(processDefinitionKey, key1, vars1)
      .AND
      .process_waits_in_task(processDefinitionKey, taskDefinitionKey)

    WHEN
      .remoteService.claim(GIVEN.task.id, "otherUser")

    THEN
      .task_query_succeeds { query, _ ->
        assertThat(
          query
            .processDefinitionKey(processDefinitionKey)
            .singleResult()
        ).apply {
          this.extracting { task -> task.assignee }.isEqualTo("otherUser")
          this.extracting { task -> task.delegationState }.isNull()
        }
      }
  }

  @Test
  fun `add candidate user and group`() {
    val taskDefinitionKey = taskDefinitionKey()
    val processDefinitionKey = processDefinitionKey()
    val key1 = "businessKey1"
    val vars1 = Variables.createVariables().putValue("VAR1", "value1")

    GIVEN
      .process_with_user_task_is_deployed(processDefinitionKey, taskDefinitionKey)
      .AND
      .process_is_started_by_key(processDefinitionKey, key1, vars1)
      .AND
      .process_waits_in_task(processDefinitionKey, taskDefinitionKey)

    WHEN
      .remoteService.apply {
        addCandidateGroup(GIVEN.task.id, "group1")
        addCandidateGroup(GIVEN.task.id, "group2")
        addCandidateUser(GIVEN.task.id, "user1")
        addCandidateUser(GIVEN.task.id, "user2")
      }

    THEN
      .task_query_succeeds { query, _ ->
        assertThat(
          query
            .processDefinitionKey(processDefinitionKey)
            .taskCandidateGroup("group1")
            .singleResult()
        ).apply {
          this.extracting { task -> task.id }.isEqualTo(GIVEN.task.id)
        }
      }
      .AND
      .task_query_succeeds { query, _ ->
        assertThat(
          query
            .processDefinitionKey(processDefinitionKey)
            .taskCandidateGroup("group2")
            .singleResult()
        ).apply {
          this.extracting { task -> task.id }.isEqualTo(GIVEN.task.id)
        }
      }
      .AND
      .task_query_succeeds { query, _ ->
        assertThat(
          query
            .processDefinitionKey(processDefinitionKey)
            .taskCandidateUser("user1")
            .singleResult()
        ).apply {
          this.extracting { task -> task.id }.isEqualTo(GIVEN.task.id)
        }
      }
      .AND
      .task_query_succeeds { query, _ ->
        assertThat(
          query
            .processDefinitionKey(processDefinitionKey)
            .taskCandidateUser("user2")
            .singleResult()
        ).apply {
          this.extracting { task -> task.id }.isEqualTo(GIVEN.task.id)
        }
      }
  }

  @Test
  fun `remove candidate user and group`() {
    val taskDefinitionKey = taskDefinitionKey()
    val processDefinitionKey = processDefinitionKey()
    val key1 = "businessKey1"
    val vars1 = Variables.createVariables().putValue("VAR1", "value1")

    GIVEN
      .process_with_user_task_is_deployed(processDefinitionKey, taskDefinitionKey)
      .AND
      .process_is_started_by_key(processDefinitionKey, key1, vars1)
      .AND
      .process_waits_in_task(processDefinitionKey, taskDefinitionKey)
      .AND
      .localService.apply {
        addCandidateGroup(GIVEN.task.id, "group1")
        addCandidateGroup(GIVEN.task.id, "group2")
        addCandidateUser(GIVEN.task.id, "user1")
        addCandidateUser(GIVEN.task.id, "user2")
      }

    WHEN
      .remoteService.apply {
        deleteCandidateGroup(GIVEN.task.id, "group1")
        deleteCandidateUser(GIVEN.task.id, "user1")
      }

    THEN
      .task_query_succeeds { query, _ ->
        assertThat(
          query
            .processDefinitionKey(processDefinitionKey)
            .taskCandidateGroup("group1")
            .count()
        ).apply {
          this.isEqualTo(0)
        }
      }
      .AND
      .task_query_succeeds { query, _ ->
        assertThat(
          query
            .processDefinitionKey(processDefinitionKey)
            .taskCandidateGroup("group2")
            .singleResult()
        ).apply {
          this.extracting { task -> task.id }.isEqualTo(GIVEN.task.id)
        }
      }
      .AND
      .task_query_succeeds { query, _ ->
        assertThat(
          query
            .processDefinitionKey(processDefinitionKey)
            .taskCandidateUser("user1")
            .count()
        ).apply {
          this.isEqualTo(0)
        }
      }
      .AND
      .task_query_succeeds { query, _ ->
        assertThat(
          query
            .processDefinitionKey(processDefinitionKey)
            .taskCandidateUser("user2")
            .singleResult()
        ).apply {
          this.extracting { task -> task.id }.isEqualTo(GIVEN.task.id)
        }
      }
  }


}

