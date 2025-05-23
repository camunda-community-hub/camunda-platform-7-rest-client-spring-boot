package org.camunda.community.rest.impl

import org.camunda.community.rest.adapter.AbstractDecisionServiceAdapter
import org.camunda.community.rest.client.api.DecisionDefinitionApiClient
import org.camunda.community.rest.impl.builder.DelegatingDecisionEvaluationBuilder
import org.camunda.community.rest.impl.builder.DelegatingDecisionsEvaluationBuilder
import org.camunda.community.rest.variables.ValueMapper
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

/**
 * Remote implementation of Camunda Core DecisionService API, delegating
 * all request over HTTP to a remote Camunda Engine.
 */
@Component
@Qualifier("remote")
class RemoteDecisionService(
  private val decisionDefinitionApiClient: DecisionDefinitionApiClient,
  private val valueMapper: ValueMapper
) : AbstractDecisionServiceAdapter() {

  override fun evaluateDecisionById(decisionDefinitionId: String?) = DelegatingDecisionsEvaluationBuilder(
    decisionDefinitionApiClient = decisionDefinitionApiClient,
    valueMapper = valueMapper,
    decisionDefinitionId = decisionDefinitionId
  )

  override fun evaluateDecisionByKey(decisionDefinitionKey: String?) = DelegatingDecisionsEvaluationBuilder(
    decisionDefinitionApiClient = decisionDefinitionApiClient,
    valueMapper = valueMapper,
    decisionDefinitionKey = decisionDefinitionKey
  )

  override fun evaluateDecisionTableById(decisionDefinitionId: String?) = DelegatingDecisionEvaluationBuilder(
    decisionDefinitionApiClient = decisionDefinitionApiClient,
    valueMapper = valueMapper,
    decisionDefinitionId = decisionDefinitionId
  )

  override fun evaluateDecisionTableByKey(decisionDefinitionKey: String?) = DelegatingDecisionEvaluationBuilder(
    decisionDefinitionApiClient = decisionDefinitionApiClient,
    valueMapper = valueMapper,
    decisionDefinitionKey = decisionDefinitionKey
  )

  override fun evaluateDecisionTableById(decisionDefinitionId: String?, variables: MutableMap<String, Any>?) =
    DelegatingDecisionEvaluationBuilder(
      decisionDefinitionApiClient = decisionDefinitionApiClient,
      valueMapper = valueMapper,
      decisionDefinitionId = decisionDefinitionId
    ).variables(variables ?: mutableMapOf()).evaluate()

  override fun evaluateDecisionTableByKey(decisionDefinitionKey: String?, variables: MutableMap<String, Any>?) =
    DelegatingDecisionEvaluationBuilder(
      decisionDefinitionApiClient = decisionDefinitionApiClient,
      valueMapper = valueMapper,
      decisionDefinitionKey = decisionDefinitionKey
    ).variables(variables ?: mutableMapOf()).evaluate()

  override fun evaluateDecisionTableByKeyAndVersion(decisionDefinitionKey: String?, version: Int?, variables: MutableMap<String, Any>?) =
    DelegatingDecisionEvaluationBuilder(
      decisionDefinitionApiClient = decisionDefinitionApiClient,
      valueMapper = valueMapper,
      decisionDefinitionKey = decisionDefinitionKey
    ).version(version).variables(variables ?: mutableMapOf()).evaluate()

}
