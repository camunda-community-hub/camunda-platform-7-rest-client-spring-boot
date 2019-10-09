package org.camunda.bpm.extension.feign.impl

import com.fasterxml.jackson.databind.ObjectMapper
import org.camunda.bpm.engine.ProcessEngine
import org.camunda.bpm.engine.rest.dto.PatchVariablesDto
import org.camunda.bpm.engine.rest.dto.VariableValueDto
import org.camunda.bpm.engine.rest.dto.runtime.ExecutionTriggerDto
import org.camunda.bpm.engine.rest.dto.runtime.StartProcessInstanceDto
import org.camunda.bpm.engine.runtime.ProcessInstance
import org.camunda.bpm.engine.runtime.SignalEventReceivedBuilder
import org.camunda.bpm.engine.variable.VariableMap
import org.camunda.bpm.engine.variable.value.TypedValue
import org.camunda.bpm.extension.feign.adapter.AbstractRuntimeServiceAdapter
import org.camunda.bpm.extension.feign.adapter.InstanceBean
import org.camunda.bpm.extension.feign.adapter.ProcessInstanceAdapter
import org.camunda.bpm.extension.feign.client.RuntimeServiceClient
import org.camunda.bpm.extension.feign.impl.builder.DelegatingMessageCorrelationBuilder
import org.camunda.bpm.extension.feign.impl.builder.DelegatingSignalEventReceivedBuilder
import org.camunda.bpm.extension.feign.variables.fromUntypedValue
import org.camunda.bpm.extension.feign.variables.toVariableValueDtoMap
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
  private val processEngine: ProcessEngine,
  private val objectMapper: ObjectMapper
) : AbstractRuntimeServiceAdapter() {

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
      processEngine = processEngine,
      objectMapper = objectMapper)

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
      processEngine = processEngine,
      objectMapper = objectMapper)

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
        this.variables = variables.toVariableValueDtoMap()
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
        this.variables = variables.toVariableValueDtoMap()
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

  private fun doSignal(executionId: String, signalName: String? = null, signalData: Any? = null, processVariables: MutableMap<String, Any>? = null) {
    val trigger = ExecutionTriggerDto().apply {
      if (processVariables != null) {
        this.variables = processVariables.toVariableValueDtoMap()
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
    DelegatingSignalEventReceivedBuilder(signalName, runtimeServiceClient, processEngine, objectMapper)

  private fun doSignalEventReceived(signalName: String, executionId: String? = null, variables: MutableMap<String, Any>? = null) {
    val builder = DelegatingSignalEventReceivedBuilder(signalName, runtimeServiceClient, processEngine, objectMapper)
    if (executionId != null) {
      builder.executionId(executionId)
    }
    if (variables != null) {
      builder.setVariables(variables)
    }
    builder.send()
  }

  override fun getVariablesLocal(executionId: String): MutableMap<String, Any> {
    return runtimeServiceClient.getVariablesLocal(executionId, true).map {
      it.key to it.value.value
    }.toMap().toMutableMap()
  }

  override fun getVariablesLocal(executionId: String, variableNames: MutableCollection<String>): MutableMap<String, Any> {
    return runtimeServiceClient.getVariablesLocal(executionId, true)
      .filter { variableNames.contains(it.key) }
      .map {
        it.key to it.value.value
      }.toMap().toMutableMap()
  }

  override fun getVariableLocal(executionId: String, variableName: String): Any {
    return runtimeServiceClient.getVariableLocal(executionId, variableName, true).value
  }

  override fun getVariablesLocalTyped(executionId: String): VariableMap =
    getVariablesLocalTyped(executionId, true)

  override fun getVariablesLocalTyped(executionId: String, deserializeValues: Boolean): VariableMap {
    val variables = runtimeServiceClient.getVariablesLocal(executionId, deserializeValues)
    return VariableValueDto.toMap(variables, processEngine, objectMapper)
  }

  override fun getVariablesLocalTyped(executionId: String, variableNames: MutableCollection<String>, deserializeValues: Boolean): VariableMap {
    val variables = runtimeServiceClient.getVariablesLocal(executionId, deserializeValues).filter { variableNames.contains(it.key) }
    return VariableValueDto.toMap(variables, processEngine, objectMapper)
  }

  override fun <T : TypedValue> getVariableLocalTyped(executionId: String, variableName: String): T? =
    getVariableLocalTyped(executionId, variableName, true)

  override fun <T : TypedValue> getVariableLocalTyped(executionId: String, variableName: String, deserializeValue: Boolean): T? {
    val dto = runtimeServiceClient.getVariableLocal(executionId, variableName, deserializeValue)
    return dto.toTypedValue(processEngine, objectMapper) as T
  }

  override fun removeVariablesLocal(executionId: String, variableNames: MutableCollection<String>) {
    return runtimeServiceClient.changeVariablesLocal(executionId, PatchVariablesDto().apply {
      deletions = variableNames.toList()
    })
  }

  override fun removeVariableLocal(executionId: String, variableName: String) {
    return runtimeServiceClient.deleteVariableLocal(executionId, variableName)
  }

  override fun setVariableLocal(executionId: String, variableName: String, value: Any) {
    val dto = if (value is TypedValue) {
      VariableValueDto.fromTypedValue(value)
    } else {
      fromUntypedValue(value)
    }
    return runtimeServiceClient.setVariableLocal(executionId, variableName, dto)
  }

  override fun setVariablesLocal(executionId: String, variables: MutableMap<String, out Any>) {
    return runtimeServiceClient.changeVariablesLocal(executionId, PatchVariablesDto().apply {
      modifications = variables.toVariableValueDtoMap()
    })
  }

  override fun getVariables(executionId: String): MutableMap<String, Any> {
    return runtimeServiceClient.getVariables(executionId, true).map {
      it.key to it.value.value
    }.toMap().toMutableMap()
  }

  override fun getVariables(executionId: String, variableNames: MutableCollection<String>): MutableMap<String, Any> {
    return runtimeServiceClient.getVariables(executionId, true)
      .filter { variableNames.contains(it.key) }
      .map {
        it.key to it.value.value
      }.toMap().toMutableMap()
  }

  override fun <T : TypedValue> getVariableTyped(executionId: String, variableName: String): T? {
    return getVariableTyped(executionId, variableName, true)
  }

  override fun <T : TypedValue> getVariableTyped(executionId: String, variableName: String, deserializeValue: Boolean): T? {
    val dto = runtimeServiceClient.getVariable(executionId, variableName, deserializeValue)
    return dto.toTypedValue(processEngine, objectMapper) as T
  }

  override fun getVariablesTyped(executionId: String): VariableMap {
    val variables = runtimeServiceClient.getVariables(executionId, true)
    return VariableValueDto.toMap(variables, processEngine, objectMapper)
  }

  override fun getVariablesTyped(executionId: String, deserializeValues: Boolean): VariableMap {
    val variables = runtimeServiceClient.getVariables(executionId, true)
    return VariableValueDto.toMap(variables, processEngine, objectMapper)
  }

  override fun getVariablesTyped(executionId: String, variableNames: MutableCollection<String>, deserializeValues: Boolean): VariableMap {
    val variables = runtimeServiceClient.getVariables(executionId, deserializeValues).filter { variableNames.contains(it.key) }
    return VariableValueDto.toMap(variables, processEngine, objectMapper)
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

  override fun setVariable(executionId: String, variableName: String, value: Any) {
    val dto = if (value is TypedValue) {
      VariableValueDto.fromTypedValue(value)
    } else {
      fromUntypedValue(value)
    }
    return runtimeServiceClient.setVariable(executionId, variableName, dto)
  }

  override fun setVariables(executionId: String, variables: MutableMap<String, out Any>) {
    return runtimeServiceClient.changeVariables(executionId, PatchVariablesDto().apply {
      modifications = variables.toVariableValueDtoMap()
    })
  }
}


