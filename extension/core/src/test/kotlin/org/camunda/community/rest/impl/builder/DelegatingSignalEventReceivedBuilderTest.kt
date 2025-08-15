package org.camunda.community.rest.impl.builder

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.camunda.community.rest.client.api.SignalApiClient
import org.camunda.community.rest.variables.ValueMapper
import org.camunda.community.rest.variables.ValueTypeResolverImpl
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.ResponseEntity

class DelegatingSignalEventReceivedBuilderTest {

  val signalApiClient = mock<SignalApiClient>()

  val builder = DelegatingSignalEventReceivedBuilder(
    signalName = "signalName",
    signalApiClient = signalApiClient,
    valueMapper = ValueMapper(
      objectMapper = jacksonObjectMapper(),
      valueTypeResolver = ValueTypeResolverImpl(),
      customValueMappers = emptyList()
    )
  ).apply {
    this.tenantId("tenantId")
    this.withoutTenantId()
    this.executionId("executionId")
    this.setVariables(mutableMapOf("var" to "value"))
  }

  @Test
  fun send() {
    whenever(signalApiClient.throwSignal(any())).thenReturn(
      ResponseEntity.ok(null)
    )
    builder.send()
    verify(signalApiClient).throwSignal(any())
  }

}
