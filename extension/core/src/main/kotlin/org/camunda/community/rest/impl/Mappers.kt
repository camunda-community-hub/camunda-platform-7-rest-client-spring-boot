package org.camunda.community.rest.impl

import io.github.oshai.kotlinlogging.KotlinLogging
import org.camunda.bpm.engine.runtime.Execution
import org.camunda.bpm.engine.runtime.MessageCorrelationResultType
import org.camunda.bpm.engine.runtime.MessageCorrelationResultWithVariables
import org.camunda.bpm.engine.runtime.ProcessInstance
import org.camunda.bpm.engine.task.DelegationState
import org.camunda.bpm.engine.task.Task
import org.camunda.bpm.engine.variable.VariableMap
import org.camunda.bpm.engine.variable.Variables
import org.camunda.community.rest.adapter.*
import org.camunda.community.rest.client.model.*
import org.camunda.community.rest.impl.query.*
import org.camunda.community.rest.variables.ValueMapper
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.*

private val logger = KotlinLogging.logger {}

fun Task.toDto(): TaskDto = TaskDto()
  .id(this.id)
  .name(this.name)
  .assignee(this.assignee)
  .owner(this.owner)
  .created(this.createTime.toOffsetDateTime())
  .due(this.dueDate.toOffsetDateTime())
  .followUp(this.followUpDate.toOffsetDateTime())
  .delegationState(
    when (this.delegationState) {
      DelegationState.PENDING -> TaskDto.DelegationStateEnum.PENDING
      DelegationState.RESOLVED -> TaskDto.DelegationStateEnum.RESOLVED
      else -> null
    }
  )
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
  .sortBy(
    when (this@toProcessInstanceSorting.property) {
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

fun QueryOrderingProperty.toHistoricProcessInstanceSorting(): HistoricProcessInstanceQueryDtoSortingInner =
  HistoricProcessInstanceQueryDtoSortingInner()
    .sortOrder(if (this.direction == SortDirection.DESC) HistoricProcessInstanceQueryDtoSortingInner.SortOrderEnum.DESC else HistoricProcessInstanceQueryDtoSortingInner.SortOrderEnum.ASC)
    .sortBy(
      when (this@toHistoricProcessInstanceSorting.property) {
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
  .sortBy(
    when (this@toExecutionSorting.property) {
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
 * Convert the query operator to its REST representation.
 */
fun QueryOperator.toRestOperator() = when (this) {
  QueryOperator.EQUALS -> VariableQueryParameterDto.OperatorEnum.EQ
  QueryOperator.GREATER_THAN -> VariableQueryParameterDto.OperatorEnum.GT
  QueryOperator.GREATER_THAN_OR_EQUAL -> VariableQueryParameterDto.OperatorEnum.GTEQ
  QueryOperator.LESS_THAN -> VariableQueryParameterDto.OperatorEnum.LT
  QueryOperator.LESS_THAN_OR_EQUAL -> VariableQueryParameterDto.OperatorEnum.LTEQ
  QueryOperator.LIKE -> VariableQueryParameterDto.OperatorEnum.LIKE
  QueryOperator.NOT_EQUALS -> VariableQueryParameterDto.OperatorEnum.NEQ
  QueryOperator.NOT_LIKE -> VariableQueryParameterDto.OperatorEnum.NOT_LIKE
}

/**
 * Convert a list of query variable values to DTOs.
 */
fun List<QueryVariableValue>.toDto() = if (this.isEmpty()) {
  null
} else {
  this.map { it.toDto() }
}

/**
 * Converts a query variable value to DTO.
 */
fun QueryVariableValue.toDto(): VariableQueryParameterDto = VariableQueryParameterDto()
  .name(this.name)
  .value(this.value)
  .operator(this.operator.toRestOperator())

/**
 * Extension function to map a java.time.OffsetDateTime to a java.util.Date.
 */
fun OffsetDateTime?.toDate() = this?.let { Date.from(it.toInstant()) }

/**
 * Extension function to map a java.time.OffsetDateTime to a java.util.Date.
 */
fun OffsetDateTime.toRequiredDate() = this.let { Date.from(it.toInstant()) }

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
  .delegationState(
    when (this.delegationState) {
      TaskWithAttachmentAndCommentDto.DelegationStateEnum.PENDING -> TaskDto.DelegationStateEnum.PENDING
      TaskWithAttachmentAndCommentDto.DelegationStateEnum.RESOLVED -> TaskDto.DelegationStateEnum.RESOLVED
      else -> null
    }
  )
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

/**
 * Create result from DTO.
 * @param valueMapper to to map variable values.
 */
fun MessageCorrelationResultWithVariableDto.fromDto(valueMapper: ValueMapper, deserializeValues: Boolean = true): MessageCorrelationResultWithVariables {

  val execution: ExecutionDto? by lazy { this.execution }
  val processInstance: ProcessInstanceDto? by lazy { this.processInstance }
  val resultType by lazy { this.resultType }
  val variables: MutableMap<String, VariableValueDto>? by lazy { this.variables }

  return object : MessageCorrelationResultWithVariables {
    override fun getExecution(): Execution? = if (execution != null) ExecutionAdapter(ExecutionBean.fromExecutionDto(execution!!)) else null
    override fun getProcessInstance(): ProcessInstance? = if (processInstance != null) ProcessInstanceAdapter(
      instanceBean = InstanceBean.fromProcessInstanceDto(
        processInstance!!
      )
    ) else null
    override fun getResultType(): MessageCorrelationResultType = when (resultType) {
      MessageCorrelationResultWithVariableDto.ResultTypeEnum.EXECUTION -> MessageCorrelationResultType.Execution
      MessageCorrelationResultWithVariableDto.ResultTypeEnum.PROCESS_DEFINITION -> MessageCorrelationResultType.ProcessDefinition
      null -> throw IllegalArgumentException("Result type should not be null")
    }
    override fun getVariables(): VariableMap? = if (variables != null) valueMapper.mapDtos(variables!!.toMap(), deserializeValues) else Variables.createVariables()
  }
}
