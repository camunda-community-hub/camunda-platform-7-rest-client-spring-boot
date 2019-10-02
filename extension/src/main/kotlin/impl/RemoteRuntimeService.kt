package org.camunda.bpm.extension.feign.impl

import com.fasterxml.jackson.databind.ObjectMapper
import org.camunda.bpm.engine.ProcessEngine
import org.camunda.bpm.engine.rest.dto.VariableValueDto
import org.camunda.bpm.engine.rest.dto.message.CorrelationMessageDto
import org.camunda.bpm.engine.rest.dto.runtime.StartProcessInstanceDto
import org.camunda.bpm.engine.runtime.ProcessInstance
import org.camunda.bpm.engine.variable.VariableMap
import org.camunda.bpm.engine.variable.value.TypedValue
import org.camunda.bpm.extension.feign.adapter.AbstractRuntimeServiceAdapter
import org.camunda.bpm.extension.feign.adapter.InstanceBean
import org.camunda.bpm.extension.feign.adapter.ProcessInstanceAdapter
import org.camunda.bpm.extension.feign.client.RuntimeServiceClient
import org.camunda.bpm.extension.feign.variables.fromUntypedValue
import org.camunda.bpm.extension.feign.variables.toVariableValueDtoMap
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

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

  /**
   * Null-safe version of message correlate.
   */
  private fun doCorrelateMessage(
    messageName: String,
    businessKey: String? = null,
    correlationKeys: MutableMap<String, Any>? = null,
    processVariables: MutableMap<String, Any>? = null) {
    runtimeServiceClient.correlateMessage(
      CorrelationMessageDto().apply {
        this.messageName = messageName
        if (businessKey != null) {
          this.businessKey = businessKey
        }
        if (processVariables != null) {
          this.processVariables = processVariables.toVariableValueDtoMap()
        }
        if (correlationKeys != null) {
          this.correlationKeys = correlationKeys.toVariableValueDtoMap()
        }
      }
    )

  }

  override fun createMessageCorrelation(messageName: String) =
    DelegatingMessageCorrelationBuilder(
      messageName = messageName,
      runtimeServiceClient = runtimeServiceClient,
      processEngine = processEngine,
      objectMapper = objectMapper)

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
}


