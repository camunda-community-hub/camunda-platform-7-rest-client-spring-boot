package org.camunda.bpm.extension.restclient.impl

import org.camunda.bpm.engine.rest.dto.message.CorrelationMessageDto
import org.camunda.bpm.engine.runtime.MessageCorrelationBuilder
import org.camunda.bpm.engine.runtime.MessageCorrelationResult
import org.camunda.bpm.engine.runtime.MessageCorrelationResultWithVariables
import org.camunda.bpm.engine.runtime.ProcessInstance
import org.camunda.bpm.extension.restclient.client.RuntimeServiceClient
import org.camunda.bpm.extension.restclient.variables.fromUntypedValue

/**
 * Correlation builder, collecting all settings in the DTO sent to the REST endpoint later.
 */
class DelegatingMessageCorrelationBuilder(
  messageName: String,
  private val runtimeServiceClient: RuntimeServiceClient
) : MessageCorrelationBuilder {

  private val correlationMessageDto: CorrelationMessageDto = CorrelationMessageDto().apply {
    this.messageName = messageName
    this.correlationKeys = mutableMapOf()
    this.localCorrelationKeys = mutableMapOf()
    this.processVariables = mutableMapOf()
    this.processVariablesLocal = mutableMapOf()
  }

  override fun withoutTenantId(): MessageCorrelationBuilder {
    correlationMessageDto.isWithoutTenantId = true
    return this
  }

  override fun tenantId(tenantId: String): MessageCorrelationBuilder {
    correlationMessageDto.tenantId = tenantId
    return this
  }

  override fun processDefinitionId(processDefinitionId: String): MessageCorrelationBuilder {
    throw UnsupportedOperationException("Unsupported remote correlation method.")
  }

  override fun setVariable(variableName: String, variableValue: Any): MessageCorrelationBuilder {
    val variables = correlationMessageDto.processVariables
    variables[variableName] = fromUntypedValue(variableValue)
    return this
  }

  override fun setVariables(variables: MutableMap<String, Any>): MessageCorrelationBuilder {
    correlationMessageDto.processVariables = variables.mapValues { fromUntypedValue(it) }
    return this
  }

  override fun setVariableLocal(variableName: String, variableValue: Any): MessageCorrelationBuilder {
    val variables = correlationMessageDto.processVariablesLocal
    variables[variableName] = fromUntypedValue(variableValue)
    return this
  }

  override fun setVariablesLocal(variables: MutableMap<String, Any>): MessageCorrelationBuilder {
    correlationMessageDto.processVariablesLocal = variables.mapValues { fromUntypedValue(it) }
    return this
  }

  override fun processInstanceBusinessKey(businessKey: String): MessageCorrelationBuilder {
    correlationMessageDto.businessKey = businessKey
    return this
  }

  override fun processInstanceId(id: String): MessageCorrelationBuilder {
    correlationMessageDto.processInstanceId = id
    return this
  }

  override fun processInstanceVariableEquals(variableName: String, variableValue: Any): MessageCorrelationBuilder {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun processInstanceVariablesEqual(variables: MutableMap<String, Any>): MessageCorrelationBuilder {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun localVariablesEqual(variables: MutableMap<String, Any>): MessageCorrelationBuilder {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun localVariableEquals(variableName: String, variableValue: Any): MessageCorrelationBuilder {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun startMessageOnly(): MessageCorrelationBuilder {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }


  override fun correlateAllWithResult(): MutableList<MessageCorrelationResult> {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun correlateWithResultAndVariables(deserializeValues: Boolean): MessageCorrelationResultWithVariables {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun correlateExclusively() {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun correlateAll() {
    correlationMessageDto.isAll = true
    runtimeServiceClient.correlateMessage(correlationMessageDto)
  }

  override fun correlateWithResult(): MessageCorrelationResult {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun correlate() {
    runtimeServiceClient.correlateMessage(correlationMessageDto)
  }

  override fun correlateStartMessage(): ProcessInstance {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun correlateAllWithResultAndVariables(deserializeValues: Boolean): MutableList<MessageCorrelationResultWithVariables> {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }
}
