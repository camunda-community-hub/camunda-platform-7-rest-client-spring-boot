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
package org.camunda.bpm.extension.rest.impl

import com.fasterxml.jackson.databind.ObjectMapper
import org.camunda.bpm.engine.ProcessEngine
import org.camunda.bpm.engine.runtime.ProcessInstance
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery
import org.camunda.bpm.engine.runtime.SignalEventReceivedBuilder
import org.camunda.bpm.engine.variable.VariableMap
import org.camunda.bpm.engine.variable.value.TypedValue
import org.camunda.bpm.extension.rest.adapter.AbstractRuntimeServiceAdapter
import org.camunda.bpm.extension.rest.adapter.InstanceBean
import org.camunda.bpm.extension.rest.adapter.ProcessInstanceAdapter
import org.camunda.bpm.extension.rest.client.api.*
import org.camunda.bpm.extension.rest.client.model.ExecutionTriggerDto
import org.camunda.bpm.extension.rest.client.model.PatchVariablesDto
import org.camunda.bpm.extension.rest.client.model.StartProcessInstanceDto
import org.camunda.bpm.extension.rest.impl.builder.DelegatingMessageCorrelationBuilder
import org.camunda.bpm.extension.rest.impl.builder.DelegatingSignalEventReceivedBuilder
import org.camunda.bpm.extension.rest.impl.query.DelegatingProcessInstanceQuery
import org.camunda.bpm.extension.rest.variables.ValueMapper
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
  processEngine: ProcessEngine,
  objectMapper: ObjectMapper
) : AbstractRuntimeServiceAdapter() {

  private val valueMapper: ValueMapper = ValueMapper(processEngine, objectMapper)

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
    return executionApiClient.getLocalExecutionVariables(executionId, true).body!!
      .mapValues { it.value.value }
      .toMutableMap()
  }

  override fun getVariablesLocal(executionId: String, variableNames: MutableCollection<String>): MutableMap<String, Any?> {
    return executionApiClient.getLocalExecutionVariables(executionId, true).body!!
      .filter { variableNames.contains(it.key) }
      .mapValues { it.value.value }
      .toMutableMap()
  }

  override fun getVariableLocal(executionId: String, variableName: String): Any? {
    val dto = executionApiClient.getLocalExecutionVariable(executionId, variableName, true).body!!
    return dto.value
  }

  override fun getVariablesLocalTyped(executionId: String): VariableMap =
    getVariablesLocalTyped(executionId, true)

  override fun getVariablesLocalTyped(executionId: String, deserializeValues: Boolean): VariableMap {
    val variables = executionApiClient.getLocalExecutionVariables(executionId, deserializeValues).body!!
    return valueMapper.mapDtos(variables, deserializeValues)
  }

  override fun getVariablesLocalTyped(
    executionId: String,
    variableNames: MutableCollection<String>,
    deserializeValues: Boolean
  ): VariableMap {
    val variables = executionApiClient
      .getLocalExecutionVariables(executionId, deserializeValues).body!!
      .filter { variableNames.contains(it.key) }
    return valueMapper.mapDtos(variables, deserializeValues)
  }

  override fun <T : TypedValue> getVariableLocalTyped(executionId: String, variableName: String): T? =
    getVariableLocalTyped(executionId, variableName, true)

  override fun <T : TypedValue> getVariableLocalTyped(executionId: String, variableName: String, deserializeValue: Boolean): T? {
    val dto = executionApiClient
      .getLocalExecutionVariable(executionId, variableName, deserializeValue).body!!
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
    return processInstanceApiClient
      .getProcessInstanceVariables(executionId, true).body!!
      .mapValues { it.value.value }
      .toMutableMap()
  }

  override fun getVariables(executionId: String, variableNames: MutableCollection<String>): MutableMap<String, Any?> {
    return processInstanceApiClient
      .getProcessInstanceVariables(executionId, true).body!!
      .filter { variableNames.contains(it.key) }
      .mapValues { it.value.value }
      .toMutableMap()
  }

  override fun <T : TypedValue> getVariableTyped(executionId: String, variableName: String): T? {
    return getVariableTyped(executionId, variableName, true)
  }

  override fun <T : TypedValue> getVariableTyped(executionId: String, variableName: String, deserializeValue: Boolean): T? {
    val dto = processInstanceApiClient.getProcessInstanceVariable(executionId, variableName, deserializeValue).body!!
    return valueMapper.mapDto(dto, deserializeValue)
  }

  override fun getVariablesTyped(executionId: String): VariableMap {
    val variables = processInstanceApiClient.getProcessInstanceVariables(executionId, true).body!!
    return valueMapper.mapDtos(variables)
  }

  override fun getVariablesTyped(executionId: String, deserializeValues: Boolean): VariableMap {
    val variables = processInstanceApiClient.getProcessInstanceVariables(executionId, deserializeValues).body!!
    return valueMapper.mapDtos(variables, deserializeValues)
  }

  override fun getVariablesTyped(executionId: String, variableNames: MutableCollection<String>, deserializeValues: Boolean): VariableMap {
    val variables = processInstanceApiClient.getProcessInstanceVariables(executionId, deserializeValues).body!!.filter { variableNames.contains(it.key) }
    return valueMapper.mapDtos(variables)
  }

  override fun getVariable(executionId: String, variableName: String): Any {
    return processInstanceApiClient.getProcessInstanceVariable(executionId, variableName, true).body!!.value
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
}


