package org.camunda.community.rest.impl.builder

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.camunda.bpm.engine.variable.Variables
import org.camunda.community.rest.client.api.SignalApiClient
import org.camunda.community.rest.variables.ValueMapper
import org.camunda.community.rest.variables.ValueTypeRegistration
import org.camunda.community.rest.variables.ValueTypeResolverImpl
import org.camunda.community.rest.variables.serialization.JsonValueSerializer
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.ResponseEntity

class DelegatingSignalEventReceivedBuilderTest {

  val signalApiClient = mock<SignalApiClient>()

  private val objectMapper = jacksonObjectMapper()
  private val typeResolver = ValueTypeResolverImpl()
  private val typeRegistration = ValueTypeRegistration()

  val builder = DelegatingSignalEventReceivedBuilder(
    signalName = "signalName",
    signalApiClient = signalApiClient,
    valueMapper = ValueMapper(
      objectMapper = objectMapper,
      valueTypeResolver = typeResolver,
      valueTypeRegistration = typeRegistration,
      valueSerializers = listOf(JsonValueSerializer(objectMapper)),
      serializationFormat = Variables.SerializationDataFormats.JSON,
      customValueSerializers = listOf()
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
