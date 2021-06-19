/*-
 * #%L
 * camunda-rest-client-spring-boot
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
package org.camunda.bpm.extension.rest.impl.query

import org.camunda.bpm.engine.ProcessEngineException
import org.camunda.bpm.engine.impl.QueryVariableValue
import org.camunda.bpm.engine.impl.TaskQueryImpl
import org.camunda.bpm.engine.impl.TaskQueryVariableValue
import org.camunda.bpm.engine.impl.persistence.entity.SuspensionState
import org.camunda.bpm.engine.rest.dto.VariableQueryParameterDto
import org.camunda.bpm.engine.rest.dto.task.TaskQueryDto
import org.camunda.bpm.engine.task.Task
import org.camunda.bpm.extension.rest.adapter.TaskAdapter
import org.camunda.bpm.extension.rest.adapter.TaskBean
import org.camunda.bpm.extension.rest.client.TaskServiceClient

/**
 * Implementation of the task query.
 */
class DelegatingTaskQuery(
  private val taskServiceClient: TaskServiceClient
) : TaskQueryImpl() {

  override fun list(): List<Task> {
    val tasks = taskServiceClient.getTasks(fillQueryDto(), this.firstResult, this.maxResults)
    return tasks.map {
      TaskAdapter(TaskBean.fromDto(it))
    }
  }

  override fun listPage(firstResult: Int, maxResults: Int): List<Task> {
    val tasks = taskServiceClient.getTasks(fillQueryDto(), firstResult, maxResults)
    return tasks.map {
      TaskAdapter(TaskBean.fromDto(it))
    }
  }

  override fun count(): Long {
    val count = taskServiceClient.getTaskCount(fillQueryDto(), this.firstResult, this.maxResults)
    return count.count
  }

  override fun singleResult(): Task? {
    val results = list()
    return when {
      results.size == 1 -> results[0]
      results.size > 1 -> throw ProcessEngineException("Query return " + results.size.toString() + " results instead of expected maximum 1")
      else -> null
    }
  }

  /**
   * Fill the DTO from the builder.
   */
  private fun fillQueryDto(): TaskQueryDto {
    val queryDto = TaskQueryDto()

    queryDto.setAssigned(this.assigned)
    queryDto.unassigned = this.unassigned
    queryDto.includeAssignedTasks = this.includeAssignedTasks

    queryDto.processInstanceId = this.processInstanceId
    queryDto.processInstanceIdIn = this.processInstanceIdIn

    queryDto.processInstanceBusinessKeyIn = this.processInstanceBusinessKeys
    queryDto.processInstanceBusinessKey = this.processInstanceBusinessKey
    queryDto.processInstanceBusinessKeyLike = this.processInstanceBusinessKeyLike

    queryDto.processInstanceBusinessKeyExpression = this.expressions["processInstanceBusinessKey"]
    queryDto.processInstanceBusinessKeyLikeExpression = this.expressions["processInstanceBusinessKeyLike"]

    queryDto.processDefinitionId = this.processDefinitionId
    queryDto.processDefinitionKeyIn = this.processDefinitionKeys
    queryDto.processDefinitionKey = this.processDefinitionKey
    queryDto.processDefinitionName = this.processDefinitionName
    queryDto.processDefinitionNameLike = this.processDefinitionNameLike

    queryDto.executionId = this.executionId

    queryDto.active = this.suspensionState == SuspensionState.ACTIVE || this.suspensionState == null
    queryDto.suspended = this.suspensionState == SuspensionState.SUSPENDED

    queryDto.activityInstanceIdIn = this.activityInstanceIdIn

    queryDto.assignee = this.assignee
    queryDto.assigneeLike = this.assigneeLike
    queryDto.assigneeIn = this.assigneeIn?.toTypedArray()
    queryDto.assigneeNotIn = this.assigneeNotIn?.toTypedArray()
    queryDto.assigneeExpression = this.expressions["taskAssignee"]
    queryDto.assigneeLikeExpression = this.expressions["taskAssigneeLike"]

    if (this.isWithCandidateGroups) {
      queryDto.setWithCandidateGroups(this.isWithCandidateGroups)
      queryDto.candidateGroup = this.candidateGroup
      queryDto.candidateGroups = this.candidateGroups
      queryDto.candidateGroupExpression = this.expressions["taskCandidateGroup"]
      queryDto.candidateGroupsExpression = this.expressions["taskCandidateGroupIn"]
    } else {
      queryDto.setWithoutCandidateGroups(this.isWithoutCandidateGroups)
    }

    if (this.isWithCandidateUsers) {
      queryDto.candidateUser = this.candidateUser
      queryDto.setWithCandidateUsers(this.isWithCandidateUsers)
      queryDto.candidateUserExpression = this.expressions["taskCandidateUser"]
    } else {
      queryDto.setWithoutCandidateUsers(this.isWithoutCandidateUsers)
    }

    queryDto.caseDefinitionId = this.caseDefinitionId
    queryDto.caseDefinitionKey = this.caseDefinitionKey
    queryDto.caseDefinitionName = this.caseDefinitionName
    queryDto.caseDefinitionNameLike = this.caseDefinitionNameLike
    queryDto.caseExecutionId = this.caseExecutionId
    queryDto.caseInstanceId = this.caseInstanceId

    queryDto.caseInstanceBusinessKey = this.caseInstanceBusinessKey
    queryDto.caseInstanceBusinessKeyLike = this.caseInstanceBusinessKeyLike

    queryDto.name = this.name
    queryDto.nameLike = this.nameLike
    queryDto.nameNotEqual = this.nameNotEqual
    queryDto.nameNotLike = this.nameNotLike

    queryDto.taskDefinitionKey = this.key
    queryDto.taskDefinitionKeyIn = this.keys
    queryDto.taskDefinitionKeyLike = this.keyLike

    queryDto.description = this.description
    queryDto.descriptionLike = this.descriptionLike

    queryDto.priority = this.priority
    queryDto.maxPriority = this.maxPriority
    queryDto.minPriority = this.minPriority

    queryDto.owner = this.owner
    queryDto.ownerExpression = this.expressions["taskOwner"]

    queryDto.involvedUser = this.involvedUser
    queryDto.involvedUserExpression = this.expressions["taskInvolvedUser"]

    queryDto.parentTaskId = this.parentTaskId

    queryDto.delegationState = this.delegationStateString

    queryDto.dueDate = this.dueDate
    queryDto.dueAfter = this.dueAfter
    queryDto.dueBefore = this.dueBefore
    queryDto.dueDateExpression = this.expressions["dueDate"]
    queryDto.dueAfterExpression = this.expressions["dueDateAfter"]
    queryDto.dueBeforeExpression = this.expressions["dueDateBefore"]

    queryDto.followUpDate = this.followUpDate
    queryDto.followUpAfter = this.followUpAfter
    queryDto.followUpBefore = this.followUpBefore
    queryDto.followUpBeforeOrNotExistent = this.followUpBefore
    queryDto.followUpBeforeOrNotExistentExpression = this.expressions["followUpBeforeOrNotExistent"]
    queryDto.followUpDateExpression = this.expressions["followUpDate"]
    queryDto.followUpAfterExpression = this.expressions["followUpDateAfter"]
    queryDto.followUpBeforeExpression = this.expressions["followUpDateBefore"]

    queryDto.createdOn = this.createTime
    queryDto.createdAfter = this.createTimeAfter
    queryDto.createdBefore = this.createTimeAfter

    queryDto.createdAfterExpression = this.expressions["taskCreatedAfter"]
    queryDto.createdBeforeExpression = this.expressions["taskCreatedBefore"]
    queryDto.createdOnExpression = this.expressions["taskCreatedOn"]


    queryDto.taskVariables = this.variables.filter { !it.isProcessInstanceVariable && it.isLocal }.map { it.toTaskVariableDto() }
    queryDto.processVariables =
      this.variables.filter { it.isProcessInstanceVariable && !it.isLocal }.map { it.toProcessInstanceVariableDto() }
    queryDto.caseInstanceVariables =
      this.variables.filter { !it.isProcessInstanceVariable && !it.isLocal }.map { it.toCaseInstanceVariableDto() }

    queryDto.isVariableNamesIgnoreCase = this.isVariableNamesIgnoreCase
    queryDto.isVariableValuesIgnoreCase = this.isVariableValuesIgnoreCase

    // bad method name, but still using it
    if (this.isTenantIdSet) {
      queryDto.withoutTenantId = true
    } else {
      if (this.tenantIds != null) {
        queryDto.tenantIdIn = this.tenantIds
      }
    }

    // FIXME: Or Queries not supported yet!
    val orQueries: List<TaskQueryDto>? = null

    return queryDto
  }
}

/**
 * Camunda constructor for the DTO is strange, but we use it here.
 */
fun QueryVariableValue.toTaskVariableDto(): VariableQueryParameterDto {
  // the task query variable value constructor parameter four and five reflect "isTaskVariable" and "isProcessVariable".
  // since we want to query for the task variables, we pass true to the task flag and false to the process instance flag
  // see QueryVariableValue class and AbstractVariableQueryImpl#addVariable
  return VariableQueryParameterDto(TaskQueryVariableValue(this.name, this.value, this.operator, true, false))
}

/**
 * Camunda constructor for the DTO is strange, but we use it here.
 */
fun QueryVariableValue.toCaseInstanceVariableDto(): VariableQueryParameterDto {
  // the task query variable value constructor parameter four and five reflect "isTaskVariable" and "isProcessVariable".
  // since we want to query for the case variables, we pass false twice
  // see QueryVariableValue class and AbstractVariableQueryImpl#addVariable
  return VariableQueryParameterDto(TaskQueryVariableValue(this.name, this.value, this.operator, false, false))
}

fun QueryVariableValue.toProcessInstanceVariableDto(): VariableQueryParameterDto {
  // the task query variable value constructor parameter four and five reflect "isTaskVariable" and "isProcessVariable".
  // since we want to query for the process instance variables, we pass pass false to the task flag and true to the process flag
  // see QueryVariableValue class and AbstractVariableQueryImpl#addVariable
  return VariableQueryParameterDto(TaskQueryVariableValue(this.name, this.value, this.operator, false, true))
}
