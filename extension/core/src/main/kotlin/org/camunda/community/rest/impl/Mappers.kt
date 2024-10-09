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
import org.camunda.community.rest.client.model.TaskWithAttachmentAndCommentDto
import org.camunda.community.rest.impl.query.QueryOrderingProperty
import org.camunda.community.rest.impl.query.Relation
import org.camunda.community.rest.impl.query.SortDirection
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.Date

private val logger = KotlinLogging.logger {}

fun Task.toDto(): TaskDto = TaskDto()
  .id(this.id)
  .name(this.name)
  .assignee(this.assignee)
  .owner(this.owner)
  .created(this.createTime.toOffsetDateTime())
  .due(this.dueDate.toOffsetDateTime())
  .followUp(this.followUpDate.toOffsetDateTime())
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
        "instanceId" -> ProcessInstanceQueryDtoSortingInner.SortByEnum.INSTANCE_ID
        "definitionId" -> ProcessInstanceQueryDtoSortingInner.SortByEnum.DEFINITION_ID
        "definitionKey" -> ProcessInstanceQueryDtoSortingInner.SortByEnum.DEFINITION_KEY
        "tenantId" -> ProcessInstanceQueryDtoSortingInner.SortByEnum.TENANT_ID
        "businessKey" -> ProcessInstanceQueryDtoSortingInner.SortByEnum.BUSINESS_KEY
        else -> {
          logger.warn { "query property ${this@toProcessInstanceSorting.property} is not supported for sorting" }
          null
        }
    })

fun QueryOrderingProperty.toHistoricProcessInstanceSorting(): HistoricProcessInstanceQueryDtoSortingInner = HistoricProcessInstanceQueryDtoSortingInner()
  .sortOrder(if (this.direction == SortDirection.DESC) HistoricProcessInstanceQueryDtoSortingInner.SortOrderEnum.DESC else HistoricProcessInstanceQueryDtoSortingInner.SortOrderEnum.ASC)
  .sortBy(when (this@toHistoricProcessInstanceSorting.property) {
    "instanceId" -> HistoricProcessInstanceQueryDtoSortingInner.SortByEnum.INSTANCE_ID
    "definitionId" -> HistoricProcessInstanceQueryDtoSortingInner.SortByEnum.DEFINITION_ID
    "definitionKey" -> HistoricProcessInstanceQueryDtoSortingInner.SortByEnum.DEFINITION_KEY
    "definitionName" -> HistoricProcessInstanceQueryDtoSortingInner.SortByEnum.DEFINITION_NAME
    "definitionVersion" -> HistoricProcessInstanceQueryDtoSortingInner.SortByEnum.DEFINITION_VERSION
    "tenantId" -> HistoricProcessInstanceQueryDtoSortingInner.SortByEnum.TENANT_ID
    "businessKey" -> HistoricProcessInstanceQueryDtoSortingInner.SortByEnum.BUSINESS_KEY
    "startTime" -> HistoricProcessInstanceQueryDtoSortingInner.SortByEnum.START_TIME
    "endTime" -> HistoricProcessInstanceQueryDtoSortingInner.SortByEnum.END_TIME
    "duration" -> HistoricProcessInstanceQueryDtoSortingInner.SortByEnum.DURATION
    else -> {
      logger.warn { "query property ${this@toHistoricProcessInstanceSorting.property} is not supported for sorting" }
      null
    }
  })

fun QueryOrderingProperty.toExecutionSorting(): ExecutionQueryDtoSortingInner = ExecutionQueryDtoSortingInner()
  .sortOrder(if (this.direction == SortDirection.DESC) ExecutionQueryDtoSortingInner.SortOrderEnum.DESC else ExecutionQueryDtoSortingInner.SortOrderEnum.ASC)
  .sortBy(when (this@toExecutionSorting.property) {
    "instanceId" -> ExecutionQueryDtoSortingInner.SortByEnum.INSTANCE_ID
    "definitionId" -> ExecutionQueryDtoSortingInner.SortByEnum.DEFINITION_ID
    "definitionKey" -> ExecutionQueryDtoSortingInner.SortByEnum.DEFINITION_KEY
    "tenantId" -> ExecutionQueryDtoSortingInner.SortByEnum.TENANT_ID
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
        Relation.TASK -> TaskQueryDtoSortingInner.SortByEnum.TASK_VARIABLE
        Relation.CASE_INSTANCE -> TaskQueryDtoSortingInner.SortByEnum.CASE_INSTANCE_VARIABLE
        Relation.CASE_EXECUTION -> TaskQueryDtoSortingInner.SortByEnum.CASE_EXECUTION_VARIABLE
        Relation.EXECUTION -> TaskQueryDtoSortingInner.SortByEnum.EXECUTION_VARIABLE
        Relation.PROCESS_INSTANCE -> TaskQueryDtoSortingInner.SortByEnum.PROCESS_VARIABLE
      }
    }
  } else dtoSorting.apply {
    this.sortBy = TaskQueryDtoSortingInner.SortByEnum.fromValue(this@toTaskSorting.property)
  }
}

/**
 * Mapping for ordering properties for the extern task query.
 */
fun QueryOrderingProperty.toExternalTaskSorting(): ExternalTaskQueryDtoSortingInner =
  ExternalTaskQueryDtoSortingInner()
    .sortOrder(if (this.direction == SortDirection.DESC) ExternalTaskQueryDtoSortingInner.SortOrderEnum.DESC else ExternalTaskQueryDtoSortingInner.SortOrderEnum.ASC)
    .sortBy(ExternalTaskQueryDtoSortingInner.SortByEnum.fromValue(this@toExternalTaskSorting.property))

/**
 * Extension function to map a java.time.OffsetDateTime to a java.util.Date.
 */
fun OffsetDateTime?.toDate() = this?.let { Date.from(it.toInstant()) }

/**
 * Extension function to map a java.util.Date to a java.time.OffsetDateTime.
 */
fun Date?.toOffsetDateTime() = this?.let { OffsetDateTime.ofInstant(it.toInstant(), ZoneOffset.UTC) }


/**
 * Extension function to map the DTO retrieved from the REST API as a response to the one used as request.
 */
fun TaskWithAttachmentAndCommentDto.toTaskDto() = TaskDto()
  .id(this.id)
  .name(this.name)
  .assignee(this.assignee)
  .owner(this.owner)
  .created(this.created)
  .due(this.due)
  .followUp(this.followUp)
  .delegationState(when (this.delegationState) {
    TaskWithAttachmentAndCommentDto.DelegationStateEnum.PENDING -> TaskDto.DelegationStateEnum.PENDING
    TaskWithAttachmentAndCommentDto.DelegationStateEnum.RESOLVED -> TaskDto.DelegationStateEnum.RESOLVED
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
  .suspended(this.suspended)
  .formKey(this.formKey)
  .tenantId(this.tenantId)
