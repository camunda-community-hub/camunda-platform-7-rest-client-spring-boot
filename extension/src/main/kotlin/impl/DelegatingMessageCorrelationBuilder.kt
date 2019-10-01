package org.camunda.bpm.extension.restclient.impl

import mu.KLogging
import org.camunda.bpm.engine.rest.dto.message.CorrelationMessageDto
import org.camunda.bpm.engine.runtime.MessageCorrelationBuilder
import org.camunda.bpm.engine.runtime.MessageCorrelationResult
import org.camunda.bpm.engine.runtime.MessageCorrelationResultWithVariables
import org.camunda.bpm.engine.runtime.ProcessInstance
import org.camunda.bpm.engine.variable.Variables.createVariables
import org.camunda.bpm.extension.restclient.client.RuntimeServiceClient
import org.camunda.bpm.extension.restclient.variables.fromUntypedValue
import org.camunda.bpm.extension.restclient.variables.messageCorrelationResultFromDto
import org.camunda.bpm.extension.restclient.variables.messageCorrelationResultWithVariablesFromDto

/**
 * Correlation builder, collecting all settings in the DTO sent to the REST endpoint later.
 */
class DelegatingMessageCorrelationBuilder(
  messageName: String,
  private val runtimeServiceClient: RuntimeServiceClient
) : MessageCorrelationBuilder {

  companion object : KLogging()

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
    logger.error { "Process definition constraint is not supported by remote message correlation" }
    return this
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
    logger.error { "Process instance variable query is not supported by remote message correlation" }
    return this
  }

  override fun processInstanceVariablesEqual(variables: MutableMap<String, Any>): MessageCorrelationBuilder {
    logger.error { "Process instance variable query is not supported by remote message correlation" }
    return this
  }

  override fun localVariablesEqual(variables: MutableMap<String, Any>): MessageCorrelationBuilder {
    logger.error { "Process instance local variable query is not supported by remote message correlation" }
    return this
  }

  override fun localVariableEquals(variableName: String, variableValue: Any): MessageCorrelationBuilder {
    logger.error { "Process instance local variable query is not supported by remote message correlation" }
    return this
  }

  override fun startMessageOnly(): MessageCorrelationBuilder {
    logger.error { "Restriction to start messages only is not supported by remote message correlation" }
    return this
  }

  override fun correlateExclusively() {
    logger.error { "Exclusive correlation is not supported by remote message correlation. Correlating anyway." }
    correlate()
  }

  override fun correlateStartMessage(): ProcessInstance {
    logger.error { "Restriction to start messages only is not supported by remote message correlation" }
    correlationMessageDto.isResultEnabled = true
    val result = runtimeServiceClient.correlateMessage(correlationMessageDto)
    return when (result.size) {
      0 -> throw IllegalStateException("No result received")
      1 -> messageCorrelationResultWithVariablesFromDto(result[0], createVariables()).processInstance
      else -> {
        logger.warn { "Multiple results received, returning the first one." }
        messageCorrelationResultWithVariablesFromDto(result[0], createVariables()).processInstance
      }
    }
  }

  override fun correlateWithResultAndVariables(deserializeValues: Boolean): MessageCorrelationResultWithVariables {
    correlationMessageDto.isResultEnabled = true
    val result = runtimeServiceClient.correlateMessage(correlationMessageDto)
    return when (result.size) {
      0 -> throw IllegalStateException("No result received")
      1 -> messageCorrelationResultWithVariablesFromDto(result[0], createVariables())
      else -> {
        logger.warn { "Multiple results received, returning the first one." }
        messageCorrelationResultWithVariablesFromDto(result[0], createVariables())
      }
    }
  }

  override fun correlateAllWithResultAndVariables(deserializeValues: Boolean): MutableList<MessageCorrelationResultWithVariables> {
    correlationMessageDto.isResultEnabled = true
    val result = runtimeServiceClient.correlateMessage(correlationMessageDto)
    return result.map { messageCorrelationResultWithVariablesFromDto(it, createVariables()) }.toMutableList()
  }

  override fun correlateAllWithResult(): MutableList<MessageCorrelationResult> {
    correlationMessageDto.isAll = true
    correlationMessageDto.isResultEnabled = true
    val result = runtimeServiceClient.correlateMessage(correlationMessageDto)
    return result.map { messageCorrelationResultFromDto(it) }.toMutableList()
  }

  override fun correlateAll() {
    correlationMessageDto.isAll = true
    runtimeServiceClient.correlateMessage(correlationMessageDto)
  }

  override fun correlateWithResult(): MessageCorrelationResult {
    correlationMessageDto.isResultEnabled = true
    val result = runtimeServiceClient.correlateMessage(correlationMessageDto)
    return when (result.size) {
      0 -> throw IllegalStateException("No result received")
      1 -> messageCorrelationResultFromDto(result[0])
      else -> {
        logger.warn { "Multiple results received, returning the first one." }
        messageCorrelationResultFromDto(result[0])
      }
    }
  }

  override fun correlate() {
    runtimeServiceClient.correlateMessage(correlationMessageDto)
  }
}
