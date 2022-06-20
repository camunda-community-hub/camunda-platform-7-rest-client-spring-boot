package org.camunda.community.rest.impl

import mu.KotlinLogging
import org.camunda.bpm.engine.impl.*
import org.camunda.bpm.engine.task.DelegationState
import org.camunda.bpm.engine.task.Task
import org.camunda.community.rest.adapter.IdentityLinkAdapter
import org.camunda.community.rest.client.model.*

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

fun IdentityLinkAdapter.toDto(): IdentityLinkDto = IdentityLinkDto()
  .userId(this.userId)
  .groupId(this.groupId)
  .type(this.type)

fun QueryOrderingProperty.toProcessInstanceSorting(): ProcessInstanceQueryDtoSortingInner = ProcessInstanceQueryDtoSortingInner()
    .sortOrder(if (this.direction == Direction.DESCENDING) ProcessInstanceQueryDtoSortingInner.SortOrderEnum.DESC else ProcessInstanceQueryDtoSortingInner.SortOrderEnum.ASC)
    .sortBy(when (this@toProcessInstanceSorting.queryProperty) {
        ProcessInstanceQueryProperty.PROCESS_INSTANCE_ID -> ProcessInstanceQueryDtoSortingInner.SortByEnum.INSTANCEID
        ProcessInstanceQueryProperty.PROCESS_DEFINITION_ID -> ProcessInstanceQueryDtoSortingInner.SortByEnum.DEFINITIONID
        ProcessInstanceQueryProperty.PROCESS_DEFINITION_KEY -> ProcessInstanceQueryDtoSortingInner.SortByEnum.DEFINITIONKEY
        ProcessInstanceQueryProperty.TENANT_ID -> ProcessInstanceQueryDtoSortingInner.SortByEnum.TENANTID
        ProcessInstanceQueryProperty.BUSINESS_KEY -> ProcessInstanceQueryDtoSortingInner.SortByEnum.BUSINESSKEY
        else -> {
          logger.warn { "query property ${this@toProcessInstanceSorting.queryProperty} is not supported for sorting" }
          null
        }
    })

fun QueryOrderingProperty.toHistoricProcessInstanceSorting(): HistoricProcessInstanceQueryDtoSortingInner = HistoricProcessInstanceQueryDtoSortingInner()
  .sortOrder(if (this.direction == Direction.DESCENDING) HistoricProcessInstanceQueryDtoSortingInner.SortOrderEnum.DESC else HistoricProcessInstanceQueryDtoSortingInner.SortOrderEnum.ASC)
  .sortBy(when (this@toHistoricProcessInstanceSorting.queryProperty) {
    HistoricProcessInstanceQueryProperty.PROCESS_INSTANCE_ID_ -> HistoricProcessInstanceQueryDtoSortingInner.SortByEnum.INSTANCEID
    HistoricProcessInstanceQueryProperty.PROCESS_DEFINITION_ID -> HistoricProcessInstanceQueryDtoSortingInner.SortByEnum.DEFINITIONID
    HistoricProcessInstanceQueryProperty.PROCESS_DEFINITION_KEY -> HistoricProcessInstanceQueryDtoSortingInner.SortByEnum.DEFINITIONKEY
    HistoricProcessInstanceQueryProperty.PROCESS_DEFINITION_NAME -> HistoricProcessInstanceQueryDtoSortingInner.SortByEnum.DEFINITIONNAME
    HistoricProcessInstanceQueryProperty.PROCESS_DEFINITION_VERSION -> HistoricProcessInstanceQueryDtoSortingInner.SortByEnum.DEFINITIONVERSION
    HistoricProcessInstanceQueryProperty.TENANT_ID -> HistoricProcessInstanceQueryDtoSortingInner.SortByEnum.TENANTID
    HistoricProcessInstanceQueryProperty.BUSINESS_KEY -> HistoricProcessInstanceQueryDtoSortingInner.SortByEnum.BUSINESSKEY
    HistoricProcessInstanceQueryProperty.START_TIME -> HistoricProcessInstanceQueryDtoSortingInner.SortByEnum.STARTTIME
    HistoricProcessInstanceQueryProperty.END_TIME -> HistoricProcessInstanceQueryDtoSortingInner.SortByEnum.ENDTIME
    HistoricProcessInstanceQueryProperty.DURATION -> HistoricProcessInstanceQueryDtoSortingInner.SortByEnum.DURATION
    else -> {
      logger.warn { "query property ${this@toHistoricProcessInstanceSorting.queryProperty} is not supported for sorting" }
      null
    }
  })


fun QueryOrderingProperty.toTaskSorting(): TaskQueryDtoSortingInner? {
  val dtoSorting = TaskQueryDtoSortingInner()
    .sortOrder(if (this.direction == Direction.DESCENDING) TaskQueryDtoSortingInner.SortOrderEnum.DESC else TaskQueryDtoSortingInner.SortOrderEnum.ASC)
  return when (this.relation) {
    QueryOrderingProperty.RELATION_VARIABLE -> {
      dtoSorting.apply {
        val variableName = this@toTaskSorting.relationConditions.find { it.property == VariableInstanceQueryProperty.VARIABLE_NAME } ?: throw IllegalStateException("variable name not found")
        val variableType = this@toTaskSorting.relationConditions.find { it.property == VariableInstanceQueryProperty.VARIABLE_TYPE } ?: throw IllegalStateException("variable type not found")
        this.parameters = SortTaskQueryParametersDto().variable(variableName.scalarValue as String).type(variableType.scalarValue as String)
        val relation = this@toTaskSorting.relationConditions.find { it.isPropertyComparison } ?: throw IllegalStateException("no relation condition for property comparison")
        this.sortBy =
          if (relation.property == VariableInstanceQueryProperty.EXECUTION_ID && relation.comparisonProperty == TaskQueryProperty.PROCESS_INSTANCE_ID)
            TaskQueryDtoSortingInner.SortByEnum.PROCESSVARIABLE
          else if (relation.property == VariableInstanceQueryProperty.EXECUTION_ID && relation.comparisonProperty == TaskQueryProperty.EXECUTION_ID)
            TaskQueryDtoSortingInner.SortByEnum.EXECUTIONVARIABLE
          else if (relation.property == VariableInstanceQueryProperty.EXECUTION_ID && relation.comparisonProperty == TaskQueryProperty.EXECUTION_ID)
            TaskQueryDtoSortingInner.SortByEnum.EXECUTIONVARIABLE
          else if (relation.property == VariableInstanceQueryProperty.TASK_ID && relation.comparisonProperty == TaskQueryProperty.TASK_ID)
            TaskQueryDtoSortingInner.SortByEnum.TASKVARIABLE
          else if (relation.property == VariableInstanceQueryProperty.CASE_EXECUTION_ID && relation.comparisonProperty == TaskQueryProperty.CASE_INSTANCE_ID)
            TaskQueryDtoSortingInner.SortByEnum.CASEINSTANCEVARIABLE
          else if (relation.property == VariableInstanceQueryProperty.CASE_EXECUTION_ID && relation.comparisonProperty == TaskQueryProperty.CASE_EXECUTION_ID)
            TaskQueryDtoSortingInner.SortByEnum.CASEEXECUTIONVARIABLE
          else {
            logger.warn { "relation not supported $relation for sorting" }
            null
          }
      }
    }
    else -> dtoSorting.apply {
      this.sortBy = when (this@toTaskSorting.queryProperty) {
        TaskQueryProperty.ASSIGNEE -> TaskQueryDtoSortingInner.SortByEnum.ASSIGNEE
        TaskQueryProperty.TASK_ID -> TaskQueryDtoSortingInner.SortByEnum.ID
        TaskQueryProperty.NAME -> TaskQueryDtoSortingInner.SortByEnum.NAME
        TaskQueryProperty.NAME_CASE_INSENSITIVE -> TaskQueryDtoSortingInner.SortByEnum.NAMECASEINSENSITIVE
        TaskQueryProperty.CASE_EXECUTION_ID -> TaskQueryDtoSortingInner.SortByEnum.CASEEXECUTIONID
        TaskQueryProperty.CASE_INSTANCE_ID -> TaskQueryDtoSortingInner.SortByEnum.CASEINSTANCEID
        TaskQueryProperty.CREATE_TIME -> TaskQueryDtoSortingInner.SortByEnum.CREATED
        TaskQueryProperty.DESCRIPTION -> TaskQueryDtoSortingInner.SortByEnum.DESCRIPTION
        TaskQueryProperty.DUE_DATE -> TaskQueryDtoSortingInner.SortByEnum.DUEDATE
        TaskQueryProperty.EXECUTION_ID -> TaskQueryDtoSortingInner.SortByEnum.EXECUTIONID
        TaskQueryProperty.PRIORITY -> TaskQueryDtoSortingInner.SortByEnum.PRIORITY
        TaskQueryProperty.PROCESS_INSTANCE_ID -> TaskQueryDtoSortingInner.SortByEnum.INSTANCEID
        else -> {
          logger.warn { "query property ${this@toTaskSorting.queryProperty} is not supported" }
          null
        }
      }
    }
  }
}
