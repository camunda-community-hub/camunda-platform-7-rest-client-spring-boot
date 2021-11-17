package org.camunda.bpm.extension.rest.impl

import mu.KotlinLogging
import org.camunda.bpm.engine.impl.*
import org.camunda.bpm.engine.task.DelegationState
import org.camunda.bpm.engine.task.Task
import org.camunda.bpm.extension.rest.adapter.IdentityLinkAdapter
import org.camunda.bpm.extension.rest.client.model.*

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

fun QueryOrderingProperty.toProcessInstanceSorting(): ProcessInstanceQueryDtoSorting = ProcessInstanceQueryDtoSorting()
    .sortOrder(if (this.direction == Direction.DESCENDING) ProcessInstanceQueryDtoSorting.SortOrderEnum.DESC else ProcessInstanceQueryDtoSorting.SortOrderEnum.ASC)
    .sortBy(when (this@toProcessInstanceSorting.queryProperty) {
        ProcessInstanceQueryProperty.PROCESS_INSTANCE_ID -> ProcessInstanceQueryDtoSorting.SortByEnum.INSTANCEID
        ProcessInstanceQueryProperty.PROCESS_DEFINITION_ID -> ProcessInstanceQueryDtoSorting.SortByEnum.DEFINITIONID
        ProcessInstanceQueryProperty.PROCESS_DEFINITION_KEY -> ProcessInstanceQueryDtoSorting.SortByEnum.DEFINITIONKEY
        ProcessInstanceQueryProperty.TENANT_ID -> ProcessInstanceQueryDtoSorting.SortByEnum.TENANTID
        ProcessInstanceQueryProperty.BUSINESS_KEY -> ProcessInstanceQueryDtoSorting.SortByEnum.BUSINESSKEY
        else -> {
          logger.warn { "query property ${this@toProcessInstanceSorting.queryProperty} is not supported for sorting" }
          null
        }
    })

fun QueryOrderingProperty.toHistoricProcessInstanceSorting(): HistoricProcessInstanceQueryDtoSorting = HistoricProcessInstanceQueryDtoSorting()
  .sortOrder(if (this.direction == Direction.DESCENDING) HistoricProcessInstanceQueryDtoSorting.SortOrderEnum.DESC else HistoricProcessInstanceQueryDtoSorting.SortOrderEnum.ASC)
  .sortBy(when (this@toHistoricProcessInstanceSorting.queryProperty) {
    HistoricProcessInstanceQueryProperty.PROCESS_INSTANCE_ID_ -> HistoricProcessInstanceQueryDtoSorting.SortByEnum.INSTANCEID
    HistoricProcessInstanceQueryProperty.PROCESS_DEFINITION_ID -> HistoricProcessInstanceQueryDtoSorting.SortByEnum.DEFINITIONID
    HistoricProcessInstanceQueryProperty.PROCESS_DEFINITION_KEY -> HistoricProcessInstanceQueryDtoSorting.SortByEnum.DEFINITIONKEY
    HistoricProcessInstanceQueryProperty.PROCESS_DEFINITION_NAME -> HistoricProcessInstanceQueryDtoSorting.SortByEnum.DEFINITIONNAME
    HistoricProcessInstanceQueryProperty.PROCESS_DEFINITION_VERSION -> HistoricProcessInstanceQueryDtoSorting.SortByEnum.DEFINITIONVERSION
    HistoricProcessInstanceQueryProperty.TENANT_ID -> HistoricProcessInstanceQueryDtoSorting.SortByEnum.TENANTID
    HistoricProcessInstanceQueryProperty.BUSINESS_KEY -> HistoricProcessInstanceQueryDtoSorting.SortByEnum.BUSINESSKEY
    HistoricProcessInstanceQueryProperty.START_TIME -> HistoricProcessInstanceQueryDtoSorting.SortByEnum.STARTTIME
    HistoricProcessInstanceQueryProperty.END_TIME -> HistoricProcessInstanceQueryDtoSorting.SortByEnum.ENDTIME
    HistoricProcessInstanceQueryProperty.DURATION -> HistoricProcessInstanceQueryDtoSorting.SortByEnum.DURATION
    else -> {
      logger.warn { "query property ${this@toHistoricProcessInstanceSorting.queryProperty} is not supported for sorting" }
      null
    }
  })


fun QueryOrderingProperty.toTaskSorting(): TaskQueryDtoSorting? {
  val dtoSorting = TaskQueryDtoSorting()
    .sortOrder(if (this.direction == Direction.DESCENDING) TaskQueryDtoSorting.SortOrderEnum.DESC else TaskQueryDtoSorting.SortOrderEnum.ASC)
  return when (this.relation) {
    QueryOrderingProperty.RELATION_VARIABLE -> {
      dtoSorting.apply {
        val variableName = this@toTaskSorting.relationConditions.find { it.property == VariableInstanceQueryProperty.VARIABLE_NAME } ?: throw IllegalStateException("variable name not found")
        val variableType = this@toTaskSorting.relationConditions.find { it.property == VariableInstanceQueryProperty.VARIABLE_TYPE } ?: throw IllegalStateException("variable type not found")
        this.parameters = SortTaskQueryParametersDto().variable(variableName.scalarValue as String).type(variableType.scalarValue as String)
        val relation = this@toTaskSorting.relationConditions.find { it.isPropertyComparison } ?: throw IllegalStateException("no relation condition for property comparison")
        this.sortBy =
          if (relation.property == VariableInstanceQueryProperty.EXECUTION_ID && relation.comparisonProperty == TaskQueryProperty.PROCESS_INSTANCE_ID)
            TaskQueryDtoSorting.SortByEnum.PROCESSVARIABLE
          else if (relation.property == VariableInstanceQueryProperty.EXECUTION_ID && relation.comparisonProperty == TaskQueryProperty.EXECUTION_ID)
            TaskQueryDtoSorting.SortByEnum.EXECUTIONVARIABLE
          else if (relation.property == VariableInstanceQueryProperty.EXECUTION_ID && relation.comparisonProperty == TaskQueryProperty.EXECUTION_ID)
            TaskQueryDtoSorting.SortByEnum.EXECUTIONVARIABLE
          else if (relation.property == VariableInstanceQueryProperty.TASK_ID && relation.comparisonProperty == TaskQueryProperty.TASK_ID)
            TaskQueryDtoSorting.SortByEnum.TASKVARIABLE
          else if (relation.property == VariableInstanceQueryProperty.CASE_EXECUTION_ID && relation.comparisonProperty == TaskQueryProperty.CASE_INSTANCE_ID)
            TaskQueryDtoSorting.SortByEnum.CASEINSTANCEVARIABLE
          else if (relation.property == VariableInstanceQueryProperty.CASE_EXECUTION_ID && relation.comparisonProperty == TaskQueryProperty.CASE_EXECUTION_ID)
            TaskQueryDtoSorting.SortByEnum.CASEEXECUTIONVARIABLE
          else {
            logger.warn { "relation not supported $relation for sorting" }
            null
          }
      }
    }
    else -> dtoSorting.apply {
      this.sortBy = when (this@toTaskSorting.queryProperty) {
        TaskQueryProperty.ASSIGNEE -> TaskQueryDtoSorting.SortByEnum.ASSIGNEE
        TaskQueryProperty.TASK_ID -> TaskQueryDtoSorting.SortByEnum.ID
        TaskQueryProperty.NAME -> TaskQueryDtoSorting.SortByEnum.NAME
        TaskQueryProperty.NAME_CASE_INSENSITIVE -> TaskQueryDtoSorting.SortByEnum.NAMECASEINSENSITIVE
        TaskQueryProperty.CASE_EXECUTION_ID -> TaskQueryDtoSorting.SortByEnum.CASEEXECUTIONID
        TaskQueryProperty.CASE_INSTANCE_ID -> TaskQueryDtoSorting.SortByEnum.CASEINSTANCEID
        TaskQueryProperty.CREATE_TIME -> TaskQueryDtoSorting.SortByEnum.CREATED
        TaskQueryProperty.DESCRIPTION -> TaskQueryDtoSorting.SortByEnum.DESCRIPTION
        TaskQueryProperty.DUE_DATE -> TaskQueryDtoSorting.SortByEnum.DUEDATE
        TaskQueryProperty.EXECUTION_ID -> TaskQueryDtoSorting.SortByEnum.EXECUTIONID
        TaskQueryProperty.PRIORITY -> TaskQueryDtoSorting.SortByEnum.PRIORITY
        TaskQueryProperty.PROCESS_INSTANCE_ID -> TaskQueryDtoSorting.SortByEnum.INSTANCEID
        else -> {
          logger.warn { "query property ${this@toTaskSorting.queryProperty} is not supported" }
          null
        }
      }
    }
  }
}
