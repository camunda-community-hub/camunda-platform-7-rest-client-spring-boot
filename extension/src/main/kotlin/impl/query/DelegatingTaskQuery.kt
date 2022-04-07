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

import mu.KLogging
import org.camunda.bpm.engine.ProcessEngineException
import org.camunda.bpm.engine.impl.TaskQueryImpl
import org.camunda.bpm.engine.impl.persistence.entity.SuspensionState
import org.camunda.bpm.engine.task.DelegationState
import org.camunda.bpm.engine.task.Task
import org.camunda.bpm.extension.rest.adapter.TaskAdapter
import org.camunda.bpm.extension.rest.adapter.TaskBean
import org.camunda.bpm.extension.rest.client.api.TaskApiClient
import org.camunda.bpm.extension.rest.client.model.TaskQueryDto
import org.camunda.bpm.extension.rest.impl.model.PatchedTaskQueryDto
import org.camunda.bpm.extension.rest.impl.toTaskSorting
import org.camunda.bpm.extension.rest.variables.toDto
import java.time.format.DateTimeFormatter
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

/**
 * Implementation of the task query.
 */
class DelegatingTaskQuery(
  private val taskApiClient: TaskApiClient
) : TaskQueryImpl() {

  companion object : KLogging()

  override fun list(): List<Task> =
    taskApiClient.queryTasks(this.firstResult, this.maxResults, fillQueryDto()).body!!.map { TaskAdapter(TaskBean.fromDto(it)) }

  override fun listPage(firstResult: Int, maxResults: Int): List<Task> =
    taskApiClient.queryTasks(firstResult, maxResults, fillQueryDto()).body!!.map { TaskAdapter(TaskBean.fromDto(it)) }

  override fun count(): Long =
    taskApiClient.queryTasksCount(fillQueryDto()).body!!.count

  override fun singleResult(): Task? {
    val results = list()
    return when {
      results.size == 1 -> results[0]
      results.size > 1 -> throw ProcessEngineException("Query return " + results.size.toString() + " results instead of expected maximum 1")
      else -> null
    }
  }

  private fun fillQueryDto() = PatchedTaskQueryDto().apply {
    checkQueryOk()
    val dtoPropertiesByName = TaskQueryDto::class.memberProperties.plus(PatchedTaskQueryDto::class.memberProperties)
      .filterIsInstance<KMutableProperty1<PatchedTaskQueryDto, Any?>>().associateBy { it.name }
    val queryPropertiesByName = TaskQueryImpl::class.memberProperties.associateBy { it.name }
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
        "withoutTenantId" -> this@DelegatingTaskQuery.isWithoutTenantId
        "assigneeExpression" -> this@DelegatingTaskQuery.expressions["taskAssignee"]
        "assigneeLikeExpression" -> this@DelegatingTaskQuery.expressions["taskAssigneeLike"]
        "assigneeIn" -> this@DelegatingTaskQuery.assigneeIn?.toList()
        "ownerExpression" -> this@DelegatingTaskQuery.expressions["taskOwner"]
        "candidateGroupExpression" -> this@DelegatingTaskQuery.expressions["taskCandidateGroup"]
        "candidateUserExpression" -> this@DelegatingTaskQuery.expressions["taskCandidateUser"]
        "involvedUserExpression" -> this@DelegatingTaskQuery.expressions["taskInvolvedUser"]
        "taskDefinitionKey" -> this@DelegatingTaskQuery.key
        "taskDefinitionKeyIn" -> this@DelegatingTaskQuery.keys?.toList()
        "taskDefinitionKeyLike" -> this@DelegatingTaskQuery.keyLike
        "dueDateExpression" -> this@DelegatingTaskQuery.expressions["dueDate"]
        "dueAfterExpression" -> this@DelegatingTaskQuery.expressions["dueDateAfter"]
        "dueBeforeExpression" -> this@DelegatingTaskQuery.expressions["dueDateBefore"]
        "withoutDueDate" -> this@DelegatingTaskQuery.isWithoutDueDate
        "followUpBefore" -> this@DelegatingTaskQuery.followUpBefore?.let { DateTimeFormatter.ISO_DATE_TIME.format(it.toInstant()) }
        "followUpDateExpression" -> this@DelegatingTaskQuery.expressions["followUpDate"]
        "followUpAfterExpression" -> this@DelegatingTaskQuery.expressions["followUpDateAfter"]
        "followUpBeforeExpression" -> this@DelegatingTaskQuery.expressions["followUpDateBefore"]
        "followUpBeforeOrNotExistent" -> this@DelegatingTaskQuery.followUpBefore
        "followUpBeforeOrNotExistentExpression" -> this@DelegatingTaskQuery.expressions["followUpBeforeOrNotExistent"]
        "createdOn" -> this@DelegatingTaskQuery.createTime
        "createdOnExpression" -> this@DelegatingTaskQuery.expressions["taskCreatedOn"]
        "createdAfter" -> this@DelegatingTaskQuery.createTimeAfter
        "createdAfterExpression" -> this@DelegatingTaskQuery.expressions["taskCreatedAfter"]
        "createdBefore" -> this@DelegatingTaskQuery.createTimeBefore
        "createdBeforeExpression" -> this@DelegatingTaskQuery.expressions["taskCreatedBefore"]
        "candidateGroupsExpression" -> this@DelegatingTaskQuery.expressions["taskCandidateGroupIn"]
        "active" -> this@DelegatingTaskQuery.suspensionState?.let { it == SuspensionState.ACTIVE }
        "suspended" -> this@DelegatingTaskQuery.suspensionState?.let { it == SuspensionState.SUSPENDED }
        "taskVariables" -> this@DelegatingTaskQuery.variables.filter { !it.isProcessInstanceVariable && it.isLocal }.toDto()
        "processVariables" -> this@DelegatingTaskQuery.variables.filter { it.isProcessInstanceVariable && !it.isLocal }.toDto()
        "caseInstanceVariables" -> this@DelegatingTaskQuery.variables.filter { !it.isProcessInstanceVariable && !it.isLocal }.toDto()
        "delegationState" -> this@DelegatingTaskQuery.delegationState?.let {
          if (it == DelegationState.PENDING) TaskQueryDto.DelegationStateEnum.PENDING else TaskQueryDto.DelegationStateEnum.RESOLVED
        }
        "orQueries" -> if (this@DelegatingTaskQuery.isOrQueryActive) throw UnsupportedOperationException("or-Queries are not supported") else null
        "sorting" -> this@DelegatingTaskQuery.orderingProperties.mapNotNull { it.toTaskSorting() }.filter { it.sortBy != null }
        else -> {
          val queryProperty = queryPropertiesByName[it.key]
          if (queryProperty == null) {
            throw IllegalArgumentException("no property found for ${it.key}")
          } else if (!queryProperty.returnType.isSubtypeOf(it.value.returnType)) {
            throw IllegalArgumentException("${queryProperty.returnType} is not assignable to ${it.value.returnType} for ${it.key}")
          } else {
            queryProperty.isAccessible = true
            queryProperty.get(this@DelegatingTaskQuery)
          }
        }
      }
      it.value.isAccessible = true
      it.value.set(this, valueToSet)
    }
  }

}
