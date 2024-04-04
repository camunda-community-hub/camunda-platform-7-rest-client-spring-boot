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
    taskApiClient
  ).apply {
    this.taskId("taskId")
    this.taskIdIn("taskIdIn")
    this.taskName("name")
    this.taskNameNotEqual("nameNotEqual")
    this.taskNameLike("nameLike")
    this.taskNameNotLike("nameNotLike")
    this.taskDescription("description")
    this.taskDescriptionLike("descriptionLike")
    this.taskPriority(1)
    this.taskMinPriority(1)
    this.taskMaxPriority(10)
    this.taskAssignee("assignee")
    this.taskAssigneeLike("assigneeLike")
    this.taskAssigneeIn("assigneeIn")
    this.taskAssigneeNotIn("assingeeNotIn")
    this.taskInvolvedUser("involvedUser")
    this.taskOwner("owner")
    this.taskUnassigned()
    this.taskAssigned()
    this.taskDelegationState(DelegationState.PENDING)
    //this.taskCandidateUser("candidateUser")
    this.taskCandidateGroup("candidateGroup")
    this.taskCandidateGroupIn(listOf("candidateGroups"))
    this.withCandidateGroups()
    this.withoutCandidateGroups()
    this.withCandidateUsers()
    this.withoutCandidateUsers()
    this.includeAssignedTasks()
    this.processInstanceId("processInstanceId")
    this.processInstanceIdIn("processInstanceIdIn")
    this.executionId("executionId")
    this.activityInstanceIdIn("activityInstanceIdIn")
    this.taskCreatedOn(Date())
    this.taskCreatedAfter(Date())
    this.taskCreatedBefore(Date())
    this.taskUpdatedAfter(Date())
    this.taskDefinitionKey("key")
    this.taskDefinitionKeyLike("keyLike")
    this.taskDefinitionKeyIn("taskDefinitionKeys")
    this.processDefinitionKey("processDefinitionKey")
    this.processDefinitionKeyIn("processDefinitionKeys")
    this.processDefinitionId("processDefinitionId")
    this.processDefinitionName("processDefinitionName")
    this.processDefinitionNameLike("processDefinitionNameLike")
    this.processInstanceBusinessKey("processInstanceBusinessKey")
    this.processInstanceBusinessKeyIn("processInstanceBusinessKeys")
    this.processInstanceBusinessKeyLike("processInstanceBusinessKeyLike")
    this.dueDate(Date())
    this.dueBefore(Date())
    this.dueAfter(Date())
    this.followUpDate(Date())
    this.followUpBefore(Date())
    this.followUpAfter(Date())
    this.excludeSubtasks()
    this.suspended()
    this.initializeFormKeys()
    this.taskParentTaskId("parentTaskId")
    //this.withoutDueDate()
    this.caseDefinitionKey("caseDefinitionKey")
    this.caseDefinitionId("caseDefinitionId")
    this.caseDefinitionName("caseDefinitionName")
    this.caseDefinitionNameLike("caseDefinitionNameLike")
    this.caseInstanceId("caseInstanceId")
    this.caseInstanceBusinessKey("caseInstanceBusinessKey")
    this.caseInstanceBusinessKeyLike("caseInstanceBusinessKeyLike")
    this.caseExecutionId("caseExecutionId")
    this.processInstanceBusinessKeyExpression("processInstanceBusinessKeyExpression")
    this.processInstanceBusinessKeyLikeExpression("processInstanceBusinessKeyLikeExpression")
    this.taskAssigneeExpression("taskAssigneeExpression")
    this.taskAssigneeLikeExpression("taskAssigneeExpression")
    this.taskOwnerExpression("taskOwnerExpression")
    this.taskCandidateGroupExpression("taskCandidateGroupExpression")
    this.taskCandidateGroupInExpression("taskCandidateGroupInExpression")
    //this.taskCandidateUserExpression("taskCandidateUserExpression")
    this.taskInvolvedUserExpression("taskInvolvedUserExpression")
    this.dueDateExpression("dueDateExpression")
    this.dueAfterExpression("dueAfterExpression")
    this.dueBeforeExpression("dueBeforeExpression")
    this.followUpDateExpression("followUpDateExpression")
    this.followUpAfterExpression("followUpAfterExpression")
    this.followUpBeforeExpression("followUpBeforeExpression")
    this.followUpBeforeOrNotExistentExpression("followUpBeforeOrNotExistentExpression")
    this.taskCreatedOnExpression("taskCreatedOnExpression")
    this.taskCreatedAfterExpression("taskCreatedAfterExpression")
    this.taskCreatedBeforeExpression("taskCreatedBeforeExpression")
    this.taskUpdatedAfterExpression("taskUpdatedAfterExpression")
    this.tenantIdIn("tenantId")
    this.matchVariableNamesIgnoreCase()
    this.matchVariableValuesIgnoreCase()
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
