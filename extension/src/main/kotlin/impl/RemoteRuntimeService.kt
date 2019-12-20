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
import impl.CompleteTaskDto
import org.camunda.bpm.engine.ProcessEngine
import org.camunda.bpm.engine.rest.dto.PatchVariablesDto
import org.camunda.bpm.engine.rest.dto.runtime.ExecutionTriggerDto
import org.camunda.bpm.engine.rest.dto.runtime.StartProcessInstanceDto
import org.camunda.bpm.engine.runtime.ProcessInstance
import org.camunda.bpm.engine.runtime.SignalEventReceivedBuilder
import org.camunda.bpm.engine.variable.VariableMap
import org.camunda.bpm.engine.variable.value.TypedValue
import org.camunda.bpm.extension.rest.adapter.AbstractRuntimeServiceAdapter
import org.camunda.bpm.extension.rest.adapter.InstanceBean
import org.camunda.bpm.extension.rest.adapter.ProcessInstanceAdapter
import org.camunda.bpm.extension.rest.client.RuntimeServiceClient
import org.camunda.bpm.extension.rest.impl.builder.DelegatingMessageCorrelationBuilder
import org.camunda.bpm.extension.rest.impl.builder.DelegatingSignalEventReceivedBuilder
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
  private val runtimeServiceClient: RuntimeServiceClient,
  processEngine: ProcessEngine,
  objectMapper: ObjectMapper
) : AbstractRuntimeServiceAdapter() {

  private val valueMapper: ValueMapper = ValueMapper(processEngine, objectMapper)

  override fun correlateMessage(messageName: String) =
    doCorrelateMessage(messageName)

  override fun correlateMessage(messageName: String, businessKey: String) =
    doCorrelateMessage(messageName, businessKey)

  override fun correlateMessage(messageName: String, correlationKeys: MutableMap<String, Any>) =
    doCorrelateMessage(messageName, correlationKeys = correlationKeys)

  override fun correlateMessage(messageName: String, correlationKeys: MutableMap<String, Any>, processVariables: MutableMap<String, Any>) =
    doCorrelateMessage(messageName, correlationKeys = correlationKeys, processVariables = processVariables)

  override fun correlateMessage(messageName: String, businessKey: String, processVariables: MutableMap<String, Any>) =
    doCorrelateMessage(messageName, businessKey, processVariables = processVariables)

  override fun correlateMessage(messageName: String, businessKey: String, correlationKeys: MutableMap<String, Any>, processVariables: MutableMap<String, Any>) =
    doCorrelateMessage(messageName, businessKey, correlationKeys, processVariables)

  override fun createMessageCorrelation(messageName: String) =
    DelegatingMessageCorrelationBuilder(
      messageName = messageName,
      runtimeServiceClient = runtimeServiceClient,
      valueMapper = valueMapper)

  /**
   * Null-safe version of message correlate.
   */
  private fun doCorrelateMessage(
    messageName: String,
    businessKey: String? = null,
    correlationKeys: MutableMap<String, Any>? = null,
    processVariables: MutableMap<String, Any>? = null) {

    val builder = DelegatingMessageCorrelationBuilder(
      messageName = messageName,
      runtimeServiceClient = runtimeServiceClient,
      valueMapper = valueMapper)

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

  override fun startProcessInstanceByKey(processDefinitionKey: String, businessKey: String, caseInstanceId: String, variables: MutableMap<String, Any>) =
    doStartProcessInstanceByKey(processDefinitionKey, businessKey, caseInstanceId, variables)

  /**
   * Null-safe version of starter function.
   */
  private fun doStartProcessInstanceByKey(
    processDefinitionKey: String,
    businessKey: String? = null,
    caseInstanceId: String? = null,
    variables: MutableMap<String, Any>? = null
  ): ProcessInstance {
    val startProcessInstance = StartProcessInstanceDto().apply {
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
    val instance = this.runtimeServiceClient.startProcessByKey(processDefinitionKey, startProcessInstance)
    return ProcessInstanceAdapter(instanceBean = InstanceBean.fromProcessInstanceDto(instance))
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

  override fun startProcessInstanceById(processDefinitionId: String, businessKey: String, caseInstanceId: String, variables: MutableMap<String, Any>) =
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
    val startProcessInstance = StartProcessInstanceDto().apply {
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
    val instance = this.runtimeServiceClient.startProcessById(processDefinitionId, startProcessInstance)
    return ProcessInstanceAdapter(instanceBean = InstanceBean.fromProcessInstanceDto(instance))
  }

  override fun signal(executionId: String) =
    doSignal(executionId)

  override fun signal(executionId: String, processVariables: MutableMap<String, Any>) =
    doSignal(executionId, processVariables = processVariables)

  override fun signal(executionId: String, signalName: String, signalData: Any?, processVariables: MutableMap<String, Any>) =
    doSignal(executionId, signalName, signalData, processVariables)

  @Suppress("UNUSED_PARAMETER")
  private fun doSignal(executionId: String, signalName: String? = null, signalData: Any? = null, processVariables: MutableMap<String, Any>? = null) {
    val trigger = ExecutionTriggerDto().apply {
      if (processVariables != null) {
        this.variables = valueMapper.mapValues(processVariables)
      }
    }
    runtimeServiceClient.triggerExecutionById(executionId, trigger)
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
    DelegatingSignalEventReceivedBuilder(signalName, runtimeServiceClient, valueMapper)

  private fun doSignalEventReceived(signalName: String, executionId: String? = null, variables: MutableMap<String, Any>? = null) {
    val builder = DelegatingSignalEventReceivedBuilder(signalName, runtimeServiceClient, valueMapper)
    if (executionId != null) {
      builder.executionId(executionId)
    }
    if (variables != null) {
      builder.setVariables(variables)
    }
    builder.send()
  }

  override fun getVariablesLocal(executionId: String): MutableMap<String, Any?> {
    return runtimeServiceClient.getVariablesLocal(executionId, true)
      .mapValues { it.value.value }
      .toMutableMap()
  }

  override fun getVariablesLocal(executionId: String, variableNames: MutableCollection<String>): MutableMap<String, Any?> {
    return runtimeServiceClient.getVariablesLocal(executionId, true)
      .filter { variableNames.contains(it.key) }
      .mapValues { it.value.value }
      .toMutableMap()
  }

  override fun getVariableLocal(executionId: String, variableName: String): Any? {
    val dto = runtimeServiceClient.getVariableLocal(executionId, variableName, true)
    return dto.value
  }

  override fun getVariablesLocalTyped(executionId: String): VariableMap =
    getVariablesLocalTyped(executionId, true)

  override fun getVariablesLocalTyped(executionId: String, deserializeValues: Boolean): VariableMap {
    val variables = runtimeServiceClient.getVariablesLocal(executionId, deserializeValues)
    return valueMapper.mapDtos(variables, deserializeValues)
  }

  override fun getVariablesLocalTyped(executionId: String, variableNames: MutableCollection<String>, deserializeValues: Boolean): VariableMap {
    val variables = runtimeServiceClient
      .getVariablesLocal(executionId, deserializeValues)
      .filter { variableNames.contains(it.key) }
    return valueMapper.mapDtos(variables, deserializeValues)
  }

  override fun <T : TypedValue> getVariableLocalTyped(executionId: String, variableName: String): T? =
    getVariableLocalTyped(executionId, variableName, true)

  override fun <T : TypedValue> getVariableLocalTyped(executionId: String, variableName: String, deserializeValue: Boolean): T? {
    val dto = runtimeServiceClient
      .getVariableLocal(executionId, variableName, deserializeValue)
    return valueMapper.mapDto(dto, deserializeValue)
  }

  override fun removeVariablesLocal(executionId: String, variableNames: MutableCollection<String>) {
    return runtimeServiceClient
      .changeVariablesLocal(executionId, PatchVariablesDto()
        .apply {
          deletions = variableNames.toList()
        })
  }

  override fun removeVariableLocal(executionId: String, variableName: String) {
    return runtimeServiceClient.deleteVariableLocal(executionId, variableName)
  }

  override fun setVariableLocal(executionId: String, variableName: String, value: Any?) {
    return runtimeServiceClient.setVariableLocal(executionId, variableName, valueMapper.mapValue(value))
  }

  override fun setVariablesLocal(executionId: String, variables: MutableMap<String, out Any>) {
    return runtimeServiceClient.changeVariablesLocal(executionId, PatchVariablesDto().apply {
      modifications = valueMapper.mapValues(variables)
    })
  }

  override fun getVariables(executionId: String): MutableMap<String, Any?> {
    return runtimeServiceClient
      .getVariables(executionId, true)
      .mapValues { it.value.value }
      .toMutableMap()
  }

  override fun getVariables(executionId: String, variableNames: MutableCollection<String>): MutableMap<String, Any?> {
    return runtimeServiceClient
      .getVariables(executionId, true)
      .filter { variableNames.contains(it.key) }
      .mapValues { it.value.value }
      .toMutableMap()
  }

  override fun <T : TypedValue> getVariableTyped(executionId: String, variableName: String): T? {
    return getVariableTyped(executionId, variableName, true)
  }

  override fun <T : TypedValue> getVariableTyped(executionId: String, variableName: String, deserializeValue: Boolean): T? {
    val dto = runtimeServiceClient.getVariable(executionId, variableName, deserializeValue)
    return valueMapper.mapDto(dto, deserializeValue)
  }

  override fun getVariablesTyped(executionId: String): VariableMap {
    val variables = runtimeServiceClient.getVariables(executionId, true)
    return valueMapper.mapDtos(variables)
  }

  override fun getVariablesTyped(executionId: String, deserializeValues: Boolean): VariableMap {
    val variables = runtimeServiceClient.getVariables(executionId, deserializeValues)
    return valueMapper.mapDtos(variables, deserializeValues)
  }

  override fun getVariablesTyped(executionId: String, variableNames: MutableCollection<String>, deserializeValues: Boolean): VariableMap {
    val variables = runtimeServiceClient.getVariables(executionId, deserializeValues).filter { variableNames.contains(it.key) }
    return valueMapper.mapDtos(variables)
  }

  override fun getVariable(executionId: String, variableName: String): Any {
    return runtimeServiceClient.getVariable(executionId, variableName, true).value
  }

  override fun removeVariables(executionId: String, variableNames: MutableCollection<String>) {
    return runtimeServiceClient.changeVariables(executionId, PatchVariablesDto().apply {
      deletions = variableNames.toList()
    })
  }

  override fun removeVariable(executionId: String, variableName: String) {
    return runtimeServiceClient.deleteVariable(executionId, variableName)
  }

  override fun setVariable(executionId: String, variableName: String, value: Any?) {
    return runtimeServiceClient.setVariable(executionId, variableName, valueMapper.mapValue(value))
  }

  override fun setVariables(executionId: String, variables: MutableMap<String, out Any>) {
    return runtimeServiceClient.changeVariables(executionId, PatchVariablesDto().apply {
      modifications = valueMapper.mapValues(variables)
    })
  }

  override fun completeTask(externalTaskId: String, taskWorkerId: String ) {
    this.completeTask(externalTaskId, taskWorkerId, mutableMapOf())
  }

  override fun completeTask(externalTaskId: String, taskWorkerId: String, updateVariables: MutableMap<String, out Any> ) {
    this.completeTask(externalTaskId, taskWorkerId, updateVariables, mutableMapOf())
  }

  override fun completeTask(externalTaskId: String, taskWorkerId: String, updateVariables: MutableMap<String, out Any>, updateLocalVariables: MutableMap<String, out Any> ) {
    return runtimeServiceClient.completeTask(externalTaskId, CompleteTaskDto().apply {
      variables = valueMapper.mapValues(updateVariables)
      localVariables = valueMapper.mapValues(updateLocalVariables)
      workerId = taskWorkerId
    })
  }
}


