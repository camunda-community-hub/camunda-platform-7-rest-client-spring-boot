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

package org.camunda.bpm.extension.rest.adapter

import org.camunda.bpm.engine.form.CamundaFormRef
import org.camunda.bpm.engine.task.DelegationState
import org.camunda.bpm.engine.task.Task
import org.camunda.bpm.extension.rest.client.model.TaskDto
import java.util.*

/**
 * Implementation of a task delegating to a simple bean.
 */
class TaskAdapter(private val taskBean: TaskBean) : Task {
  override fun getId(): String = taskBean.id

  override fun getName(): String = taskBean.name

  override fun setName(name: String) {
    taskBean.name = name
  }

  override fun getDescription(): String? = taskBean.description

  override fun setDescription(description: String) {
    taskBean.description = description
  }

  override fun getPriority(): Int = taskBean.priority

  override fun setPriority(priority: Int) {
    taskBean.priority = priority
  }

  override fun getOwner(): String? = taskBean.owner

  override fun setOwner(owner: String?) {
    taskBean.owner = owner
  }

  override fun getAssignee(): String? = taskBean.assignee

  override fun setAssignee(assignee: String?) {
    taskBean.assignee = assignee
  }

  override fun getDelegationState(): DelegationState? = taskBean.delegationState

  override fun setDelegationState(delegationState: DelegationState?) {
    taskBean.delegationState = delegationState
  }

  override fun getProcessInstanceId(): String? = taskBean.processInstanceId

  override fun getExecutionId(): String? = taskBean.processExecutionId

  override fun getProcessDefinitionId(): String? = taskBean.processDefinitionId

  override fun getCaseInstanceId(): String? = taskBean.caseInstanceId

  override fun setCaseInstanceId(caseInstanceId: String?) {
    taskBean.caseInstanceId = caseInstanceId
  }

  override fun getCaseExecutionId(): String? = taskBean.caseExecutionId

  override fun getCaseDefinitionId(): String? = taskBean.caseDefinitionId

  override fun getCreateTime(): Date? = taskBean.created

  override fun getTaskDefinitionKey(): String? = taskBean.taskDefinitionKey

  override fun getDueDate(): Date? = taskBean.due

  override fun setDueDate(dueDate: Date?) {
    taskBean.due = dueDate
  }

  override fun getFollowUpDate(): Date? = taskBean.followUp

  override fun setFollowUpDate(followUpDate: Date?) {
    taskBean.followUp = followUpDate
  }

  override fun delegate(userId: String?) {
    taskBean.delegationState = DelegationState.PENDING
    taskBean.assignee = userId
  }

  override fun setParentTaskId(parentTaskId: String?) {
    taskBean.parentTaskId = parentTaskId
  }

  override fun getParentTaskId(): String? = taskBean.parentTaskId

  override fun isSuspended(): Boolean = taskBean.suspended

  override fun getFormKey(): String? = taskBean.formKey

  override fun getTenantId(): String? = taskBean.tenantId

  override fun setTenantId(tenantId: String?) {
    taskBean.tenantId = tenantId
  }

  override fun getCamundaFormRef(): CamundaFormRef? = TODO("Not yet implemented")

}

/**
 * POJO to hold the values of a task.
 */
data class TaskBean(
  val taskDefinitionKey: String?,
  val id: String,
  val formKey: String?,
  val caseDefinitionId: String?,
  val caseExecutionId: String?,
  val processDefinitionId: String?,
  val processInstanceId: String?,
  val processExecutionId: String?,
  var name: String,
  var description: String?,
  var priority: Int,
  var assignee: String?,
  var owner: String?,
  var parentTaskId: String?,
  var caseInstanceId: String?,
  var suspended: Boolean,
  var created: Date?,
  var due: Date?,
  var followUp: Date?,
  var tenantId: String?,
  var delegationState: DelegationState?,
) {
  companion object {
    /**
     * Factory method to create bean from REST representation.
     */
    @JvmStatic
    fun fromDto(dto: TaskDto) = TaskBean(
      assignee = dto.assignee,
      id = dto.id,
      name = dto.name,
      caseDefinitionId = dto.caseDefinitionId,
      caseExecutionId = dto.caseExecutionId,
      caseInstanceId = dto.caseInstanceId,
      suspended = dto.suspended,
      created = dto.created,
      due = dto.due,
      followUp = dto.followUp,
      formKey = dto.formKey,
      processDefinitionId = dto.processDefinitionId,
      processInstanceId = dto.processInstanceId,
      processExecutionId = dto.executionId,
      delegationState = if (dto.delegationState != null) DelegationState.valueOf(dto.delegationState.name) else null,
      tenantId = dto.tenantId,
      description = dto.description,
      priority = dto.priority,
      parentTaskId = dto.parentTaskId,
      owner = dto.owner,
      taskDefinitionKey = dto.taskDefinitionKey
    )
  }
}
