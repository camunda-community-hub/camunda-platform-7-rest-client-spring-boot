package org.camunda.community.rest.impl

import com.fasterxml.jackson.databind.ObjectMapper
import org.camunda.bpm.engine.variable.type.ValueTypeResolver
import org.camunda.community.rest.adapter.AbstractDecisionServiceAdapter
import org.camunda.community.rest.client.api.DecisionDefinitionApiClient
import org.camunda.community.rest.impl.builder.DelegatingDecisionEvaluationBuilder
import org.camunda.community.rest.impl.builder.DelegatingDecisionsEvaluationBuilder
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

/**
 * Remote implementation of Camunda Core DecisionService API, delegating
 * all request over HTTP to a remote Camunda Engine.
 */
@Component
@Qualifier("remote")
class RemoteDecisionService(
  private val decisionApiClient: DecisionDefinitionApiClient,
  private val objectMapper: ObjectMapper,
  private val valueTypeResolver: ValueTypeResolver,
) : AbstractDecisionServiceAdapter() {

  override fun evaluateDecisionById(decisionDefinitionId: String?) = DelegatingDecisionsEvaluationBuilder(decisionApiClient, objectMapper, valueTypeResolver, decisionDefinitionId = decisionDefinitionId)

  override fun evaluateDecisionByKey(decisionDefinitionKey: String?) = DelegatingDecisionsEvaluationBuilder(decisionApiClient, objectMapper, valueTypeResolver, decisionDefinitionKey = decisionDefinitionKey)

  override fun evaluateDecisionTableById(decisionDefinitionId: String?) = DelegatingDecisionEvaluationBuilder(decisionApiClient, objectMapper, valueTypeResolver, decisionDefinitionId = decisionDefinitionId)

  override fun evaluateDecisionTableByKey(decisionDefinitionKey: String?) = DelegatingDecisionEvaluationBuilder(decisionApiClient, objectMapper, valueTypeResolver, decisionDefinitionKey = decisionDefinitionKey)

  override fun evaluateDecisionTableById(decisionDefinitionId: String?, variables: MutableMap<String, Any>?) =
    DelegatingDecisionEvaluationBuilder(decisionApiClient, objectMapper, valueTypeResolver, decisionDefinitionId = decisionDefinitionId).variables(variables ?: mutableMapOf()).evaluate()

  override fun evaluateDecisionTableByKey(decisionDefinitionKey: String?, variables: MutableMap<String, Any>?) =
    DelegatingDecisionEvaluationBuilder(decisionApiClient, objectMapper, valueTypeResolver, decisionDefinitionKey = decisionDefinitionKey).variables(variables ?: mutableMapOf()).evaluate()

  override fun evaluateDecisionTableByKeyAndVersion(decisionDefinitionKey: String?, version: Int?, variables: MutableMap<String, Any>?) =
    DelegatingDecisionEvaluationBuilder(decisionApiClient, objectMapper, valueTypeResolver, decisionDefinitionKey = decisionDefinitionKey)
      .version(version)
      .variables(variables ?: mutableMapOf()).evaluate()

}
