package org.camunda.bpm.extension.feign.impl.builder

import org.camunda.bpm.engine.rest.dto.SignalDto
import org.camunda.bpm.engine.runtime.SignalEventReceivedBuilder
import org.camunda.bpm.extension.feign.client.RuntimeServiceClient
import org.camunda.bpm.extension.feign.variables.ValueMapper

/**
 * Correlation builder, collecting all settings in the DTO sent to the REST endpoint later.
 */
class DelegatingSignalEventReceivedBuilder(
  signalName: String,
  private val runtimeServiceClient: RuntimeServiceClient,
  private val valueMapper: ValueMapper
) : SignalEventReceivedBuilder {

  private val signalDto = SignalDto().apply {
    this.name = signalName
    this.variables = mutableMapOf()
  }

  override fun setVariables(variables: MutableMap<String, Any>): SignalEventReceivedBuilder {
    signalDto.variables = valueMapper.mapValues(variables)
    return this
  }

  override fun tenantId(tenantId: String): SignalEventReceivedBuilder {
    signalDto.tenantId = tenantId
    return this
  }

  override fun executionId(executionId: String): SignalEventReceivedBuilder {
    signalDto.executionId = executionId
    return this
  }

  override fun withoutTenantId(): SignalEventReceivedBuilder {
    signalDto.isWithoutTenantId = true
    return this
  }

  override fun send() {
    runtimeServiceClient.signalEventReceived(signalDto)
  }

}
