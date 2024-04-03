/*-
 * #%L
 * camunda-platform-7-rest-client-spring-boot
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
package org.camunda.community.rest.impl.query

import mu.KLogging
import org.camunda.bpm.engine.ProcessEngineException
import org.camunda.bpm.engine.task.DelegationState
import org.camunda.bpm.engine.task.Task
import org.camunda.bpm.engine.task.TaskQuery
import org.camunda.bpm.engine.variable.type.ValueType
import org.camunda.community.rest.adapter.TaskAdapter
import org.camunda.community.rest.adapter.TaskBean
import org.camunda.community.rest.client.api.TaskApiClient
import org.camunda.community.rest.client.model.TaskQueryDto
import org.camunda.community.rest.impl.toOffsetDateTime
import org.camunda.community.rest.impl.toTaskSorting
import org.camunda.community.rest.variables.toDto
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

/**
 * Implementation of the task query.
 */
class DelegatingTaskQuery(
  private val taskApiClient: TaskApiClient,
  var taskId: String? = null,
  var taskIdIn: Array<out String>? = null,
  var name: String? = null,
  var nameNotEqual: String? = null,
  var nameLike: String? = null,
  var nameNotLike: String? = null,
  var description: String? = null,
  var descriptionLike: String? = null,
  var priority: Int? = null,
  var minPriority: Int? = null,
  var maxPriority: Int? = null,
  var assignee: String? = null,
  var assigneeLike: String? = null,
  var assigneeIn: Set<String>? = null,
  var assingeeNotIn: Set<String>? = null,
  var involvedUser: String? = null,
  var owner: String? = null,
  var unassigned: Boolean? = null,
  var assigned: Boolean? = null,
  var noDelegationState: Boolean = false,
  var delegationState: DelegationState? = null,
  var candidateUser: String? = null,
  var candidateGroup: String? = null,
  var candidateGroups: List<String>? = null,
  var withCandidateGroups: Boolean? = null,
  var withoutCandidateGroups: Boolean? = null,
  var withCandidateUsers: Boolean? = null,
  var withoutCandidateUsers: Boolean? = null,
  var includeAssignedTasks: Boolean? = null,
  var processInstanceId: String? = null,
  var processInstanceIdIn: Array<out String>? = null,
  var executionId: String? = null,
  var activityInstanceIdIn: Array<out String>? = null,
  var createTime: Date? = null,
  var createTimeBefore: Date? = null,
  var createTimeAfter: Date? = null,
  var updatedAfter: Date? = null,
  var key: String? = null,
  var keyLike: String? = null,
  var taskDefinitionKeys: Array<out String>? = null,
  var processDefinitionKey: String? = null,
  var processDefinitionKeys: Array<out String>? = null,
  var processDefinitionId: String? = null,
  var processDefinitionName: String? = null,
  var processDefinitionNameLike: String? = null,
  var processInstanceBusinessKey: String? = null,
  var processInstanceBusinessKeys: Array<out String>? = null,
  var processInstanceBusinessKeyLike: String? = null,
  var dueDate: Date? = null,
  var dueBefore: Date? = null,
  var dueAfter: Date? = null,
  var followUpDate: Date? = null,
  var followUpBefore: Date? = null,
  var followUpNullAccepted: Boolean = false,
  var followUpAfter: Date? = null,
  var excludeSubtasks: Boolean = false,
  var suspensionState: SuspensionState? = null,
  var initializeFormKeys: Boolean = false,
  var parentTaskId: String? = null,
  var isWithoutDueDate: Boolean = false,
  var caseDefinitionKey: String? = null,
  var caseDefinitionId: String? = null,
  var caseDefinitionName: String? = null,
  var caseDefinitionNameLike: String? = null,
  var caseInstanceId: String? = null,
  var caseInstanceBusinessKey: String? = null,
  var caseInstanceBusinessKeyLike: String? = null,
  var caseExecutionId: String? = null,
  val expressions: MutableMap<String, String> = mutableMapOf(),
  var isOrQueryActive: Boolean = false
) : BaseVariableQuery<TaskQuery, Task>(), TaskQuery {

  companion object : KLogging()

  override fun taskId(taskId: String?) = this.apply { this.taskId = requireNotNull(taskId) }

  override fun taskIdIn(vararg taskIdIn: String) = this.apply { this.taskIdIn = taskIdIn }

  override fun taskName(taskName: String?) = this.apply { this.name = requireNotNull(taskName) }

  override fun taskNameNotEqual(taskNameNotEqual: String?) = this.apply { this.nameNotEqual = requireNotNull(taskNameNotEqual) }

  override fun taskNameLike(taskNameLike: String?) = this.apply { this.nameLike = requireNotNull(taskNameLike) }

  override fun taskNameNotLike(taskNameNotLike: String?) = this.apply { this.nameNotLike = requireNotNull(taskNameNotLike) }

  override fun taskDescription(taskDescription: String?) = this.apply { this.description = requireNotNull(taskDescription) }

  override fun taskDescriptionLike(taskDescriptionLike: String?) = this.apply { this.descriptionLike = requireNotNull(taskDescriptionLike) }

  override fun taskPriority(taskPriority: Int?) = this.apply { this.priority = requireNotNull(taskPriority) }

  override fun taskMinPriority(taskMinPriority: Int?) = this.apply { this.minPriority = requireNotNull(taskMinPriority) }

  override fun taskMaxPriority(taskMaxPriority: Int?) = this.apply { this.maxPriority = requireNotNull(taskMaxPriority) }

  override fun taskAssignee(taskAssignee: String?) = this.apply { this.assignee = requireNotNull(taskAssignee) }

  override fun taskAssigneeExpression(taskAssigneeExpression: String?) = this.apply {
    this.expressions["taskAssignee"] = requireNotNull(taskAssigneeExpression)
  }

  override fun taskAssigneeLike(taskAssigneeLike: String?) = this.apply {
    this.assigneeLike = requireNotNull(taskAssigneeLike)
    this.expressions.remove("taskAssigneeLike")
  }

  override fun taskAssigneeLikeExpression(taskAssigneeLikeExpression: String?) = this.apply {
    this.expressions["taskAssigneeLike"] = requireNotNull(taskAssigneeLikeExpression)
  }

  override fun taskAssigneeIn(vararg taskAssigneeIn: String) = this.apply {
    this.assigneeIn = taskAssigneeIn.toSet()
    this.expressions.remove("taskAssigneeIn")
  }

  override fun taskAssigneeNotIn(vararg taskAssigneeNotIn: String) = this.apply {
    this.assingeeNotIn = taskAssigneeNotIn.toSet()
    this.expressions.remove("taskAssigneeNotIn")
  }

  override fun taskOwner(taskOwner: String?) = this.apply {
    this.owner = requireNotNull(taskOwner)
    this.expressions.remove("taskOwner")
  }

  override fun taskOwnerExpression(taskOwnerExpression: String?) = this.apply {
    this.expressions["taskOwner"] = requireNotNull(taskOwnerExpression)
  }

  override fun taskUnassigned() = this.apply { this.unassigned = true }

  @Deprecated("Deprecated in Java")
  override fun taskUnnassigned() = this.taskUnassigned()

  override fun taskAssigned() = this.apply { this.assigned = true }

  override fun taskDelegationState(taskDelegationState: DelegationState?) = this.apply {
    if (taskDelegationState == null)
      this.noDelegationState = true
    else
      this.delegationState = taskDelegationState
  }

  override fun taskCandidateUser(candidateUser: String?) = this.apply {
    if (candidateGroup != null || expressions.containsKey("taskCandidateGroup")) {
      throw ProcessEngineException("Invalid query usage: cannot set both candidateUser and candidateGroup")
    }
    if (candidateGroups != null || expressions.containsKey("taskCandidateGroupIn")) {
      throw ProcessEngineException("Invalid query usage: cannot set both candidateUser and candidateGroupIn")
    }
    this.candidateUser = requireNotNull(candidateUser)
    this.expressions.remove("taskCandidateUser")
  }

  override fun taskCandidateUserExpression(taskCandidateUserExpression: String?) = this.apply {
    if (candidateGroup != null || expressions.containsKey("taskCandidateGroup")) {
      throw ProcessEngineException("Invalid query usage: cannot set both candidateUser and candidateGroup")
    }
    if (candidateGroups != null || expressions.containsKey("taskCandidateGroupIn")) {
      throw ProcessEngineException("Invalid query usage: cannot set both candidateUser and candidateGroupIn")
    }
    this.expressions["taskCandidateUser"] = requireNotNull(taskCandidateUserExpression)
  }

  override fun taskInvolvedUser(involvedUser: String?) = this.apply {
    this.involvedUser = requireNotNull(involvedUser)
    this.expressions.remove("taskInvolvedUser")
  }

  override fun taskInvolvedUserExpression(taskInvolvedUserExpression: String?) = this.apply {
    this.expressions["taskInvolvedUser"] = requireNotNull(taskInvolvedUserExpression)
  }

  override fun withCandidateGroups() = this.apply { this.withCandidateGroups = true }

  override fun withoutCandidateGroups() = this.apply { this.withoutCandidateGroups = true }

  override fun withCandidateUsers() = this.apply { this.withCandidateUsers = true }

  override fun withoutCandidateUsers() = this.apply { this.withoutCandidateUsers = true }

  override fun taskCandidateGroup(taskCandidateGroup: String?) = this.apply {
    if (candidateUser != null || expressions.containsKey("taskCandidateUser")) {
      throw ProcessEngineException("Invalid query usage: cannot set both candidateGroup and candidateUser")
    }
    this.candidateGroup = requireNotNull(taskCandidateGroup)
    this.expressions.remove("taskCandidateGroup")
  }

  override fun taskCandidateGroupExpression(taskCandidateGroupExpression: String?) = this.apply {
    if (candidateUser != null || expressions.containsKey("taskCandidateUser")) {
      throw ProcessEngineException("Invalid query usage: cannot set both candidateGroup and candidateUser")
    }
    this.expressions["taskCandidateGroup"] = requireNotNull(taskCandidateGroupExpression)
  }

  override fun taskCandidateGroupIn(taskCandidateGroupIn: List<String>) = this.apply {
    if (candidateUser != null || expressions.containsKey("taskCandidateUser")) {
      throw ProcessEngineException("Invalid query usage: cannot set both candidateGroupIn and candidateUser")
    }
    this.candidateGroups = taskCandidateGroupIn
    this.expressions.remove("taskCandidateGroupIn")
  }

  override fun taskCandidateGroupInExpression(taskCandidateGroupInExpression: String?) = this.apply {
    if (candidateUser != null || expressions.containsKey("taskCandidateUser")) {
      throw ProcessEngineException("Invalid query usage: cannot set both candidateGroupIn and candidateUser")
    }
    this.expressions["taskCandidateGroupIn"] = requireNotNull(taskCandidateGroupInExpression)
  }

  override fun includeAssignedTasks() = this.apply {
    if (candidateUser == null && candidateGroup == null && candidateGroups == null && withCandidateGroups != true && withoutCandidateGroups != true
      && withCandidateUsers != true && withoutCandidateUsers != true
      && !expressions.containsKey("taskCandidateUser") && !expressions.containsKey("taskCandidateGroup")
      && !expressions.containsKey("taskCandidateGroupIn")
    ) {
      throw ProcessEngineException("Invalid query usage: candidateUser, candidateGroup, candidateGroupIn, withCandidateGroups, withoutCandidateGroups, withCandidateUsers, withoutCandidateUsers has to be called before 'includeAssignedTasks'.")
    }
    this.includeAssignedTasks = true
  }

  override fun processInstanceId(processInstanceId: String?) = this.apply { this.processInstanceId = requireNotNull(processInstanceId) }

  override fun processInstanceIdIn(vararg processInstanceIdIn: String) = this.apply { this.processInstanceIdIn = processInstanceIdIn }

  override fun processInstanceBusinessKey(processInstanceBusinessKey: String?) = this.apply {
    this.processInstanceBusinessKey = requireNotNull(processInstanceBusinessKey)
    this.expressions.remove("processInstanceBusinessKey")
  }

  override fun processInstanceBusinessKeyExpression(processInstanceBusinessKeyExpression: String?) = this.apply {
    this.expressions["processInstanceBusinessKey"] = requireNotNull(processInstanceBusinessKeyExpression)
  }

  override fun processInstanceBusinessKeyIn(vararg processInstanceBusinessKeyIn: String) = this.apply { this.processInstanceBusinessKeys = processInstanceBusinessKeyIn }

  override fun processInstanceBusinessKeyLike(processInstanceBusinessKeyLike: String?) = this.apply {
    this.processInstanceBusinessKeyLike = requireNotNull(processInstanceBusinessKeyLike)
    this.expressions.remove("processInstanceBusinessKeyLike")
  }

  override fun processInstanceBusinessKeyLikeExpression(processInstanceBusinessKeyLikeExpression: String?) = this.apply {
    this.expressions["processInstanceBusinessKeyLike"] = requireNotNull(processInstanceBusinessKeyLikeExpression)
  }
  override fun executionId(executionId: String?) = this.apply { this.executionId = requireNotNull(executionId) }

  override fun activityInstanceIdIn(vararg activityInstanceIdIn: String) = this.apply { this.activityInstanceIdIn = activityInstanceIdIn }

  override fun taskCreatedOn(taskCreatedOn: Date?) = this.apply {
    this.createTime = requireNotNull(taskCreatedOn)
    this.expressions.remove("taskCreatedOn")
  }

  override fun taskCreatedOnExpression(taskCreatedOnExpression: String?) = this.apply {
    this.expressions["taskCreatedOn"] = requireNotNull(taskCreatedOnExpression)
  }

  override fun taskCreatedBefore(taskCreatedBefore: Date?) = this.apply {
    this.createTimeBefore = requireNotNull(taskCreatedBefore)
    this.expressions.remove("taskCreatedBefore")
  }

  override fun taskCreatedBeforeExpression(taskCreatedBeforeExpression: String?) = this.apply {
    this.expressions["taskCreatedBefore"] = requireNotNull(taskCreatedBeforeExpression)
  }
  override fun taskCreatedAfter(taskCreatedAfter: Date?) = this.apply {
    this.createTimeAfter = requireNotNull(taskCreatedAfter)
    this.expressions.remove("taskCreatedAfter")
  }

  override fun taskCreatedAfterExpression(taskCreatedAfterExpression: String?) = this.apply {
    this.expressions["taskCreatedAfter"] = requireNotNull(taskCreatedAfterExpression)
  }

  override fun taskUpdatedAfter(taskUpdatedAfter: Date?) = this.apply {
    this.updatedAfter = requireNotNull(taskUpdatedAfter)
    this.expressions.remove("taskUpdatedAfter")
  }

  override fun taskUpdatedAfterExpression(taskUpdatedAfterExpression: String?) = this.apply {
    this.expressions["taskUpdatedAfter"] = requireNotNull(taskUpdatedAfterExpression)
  }

  override fun excludeSubtasks() = this.apply { this.excludeSubtasks = true }

  override fun taskDefinitionKey(taskDefinitionKey: String?) = this.apply { this.key = requireNotNull(taskDefinitionKey) }

  override fun taskDefinitionKeyLike(taskDefinitionKeyLike: String?) = this.apply { this.keyLike = requireNotNull(taskDefinitionKeyLike) }

  override fun taskDefinitionKeyIn(vararg taskDefinitionKeyIn: String) = this.apply { this.taskDefinitionKeys = taskDefinitionKeyIn }

  override fun taskParentTaskId(taskParentTaskId: String?) = this.apply { this.parentTaskId = requireNotNull(taskParentTaskId) }

  override fun caseInstanceId(caseInstanceId: String?) = this.apply { this.caseInstanceId = requireNotNull(caseInstanceId) }

  override fun caseInstanceBusinessKey(caseInstanceBusinessKey: String?) = this.apply { this.caseInstanceBusinessKey = requireNotNull(caseInstanceBusinessKey) }

  override fun caseInstanceBusinessKeyLike(caseInstanceBusinessKeyLike: String?) = this.apply { this.caseInstanceBusinessKeyLike = requireNotNull(caseInstanceBusinessKeyLike) }

  override fun caseExecutionId(caseExecutionId: String?) = this.apply { this.caseExecutionId = requireNotNull(caseExecutionId) }

  override fun caseDefinitionKey(caseDefinitionKey: String?) = this.apply { this.caseDefinitionKey = requireNotNull(caseDefinitionKey) }

  override fun caseDefinitionId(caseDefinitionId: String?) = this.apply { this.caseDefinitionId = requireNotNull(caseDefinitionId) }

  override fun caseDefinitionName(caseDefinitionName: String?) = this.apply { this.caseDefinitionName = requireNotNull(caseDefinitionName) }

  override fun caseDefinitionNameLike(caseDefinitionNameLike: String?) = this.apply { this.caseDefinitionNameLike = requireNotNull(caseDefinitionNameLike) }

  override fun taskVariableValueEquals(name: String, value: Any?) = this.apply {
    queryVariableValues.add(QueryVariableValue(name = name, value = value, operator = QueryOperator.EQUALS, taskVariable = true))
  }

  override fun taskVariableValueNotEquals(name: String, value: Any?) = this.apply {
    queryVariableValues.add(QueryVariableValue(name = name, value = value, operator = QueryOperator.NOT_EQUALS, taskVariable = true))
  }

  override fun taskVariableValueLike(name: String, value: String?) = this.apply {
    queryVariableValues.add(QueryVariableValue(name = name, value = value, operator = QueryOperator.LIKE, taskVariable = true))
  }

  override fun taskVariableValueGreaterThan(name: String, value: Any?) = this.apply {
    queryVariableValues.add(QueryVariableValue(name = name, value = value, operator = QueryOperator.GREATER_THAN, taskVariable = true))
  }

  override fun taskVariableValueGreaterThanOrEquals(name: String, value: Any?) = this.apply {
    queryVariableValues.add(QueryVariableValue(name = name, value = value, operator = QueryOperator.GREATER_THAN_OR_EQUAL, taskVariable = true))
  }

  override fun taskVariableValueLessThan(name: String, value: Any?) = this.apply {
    queryVariableValues.add(QueryVariableValue(name = name, value = value, operator = QueryOperator.LESS_THAN, taskVariable = true))
  }

  override fun taskVariableValueLessThanOrEquals(name: String, value: Any?) = this.apply {
    queryVariableValues.add(QueryVariableValue(name = name, value = value, operator = QueryOperator.LESS_THAN_OR_EQUAL, taskVariable = true))
  }

  override fun processVariableValueEquals(name: String, value: Any?) = this.apply {
    queryVariableValues.add(QueryVariableValue(name = name, value = value, operator = QueryOperator.EQUALS, processVariable = true))
  }

  override fun processVariableValueNotEquals(name: String, value: Any?) = this.apply {
    queryVariableValues.add(QueryVariableValue(name = name, value = value, operator = QueryOperator.NOT_EQUALS, processVariable = true))
  }

  override fun processVariableValueLike(name: String, value: String?) = this.apply {
    queryVariableValues.add(QueryVariableValue(name = name, value = value, operator = QueryOperator.LIKE, processVariable = true))
  }

  override fun processVariableValueNotLike(name: String, value: String?) = this.apply {
    queryVariableValues.add(QueryVariableValue(name = name, value = value, operator = QueryOperator.NOT_LIKE, processVariable = true))
  }

  override fun processVariableValueGreaterThan(name: String, value: Any?) = this.apply {
    queryVariableValues.add(QueryVariableValue(name = name, value = value, operator = QueryOperator.GREATER_THAN, processVariable = true))
  }

  override fun processVariableValueGreaterThanOrEquals(name: String, value: Any?) = this.apply {
    queryVariableValues.add(QueryVariableValue(name = name, value = value, operator = QueryOperator.GREATER_THAN_OR_EQUAL, processVariable = true))
  }

  override fun processVariableValueLessThan(name: String, value: Any?) = this.apply {
    queryVariableValues.add(QueryVariableValue(name = name, value = value, operator = QueryOperator.LESS_THAN, processVariable = true))
  }

  override fun processVariableValueLessThanOrEquals(name: String, value: Any?) = this.apply {
    queryVariableValues.add(QueryVariableValue(name = name, value = value, operator = QueryOperator.LESS_THAN_OR_EQUAL, processVariable = true))
  }

  override fun caseInstanceVariableValueEquals(name: String, value: Any?) = this.apply {
    queryVariableValues.add(QueryVariableValue(name = name, value = value, operator = QueryOperator.EQUALS))
  }

  override fun caseInstanceVariableValueNotEquals(name: String, value: Any?) = this.apply {
    queryVariableValues.add(QueryVariableValue(name = name, value = value, operator = QueryOperator.NOT_EQUALS))
  }

  override fun caseInstanceVariableValueLike(name: String, value: String?) = this.apply {
    queryVariableValues.add(QueryVariableValue(name = name, value = value, operator = QueryOperator.LIKE))
  }

  override fun caseInstanceVariableValueNotLike(name: String, value: String?) = this.apply {
    queryVariableValues.add(QueryVariableValue(name = name, value = value, operator = QueryOperator.NOT_LIKE))
  }

  override fun caseInstanceVariableValueGreaterThan(name: String, value: Any?) = this.apply {
    queryVariableValues.add(QueryVariableValue(name = name, value = value, operator = QueryOperator.GREATER_THAN))
  }

  override fun caseInstanceVariableValueGreaterThanOrEquals(name: String, value: Any?) = this.apply {
    queryVariableValues.add(QueryVariableValue(name = name, value = value, operator = QueryOperator.GREATER_THAN_OR_EQUAL))
  }

  override fun caseInstanceVariableValueLessThan(name: String, value: Any?) = this.apply {
    queryVariableValues.add(QueryVariableValue(name = name, value = value, operator = QueryOperator.LESS_THAN))
  }

  override fun caseInstanceVariableValueLessThanOrEquals(name: String, value: Any?) = this.apply {
    queryVariableValues.add(QueryVariableValue(name = name, value = value, operator = QueryOperator.LESS_THAN_OR_EQUAL))
  }

  override fun processDefinitionKey(processDefinitionKey: String?) = this.apply { this.processDefinitionKey = requireNotNull(processDefinitionKey) }

  override fun processDefinitionKeyIn(vararg processDefinitionKeyIn: String) = this.apply { this.processDefinitionKeys = processDefinitionKeyIn }

  override fun processDefinitionId(processDefinitionId: String?) = this.apply { this.processDefinitionId = requireNotNull(processDefinitionId) }

  override fun processDefinitionName(processDefinitionName: String?) = this.apply { this.processDefinitionName = requireNotNull(processDefinitionName) }

  override fun processDefinitionNameLike(processDefinitionNameLike: String?) = this.apply { this.processDefinitionNameLike = requireNotNull(processDefinitionNameLike) }

  override fun dueDate(dueDate: Date?) = this.apply {
    if (isWithoutDueDate) {
      throw ProcessEngineException("Invalid query usage: cannot set both dueDate and withoutDueDate filters.")
    }
    this.dueDate = requireNotNull(dueDate)
    this.expressions.remove("dueDate")
  }

  override fun dueDateExpression(dueDateExpression: String?) = this.apply {
    if (isWithoutDueDate) {
      throw ProcessEngineException("Invalid query usage: cannot set both dueDateExpression and withoutDueDate filters.")
    }
    this.expressions["dueDate"] = requireNotNull(dueDateExpression)
  }

  override fun dueBefore(dueBefore: Date?) = this.apply {
    if (isWithoutDueDate) {
      throw ProcessEngineException("Invalid query usage: cannot set both dueBefore and withoutDueDate filters.")
    }
    this.dueBefore = requireNotNull(dueBefore)
    this.expressions.remove("dueBefore")
  }

  override fun dueBeforeExpression(dueBeforeExpression: String?) = this.apply {
    if (isWithoutDueDate) {
      throw ProcessEngineException("Invalid query usage: cannot set both dueBeforeExpression and withoutDueDate filters.")
    }
    this.expressions["dueBefore"] = requireNotNull(dueBeforeExpression)
  }

  override fun dueAfter(dueAfter: Date?) = this.apply {
    if (isWithoutDueDate) {
      throw ProcessEngineException("Invalid query usage: cannot set both dueAfter and withoutDueDate filters.")
    }
    this.dueAfter = requireNotNull(dueAfter)
    this.expressions.remove("dueAfter")
  }

  override fun dueAfterExpression(dueAfterExpression: String?) = this.apply {
    if (isWithoutDueDate) {
      throw ProcessEngineException("Invalid query usage: cannot set both dueAfterExpression and withoutDueDate filters.")
    }
    this.expressions["dueAfter"] = requireNotNull(dueAfterExpression)
  }

  override fun followUpDate(followUpDate: Date?) = this.apply {
    this.followUpDate = requireNotNull(followUpDate)
    expressions.remove("followUpDate")
  }

  override fun followUpDateExpression(followUpDateExpression: String?) = this.apply {
    this.expressions["followUpDate"] = requireNotNull(followUpDateExpression)
  }

  override fun followUpBefore(followUpBefore: Date?) = this.apply {
    this.followUpNullAccepted = false
    this.followUpBefore = requireNotNull(followUpBefore)
    expressions.remove("followUpBefore")
  }

  override fun followUpBeforeExpression(followUpBeforeExpression: String?) = this.apply {
    this.followUpNullAccepted = false
    this.expressions["followUpBefore"] = requireNotNull(followUpBeforeExpression)
  }

  override fun followUpBeforeOrNotExistent(followUpBeforeOrNotExistent: Date?) = this.apply {
    this.followUpNullAccepted = true
    this.followUpBefore = followUpBeforeOrNotExistent
    this.expressions.remove("followUpBeforeOrNotExistent")
  }

  override fun followUpBeforeOrNotExistentExpression(followUpBeforeOrNotExistentExpression: String?) = this.apply {
    this.followUpNullAccepted = true
    this.expressions["followUpBeforeOrNotExistent"] = requireNotNull(followUpBeforeOrNotExistentExpression)
  }

  override fun followUpAfter(followUpAfter: Date?) = this.apply {
    this.followUpAfter = requireNotNull(followUpAfter)
    this.expressions.remove("followUpAfter")
  }

  override fun followUpAfterExpression(followUpAfterExpression: String?) = this.apply {
    this.expressions["followUpAfter"] = requireNotNull(followUpAfterExpression)
  }

  override fun suspended() = this.apply { this.suspensionState = SuspensionState.SUSPENDED }

  override fun active() = this.apply { this.suspensionState = SuspensionState.ACTIVE }

  override fun initializeFormKeys() = this.apply { this.initializeFormKeys = true }

  override fun withoutDueDate() = this.apply { this.isWithoutDueDate = true }
  override fun orderByTaskId() = this.apply { orderBy("id") }

  override fun orderByTaskName() = this.apply { orderBy("name") }

  override fun orderByTaskNameCaseInsensitive() = this.apply { orderBy("nameCaseInsensitive") }

  override fun orderByTaskDescription() = this.apply { orderBy("description") }

  override fun orderByTaskPriority() = this.apply { orderBy("priority") }

  override fun orderByTaskAssignee() = this.apply { orderBy("assignee") }

  override fun orderByTaskCreateTime() = this.apply { orderBy("created") }

  override fun orderByLastUpdated() = this.apply { orderBy("lastUpdated") }

  override fun orderByProcessInstanceId() = this.apply { orderBy("instanceId") }

  override fun orderByCaseInstanceId() = this.apply { orderBy("caseInstanceId") }

  override fun orderByExecutionId() = this.apply { orderBy("executionId") }

  override fun orderByCaseExecutionId() = this.apply { orderBy("caseExecutionId") }

  override fun orderByDueDate() = this.apply { orderBy("dueDate") }

  override fun orderByFollowUpDate() = this.apply { orderBy("followUpDate") }

  override fun orderByProcessVariable(property: String, type: ValueType) = this.apply {
    this.orderingProperties.add(QueryOrderingProperty(property = property, type = type, relation = Relation.PROCESS_INSTANCE))
  }

  override fun orderByExecutionVariable(property: String, type: ValueType) = this.apply {
    this.orderingProperties.add(QueryOrderingProperty(property = property, type = type, relation = Relation.EXECUTION))
  }

  override fun orderByTaskVariable(property: String, type: ValueType) = this.apply {
    this.orderingProperties.add(QueryOrderingProperty(property = property, type = type, relation = Relation.TASK))
  }

  override fun orderByCaseExecutionVariable(property: String, type: ValueType) = this.apply {
    this.orderingProperties.add(QueryOrderingProperty(property = property, type = type, relation = Relation.CASE_EXECUTION))
  }

  override fun orderByCaseInstanceVariable(property: String, type: ValueType) = this.apply {
    this.orderingProperties.add(QueryOrderingProperty(property = property, type = type, relation = Relation.CASE_INSTANCE))
  }

  override fun or() = this.apply { this.isOrQueryActive = true }

  override fun endOr() = this

  override fun listPage(firstResult: Int, maxResults: Int): List<Task> =
    taskApiClient.queryTasks(firstResult, maxResults, fillQueryDto()).body!!.map { TaskAdapter(TaskBean.fromDto(it)) }

  override fun count(): Long =
    taskApiClient.queryTasksCount(fillQueryDto()).body!!.count

  private fun fillQueryDto() = TaskQueryDto().apply {
    validate()
    val dtoPropertiesByName = TaskQueryDto::class.memberProperties.plus(TaskQueryDto::class.memberProperties)
      .filterIsInstance<KMutableProperty1<TaskQueryDto, Any?>>().associateBy { it.name }
    dtoPropertiesByName.forEach {
      val valueToSet = when (it.key) {
        "taskIdIn" -> this@DelegatingTaskQuery.taskIdIn?.toList()
        "processInstanceBusinessKeyExpression" -> this@DelegatingTaskQuery.expressions["processInstanceBusinessKey"]
        "processInstanceBusinessKeyIn" -> this@DelegatingTaskQuery.processInstanceBusinessKeys?.toList()
        "processInstanceBusinessKeyLikeExpression" -> this@DelegatingTaskQuery.expressions["processInstanceBusinessKeyLike"]
        "processInstanceIdIn" -> this@DelegatingTaskQuery.processInstanceIdIn?.toList()
        "processDefinitionKeyIn" -> this@DelegatingTaskQuery.processDefinitionKeys?.toList()
        "activityInstanceIdIn" -> this@DelegatingTaskQuery.activityInstanceIdIn?.toList()
        "tenantIdIn" -> this@DelegatingTaskQuery.tenantIds?.toList()
        "withoutTenantId" -> this@DelegatingTaskQuery.tenantIdsSet && this@DelegatingTaskQuery.tenantIds == null
        "assigneeExpression" -> this@DelegatingTaskQuery.expressions["taskAssignee"]
        "assigneeLikeExpression" -> this@DelegatingTaskQuery.expressions["taskAssigneeLike"]
        "assigneeIn" -> this@DelegatingTaskQuery.assigneeIn?.toList()
        "assigneeNotIn" -> this@DelegatingTaskQuery.assingeeNotIn?.toList()
        "ownerExpression" -> this@DelegatingTaskQuery.expressions["taskOwner"]
        "candidateGroups" -> this@DelegatingTaskQuery.candidateGroups
        "candidateGroupExpression" -> this@DelegatingTaskQuery.expressions["taskCandidateGroup"]
        "candidateUserExpression" -> this@DelegatingTaskQuery.expressions["taskCandidateUser"]
        "involvedUserExpression" -> this@DelegatingTaskQuery.expressions["taskInvolvedUser"]
        "taskDefinitionKey" -> this@DelegatingTaskQuery.key
        "taskDefinitionKeyIn" -> this@DelegatingTaskQuery.taskDefinitionKeys?.toList()
        "taskDefinitionKeyLike" -> this@DelegatingTaskQuery.keyLike
        "dueDateExpression" -> this@DelegatingTaskQuery.expressions["dueDate"]
        "dueAfterExpression" -> this@DelegatingTaskQuery.expressions["dueDateAfter"]
        "dueBeforeExpression" -> this@DelegatingTaskQuery.expressions["dueDateBefore"]
        "withoutDueDate" -> this@DelegatingTaskQuery.isWithoutDueDate
        "followUpBefore" -> this@DelegatingTaskQuery.followUpBefore?.let { DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(it.toOffsetDateTime()) }
        "followUpDateExpression" -> this@DelegatingTaskQuery.expressions["followUpDate"]
        "followUpAfterExpression" -> this@DelegatingTaskQuery.expressions["followUpDateAfter"]
        "followUpBeforeExpression" -> this@DelegatingTaskQuery.expressions["followUpDateBefore"]
        "followUpBeforeOrNotExistent" -> if (this@DelegatingTaskQuery.followUpNullAccepted) this@DelegatingTaskQuery.followUpBefore?.let { DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(it.toOffsetDateTime()) } else null
        "followUpBeforeOrNotExistentExpression" -> this@DelegatingTaskQuery.expressions["followUpBeforeOrNotExistent"]
        "createdOn" -> this@DelegatingTaskQuery.createTime.toOffsetDateTime()
        "createdOnExpression" -> this@DelegatingTaskQuery.expressions["taskCreatedOn"]
        "createdAfter" -> this@DelegatingTaskQuery.createTimeAfter.toOffsetDateTime()
        "createdAfterExpression" -> this@DelegatingTaskQuery.expressions["taskCreatedAfter"]
        "createdBefore" -> this@DelegatingTaskQuery.createTimeBefore.toOffsetDateTime()
        "createdBeforeExpression" -> this@DelegatingTaskQuery.expressions["taskCreatedBefore"]
        "updatedAfter" -> this@DelegatingTaskQuery.updatedAfter.toOffsetDateTime()
        "updatedAfterExpression" -> this@DelegatingTaskQuery.expressions["taskUpdatedAfter"]
        "candidateGroupsExpression" -> this@DelegatingTaskQuery.expressions["taskCandidateGroupIn"]
        "active" -> this@DelegatingTaskQuery.suspensionState?.let { it == SuspensionState.ACTIVE }
        "suspended" -> this@DelegatingTaskQuery.suspensionState?.let { it == SuspensionState.SUSPENDED }
        "taskVariables" -> this@DelegatingTaskQuery.queryVariableValues.filter { !it.processVariable && it.taskVariable }.toDto()
        "processVariables" -> this@DelegatingTaskQuery.queryVariableValues.filter { it.processVariable && !it.taskVariable }.toDto()
        "caseInstanceVariables" -> this@DelegatingTaskQuery.queryVariableValues.filter { !it.processVariable && !it.taskVariable }.toDto()
        "delegationState" -> this@DelegatingTaskQuery.delegationState?.let {
          if (it == DelegationState.PENDING) TaskQueryDto.DelegationStateEnum.PENDING else TaskQueryDto.DelegationStateEnum.RESOLVED
        }
        "orQueries" -> if (this@DelegatingTaskQuery.isOrQueryActive) throw UnsupportedOperationException("or-Queries are not supported") else null
        "sorting" -> this@DelegatingTaskQuery.orderingProperties.mapNotNull { it.toTaskSorting() }.filter { it.sortBy != null }
        else -> valueForProperty(it.key, this@DelegatingTaskQuery, it.value.returnType)
      }
      it.value.isAccessible = true
      it.value.set(this, valueToSet)
    }
  }

}
