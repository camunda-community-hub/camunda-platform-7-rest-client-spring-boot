/*-
 * #%L
 * camunda-bpm-feign
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
package org.camunda.bpm.extension.feign.impl.builder

import mu.KLogging
import org.camunda.bpm.engine.rest.dto.message.CorrelationMessageDto
import org.camunda.bpm.engine.rest.dto.message.MessageCorrelationResultDto
import org.camunda.bpm.engine.runtime.MessageCorrelationBuilder
import org.camunda.bpm.engine.runtime.MessageCorrelationResult
import org.camunda.bpm.engine.runtime.MessageCorrelationResultWithVariables
import org.camunda.bpm.engine.runtime.ProcessInstance
import org.camunda.bpm.extension.feign.client.RuntimeServiceClient
import org.camunda.bpm.extension.feign.variables.ValueMapper
import org.camunda.bpm.extension.feign.variables.fromDto

/**
 * Correlation builder, collecting all settings in the DTO sent to the REST endpoint later.
 */
class DelegatingMessageCorrelationBuilder(
  messageName: String,
  private val runtimeServiceClient: RuntimeServiceClient,
  private val valueMapper: ValueMapper
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
    correlationMessageDto.correlationKeys.putAll(valueMapper.mapValues(correlationKeys))
    return this
  }

  override fun setVariable(variableName: String, variableValue: Any?): MessageCorrelationBuilder {
    correlationMessageDto.processVariables[variableName] = valueMapper.mapValue(variableValue)
    return this
  }

  override fun setVariables(variables: MutableMap<String, Any>): MessageCorrelationBuilder {
    correlationMessageDto.processVariables.putAll(valueMapper.mapValues(variables))
    return this
  }

  override fun setVariableLocal(variableName: String, variableValue: Any?): MessageCorrelationBuilder {
    correlationMessageDto.processVariablesLocal[variableName] = valueMapper.mapValue(variableValue)
    return this
  }

  override fun setVariablesLocal(variables: MutableMap<String, Any>): MessageCorrelationBuilder {
    correlationMessageDto.processVariablesLocal.putAll(valueMapper.mapValues(variables))
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
    correlationMessageDto.correlationKeys[variableName] = valueMapper.mapValue(variableValue)
    return this
  }

  override fun processInstanceVariablesEqual(variables: MutableMap<String, Any>): MessageCorrelationBuilder {
    variables.forEach {
      correlationMessageDto.correlationKeys[it.key] = valueMapper.mapValue(it.value)
    }
    return this
  }

  override fun localVariablesEqual(variables: MutableMap<String, Any>): MessageCorrelationBuilder {
    variables.forEach {
      correlationMessageDto.localCorrelationKeys[it.key] = valueMapper.mapValue(it.value)
    }
    return this
  }

  override fun localVariableEquals(variableName: String, variableValue: Any): MessageCorrelationBuilder {
    correlationMessageDto.localCorrelationKeys[variableName] = valueMapper.mapValue(variableValue)
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
    logger.debug { "Ignoring 'deserializeValues' flag." }
    correlationMessageDto.isResultEnabled = true
    correlationMessageDto.isVariablesInResultEnabled = true
    val result = runtimeServiceClient.correlateMessage(correlationMessageDto)
    return when (result.size) {
      0 -> throw IllegalStateException("No result received")
      1 -> result[0].fromDto(valueMapper)
      else -> {
        logger.warn { "Multiple results received, returning the first one." }
        result[0].fromDto(valueMapper)
      }
    }
  }

  override fun correlateAllWithResultAndVariables(deserializeValues: Boolean): MutableList<MessageCorrelationResultWithVariables> {
    correlationMessageDto.isResultEnabled = true
    correlationMessageDto.isVariablesInResultEnabled = true
    // FIXME: check if this flag can be used during de-serialization
    logger.debug { "Ignoring 'deserializeValues' flag." }
    val result = runtimeServiceClient.correlateMessage(correlationMessageDto)
    return result.map { result[0].fromDto(valueMapper) }.toMutableList()
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
