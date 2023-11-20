package org.camunda.community.rest.impl.builder

import com.fasterxml.jackson.databind.ObjectMapper
import mu.KLogging
import org.camunda.bpm.dmn.engine.DmnDecisionResult
import org.camunda.bpm.engine.BadUserRequestException
import org.camunda.bpm.engine.ProcessEngine
import org.camunda.bpm.engine.dmn.DecisionEvaluationBuilder
import org.camunda.bpm.engine.dmn.DecisionsEvaluationBuilder
import org.camunda.bpm.engine.exception.NotFoundException
import org.camunda.bpm.engine.exception.NotValidException
import org.camunda.bpm.engine.variable.type.ValueTypeResolver
import org.camunda.bpm.engine.variable.value.TypedValue
import org.camunda.community.rest.client.api.DecisionDefinitionApiClient
import org.camunda.community.rest.client.model.EvaluateDecisionDto
import org.camunda.community.rest.impl.builder.decision.DelegatingDmnDecisionResult
import org.camunda.community.rest.impl.builder.decision.DelegatingDmnDecisionResultEntries
import org.camunda.community.rest.impl.builder.decision.DelegatingDmnDecisionRuleResult
import org.camunda.community.rest.impl.builder.decision.DelegatingDmnDecisionTableResult
import org.camunda.community.rest.variables.ValueMapper

/**
 * Decision evaluation builder, collecting all settings in the DTO sent to the REST endpoint later.
 */
abstract class AbstractDecisionEvaluationBuilder<T : AbstractDecisionEvaluationBuilder<T>>(
  private val decisionDefinitionApiClient: DecisionDefinitionApiClient,
  objectMapper: ObjectMapper,
  valueTypeResolver: ValueTypeResolver,
  private val decisionDefinitionId: String? = null,
  private val decisionDefinitionKey: String? = null
) {

  companion object : KLogging()

  private val valueMapper: ValueMapper = ValueMapper(objectMapper, valueTypeResolver)

  var tenantIdSet: Boolean = false
  var tenantId: String? = null
  var version: Int? = null
  var variables: MutableMap<String, Any> = mutableMapOf()

  @Suppress("UNCHECKED_CAST")
  fun decisionDefinitionTenantId(tenantId: String) = this.apply {
    this.tenantId = tenantId
    this.tenantIdSet = true
  } as T

  @Suppress("UNCHECKED_CAST")
  fun decisionDefinitionWithoutTenantId() = this.apply {
    this.tenantId = null
    this.tenantIdSet = true
  } as T

  @Suppress("UNCHECKED_CAST")
  fun version(version: Int?) = this.apply { this.version = version } as T

  @Suppress("UNCHECKED_CAST")
  fun variables(variables: MutableMap<String, Any>) = this.apply { this.variables = variables } as T

  fun evaluateDecision(): List<Map<String, TypedValue>>  {
    if (!((decisionDefinitionId != null) xor (decisionDefinitionKey != null))) {
      throw NotValidException("either decision definition id or key must be set")
    }
    if (tenantIdSet && decisionDefinitionId != null) {
      throw BadUserRequestException("Cannot specify a tenant-id when evaluate a decision definition by decision definition id.")
    }

    val evaluateDecisionDto = EvaluateDecisionDto().variables(valueMapper.mapValues(this.variables))
    val result = if (decisionDefinitionKey != null) {
      if (version != null) {
        val decisionDefinitions = decisionDefinitionApiClient.getDecisionDefinitions(
          null, null, null, null, null,
          null, null, null, null, null, null, decisionDefinitionKey, null,
          null, null, version, null, null, null, null,
          null, null, null, null,
          null, null, null
        )
        if (decisionDefinitions.body?.size != 1) {
          throw NotFoundException("No decision for key $decisionDefinitionKey and version $version found")
        }
        decisionDefinitionApiClient.evaluateDecisionById(decisionDefinitions.body!![0].id, evaluateDecisionDto)
      } else if (tenantId != null) {
        decisionDefinitionApiClient.evaluateDecisionByKeyAndTenant(decisionDefinitionKey, tenantId, evaluateDecisionDto)
      } else {
        decisionDefinitionApiClient.evaluateDecisionByKey(decisionDefinitionKey, evaluateDecisionDto)
      }
    } else {
      decisionDefinitionApiClient.evaluateDecisionById(decisionDefinitionId, evaluateDecisionDto)
    }
    return result.body?.map {
      it.mapValues { v -> valueMapper.mapDto<TypedValue>(v.value)!! }
    } ?: emptyList()
  }

}

class DelegatingDecisionEvaluationBuilder(
  decisionDefinitionApiClient: DecisionDefinitionApiClient,
  objectMapper: ObjectMapper,
  valueTypeResolver: ValueTypeResolver,
  decisionDefinitionId: String? = null,
  decisionDefinitionKey: String? = null
) : DecisionEvaluationBuilder, AbstractDecisionEvaluationBuilder<DelegatingDecisionEvaluationBuilder>(
  decisionDefinitionApiClient,
  objectMapper,
  valueTypeResolver,
  decisionDefinitionId,
  decisionDefinitionKey
) {

  override fun evaluate() =
    DelegatingDmnDecisionTableResult(
      evaluateDecision().map {
        DelegatingDmnDecisionRuleResult(it)
      }
    )

}

class DelegatingDecisionsEvaluationBuilder(
  decisionDefinitionApiClient: DecisionDefinitionApiClient,
  objectMapper: ObjectMapper,
  valueTypeResolver: ValueTypeResolver,
  decisionDefinitionId: String? = null,
  decisionDefinitionKey: String? = null
) : DecisionsEvaluationBuilder, AbstractDecisionEvaluationBuilder<DelegatingDecisionsEvaluationBuilder>(
  decisionDefinitionApiClient,
  objectMapper,
  valueTypeResolver,
  decisionDefinitionId,
  decisionDefinitionKey
) {

  override fun evaluate(): DmnDecisionResult {
    return DelegatingDmnDecisionResult(
      evaluateDecision().map {
        DelegatingDmnDecisionResultEntries(it)
      }
    )
  }

}
