package org.camunda.community.rest.impl.builder

import com.fasterxml.jackson.databind.ObjectMapper
import mu.KLogging
import org.camunda.bpm.dmn.engine.DmnDecisionTableResult
import org.camunda.bpm.dmn.engine.impl.DmnDecisionResultEntriesImpl
import org.camunda.bpm.dmn.engine.impl.DmnDecisionResultImpl
import org.camunda.bpm.dmn.engine.impl.DmnDecisionRuleResultImpl
import org.camunda.bpm.dmn.engine.impl.DmnDecisionTableResultImpl
import org.camunda.bpm.engine.BadUserRequestException
import org.camunda.bpm.engine.ProcessEngine
import org.camunda.bpm.engine.dmn.DecisionEvaluationBuilder
import org.camunda.bpm.engine.dmn.DecisionsEvaluationBuilder
import org.camunda.bpm.engine.exception.NotValidException
import org.camunda.bpm.engine.impl.util.EnsureUtil
import org.camunda.bpm.engine.variable.value.TypedValue
import org.camunda.community.rest.client.api.DecisionDefinitionApiClient
import org.camunda.community.rest.client.model.EvaluateDecisionDto
import org.camunda.community.rest.variables.ValueMapper

/**
 * Decision evaluation builder, collecting all settings in the DTO sent to the REST endpoint later.
 */
abstract class AbstractDecisionEvaluationBuilder<T : AbstractDecisionEvaluationBuilder<T>>(
  private val decisionDefinitionApiClient: DecisionDefinitionApiClient,
  processEngine: ProcessEngine,
  objectMapper: ObjectMapper,
  private val decisionDefinitionId: String? = null,
  private val decisionDefinitionKey: String? = null
) {

  companion object : KLogging()

  private val valueMapper: ValueMapper = ValueMapper(processEngine, objectMapper)

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
    EnsureUtil.ensureOnlyOneNotNull(
      NotValidException::class.java,
      "either decision definition id or key must be set",
      decisionDefinitionId,
      decisionDefinitionKey
    )
    if (tenantIdSet && decisionDefinitionId != null) {
      throw BadUserRequestException("Cannot specify a tenant-id when evaluate a decision definition by decision definition id.")
    }

    val evaluateDecisionDto = EvaluateDecisionDto().variables(valueMapper.mapValues(this.variables))
    val result = if (decisionDefinitionKey != null) {
      if (version != null) {
        TODO("Not yet implemented")
      } else if (tenantIdSet) {
        if (tenantId == null) {
          TODO("Not yet implemented")
        } else {
          decisionDefinitionApiClient.evaluateDecisionByKeyAndTenant(decisionDefinitionKey, tenantId, evaluateDecisionDto)
        }
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
  processEngine: ProcessEngine,
  objectMapper: ObjectMapper,
  decisionDefinitionId: String? = null,
  decisionDefinitionKey: String? = null
) : DecisionEvaluationBuilder, AbstractDecisionEvaluationBuilder<DelegatingDecisionEvaluationBuilder>(
  decisionDefinitionApiClient,
  processEngine,
  objectMapper,
  decisionDefinitionId,
  decisionDefinitionKey
) {

  override fun evaluate(): DmnDecisionTableResult {
    return DmnDecisionTableResultImpl(
      evaluateDecision().map {
        DmnDecisionRuleResultImpl().apply {
          it.entries.forEach { entry -> putValue(entry.key, entry.value) }
        }
      }
    )
  }

}

class DelegatingDecisionsEvaluationBuilder(
  decisionDefinitionApiClient: DecisionDefinitionApiClient,
  processEngine: ProcessEngine,
  objectMapper: ObjectMapper,
  decisionDefinitionId: String? = null,
  decisionDefinitionKey: String? = null
) : DecisionsEvaluationBuilder, AbstractDecisionEvaluationBuilder<DelegatingDecisionsEvaluationBuilder>(
  decisionDefinitionApiClient,
  processEngine,
  objectMapper,
  decisionDefinitionId,
  decisionDefinitionKey
) {

  override fun evaluate(): DmnDecisionResultImpl {
    return DmnDecisionResultImpl(
      evaluateDecision().map {
        DmnDecisionResultEntriesImpl().apply {
          it.entries.forEach { entry -> putValue(entry.key, entry.value) }
        }
      }
    )
  }

}
