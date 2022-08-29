package org.camunda.community.rest.adapter

import org.camunda.bpm.dmn.engine.DmnDecisionTableResult
import org.camunda.bpm.engine.DecisionService
import org.camunda.bpm.engine.dmn.DecisionEvaluationBuilder
import org.camunda.bpm.engine.dmn.DecisionsEvaluationBuilder
import org.camunda.community.rest.impl.RemoteDecisionService
import org.camunda.community.rest.impl.implementedBy

abstract class AbstractDecisionServiceAdapter : DecisionService {

  override fun evaluateDecisionTableById(decisionDefinitionId: String?, variables: MutableMap<String, Any>?): DmnDecisionTableResult {
    implementedBy(RemoteDecisionService::class)
  }

  override fun evaluateDecisionTableById(decisionDefinitionId: String?): DecisionEvaluationBuilder {
    implementedBy(RemoteDecisionService::class)
  }

  override fun evaluateDecisionTableByKey(decisionDefinitionKey: String?, variables: MutableMap<String, Any>?): DmnDecisionTableResult {
    implementedBy(RemoteDecisionService::class)
  }

  override fun evaluateDecisionTableByKey(decisionDefinitionKey: String?): DecisionEvaluationBuilder {
    implementedBy(RemoteDecisionService::class)
  }

  override fun evaluateDecisionTableByKeyAndVersion(decisionDefinitionKey: String?, version: Int?, variables: MutableMap<String, Any>?): DmnDecisionTableResult {
    implementedBy(RemoteDecisionService::class)
  }

  override fun evaluateDecisionByKey(decisionDefinitionKey: String?): DecisionsEvaluationBuilder {
    implementedBy(RemoteDecisionService::class)
  }

  override fun evaluateDecisionById(decisionDefinitionId: String?): DecisionsEvaluationBuilder {
    implementedBy(RemoteDecisionService::class)
  }

}
