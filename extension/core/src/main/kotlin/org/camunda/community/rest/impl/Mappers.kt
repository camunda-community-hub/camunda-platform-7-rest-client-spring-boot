package org.camunda.community.rest.impl

import mu.KotlinLogging
import org.camunda.bpm.engine.task.DelegationState
import org.camunda.bpm.engine.task.Task
import org.camunda.community.rest.adapter.IdentityLinkAdapter
import org.camunda.community.rest.client.model.ExecutionQueryDtoSortingInner
import org.camunda.community.rest.client.model.ExternalTaskQueryDtoSortingInner
import org.camunda.community.rest.client.model.HistoricProcessInstanceQueryDtoSortingInner
import org.camunda.community.rest.client.model.IdentityLinkDto
import org.camunda.community.rest.client.model.ProcessInstanceQueryDtoSortingInner
import org.camunda.community.rest.client.model.SortTaskQueryParametersDto
import org.camunda.community.rest.client.model.TaskDto
import org.camunda.community.rest.client.model.TaskQueryDtoSortingInner
import org.camunda.community.rest.impl.query.QueryOrderingProperty
import org.camunda.community.rest.impl.query.Relation
import org.camunda.community.rest.impl.query.SortDirection

private val logger = KotlinLogging.logger {}

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

/**
 * Maps for the identity link adapter to the DTO used in the REST calls.
 */
fun IdentityLinkAdapter.toDto(): IdentityLinkDto = IdentityLinkDto(this.type)
  .userId(this.userId)
  .groupId(this.groupId)

fun QueryOrderingProperty.toProcessInstanceSorting(): ProcessInstanceQueryDtoSortingInner = ProcessInstanceQueryDtoSortingInner()
    .sortOrder(if (this.direction == SortDirection.DESC) ProcessInstanceQueryDtoSortingInner.SortOrderEnum.DESC else ProcessInstanceQueryDtoSortingInner.SortOrderEnum.ASC)
    .sortBy(when (this@toProcessInstanceSorting.property) {
        "instanceId" -> ProcessInstanceQueryDtoSortingInner.SortByEnum.INSTANCEID
        "definitionId" -> ProcessInstanceQueryDtoSortingInner.SortByEnum.DEFINITIONID
        "definitionKey" -> ProcessInstanceQueryDtoSortingInner.SortByEnum.DEFINITIONKEY
        "tenantId" -> ProcessInstanceQueryDtoSortingInner.SortByEnum.TENANTID
        "businessKey" -> ProcessInstanceQueryDtoSortingInner.SortByEnum.BUSINESSKEY
        else -> {
          logger.warn { "query property ${this@toProcessInstanceSorting.property} is not supported for sorting" }
          null
        }
    })

fun QueryOrderingProperty.toHistoricProcessInstanceSorting(): HistoricProcessInstanceQueryDtoSortingInner = HistoricProcessInstanceQueryDtoSortingInner()
  .sortOrder(if (this.direction == SortDirection.DESC) HistoricProcessInstanceQueryDtoSortingInner.SortOrderEnum.DESC else HistoricProcessInstanceQueryDtoSortingInner.SortOrderEnum.ASC)
  .sortBy(when (this@toHistoricProcessInstanceSorting.property) {
    "instanceId" -> HistoricProcessInstanceQueryDtoSortingInner.SortByEnum.INSTANCEID
    "definitionId" -> HistoricProcessInstanceQueryDtoSortingInner.SortByEnum.DEFINITIONID
    "definitionKey" -> HistoricProcessInstanceQueryDtoSortingInner.SortByEnum.DEFINITIONKEY
    "definitionName" -> HistoricProcessInstanceQueryDtoSortingInner.SortByEnum.DEFINITIONNAME
    "definitionVersion" -> HistoricProcessInstanceQueryDtoSortingInner.SortByEnum.DEFINITIONVERSION
    "tenantId" -> HistoricProcessInstanceQueryDtoSortingInner.SortByEnum.TENANTID
    "businessKey" -> HistoricProcessInstanceQueryDtoSortingInner.SortByEnum.BUSINESSKEY
    "startTime" -> HistoricProcessInstanceQueryDtoSortingInner.SortByEnum.STARTTIME
    "endTime" -> HistoricProcessInstanceQueryDtoSortingInner.SortByEnum.ENDTIME
    "duration" -> HistoricProcessInstanceQueryDtoSortingInner.SortByEnum.DURATION
    else -> {
      logger.warn { "query property ${this@toHistoricProcessInstanceSorting.property} is not supported for sorting" }
      null
    }
  })

fun QueryOrderingProperty.toExecutionSorting(): ExecutionQueryDtoSortingInner = ExecutionQueryDtoSortingInner()
  .sortOrder(if (this.direction == SortDirection.DESC) ExecutionQueryDtoSortingInner.SortOrderEnum.DESC else ExecutionQueryDtoSortingInner.SortOrderEnum.ASC)
  .sortBy(when (this@toExecutionSorting.property) {
    "instanceId" -> ExecutionQueryDtoSortingInner.SortByEnum.INSTANCEID
    "definitionId" -> ExecutionQueryDtoSortingInner.SortByEnum.DEFINITIONID
    "definitionKey" -> ExecutionQueryDtoSortingInner.SortByEnum.DEFINITIONKEY
    "tenantId" -> ExecutionQueryDtoSortingInner.SortByEnum.TENANTID
    else -> {
      logger.warn { "query property ${this@toExecutionSorting.property} is not supported for sorting" }
      null
    }
  })


fun QueryOrderingProperty.toTaskSorting(): TaskQueryDtoSortingInner? {
  val dtoSorting = TaskQueryDtoSortingInner()
    .sortOrder(if (this.direction == SortDirection.DESC) TaskQueryDtoSortingInner.SortOrderEnum.DESC else TaskQueryDtoSortingInner.SortOrderEnum.ASC)
  return if (this.relation != null) {
    dtoSorting.apply {
      this.parameters = SortTaskQueryParametersDto().variable(this@toTaskSorting.property).type(this@toTaskSorting.type!!.name)
      this.sortBy = when (this@toTaskSorting.relation) {
        Relation.TASK -> TaskQueryDtoSortingInner.SortByEnum.TASKVARIABLE
        Relation.CASE_INSTANCE -> TaskQueryDtoSortingInner.SortByEnum.CASEINSTANCEVARIABLE
        Relation.CASE_EXECUTION -> TaskQueryDtoSortingInner.SortByEnum.CASEEXECUTIONVARIABLE
        Relation.EXECUTION -> TaskQueryDtoSortingInner.SortByEnum.EXECUTIONVARIABLE
        Relation.PROCESS_INSTANCE -> TaskQueryDtoSortingInner.SortByEnum.PROCESSVARIABLE
      }
    }
  } else dtoSorting.apply {
    this.sortBy = TaskQueryDtoSortingInner.SortByEnum.fromValue(this@toTaskSorting.property)
  }
}

fun QueryOrderingProperty.toExternalTaskSorting(): ExternalTaskQueryDtoSortingInner =
  ExternalTaskQueryDtoSortingInner()
    .sortOrder(if (this.direction == SortDirection.DESC) ExternalTaskQueryDtoSortingInner.SortOrderEnum.DESC else ExternalTaskQueryDtoSortingInner.SortOrderEnum.ASC)
    .sortBy(ExternalTaskQueryDtoSortingInner.SortByEnum.fromValue(this@toExternalTaskSorting.property))

