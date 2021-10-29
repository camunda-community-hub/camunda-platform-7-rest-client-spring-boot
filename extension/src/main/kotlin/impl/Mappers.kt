package org.camunda.bpm.extension.rest.impl

import org.camunda.bpm.engine.task.DelegationState
import org.camunda.bpm.engine.task.Task
import org.camunda.bpm.extension.rest.adapter.IdentityLinkAdapter
import org.camunda.bpm.extension.rest.client.model.IdentityLinkDto
import org.camunda.bpm.extension.rest.client.model.TaskDto

fun Task.toDto(): TaskDto = TaskDto()
  .id(this.id)
  .name(this.name)
  .assignee(this.assignee)
  .owner(this.owner)
  .created(this.createTime)
  .due(this.dueDate)
  .followUp(this.followUpDate)
  .delegationState(when (this.delegationState) {
    DelegationState.PENDING -> TaskDto.DelegationStateEnum.PENDING
    DelegationState.RESOLVED -> TaskDto.DelegationStateEnum.RESOLVED
    else -> null
  })
  .description(this.description)
  .executionId(this.executionId)
  .parentTaskId(this.parentTaskId)
  .priority(this.priority)
  .processDefinitionId(this.processDefinitionId)
  .processInstanceId(this.processInstanceId)
  .caseDefinitionId(this.caseDefinitionId)
  .caseInstanceId(this.caseInstanceId)
  .caseExecutionId(this.caseExecutionId)
  .taskDefinitionKey(this.taskDefinitionKey)
  .suspended(this.isSuspended)
  .formKey(this.formKey)
  .tenantId(this.tenantId)

fun IdentityLinkAdapter.toDto(): IdentityLinkDto = IdentityLinkDto()
  .userId(this.userId)
  .groupId(this.groupId)
  .type(this.type)
