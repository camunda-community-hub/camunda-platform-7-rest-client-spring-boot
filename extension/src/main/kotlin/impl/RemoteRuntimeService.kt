package org.camunda.bpm.extension.restclient.impl

import org.camunda.bpm.engine.rest.dto.message.CorrelationMessageDto
import org.camunda.bpm.extension.restclient.adapter.AbstractRuntimeServiceAdapter
import org.camunda.bpm.extension.restclient.client.RuntimeServiceClient
import org.camunda.bpm.extension.restclient.variables.fromUntypedValue
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

@Component
@Qualifier("remote")
class RemoteRuntimeService(
  private val runtimeServiceClient: RuntimeServiceClient
) : AbstractRuntimeServiceAdapter() {


  override fun correlateMessage(messageName: String) =
    runtimeServiceClient.correlateMessage(
      CorrelationMessageDto().apply {
        this.messageName = messageName
      }
    )

  override fun correlateMessage(messageName: String, businessKey: String) =
    runtimeServiceClient.correlateMessage(
      CorrelationMessageDto().apply {
        this.messageName = messageName
        this.businessKey = businessKey
      }
    )

  override fun correlateMessage(messageName: String, correlationKeys: MutableMap<String, Any>) =
    runtimeServiceClient.correlateMessage(
      CorrelationMessageDto().apply {
        this.messageName = messageName
        this.correlationKeys = correlationKeys.mapValues { fromUntypedValue(it) }
      }
    )

  override fun correlateMessage(messageName: String, correlationKeys: MutableMap<String, Any>, processVariables: MutableMap<String, Any>) =
    runtimeServiceClient.correlateMessage(
      CorrelationMessageDto().apply {
        this.messageName = messageName
        this.correlationKeys = correlationKeys.mapValues { fromUntypedValue(it) }
        this.processVariables = processVariables.mapValues { fromUntypedValue(it) }
      }
    )


  override fun correlateMessage(messageName: String, businessKey: String, correlationKeys: MutableMap<String, Any>, processVariables: MutableMap<String, Any>) =
    runtimeServiceClient.correlateMessage(
      CorrelationMessageDto().apply {
        this.messageName = messageName
        this.businessKey = businessKey
        this.correlationKeys = correlationKeys.mapValues { fromUntypedValue(it) }
        this.processVariables = processVariables.mapValues { fromUntypedValue(it) }
      }
    )

  override fun correlateMessage(messageName: String, businessKey: String, processVariables: MutableMap<String, Any>) =
    runtimeServiceClient.correlateMessage(
      CorrelationMessageDto().apply {
        this.messageName = messageName
        this.businessKey = businessKey
        this.processVariables = processVariables.mapValues { fromUntypedValue(it.value) }
      }
    )

  override fun createMessageCorrelation(messageName: String) =
    DelegatingMessageCorrelationBuilder(messageName, runtimeServiceClient)
}
