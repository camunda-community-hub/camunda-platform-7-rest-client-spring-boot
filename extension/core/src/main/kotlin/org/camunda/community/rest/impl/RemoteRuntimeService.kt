/*-
 * #%L
 * camunda-platform-7-rest-client-spring-boot
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
package org.camunda.community.rest.impl

import com.fasterxml.jackson.databind.ObjectMapper
import org.camunda.bpm.engine.ProcessEngine
import org.camunda.bpm.engine.batch.Batch
import org.camunda.bpm.engine.history.HistoricProcessInstanceQuery
import org.camunda.bpm.engine.runtime.*
import org.camunda.bpm.engine.variable.VariableMap
import org.camunda.bpm.engine.variable.value.TypedValue
import org.camunda.community.rest.adapter.*
import org.camunda.community.rest.client.api.*
import org.camunda.community.rest.client.model.*
import org.camunda.community.rest.impl.builder.DelegatingMessageCorrelationBuilder
import org.camunda.community.rest.impl.builder.DelegatingSignalEventReceivedBuilder
import org.camunda.community.rest.impl.builder.RemoteUpdateProcessInstanceSuspensionStateSelectBuilder
import org.camunda.community.rest.impl.query.DelegatingEventSubscriptionQuery
import org.camunda.community.rest.impl.query.DelegatingHistoricProcessInstanceQuery
import org.camunda.community.rest.impl.query.DelegatingIncidentQuery
import org.camunda.community.rest.impl.query.DelegatingProcessInstanceQuery
import org.camunda.community.rest.variables.CustomValueMapper
import org.camunda.community.rest.variables.ValueMapper
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

/**
 * Remote implementation of Camunda Core RuntimeService API, delegating
 * all request over HTTP to a remote Camunda Engine.
 */
@Component
@Qualifier("remote")
class RemoteRuntimeService(
  private val processInstanceApiClient: ProcessInstanceApiClient,
  private val processDefinitionApiClient: ProcessDefinitionApiClient,
  private val messageApiClient: MessageApiClient,
  private val signalApiClient: SignalApiClient,
  private val executionApiClient: ExecutionApiClient,
  private val incidentApiClient: IncidentApiClient,
  private val variableInstanceApiClient: VariableInstanceApiClient,
  private val eventSubscriptionApiClient: EventSubscriptionApiClient,
  customValueMapper: List<CustomValueMapper>,
  processEngine: ProcessEngine,
  objectMapper: ObjectMapper
) : AbstractRuntimeServiceAdapter() {

  private val valueMapper: ValueMapper = ValueMapper(processEngine, objectMapper, customValueMapper)

  override fun correlateMessage(messageName: String) =
    doCorrelateMessage(messageName = messageName)

  override fun correlateMessage(messageName: String, businessKey: String) =
    doCorrelateMessage(messageName = messageName, businessKey = businessKey)

  override fun correlateMessage(messageName: String, correlationKeys: MutableMap<String, Any>) =
    doCorrelateMessage(messageName = messageName, correlationKeys = correlationKeys)

  override fun correlateMessage(messageName: String, correlationKeys: MutableMap<String, Any>, processVariables: MutableMap<String, Any>) =
    doCorrelateMessage(messageName = messageName, correlationKeys = correlationKeys, processVariables = processVariables)

  override fun correlateMessage(messageName: String, businessKey: String, processVariables: MutableMap<String, Any>) =
    doCorrelateMessage(messageName = messageName, businessKey = businessKey, processVariables = processVariables)

  override fun correlateMessage(
    messageName: String,
    businessKey: String,
    correlationKeys: MutableMap<String, Any>,
    processVariables: MutableMap<String, Any>
  ) =
    doCorrelateMessage(
      messageName = messageName,
      businessKey = businessKey,
      correlationKeys = correlationKeys,
      processVariables = processVariables
    )

  override fun createMessageCorrelation(messageName: String) =
    DelegatingMessageCorrelationBuilder(
      messageName = messageName,
      messageApiClient = messageApiClient,
      valueMapper = valueMapper
    )

  /**
   * Null-safe version of message correlate.
   */
  private fun doCorrelateMessage(
    messageName: String,
    businessKey: String? = null,
    correlationKeys: MutableMap<String, Any>? = null,
    processVariables: MutableMap<String, Any>? = null
  ) {

    val builder = DelegatingMessageCorrelationBuilder(
      messageName = messageName,
      messageApiClient = messageApiClient,
      valueMapper = valueMapper
    )

    if (businessKey != null) {
      builder.processInstanceBusinessKey(businessKey)
    }
    if (processVariables != null) {
      builder.setVariables(processVariables)
    }
    if (correlationKeys != null) {
      builder.setCorrelationKeys(correlationKeys)
    }

    builder.correlate()
  }

  override fun startProcessInstanceByKey(processDefinitionKey: String) =
    doStartProcessInstanceByKey(processDefinitionKey)

  override fun startProcessInstanceByKey(processDefinitionKey: String, businessKey: String) =
    doStartProcessInstanceByKey(processDefinitionKey, businessKey)

  override fun startProcessInstanceByKey(processDefinitionKey: String, businessKey: String, caseInstanceId: String) =
    doStartProcessInstanceByKey(processDefinitionKey, businessKey, caseInstanceId)

  override fun startProcessInstanceByKey(processDefinitionKey: String, variables: MutableMap<String, Any>) =
    doStartProcessInstanceByKey(processDefinitionKey, variables = variables)

  override fun startProcessInstanceByKey(processDefinitionKey: String, businessKey: String, variables: MutableMap<String, Any>) =
    doStartProcessInstanceByKey(processDefinitionKey, businessKey, variables = variables)

  override fun startProcessInstanceByKey(
    processDefinitionKey: String,
    businessKey: String,
    caseInstanceId: String,
    variables: MutableMap<String, Any>
  ) =
    doStartProcessInstanceByKey(processDefinitionKey, businessKey, caseInstanceId, variables)

  /**
   * Create process instance start DTO.
   */
  private fun createStartProcessInstanceDto(
    businessKey: String? = null,
    caseInstanceId: String? = null,
    variables: MutableMap<String, Any>? = null
  ) = StartProcessInstanceDto().apply {
    if (businessKey != null) {
      this.businessKey = businessKey
    }
    if (caseInstanceId != null) {
      this.caseInstanceId = caseInstanceId
    }
    if (variables != null) {
      this.variables = valueMapper.mapValues(variables)
    }
  }

  /**
   * Null-safe version of starter function.
   */
  private fun doStartProcessInstanceByKey(
    processDefinitionKey: String,
    businessKey: String? = null,
    caseInstanceId: String? = null,
    variables: MutableMap<String, Any>? = null
  ): ProcessInstance {
    val instance = this.processDefinitionApiClient.startProcessInstanceByKey(
      processDefinitionKey, createStartProcessInstanceDto(
        businessKey = businessKey,
        caseInstanceId = caseInstanceId,
        variables = variables
      )
    )
    return ProcessInstanceAdapter(instanceBean = InstanceBean.fromProcessInstanceDto(instance.body!!))
  }

  override fun startProcessInstanceById(processDefinitionId: String) =
    doStartProcessInstanceById(processDefinitionId)

  override fun startProcessInstanceById(processDefinitionId: String, businessKey: String) =
    doStartProcessInstanceById(processDefinitionId, businessKey)

  override fun startProcessInstanceById(processDefinitionId: String, businessKey: String, caseInstanceId: String) =
    doStartProcessInstanceById(processDefinitionId, businessKey, caseInstanceId)

  override fun startProcessInstanceById(processDefinitionId: String, variables: MutableMap<String, Any>) =
    doStartProcessInstanceById(processDefinitionId, variables = variables)

  override fun startProcessInstanceById(processDefinitionId: String, businessKey: String, variables: MutableMap<String, Any>) =
    doStartProcessInstanceById(processDefinitionId, businessKey, variables = variables)

  override fun startProcessInstanceById(
    processDefinitionId: String,
    businessKey: String,
    caseInstanceId: String,
    variables: MutableMap<String, Any>
  ) =
    doStartProcessInstanceById(processDefinitionId, businessKey, caseInstanceId, variables)

  /**
   * Null-safe version of startById.
   */
  private fun doStartProcessInstanceById(
    processDefinitionId: String,
    businessKey: String? = null,
    caseInstanceId: String? = null,
    variables: MutableMap<String, Any>? = null
  ): ProcessInstance {
    val instance = this.processDefinitionApiClient.startProcessInstance(
      processDefinitionId, createStartProcessInstanceDto(
        businessKey = businessKey,
        caseInstanceId = caseInstanceId,
        variables = variables
      )
    )
    return ProcessInstanceAdapter(instanceBean = InstanceBean.fromProcessInstanceDto(instance.body!!))
  }

  override fun signal(executionId: String) =
    doSignal(executionId)

  override fun signal(executionId: String, processVariables: MutableMap<String, Any>) =
    doSignal(executionId, processVariables = processVariables)

  override fun signal(executionId: String, signalName: String, signalData: Any?, processVariables: MutableMap<String, Any>) =
    doSignal(executionId, signalName, signalData, processVariables)

  @Suppress("UNUSED_PARAMETER")
  private fun doSignal(
    executionId: String,
    signalName: String? = null,
    signalData: Any? = null,
    processVariables: MutableMap<String, Any>? = null
  ) {
    val trigger = ExecutionTriggerDto().apply {
      if (processVariables != null) {
        this.variables = valueMapper.mapValues(processVariables)
      }
    }
    executionApiClient.signalExecution(executionId, trigger)
  }

  override fun signalEventReceived(signalName: String) =
    doSignalEventReceived(signalName)

  override fun signalEventReceived(signalName: String, processVariables: MutableMap<String, Any>) =
    doSignalEventReceived(signalName, variables = processVariables)

  override fun signalEventReceived(signalName: String, executionId: String) =
    doSignalEventReceived(signalName, executionId)

  override fun signalEventReceived(signalName: String, executionId: String, processVariables: MutableMap<String, Any>) =
    doSignalEventReceived(signalName, executionId, processVariables)

  override fun createSignalEvent(signalName: String): SignalEventReceivedBuilder =
    DelegatingSignalEventReceivedBuilder(signalName, signalApiClient, valueMapper)

  private fun doSignalEventReceived(signalName: String, executionId: String? = null, variables: MutableMap<String, Any>? = null) {
    val builder = DelegatingSignalEventReceivedBuilder(signalName, signalApiClient, valueMapper)
    if (executionId != null) {
      builder.executionId(executionId)
    }
    if (variables != null) {
      builder.setVariables(variables)
    }
    builder.send()
  }

  override fun getVariablesLocal(executionId: String): MutableMap<String, Any?> {
    return executionApiClient.getLocalExecutionVariables(executionId, false).body!!
      .mapValues { valueMapper.mapDto<TypedValue>(it.value, true)?.value }
      .toMutableMap()
  }

  override fun getVariablesLocal(executionId: String, variableNames: MutableCollection<String>): MutableMap<String, Any?> {
    return executionApiClient.getLocalExecutionVariables(executionId, false).body!!
      .filter { variableNames.contains(it.key) }
      .mapValues { valueMapper.mapDto<TypedValue>(it.value, true)?.value }
      .toMutableMap()
  }

  override fun getVariableLocal(executionId: String, variableName: String): Any? {
    val dto = executionApiClient.getLocalExecutionVariable(executionId, variableName, false).body!!
    return valueMapper.mapDto<TypedValue>(dto, true)?.value
  }

  override fun getVariablesLocalTyped(executionId: String): VariableMap =
    getVariablesLocalTyped(executionId, true)

  override fun getVariablesLocalTyped(executionId: String, deserializeValues: Boolean): VariableMap {
    val variables = executionApiClient.getLocalExecutionVariables(executionId, false).body!!
    return valueMapper.mapDtos(variables, deserializeValues)
  }

  override fun getVariablesLocalTyped(
    executionId: String,
    variableNames: MutableCollection<String>,
    deserializeValues: Boolean
  ): VariableMap {
    val variables = executionApiClient
      .getLocalExecutionVariables(executionId, false).body!!
      .filter { variableNames.contains(it.key) }
    return valueMapper.mapDtos(variables, deserializeValues)
  }

  override fun <T : TypedValue> getVariableLocalTyped(executionId: String, variableName: String): T? =
    getVariableLocalTyped(executionId, variableName, true)

  override fun <T : TypedValue> getVariableLocalTyped(executionId: String, variableName: String, deserializeValue: Boolean): T? {
    val dto = executionApiClient
      .getLocalExecutionVariable(executionId, variableName, false).body!!
    return valueMapper.mapDto(dto, deserializeValue)
  }

  override fun removeVariablesLocal(executionId: String, variableNames: MutableCollection<String>) {
    executionApiClient
      .modifyLocalExecutionVariables(executionId, PatchVariablesDto()
        .apply {
          deletions = variableNames.toList()
        })
  }

  override fun removeVariableLocal(executionId: String, variableName: String) {
    executionApiClient.deleteLocalExecutionVariable(executionId, variableName)
  }

  override fun setVariableLocal(executionId: String, variableName: String, value: Any?) {
    executionApiClient.putLocalExecutionVariable(executionId, variableName, valueMapper.mapValue(value))
  }

  override fun setVariablesLocal(executionId: String, variables: MutableMap<String, out Any>) {
    executionApiClient.modifyLocalExecutionVariables(executionId, PatchVariablesDto().apply {
      modifications = valueMapper.mapValues(variables)
    })
  }

  override fun getVariables(executionId: String): MutableMap<String, Any?> {
    return variableInstanceApiClient
      .queryVariableInstances(null, null, false,
        VariableInstanceQueryDto().executionIdIn(listOf(executionId))
      )
      .body!!
      .associateBy { it.name }
      .mapValues { valueMapper.mapDto<TypedValue>(it.value, true)?.value }
      .toMutableMap()
  }

  override fun getVariables(executionId: String, variableNames: MutableCollection<String>): MutableMap<String, Any?> {
    return variableInstanceApiClient
      .queryVariableInstances(null, null, false,
        VariableInstanceQueryDto().executionIdIn(listOf(executionId))
      )
      .body!!
      .filter { variableNames.contains(it.name) }
      .associateBy { it.name }
      .mapValues { valueMapper.mapDto<TypedValue>(it.value, true)?.value }
      .toMutableMap()
  }

  override fun <T : TypedValue> getVariableTyped(executionId: String, variableName: String): T? {
    return getVariableTyped(executionId, variableName, true)
  }

  override fun <T : TypedValue> getVariableTyped(executionId: String, variableName: String, deserializeValue: Boolean): T? {
    val dto = variableInstanceApiClient.queryVariableInstances(null, null, false,
      VariableInstanceQueryDto().variableName(variableName).executionIdIn(listOf(executionId))
    ).body
    return if (dto.isNullOrEmpty()) null else valueMapper.mapDto(dto[0], deserializeValue)
  }

  override fun getVariablesTyped(executionId: String): VariableMap =
    getVariablesTyped(executionId = executionId, deserializeValues = true)

  override fun getVariablesTyped(executionId: String, deserializeValues: Boolean): VariableMap {
    val variables = variableInstanceApiClient.queryVariableInstances(null, null, false,
      VariableInstanceQueryDto().executionIdIn(listOf(executionId))
    ).body!!
    return valueMapper.mapDtos(
      variables
        .associateBy { it.name }
        .mapValues { VariableValueDto().type(it.value.type).value(it.value.value).valueInfo(it.value.valueInfo) },
      deserializeValues = deserializeValues
    )
  }

  override fun getVariablesTyped(executionId: String, variableNames: MutableCollection<String>, deserializeValues: Boolean): VariableMap {
    val variables = variableInstanceApiClient.queryVariableInstances(null, null, false,
      VariableInstanceQueryDto().executionIdIn(listOf(executionId))
    ).body!!
    return valueMapper.mapDtos(
      variables
        .filter { variableNames.contains(it.name) }
        .associateBy { it.name }
        .mapValues { VariableValueDto().type(it.value.type).value(it.value.value).valueInfo(it.value.valueInfo) },
      deserializeValues = deserializeValues
    )
  }

  override fun getVariable(executionId: String, variableName: String): Any? {
    return variableInstanceApiClient
      .queryVariableInstances(null, null, false,
        VariableInstanceQueryDto().executionIdIn(listOf(executionId)).variableName(variableName)
      )
      .body!!
      .map { valueMapper.mapDto<TypedValue>(it, true)?.value }
      .firstOrNull()
  }

  override fun removeVariables(executionId: String, variableNames: MutableCollection<String>) {
    processInstanceApiClient.modifyProcessInstanceVariables(executionId, PatchVariablesDto().apply {
      deletions = variableNames.toList()
    })
  }

  override fun removeVariable(executionId: String, variableName: String) {
    processInstanceApiClient.deleteProcessInstanceVariable(executionId, variableName)
  }

  override fun setVariable(executionId: String, variableName: String, value: Any?) {
    processInstanceApiClient.setProcessInstanceVariable(executionId, variableName, valueMapper.mapValue(value))
  }

  override fun setVariables(executionId: String, variables: MutableMap<String, out Any>) {
    processInstanceApiClient.modifyProcessInstanceVariables(executionId, PatchVariablesDto().apply {
      modifications = valueMapper.mapValues(variables)
    })
  }

  override fun createProcessInstanceQuery(): ProcessInstanceQuery {
    return DelegatingProcessInstanceQuery(processInstanceApiClient)
  }

  override fun activateProcessInstanceById(processInstanceId: String?) {
    processInstanceApiClient.updateSuspensionStateById(processInstanceId, SuspensionStateDto().suspended(false))
  }

  override fun activateProcessInstanceByProcessDefinitionId(processDefinitionId: String?) {
    processInstanceApiClient.updateSuspensionState(ProcessInstanceSuspensionStateDto().processDefinitionId(processDefinitionId).suspended(false))
  }

  override fun activateProcessInstanceByProcessDefinitionKey(processDefinitionKey: String?) {
    processInstanceApiClient.updateSuspensionState(ProcessInstanceSuspensionStateDto().processDefinitionKey(processDefinitionKey).suspended(false))
  }

  override fun suspendProcessInstanceById(processInstanceId: String?) {
    processInstanceApiClient.updateSuspensionStateById(processInstanceId, SuspensionStateDto().suspended(true))
  }

  override fun suspendProcessInstanceByProcessDefinitionId(processDefinitionId: String?) {
    processInstanceApiClient.updateSuspensionState(ProcessInstanceSuspensionStateDto().processDefinitionId(processDefinitionId).suspended(true))
  }

  override fun suspendProcessInstanceByProcessDefinitionKey(processDefinitionKey: String?) {
    processInstanceApiClient.updateSuspensionState(ProcessInstanceSuspensionStateDto().processDefinitionKey(processDefinitionKey).suspended(true))
  }

  override fun updateProcessInstanceSuspensionState(): UpdateProcessInstanceSuspensionStateSelectBuilder =
    RemoteUpdateProcessInstanceSuspensionStateSelectBuilder(processInstanceApiClient)

  override fun resolveIncident(incidentId: String?) {
    incidentApiClient.resolveIncident(incidentId)
  }

  override fun createIncident(incidentType: String?, executionId: String?, configuration: String?): Incident =
    IncidentAdapter(IncidentBean.fromDto(
      executionApiClient.createIncident(executionId, CreateIncidentDto().incidentType(incidentType)._configuration(configuration)).body!!
    ))

  override fun createIncident(incidentType: String?, executionId: String?, configuration: String?, message: String?): Incident =
    IncidentAdapter(IncidentBean.fromDto(
      executionApiClient.createIncident(executionId, CreateIncidentDto().incidentType(incidentType)._configuration(configuration).message(message)).body!!
    ))

  override fun createIncidentQuery() = DelegatingIncidentQuery(incidentApiClient)

  override fun setAnnotationForIncidentById(incidentId: String, annotation: String) {
    incidentApiClient.setIncidentAnnotation(incidentId, AnnotationDto().annotation(annotation))
  }

  override fun clearAnnotationForIncidentById(incidentId: String) {
    incidentApiClient.clearIncidentAnnotation(incidentId)
  }

  override fun startProcessInstanceByMessage(messageName: String): ProcessInstance =
    doStartProcessInstanceByMessage(messageName = messageName)

  override fun startProcessInstanceByMessage(messageName: String, businessKey: String): ProcessInstance =
    doStartProcessInstanceByMessage(messageName = messageName, businessKey = businessKey)

  override fun startProcessInstanceByMessage(messageName: String, processVariables: MutableMap<String, Any>): ProcessInstance =
    doStartProcessInstanceByMessage(messageName = messageName, variables = processVariables)

  override fun startProcessInstanceByMessage(messageName: String, businessKey: String, processVariables: MutableMap<String, Any>): ProcessInstance =
    doStartProcessInstanceByMessage(messageName = messageName, businessKey = businessKey, variables = processVariables)

  /**
   * Null-safe version of starter function.
   */
  private fun doStartProcessInstanceByMessage(
    messageName: String,
    businessKey: String? = null,
    variables: MutableMap<String, Any>? = null
  ): ProcessInstance =
    createMessageCorrelation(messageName).apply {
      if (businessKey != null) {
        this.processInstanceBusinessKey(businessKey)
      }
      if (variables != null) {
        this.setVariables(variables)
      }
    }.correlateStartMessage()

  override fun deleteProcessInstancesAsync(processInstanceIds: MutableList<String>?, processInstanceQuery: ProcessInstanceQuery?, historicProcessInstanceQuery: HistoricProcessInstanceQuery?, deleteReason: String?, skipCustomListeners: Boolean, skipSubprocesses: Boolean): Batch =
    BatchAdapter(BatchBean.fromDto(processInstanceApiClient.deleteProcessInstancesAsyncOperation(
      DeleteProcessInstancesDto()
        .processInstanceIds(processInstanceIds)
        .processInstanceQuery(processInstanceQuery?.toDto())
        .historicProcessInstanceQuery(historicProcessInstanceQuery?.toDto())
        .deleteReason(deleteReason)
        .skipSubprocesses(skipSubprocesses)
        .skipCustomListeners(skipCustomListeners)
    ).body!!))

  override fun deleteProcessInstancesAsync(processInstanceIds: MutableList<String>?, processInstanceQuery: ProcessInstanceQuery?, deleteReason: String?) =
    deleteProcessInstancesAsync(processInstanceIds = processInstanceIds, processInstanceQuery = processInstanceQuery, historicProcessInstanceQuery = null,
      deleteReason = deleteReason, skipCustomListeners = false, skipSubprocesses = false)

  override fun deleteProcessInstancesAsync(processInstanceIds: MutableList<String>?, processInstanceQuery: ProcessInstanceQuery?, deleteReason: String?, skipCustomListeners: Boolean) =
    deleteProcessInstancesAsync(processInstanceIds = processInstanceIds, processInstanceQuery = processInstanceQuery, historicProcessInstanceQuery = null,
      deleteReason = deleteReason, skipCustomListeners = skipCustomListeners, skipSubprocesses = false)

  override fun deleteProcessInstancesAsync(processInstanceIds: MutableList<String>?, processInstanceQuery: ProcessInstanceQuery?, deleteReason: String?, skipCustomListeners: Boolean, skipSubprocesses: Boolean) =
    deleteProcessInstancesAsync(processInstanceIds = processInstanceIds, processInstanceQuery = processInstanceQuery, historicProcessInstanceQuery = null,
      deleteReason = deleteReason, skipCustomListeners = skipCustomListeners, skipSubprocesses = skipSubprocesses)

  override fun deleteProcessInstancesAsync(processInstanceQuery: ProcessInstanceQuery?, deleteReason: String?) =
    deleteProcessInstancesAsync(processInstanceIds = null, processInstanceQuery = processInstanceQuery, historicProcessInstanceQuery = null,
      deleteReason = deleteReason, skipCustomListeners = false, skipSubprocesses = false)

  override fun deleteProcessInstancesAsync(processInstanceIds: MutableList<String>?, deleteReason: String?) =
    deleteProcessInstancesAsync(processInstanceIds = processInstanceIds, processInstanceQuery = null, historicProcessInstanceQuery = null,
      deleteReason = deleteReason, skipCustomListeners = false, skipSubprocesses = false)

  override fun deleteProcessInstanceIfExists(processInstanceId: String?, deleteReason: String?, skipCustomListeners: Boolean, externallyTerminated: Boolean, skipIoMappings: Boolean, skipSubprocesses: Boolean) {
    processInstanceApiClient.deleteProcessInstance(processInstanceId, skipCustomListeners, skipIoMappings, skipSubprocesses, false)
  }

  override fun deleteProcessInstances(processInstanceIds: MutableList<String>?, deleteReason: String?, skipCustomListeners: Boolean, externallyTerminated: Boolean) {
    processInstanceIds?.forEach {
      deleteProcessInstance(processInstanceId = it, deleteReason = deleteReason, skipCustomListeners = skipCustomListeners, externallyTerminated = externallyTerminated)
    }
  }

  override fun deleteProcessInstances(processInstanceIds: MutableList<String>?, deleteReason: String?, skipCustomListeners: Boolean, externallyTerminated: Boolean, skipSubprocesses: Boolean) {
    processInstanceIds?.forEach {
      deleteProcessInstance(processInstanceId = it, deleteReason = deleteReason, skipCustomListeners = skipCustomListeners,
        skipSubprocesses = skipSubprocesses, externallyTerminated = externallyTerminated, skipIoMappings = false)
    }
  }

  override fun deleteProcessInstancesIfExists(processInstanceIds: MutableList<String>?, deleteReason: String?, skipCustomListeners: Boolean, externallyTerminated: Boolean, skipSubprocesses: Boolean) {
    processInstanceIds?.forEach {
      deleteProcessInstanceIfExists(processInstanceId = it, deleteReason = deleteReason, skipCustomListeners = skipCustomListeners,
        skipSubprocesses = skipSubprocesses, externallyTerminated = externallyTerminated, skipIoMappings = false)
    }
  }

  override fun deleteProcessInstance(processInstanceId: String?, deleteReason: String?) {
    deleteProcessInstance(processInstanceId = processInstanceId, deleteReason = deleteReason, skipCustomListeners = false)
  }

  override fun deleteProcessInstance(processInstanceId: String?, deleteReason: String?, skipCustomListeners: Boolean) {
    deleteProcessInstance(processInstanceId = processInstanceId, deleteReason = deleteReason, skipCustomListeners = skipCustomListeners, externallyTerminated = false)
  }

  override fun deleteProcessInstance(processInstanceId: String?, deleteReason: String?, skipCustomListeners: Boolean, externallyTerminated: Boolean) {
    deleteProcessInstance(processInstanceId = processInstanceId, deleteReason = deleteReason, skipCustomListeners = skipCustomListeners,
      externallyTerminated = externallyTerminated, skipIoMappings = false)
  }

  override fun deleteProcessInstance(processInstanceId: String?, deleteReason: String?, skipCustomListeners: Boolean, externallyTerminated: Boolean, skipIoMappings: Boolean) {
    deleteProcessInstance(processInstanceId = processInstanceId, deleteReason = deleteReason, skipCustomListeners = skipCustomListeners,
      externallyTerminated = externallyTerminated, skipIoMappings = skipIoMappings, skipSubprocesses = false)
  }

  override fun deleteProcessInstance(processInstanceId: String?, deleteReason: String?, skipCustomListeners: Boolean, externallyTerminated: Boolean, skipIoMappings: Boolean, skipSubprocesses: Boolean) {
    processInstanceApiClient.deleteProcessInstance(processInstanceId, skipCustomListeners, skipIoMappings, skipCustomListeners, true)
  }

  override fun createEventSubscriptionQuery() = DelegatingEventSubscriptionQuery(eventSubscriptionApiClient)

  private fun ProcessInstanceQuery.toDto() = if (this is DelegatingProcessInstanceQuery) this.fillQueryDto() else throw IllegalArgumentException()

  private fun HistoricProcessInstanceQuery.toDto() = if (this is DelegatingHistoricProcessInstanceQuery) this.fillQueryDto() else throw IllegalArgumentException()

}


