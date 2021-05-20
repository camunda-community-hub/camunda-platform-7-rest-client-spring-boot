package org.camunda.bpm.extension.rest.impl.query

import org.camunda.bpm.engine.ProcessEngineException
import org.camunda.bpm.engine.impl.ProcessInstanceQueryImpl
import org.camunda.bpm.engine.impl.QueryVariableValue
import org.camunda.bpm.engine.impl.TaskQueryVariableValue
import org.camunda.bpm.engine.impl.persistence.entity.SuspensionState
import org.camunda.bpm.engine.rest.dto.ConditionQueryParameterDto
import org.camunda.bpm.engine.rest.dto.ConditionQueryParameterDto.*
import org.camunda.bpm.engine.rest.dto.VariableQueryParameterDto
import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceQueryDto
import org.camunda.bpm.engine.runtime.ProcessInstance
import org.camunda.bpm.extension.rest.adapter.InstanceBean
import org.camunda.bpm.extension.rest.adapter.ProcessInstanceAdapter
import org.camunda.bpm.extension.rest.client.RuntimeServiceClient

/**
 * Implementation of the process instance query.
 */
class DelegatingProcessInstanceQuery(private val runtimeServiceClient: RuntimeServiceClient) : ProcessInstanceQueryImpl() {

  override fun list(): List<ProcessInstance> {
    val instances = runtimeServiceClient.getProcessInstances(fillQueryDto(), this.firstResult, this.maxResults)
    return instances.map {
      ProcessInstanceAdapter(InstanceBean.fromProcessInstanceDto(it))
    }
  }

  override fun listPage(firstResult: Int, maxResults: Int): List<ProcessInstance> {
    val instances = runtimeServiceClient.getProcessInstances(fillQueryDto(), firstResult, maxResults)
    return instances.map {
      ProcessInstanceAdapter(InstanceBean.fromProcessInstanceDto(it))
    }
  }

  override fun listIds(): List<String> {
    return list().map { it.processInstanceId }
  }

  override fun unlimitedList(): List<ProcessInstance> {
    // FIXME: best approximation so far.
    return list()
  }

  override fun count(): Long {
    val count = runtimeServiceClient.countProcessInstances(fillQueryDto(), firstResult, maxResults)
    return count.count
  }

  override fun singleResult(): ProcessInstance? {
    val results = list()
    return when {
      results.size == 1 -> results[0]
      results.size > 1 -> throw ProcessEngineException("Query return " + results.size.toString() + " results instead of max 1")
      else -> null
    }
  }

  private fun fillQueryDto(): ProcessInstanceQueryDto {

    val query = ProcessInstanceQueryDto()

    query.businessKey = this.businessKey
    query.businessKeyLike = this.businessKeyLike

    query.isActive = this.suspensionState == SuspensionState.ACTIVE
    query.isSuspended = this.suspensionState == SuspensionState.SUSPENDED

    query.deploymentId = this.deploymentId

    query.isWithIncident = this.withIncident
    query.incidentId = this.incidentId
    query.incidentMessage = this.incidentMessage
    query.incidentMessageLike = this.incidentMessageLike
    query.incidentType = this.incidentType

    query.caseInstanceId = this.caseInstanceId
    query.subCaseInstance = this.subCaseInstanceId
    query.superCaseInstance = this.superCaseInstanceId

    query.processInstanceIds = this.processInstanceIds
    if (this.processInstanceId != null) {
      query.processInstanceIds = (query.processInstanceIds ?: listOf(this.processInstanceId)).toMutableSet().plus(this.processInstanceId)
    }
    query.subProcessInstance = this.subProcessInstanceId
    query.superProcessInstance = this.superProcessInstanceId
    query.isRootProcessInstances = this.isRootProcessInstances
    query.isLeafProcessInstances = this.isLeafProcessInstances

    query.processDefinitionKey = this.processDefinitionKey
    query.processDefinitionId = this.processDefinitionId
    query.isProcessDefinitionWithoutTenantId = this.isProcessDefinitionWithoutTenantId

    if (this.processDefinitionKeys != null) {
      query.setProcessDefinitionKeyIn(this.processDefinitionKeys.toList())
    }
    if (this.processDefinitionKeyNotIn != null) {
      query.processDefinitionKeyNotIn = this.processDefinitionKeyNotIn.toList()
    }
    if (this.tenantIds != null) {
      query.tenantIdIn = this.tenantIds.toList()
    }
    query.isWithoutTenantId = !this.isTenantIdSet

    if (this.activityIds != null) {
      query.setActivityIdIn(this.activityIds.toList())
    }
    query.variables = this.queryVariableValues.map { it.toDto() }
    query.isVariableNamesIgnoreCase = this.isVariableNamesIgnoreCase
    query.isVariableValuesIgnoreCase = this.isVariableValuesIgnoreCase
    return query
  }
}

/**
 * Camunda constructor for the DTO is strange, but we use it here.
 */
fun QueryVariableValue.toDto(): VariableQueryParameterDto {
  // the task query variable value constructor parameter four and five reflect "isTaskVariable" and "isProcessVariable".
  // the query is saving the scoping information in the local flag actually always passing "true" to it.
  // since we want to query for the process variables, we invert the isLocal flag
  // see QueryVariableValue class and AbstractVariableQueryImpl#addVariable
  return VariableQueryParameterDto(TaskQueryVariableValue(this.name, this.value, this.operator, !this.isLocal, true))
}
