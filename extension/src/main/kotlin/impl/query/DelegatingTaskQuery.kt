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
import org.camunda.bpm.engine.impl.TaskQueryImpl
import org.camunda.bpm.engine.impl.persistence.entity.SuspensionState
import org.camunda.bpm.engine.rest.dto.VariableQueryParameterDto
import org.camunda.bpm.engine.rest.dto.task.TaskQueryDto
import org.camunda.bpm.engine.task.Task
import org.camunda.bpm.extension.rest.adapter.TaskAdapter
import org.camunda.bpm.extension.rest.adapter.TaskBean
import org.camunda.bpm.extension.rest.client.TaskServiceClient
import java.util.*

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
    queryDto.assigneeLikeExpression = this.expressions["taskAsigneeLike"]

    queryDto.setWithCandidateGroups(this.isWithCandidateUsers)
    queryDto.setWithoutCandidateGroups(this.isWithoutCandidateGroups)
    queryDto.candidateGroup = this.candidateGroup
    queryDto.candidateGroups = this.candidateGroups
    queryDto.candidateGroupExpression = this.expressions["taskCandidateGroup"]
    queryDto.candidateGroupsExpression = this.expressions["taskCandidateGroupIn"]

    queryDto.candidateUser = this.candidateUser
    queryDto.setWithCandidateUsers(this.isWithCandidateUsers)
    queryDto.setWithoutCandidateUsers(this.isWithoutCandidateUsers)
    queryDto.candidateUserExpression = this.expressions["taskCandidateUser"]

    queryDto.caseDefinitionId = this.caseDefinitionId
    queryDto.caseDefinitionKey = this.caseDefinitionKey
    queryDto.caseDefinitionName = this.caseDefinitionName
    queryDto.caseDefinitionNameLike = this.caseDefinitionNameLike
    queryDto.caseExecutionId = this.caseExecutionId
    queryDto.caseInstanceId = this.caseInstanceId

    queryDto.caseInstanceBusinessKey = this.caseInstanceBusinessKey
    queryDto.caseInstanceBusinessKeyLike = this.caseInstanceBusinessKeyLike

    queryDto.taskVariables = this.variables.map { it.toDto() }
    queryDto.isVariableNamesIgnoreCase = this.isVariableNamesIgnoreCase
    queryDto.isVariableValuesIgnoreCase = this.isVariableValuesIgnoreCase

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

    // FIXME:
    val dueAfter: Date? = null
    val dueAfterExpression: String? = null
    val dueBefore: Date? = null
    val dueBeforeExpression: String? = null
    val dueDate: Date? = null
    val dueDateExpression: String? = null

    val followUpAfter: Date? = null
    val followUpAfterExpression: String? = null
    val followUpBefore: Date? = null
    val followUpBeforeExpression: String? = null
    val followUpBeforeOrNotExistent: Date? = null
    val followUpBeforeOrNotExistentExpression: String? = null
    val followUpDate: Date? = null
    val followUpDateExpression: String? = null

    val createdAfter: Date? = null
    val createdAfterExpression: String? = null
    val createdBefore: Date? = null
    val createdBeforeExpression: String? = null
    val createdOn: Date? = null
    val createdOnExpression: String? = null

    val processVariables: List<VariableQueryParameterDto>? = null
    val caseInstanceVariables: List<VariableQueryParameterDto>? = null
    val orQueries: List<TaskQueryDto>? = null

    this.variableNamesIgnoreCase = this.variableNamesIgnoreCase
    this.variableValuesIgnoreCase = this.variableValuesIgnoreCase

    if (this.isTenantIdSet) { // TODO: check
      if (this.tenantIds != null) {
        queryDto.setTenantIdIn(this.tenantIds)
      } else {
        queryDto.setWithoutTenantId(true)
      }
    }

    return queryDto
  }
}
