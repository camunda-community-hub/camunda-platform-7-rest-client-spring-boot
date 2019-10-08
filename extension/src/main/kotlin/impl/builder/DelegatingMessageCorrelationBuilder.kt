package org.camunda.bpm.extension.feign.impl.builder

import com.fasterxml.jackson.databind.ObjectMapper
import mu.KLogging
import org.camunda.bpm.engine.ProcessEngine
import org.camunda.bpm.engine.rest.dto.VariableValueDto
import org.camunda.bpm.engine.rest.dto.message.CorrelationMessageDto
import org.camunda.bpm.engine.rest.dto.message.MessageCorrelationResultDto
import org.camunda.bpm.engine.runtime.MessageCorrelationBuilder
import org.camunda.bpm.engine.runtime.MessageCorrelationResult
import org.camunda.bpm.engine.runtime.MessageCorrelationResultWithVariables
import org.camunda.bpm.engine.runtime.ProcessInstance
import org.camunda.bpm.engine.variable.value.TypedValue
import org.camunda.bpm.extension.feign.client.RuntimeServiceClient
import org.camunda.bpm.extension.feign.variables.fromDto
import org.camunda.bpm.extension.feign.variables.fromUntypedValue
import org.camunda.bpm.extension.feign.variables.toVariableValueDtoMap

/**
 * Correlation builder, collecting all settings in the DTO sent to the REST endpoint later.
 */
class DelegatingMessageCorrelationBuilder(
  messageName: String,
  private val runtimeServiceClient: RuntimeServiceClient,
  private val processEngine: ProcessEngine,
  private val objectMapper: ObjectMapper
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

  /**
   * Sets correlation keys for message correlation.
   * @param correlationKeys keys to set.
   * @return modified fluent builder.
   */
  fun setCorrelationKeys(correlationKeys: MutableMap<String, Any>): MessageCorrelationBuilder {
    correlationMessageDto.correlationKeys = correlationKeys.toVariableValueDtoMap()
    return this
  }

  override fun setVariable(variableName: String, variableValue: Any): MessageCorrelationBuilder {
    val variables = correlationMessageDto.processVariables
    variables[variableName] = if (variableValue is TypedValue) {
      VariableValueDto.fromTypedValue(variableValue)
    } else {
      fromUntypedValue(variableValue)
    }
    return this
  }

  override fun setVariables(variables: MutableMap<String, Any>): MessageCorrelationBuilder {
    correlationMessageDto.processVariables = variables.toVariableValueDtoMap()
    return this
  }

  override fun setVariableLocal(variableName: String, variableValue: Any): MessageCorrelationBuilder {
    val variables = correlationMessageDto.processVariablesLocal
    variables[variableName] = if (variableValue is TypedValue) {
      VariableValueDto.fromTypedValue(variableValue)
    } else {
      fromUntypedValue(variableValue)
    }
    return this
  }

  override fun setVariablesLocal(variables: MutableMap<String, Any>): MessageCorrelationBuilder {
    correlationMessageDto.processVariablesLocal = variables.toVariableValueDtoMap()
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

  override fun processDefinitionId(processDefinitionId: String): MessageCorrelationBuilder {
    // FIXME: check if this can be solved
    logger.error { "Process definition constraint is not supported by remote message correlation" }
    return this
  }

  override fun processInstanceVariableEquals(variableName: String, variableValue: Any): MessageCorrelationBuilder {
    val correlationKeys = correlationMessageDto.correlationKeys
    correlationKeys[variableName] = if (variableValue is TypedValue) {
      VariableValueDto.fromTypedValue(variableValue)
    } else {
      fromUntypedValue(variableValue)
    }
    return this
  }

  override fun processInstanceVariablesEqual(variables: MutableMap<String, Any>): MessageCorrelationBuilder {
    val correlationKeys = correlationMessageDto.correlationKeys
    variables.forEach {
      correlationKeys[it.key] = if (it.value is TypedValue) {
        VariableValueDto.fromTypedValue(it.value as TypedValue)
      } else {
        fromUntypedValue(it.value)
      }
    }

    return this
  }

  override fun localVariablesEqual(variables: MutableMap<String, Any>): MessageCorrelationBuilder {
    // FIXME: check if this can be solved
    logger.error { "Process instance local variable query is not supported by remote message correlation" }
    return this
  }

  override fun localVariableEquals(variableName: String, variableValue: Any): MessageCorrelationBuilder {
    // FIXME: check if this can be solved
    logger.error { "Process instance local variable query is not supported by remote message correlation" }
    return this
  }

  override fun startMessageOnly(): MessageCorrelationBuilder {
    // FIXME: check if this can be solved
    logger.error { "Restriction to start messages only is not supported by remote message correlation" }
    return this
  }

  override fun correlateStartMessage(): ProcessInstance {
    // FIXME: check if this can be solved
    logger.debug { "Restriction to start messages only is not supported by remote message correlation" }
    correlationMessageDto.isResultEnabled = true
    val result = runtimeServiceClient.correlateMessage(correlationMessageDto)
    return when (result.size) {
      0 -> throw IllegalStateException("No result received")
      1 -> (result[0] as MessageCorrelationResultDto).fromDto().processInstance
      else -> {
        logger.warn { "Multiple results received, returning the first one." }
        (result[0] as MessageCorrelationResultDto).fromDto().processInstance
      }
    }
  }

  override fun correlateWithResultAndVariables(deserializeValues: Boolean): MessageCorrelationResultWithVariables {
    // FIXME: check if this flag can be used during de-serialization
    logger.debug { "Ignoring 'deserializeValues' flag."}
    correlationMessageDto.isResultEnabled = true
    correlationMessageDto.isVariablesInResultEnabled = true
    val result = runtimeServiceClient.correlateMessage(correlationMessageDto)
    return when (result.size) {
      0 -> throw IllegalStateException("No result received")
      1 -> result[0].fromDto(processEngine = processEngine, objectMapper = objectMapper)
      else -> {
        logger.warn { "Multiple results received, returning the first one." }
        result[0].fromDto(processEngine = processEngine, objectMapper = objectMapper)
      }
    }
  }

  override fun correlateAllWithResultAndVariables(deserializeValues: Boolean): MutableList<MessageCorrelationResultWithVariables> {
    correlationMessageDto.isResultEnabled = true
    correlationMessageDto.isVariablesInResultEnabled = true
    // FIXME: check if this flag can be used during de-serialization
    logger.debug { "Ignoring 'deserializeValues' flag."}
    val result = runtimeServiceClient.correlateMessage(correlationMessageDto)
    return result.map { result[0].fromDto(processEngine = processEngine, objectMapper = objectMapper) }.toMutableList()
  }

  override fun correlateAllWithResult(): MutableList<MessageCorrelationResult> {
    correlationMessageDto.isAll = true
    correlationMessageDto.isResultEnabled = true
    val result = runtimeServiceClient.correlateMessage(correlationMessageDto)
    return result.map { (it as MessageCorrelationResultDto).fromDto() }.toMutableList()
  }

  override fun correlateWithResult(): MessageCorrelationResult {
    correlationMessageDto.isResultEnabled = true
    val result = runtimeServiceClient.correlateMessage(correlationMessageDto)
    return when (result.size) {
      0 -> throw IllegalStateException("No result received")
      1 -> (result[0] as MessageCorrelationResultDto).fromDto()
      else -> {
        logger.warn { "Multiple results received, returning the first one." }
        (result[0] as MessageCorrelationResultDto).fromDto()
      }
    }
  }

  override fun correlateExclusively() {
    // FIXME: check if this can be solved
    logger.debug { "Exclusive correlation is not supported by remote message correlation. Correlating anyway." }
    correlate()
  }

  override fun correlateAll() {
    correlationMessageDto.isAll = true
    runtimeServiceClient.correlateMessage(correlationMessageDto)
  }

  override fun correlate() {
    runtimeServiceClient.correlateMessage(correlationMessageDto)
  }
}
