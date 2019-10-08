package org.camunda.bpm.extension.feign.impl.builder

import com.fasterxml.jackson.databind.ObjectMapper
import org.camunda.bpm.engine.ProcessEngine
import org.camunda.bpm.engine.rest.dto.SignalDto
import org.camunda.bpm.engine.runtime.SignalEventReceivedBuilder
import org.camunda.bpm.extension.feign.client.RuntimeServiceClient
import org.camunda.bpm.extension.feign.variables.toVariableValueDtoMap

/**
 * Correlation builder, collecting all settings in the DTO sent to the REST endpoint later.
 */
class DelegatingSignalEventReceivedBuilder(
  signalName: String,
  private val runtimeServiceClient: RuntimeServiceClient,
  private val processEngine: ProcessEngine,
  private val objectMapper: ObjectMapper
) : SignalEventReceivedBuilder {

  private val signalDto = SignalDto().apply {
    this.name = signalName
    this.variables = mutableMapOf()
  }

  override fun setVariables(variables: MutableMap<String, Any>): SignalEventReceivedBuilder {
    signalDto.variables = variables.toVariableValueDtoMap()
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
