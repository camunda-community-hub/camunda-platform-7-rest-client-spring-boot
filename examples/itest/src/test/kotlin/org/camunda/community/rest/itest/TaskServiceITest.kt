package org.camunda.community.rest.itest

import com.tngtech.jgiven.annotation.As
import io.toolisticon.testing.jgiven.AND
import io.toolisticon.testing.jgiven.GIVEN
import io.toolisticon.testing.jgiven.THEN
import io.toolisticon.testing.jgiven.WHEN
import org.assertj.core.api.Assertions.assertThat
import org.camunda.bpm.engine.TaskService
import org.camunda.bpm.engine.task.DelegationState
import org.camunda.bpm.engine.variable.Variables.*
import org.camunda.bpm.engine.variable.type.ValueType
import org.camunda.bpm.engine.variable.value.IntegerValue
import org.camunda.bpm.engine.variable.value.StringValue
import org.camunda.community.rest.itest.stages.CamundaRestClientITestBase
import org.camunda.community.rest.itest.stages.TaskServiceActionStage
import org.camunda.community.rest.itest.stages.TaskServiceAssertStage
import org.camunda.community.rest.itest.stages.TaskServiceCategory
import org.junit.Test
import org.springframework.test.annotation.DirtiesContext
import java.time.Instant.now
import java.util.*

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
    val vars1 = createVariables().putValue("VAR1", "value1")
    val key2 = "businessKey2"
    val vars2 = createVariables().putValue("VAR1", "value2")

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
      .AND
      .task_query_succeeds { query, stage ->
        assertThat(query.taskId(stage.tasksIds[0]).count()).isEqualTo(1)
        assertThat(query.taskId(stage.tasksIds[1]).count()).isEqualTo(1)
      }
      .AND
      .task_query_succeeds { query, stage ->
        assertThat(query.taskIdIn(stage.tasksIds[0]).count()).isEqualTo(1)
        assertThat(query.taskIdIn(stage.tasksIds[0], stage.tasksIds[1]).count()).isEqualTo(2)
      }
  }

  @Test
  fun claim() {
    val taskDefinitionKey = taskDefinitionKey()
    val processDefinitionKey = processDefinitionKey()
    val key1 = "businessKey1"
    val vars1 = createVariables().putValue("VAR1", "value1")

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
  fun `delegate task`() {
    val processDefinitionKey = processDefinitionKey()
    val taskDefinitionKey = taskDefinitionKey()
    val key1 = "businessKey1"
    val vars1 = createVariables().putValue("VAR1", "value1")

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
    val vars1 = createVariables().putValue("VAR1", "value1")

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
  fun `set owner, priority, assignee, description`() {
    val taskDefinitionKey = taskDefinitionKey()
    val processDefinitionKey = processDefinitionKey()
    val key1 = "businessKey1"
    val vars1 = createVariables().putValue("VAR1", "value1")

    GIVEN
      .process_with_user_task_is_deployed(processDefinitionKey, taskDefinitionKey)
      .AND
      .process_is_started_by_key(processDefinitionKey, key1, vars1)
      .AND
      .process_waits_in_task(processDefinitionKey, taskDefinitionKey)

    WHEN
      .remoteService.apply {
        setOwner(GIVEN.task.id, "owner")
        setPriority(GIVEN.task.id, 77)
        setAssignee(GIVEN.task.id, "assignee")
      }


    THEN
      .task_query_succeeds { query, _ ->
        assertThat(
          query
            .processDefinitionKey(processDefinitionKey)
            .singleResult()
        ).apply {
          this.extracting { task -> task.assignee }.isEqualTo("assignee")
          this.extracting { task -> task.priority }.isEqualTo(77)
          this.extracting { task -> task.owner }.isEqualTo("owner")
        }
      }
  }


  @Test
  fun `manipulate variables`() {
    val taskDefinitionKey = taskDefinitionKey()
    val processDefinitionKey = processDefinitionKey()
    val key1 = "businessKey1"
    val now = Date.from(now())
    val vars1 = createVariables()
      .putValue("VAR1", "value1")
      .putValue("VAR2", "value2")
      .putValue("VAR3", "value3")


    GIVEN
      .process_with_user_task_is_deployed(processDefinitionKey, taskDefinitionKey)
      .AND
      .process_is_started_by_key(processDefinitionKey, key1, vars1)
      .AND
      .process_waits_in_task(processDefinitionKey, taskDefinitionKey)

    WHEN
      .remoteService.apply {
        removeVariable(GIVEN.task.id, "VAR1")
        removeVariable(GIVEN.task.id, "VAR2")
        setVariable(GIVEN.task.id, "VAR2", "otherValue")
        setVariables(GIVEN.task.id, createVariables().putValueTyped("VAR4", integerValue(123)).putValue("VAR5", now))
        setVariableLocal(GIVEN.task.id, "LOCAL-VAR", "localValue")
        // println(getVariables(GIVEN.task.id))
      }


    THEN
      .task_query_succeeds { query, _ ->
        assertThat(
          query
            .processDefinitionKey(processDefinitionKey)
            .processVariableValueEquals("VAR3", "value3") // still there
            .singleResult()
        ).apply {
          this.isNotNull
          this.extracting { task -> task.id }.isEqualTo(GIVEN.task.id)
        }
      }
      .AND
      .task_query_succeeds { query, _ ->
        assertThat(
          query
            .processDefinitionKey(processDefinitionKey)
            .processVariableValueNotEquals("VAR2", "value2") // removed, but a local is created
            .singleResult()
        ).apply {
          this.isNotNull
          this.extracting { task -> task.id }.isEqualTo(GIVEN.task.id)
        }
      }
      // direct variable access
      .AND
      .assertThat {
        assertThat(it.remoteService.getVariable(GIVEN.task.id, "VAR4")).isEqualTo(123)
        assertThat(it.remoteService.getVariableTyped<IntegerValue>(GIVEN.task.id, "VAR4")).isEqualTo(integerValue(123))
        assertThat(it.remoteService.getVariableLocal(GIVEN.task.id, "LOCAL-VAR")).isEqualTo("localValue")
        assertThat(it.remoteService.getVariableLocalTyped<StringValue>(GIVEN.task.id, "LOCAL-VAR")).isEqualTo(stringValue("localValue"))
      }

  }

  @Test
  fun `complete task`() {
    val taskDefinitionKey = taskDefinitionKey()
    val processDefinitionKey = processDefinitionKey()
    val key1 = "businessKey1"
    val vars1 = createVariables().putValue("VAR1", "value1")


    GIVEN
      .process_with_user_task_is_deployed(processDefinitionKey, taskDefinitionKey)
      .AND
      .process_is_started_by_key(processDefinitionKey, key1, vars1)
      .AND
      .process_waits_in_task(processDefinitionKey, taskDefinitionKey)

    WHEN
      .remoteService.complete(GIVEN.task.id)


    THEN
      .assertThat {
        assertThat(it.localService.createTaskQuery().processDefinitionKey(processDefinitionKey).count()).isEqualTo(0)
      }
  }


  @Test
  fun `add candidate user and group`() {
    val taskDefinitionKey = taskDefinitionKey()
    val processDefinitionKey = processDefinitionKey()
    val key1 = "businessKey1"
    val vars1 = createVariables().putValue("VAR1", "value1")

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
    val vars1 = createVariables().putValue("VAR1", "value1")

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

  @Test
  fun `find tasks sorted`() {
    val processDefinitionKey = processDefinitionKey()
    val taskDefinitionKey = taskDefinitionKey()
    val key1 = "businessKey1"
    val vars1 = createVariables().putValue("VAR1", "value1")
    val key2 = "businessKey2"
    val vars2 = createVariables().putValue("VAR1", "value2")

    GIVEN
      .no_deployment_exists()
      .AND
      .process_with_user_task_is_deployed(processDefinitionKey, taskDefinitionKey)

    val processInstanceId1 = WHEN
      .process_is_started_by_key(processDefinitionKey, key1, vars1)
      .processInstance.id
    val processInstanceId2 = WHEN
      .process_is_started_by_key(processDefinitionKey, key2, vars2)
      .processInstance.id

    THEN
      .task_query_succeeds { query, _ ->
        assertThat(
          query
            .orderByProcessVariable("VAR1", ValueType.STRING).desc()
            .list()
            .map { it.processInstanceId }
        ).containsExactly(processInstanceId2, processInstanceId1)
      }
      .AND
      .task_query_succeeds { query, _ ->
        assertThat(
          query
            .orderByTaskCreateTime().asc()
            .list()
            .map { it.processInstanceId }
        ).containsExactly(processInstanceId1, processInstanceId2)
      }
  }


}

