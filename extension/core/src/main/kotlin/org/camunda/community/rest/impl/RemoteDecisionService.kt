package org.camunda.community.rest.impl

import com.fasterxml.jackson.databind.ObjectMapper
import org.camunda.bpm.engine.ProcessEngine
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
  private val processEngine: ProcessEngine,
  private val objectMapper: ObjectMapper,
) : AbstractDecisionServiceAdapter() {

  override fun evaluateDecisionById(decisionDefinitionId: String?) = DelegatingDecisionsEvaluationBuilder(decisionApiClient, processEngine, objectMapper, decisionDefinitionId = decisionDefinitionId)

  override fun evaluateDecisionByKey(decisionDefinitionKey: String?) = DelegatingDecisionsEvaluationBuilder(decisionApiClient, processEngine, objectMapper, decisionDefinitionKey = decisionDefinitionKey)

  override fun evaluateDecisionTableById(decisionDefinitionId: String?) = DelegatingDecisionEvaluationBuilder(decisionApiClient, processEngine, objectMapper, decisionDefinitionId = decisionDefinitionId)

  override fun evaluateDecisionTableByKey(decisionDefinitionKey: String?) = DelegatingDecisionEvaluationBuilder(decisionApiClient, processEngine, objectMapper, decisionDefinitionKey = decisionDefinitionKey)

  override fun evaluateDecisionTableById(decisionDefinitionId: String?, variables: MutableMap<String, Any>?) =
    DelegatingDecisionEvaluationBuilder(decisionApiClient, processEngine, objectMapper, decisionDefinitionId = decisionDefinitionId).variables(variables ?: mutableMapOf()).evaluate()

  override fun evaluateDecisionTableByKey(decisionDefinitionKey: String?, variables: MutableMap<String, Any>?) =
    DelegatingDecisionEvaluationBuilder(decisionApiClient, processEngine, objectMapper, decisionDefinitionKey = decisionDefinitionKey).variables(variables ?: mutableMapOf()).evaluate()

  override fun evaluateDecisionTableByKeyAndVersion(decisionDefinitionKey: String?, version: Int?, variables: MutableMap<String, Any>?) =
    DelegatingDecisionEvaluationBuilder(decisionApiClient, processEngine, objectMapper, decisionDefinitionKey = decisionDefinitionKey)
      .version(version)
      .variables(variables ?: mutableMapOf()).evaluate()

}
