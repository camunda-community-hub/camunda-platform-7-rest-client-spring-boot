package org.camunda.community.rest.impl.query

import org.assertj.core.api.Assertions.assertThat
import org.camunda.bpm.engine.task.DelegationState
import org.camunda.community.rest.client.api.TaskApiClient
import org.camunda.community.rest.client.model.CountResultDto
import org.camunda.community.rest.client.model.TaskDto
import org.junit.Test

import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.http.ResponseEntity
import java.util.*

class DelegatingTaskQueryTest {

  val taskApiClient = mock<TaskApiClient>()

  val query = DelegatingTaskQuery(
    taskApiClient,
    taskId = "taskId",
    taskIdIn = arrayOf("taskIdIn"),
    name = "name",
    nameNotEqual = "nameNotEqual",
    nameLike = "nameLike",
    nameNotLike = "nameNotLike",
    description = "description",
    descriptionLike = "descriptionLike",
    priority = 1,
    minPriority = 1,
    maxPriority = 10,
    assignee = "assignee",
    assigneeLike = "assigneeLike",
    assigneeIn = setOf("assigneeIn"),
    assingeeNotIn = setOf("assingeeNotIn"),
    involvedUser = "involvedUser",
    owner = "owner",
    unassigned = false,
    assigned = true,
    noDelegationState = true,
    delegationState = DelegationState.PENDING,
    candidateUser = "candidateUser",
    candidateGroup = "candidateGroup",
    candidateGroups = listOf("candidateGroups"),
    withCandidateGroups = true,
    withoutCandidateGroups = false,
    withCandidateUsers = false,
    withoutCandidateUsers = true,
    includeAssignedTasks = true,
    processInstanceId = "processInstanceId",
    processInstanceIdIn = arrayOf("processInstanceIdIn"),
    executionId = "executionId",
    activityInstanceIdIn = arrayOf("activityInstanceIdIn"),
    createTime = Date(),
    createTimeBefore = Date(),
    createTimeAfter = Date(),
    updatedAfter = Date(),
    key = "key",
    keyLike = "keyLike",
    taskDefinitionKeys = arrayOf("taskDefinitionKeys"),
    processDefinitionKey = "processDefinitionKey",
    processDefinitionKeys = arrayOf("processDefinitionKeys"),
    processDefinitionId = "processDefinitionId",
    processDefinitionName = "processDefinitionName",
    processDefinitionNameLike = "processDefinitionNameLike",
    processInstanceBusinessKey = "processInstanceBusinessKey",
    processInstanceBusinessKeys = arrayOf("processInstanceBusinessKeys"),
    processInstanceBusinessKeyLike = "processInstanceBusinessKeyLike",
    dueDate = Date(),
    dueBefore = Date(),
    dueAfter = Date(),
    followUpDate = Date(),
    followUpBefore = Date(),
    followUpNullAccepted = false,
    followUpAfter = Date(),
    excludeSubtasks = true,
    suspensionState = SuspensionState.ACTIVE,
    initializeFormKeys = true,
    parentTaskId = "parentTaskId",
    isWithoutDueDate = false,
    caseDefinitionKey = "caseDefinitionKey",
    caseDefinitionId = "caseDefinitionId",
    caseDefinitionName = "caseDefinitionName",
    caseDefinitionNameLike = "caseDefinitionNameLike",
    caseInstanceId = "caseInstanceId",
    caseInstanceBusinessKey = "caseInstanceBusinessKey",
    caseInstanceBusinessKeyLike = "caseInstanceBusinessKeyLike",
    caseExecutionId = "caseExecutionId",
    expressions = mutableMapOf(
      "processInstanceBusinessKey" to "value",
      "processInstanceBusinessKeyLike" to "value",
      "taskAssignee" to "value",
      "taskAssigneeLike" to "value",
      "taskOwner" to "value",
      "taskCandidateGroup" to "value",
      "taskCandidateUser" to "value",
      "taskInvolvedUser" to "value",
      "dueDate" to "value",
      "dueDateAfter" to "value",
      "dueDateBefore" to "value",
      "followUpDate" to "value",
      "followUpDateAfter" to "value",
      "followUpDateBefore" to "value",
      "followUpBeforeOrNotExistent" to "value",
      "taskCreatedOn" to "value",
      "taskCreatedAfter" to "value",
      "taskCreatedBefore" to "value",
      "taskUpdatedAfter" to "value",
      "taskCandidateGroupIn" to "value",
    ),
    isOrQueryActive = false
  ).apply {
    this.tenantIdIn("tenantId")
    this.variableNamesIgnoreCase = true
    this.variableValuesIgnoreCase = true
    this.variableValueEquals("var", "value")
    this.orderByTaskName().asc()
  }

  @Test
  fun listPage() {
    whenever(taskApiClient.queryTasks(eq(1), eq(10), any())).thenReturn(
      ResponseEntity.ok(listOf(TaskDto().id("taskId").name("taskName").priority(1).suspended(false)))
    )
    val result = query.listPage(1, 10)
    assertThat(result).hasSize(1)
  }

  @Test
  fun count() {
    whenever(taskApiClient.queryTasksCount(any())).thenReturn(
      ResponseEntity.ok(CountResultDto().count(5))
    )
    val result = query.count()
    assertThat(result).isEqualTo(5)
  }

}
