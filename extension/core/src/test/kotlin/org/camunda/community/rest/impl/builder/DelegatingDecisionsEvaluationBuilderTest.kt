package org.camunda.community.rest.impl.builder

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.assertj.core.api.Assertions
import org.camunda.community.rest.client.api.DecisionDefinitionApiClient
import org.camunda.community.rest.client.model.VariableValueDto
import org.camunda.community.rest.variables.SpinValueMapper
import org.camunda.community.rest.variables.ValueMapper
import org.camunda.community.rest.variables.ValueTypeResolverImpl
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.http.ResponseEntity

internal class DelegatingDecisionsEvaluationBuilderTest {

  private val decisionDefinitionApiClient = mock<DecisionDefinitionApiClient>()
  private val valueTypeResolver = ValueTypeResolverImpl()
  private val valueMapper = ValueMapper(
    objectMapper = jacksonObjectMapper(),
    valueTypeResolver = valueTypeResolver,
    customValueMapper = listOf(SpinValueMapper(valueTypeResolver))
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
