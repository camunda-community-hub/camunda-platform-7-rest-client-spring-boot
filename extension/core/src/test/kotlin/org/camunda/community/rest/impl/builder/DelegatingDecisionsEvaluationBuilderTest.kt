package org.camunda.community.rest.impl.builder

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.assertj.core.api.Assertions
import org.camunda.bpm.engine.variable.Variables
import org.camunda.community.rest.client.api.DecisionDefinitionApiClient
import org.camunda.community.rest.client.model.VariableValueDto
import org.camunda.community.rest.variables.serialization.SpinValueSerializer
import org.camunda.community.rest.variables.ValueMapper
import org.camunda.community.rest.variables.ValueTypeRegistration
import org.camunda.community.rest.variables.ValueTypeResolverImpl
import org.camunda.community.rest.variables.serialization.JsonValueSerializer
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.http.ResponseEntity

internal class DelegatingDecisionsEvaluationBuilderTest {

  private val decisionDefinitionApiClient = mock<DecisionDefinitionApiClient>()
  private val objectMapper = jacksonObjectMapper()
  private val typeResolver = ValueTypeResolverImpl()
  private val typeRegistration = ValueTypeRegistration()
  private val valueMapper = ValueMapper(
    objectMapper = objectMapper,
    valueTypeResolver = typeResolver,
    valueTypeRegistration = typeRegistration,
    valueSerializers = listOf(JsonValueSerializer(objectMapper)),
    serializationFormat = Variables.SerializationDataFormats.JSON,
    customValueSerializers = listOf(SpinValueSerializer(typeResolver, typeRegistration))
  )
  val builder = DelegatingDecisionsEvaluationBuilder(
    decisionDefinitionApiClient = decisionDefinitionApiClient,
    valueMapper = valueMapper,
    decisionDefinitionKey = "decisionDefinitionKey"
  ).apply {
    this.variables(mutableMapOf("var" to "value"))
    this.decisionDefinitionWithoutTenantId()
  }

  @Test
  fun testEvaluateDecisionWithKey() {
    whenever(decisionDefinitionApiClient.evaluateDecisionByKey(eq("decisionDefinitionKey"), any())).thenReturn(
      ResponseEntity.ok(listOf(mapOf("resultVar" to VariableValueDto().value("resultValue").type("string"))))
    )
    val result = builder.evaluate()
    Assertions.assertThat(result).hasSize(1)
  }

}
