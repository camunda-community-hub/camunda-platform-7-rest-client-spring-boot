package org.camunda.community.rest.impl.builder

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.camunda.bpm.engine.BadUserRequestException
import org.camunda.bpm.engine.exception.NotFoundException
import org.camunda.bpm.engine.exception.NotValidException
import org.camunda.community.rest.client.api.DecisionDefinitionApiClient
import org.camunda.community.rest.client.model.DecisionDefinitionDto
import org.camunda.community.rest.client.model.VariableValueDto
import org.camunda.community.rest.variables.ValueTypeResolverImpl
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.isNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.http.ResponseEntity

class DelegatingDecisionEvaluationBuilderTest {

  val decisionDefinitionApiClient = mock<DecisionDefinitionApiClient>()

  val builder = DelegatingDecisionEvaluationBuilder(
    decisionDefinitionApiClient = decisionDefinitionApiClient,
    objectMapper = jacksonObjectMapper(),
    valueTypeResolver = ValueTypeResolverImpl(),
    decisionDefinitionKey = "decisionDefinitionKey"
  ).apply {
    this.variables(mutableMapOf("var" to "value"))
    this.decisionDefinitionWithoutTenantId()
  }

  @Test
  fun cannotSupplyDecisionDefinitionIdAndKey() {
    val builder = DelegatingDecisionEvaluationBuilder(
      decisionDefinitionApiClient = decisionDefinitionApiClient,
      objectMapper = jacksonObjectMapper(),
      valueTypeResolver = ValueTypeResolverImpl(),
      decisionDefinitionKey = "decisionDefinitionKey",
      decisionDefinitionId = "decisionDefinitionId"
    )
    assertThatThrownBy { builder.evaluate() }.isInstanceOf(NotValidException::class.java).hasMessage(
      "either decision definition id or key must be set"
    )
  }

  @Test
  fun cannotUseTenantWithDecisionDefinitionId() {
    val builder = DelegatingDecisionEvaluationBuilder(
      decisionDefinitionApiClient = decisionDefinitionApiClient,
      objectMapper = jacksonObjectMapper(),
      valueTypeResolver = ValueTypeResolverImpl(),
      decisionDefinitionId = "decisionDefinitionId"
    ).apply {
      this.decisionDefinitionTenantId("tenantId")
    }
    assertThatThrownBy { builder.evaluate() }.isInstanceOf(BadUserRequestException::class.java).hasMessage(
      "Cannot specify a tenant-id when evaluate a decision definition by decision definition id."
    )
  }

  @Test
  fun testEvaluateDecisionWithKeyAndVersion() {
    builder.version(1)
    whenever(decisionDefinitionApiClient.getDecisionDefinitions(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
      isNull(), isNull(), isNull(), isNull(), eq("decisionDefinitionKey"), isNull(), isNull(), isNull(), eq(1), isNull(), isNull(), isNull(),
      isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull())
    ).thenReturn(
      ResponseEntity.ok(listOf(DecisionDefinitionDto().id("decisionDefinitionId")))
    )
    whenever(decisionDefinitionApiClient.evaluateDecisionById(eq("decisionDefinitionId"), any())).thenReturn(
      ResponseEntity.ok(listOf(mapOf("resultVar" to VariableValueDto().value("resultValue").type("string"))))
    )
    val result = builder.evaluate()
    assertThat(result).hasSize(1)
  }

  @Test
  fun testEvaluateDecisionWithKeyAndVersionNotFound() {
    builder.version(1)
    whenever(decisionDefinitionApiClient.getDecisionDefinitions(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
      isNull(), isNull(), isNull(), isNull(), eq("decisionDefinitionKey"), isNull(), isNull(), isNull(), eq(1), isNull(), isNull(), isNull(),
      isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull())
    ).thenReturn(
      ResponseEntity.ok(listOf())
    )
    assertThatThrownBy { builder.evaluate() }.isInstanceOf(NotFoundException::class.java).hasMessage(
      "No decision for key decisionDefinitionKey and version 1 found"
    )
  }

  @Test
  fun testEvaluateDecisionWithKeyAndTenant() {
    builder.decisionDefinitionTenantId("tenantId")
    whenever(decisionDefinitionApiClient.evaluateDecisionByKeyAndTenant(eq("decisionDefinitionKey"), eq("tenantId"), any())).thenReturn(
      ResponseEntity.ok(listOf(mapOf("resultVar" to VariableValueDto().value("resultValue").type("string"))))
    )
    val result = builder.evaluate()
    assertThat(result).hasSize(1)
  }

  @Test
  fun testEvaluateDecisionWithKey() {
    whenever(decisionDefinitionApiClient.evaluateDecisionByKey(eq("decisionDefinitionKey"), any())).thenReturn(
      ResponseEntity.ok(listOf(mapOf("resultVar" to VariableValueDto().value("resultValue").type("string"))))
    )
    val result = builder.evaluate()
    assertThat(result).hasSize(1)
  }

  @Test
  fun testEvaluateByDecisionDefinitionId() {
    val builder = DelegatingDecisionEvaluationBuilder(
      decisionDefinitionApiClient = decisionDefinitionApiClient,
      objectMapper = jacksonObjectMapper(),
      valueTypeResolver = ValueTypeResolverImpl(),
      decisionDefinitionId = "decisionDefinitionId"
    )
    whenever(decisionDefinitionApiClient.evaluateDecisionById(eq("decisionDefinitionId"), any())).thenReturn(
      ResponseEntity.ok(listOf(mapOf("resultVar" to VariableValueDto().value("resultValue").type("string"))))
    )
    val result = builder.evaluate()
    assertThat(result).hasSize(1)
  }

}
