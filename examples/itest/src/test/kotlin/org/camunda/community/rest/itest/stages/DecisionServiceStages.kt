/*-
 * #%L
 * camunda-platform-7-rest-client-spring-boot-itest
 * %%
 * Copyright (C) 2019 Camunda Services GmbH
 * %%
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 *  under one or more contributor license agreements. See the NOTICE file
 *  distributed with this work for additional information regarding copyright
 *  ownership. Camunda licenses this file to you under the Apache License,
 *  Version 2.0; you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * #L%
 */
package org.camunda.community.rest.itest.stages

import com.tngtech.jgiven.annotation.ProvidedScenarioState
import com.tngtech.jgiven.annotation.ScenarioState
import com.tngtech.jgiven.integration.spring.JGivenStage
import io.holunda.decision.model.CamundaDecisionGenerator.diagram
import io.holunda.decision.model.CamundaDecisionGenerator.rule
import io.holunda.decision.model.CamundaDecisionGenerator.table
import io.holunda.decision.model.CamundaDecisionModel
import io.holunda.decision.model.FeelConditions.feelEqual
import io.holunda.decision.model.FeelConditions.resultValue
import io.holunda.decision.model.api.CamundaDecisionModelApi.InputDefinitions.stringInput
import io.holunda.decision.model.api.CamundaDecisionModelApi.OutputDefinitions.stringOutput
import io.toolisticon.testing.jgiven.step
import org.assertj.core.api.Assertions.assertThat
import org.camunda.bpm.dmn.engine.DmnDecisionResult
import org.camunda.bpm.dmn.engine.DmnDecisionTableResult
import org.camunda.bpm.engine.DecisionService
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.repository.DecisionDefinition
import org.camunda.bpm.model.dmn.Dmn
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier


@JGivenStage
class DecisionServiceActionStage : ActionStage<DecisionServiceActionStage, DecisionService>() {

  @Autowired
  @ProvidedScenarioState
  lateinit var repositoryService: RepositoryService

  @Autowired
  @Qualifier("remote")
  @ProvidedScenarioState(resolution = ScenarioState.Resolution.NAME)
  override lateinit var remoteService: DecisionService

  @Autowired
  @Qualifier("decisionService")
  @ProvidedScenarioState(resolution = ScenarioState.Resolution.NAME)
  override lateinit var localService: DecisionService

  @ProvidedScenarioState(resolution = ScenarioState.Resolution.TYPE)
  lateinit var decisionDefinition: DecisionDefinition

  @ProvidedScenarioState(resolution = ScenarioState.Resolution.TYPE)
  lateinit var decisionResult: DmnDecisionResult

  @ProvidedScenarioState(resolution = ScenarioState.Resolution.TYPE)
  lateinit var decisionTableResult: DmnDecisionTableResult

  fun no_deployment_exists() = step {
    repositoryService.createDeploymentQuery().list().map {
      repositoryService.deleteDeployment(it.id, true)
    }
  }

  fun decision_table_is_deployed(
    decisionDefinitionKey: String = "decision_table",
    tenantId: String? = null
  ): DecisionServiceActionStage {

    val diagramBuilder = diagram("Test")
      .id("test")
      .addDecisionTable(
        table(decisionDefinitionKey)
          .name("A generated table")
          .versionTag("1")
          .addRule(rule()
            .condition(stringInput("input1", "Input 1").feelEqual("Rule1"))
            .description("test")
            .result(stringOutput("output1", "Output 1").resultValue("Rule1Output"))
          )
      )

    val diagram = diagramBuilder.build()

    val modelInstance = Dmn.readModelFromStream(CamundaDecisionModel.createXml(diagram).byteInputStream())

    val deployment = repositoryService
      .createDeployment()
      .addModelInstance("$decisionDefinitionKey.dmn", modelInstance)
      .name("decision_table")
      .tenantId(tenantId)
      .deploy()

    decisionDefinition = repositoryService
      .createDecisionDefinitionQuery()
      .deploymentId(deployment.id)
      .singleResult()

    return self()
  }

  fun decision_is_evaluated_by_id(variables: MutableMap<String, Any>): DecisionServiceActionStage {
    decisionResult = remoteService.evaluateDecisionById(decisionDefinition.id).variables(variables).evaluate()
    return self()
  }

  fun decision_is_evaluated_by_key(decisionDefinitionKey: String = "decision_table", variables: MutableMap<String, Any>): DecisionServiceActionStage {
    decisionResult = remoteService.evaluateDecisionByKey(decisionDefinitionKey).variables(variables).evaluate()
    return self()
  }

  fun decision_table_is_evaluated_by_id(variables: MutableMap<String, Any>): DecisionServiceActionStage {
    decisionTableResult = remoteService.evaluateDecisionTableById(decisionDefinition.id, variables)
    return self()
  }

  fun decision_table_is_evaluated_by_key(decisionDefinitionKey: String = "decision_table", variables: MutableMap<String, Any>): DecisionServiceActionStage {
    decisionTableResult = remoteService.evaluateDecisionTableByKey(decisionDefinitionKey, variables)
    return self()
  }

  fun decision_table_is_evaluated_by_key_and_tenant(decisionDefinitionKey: String = "decision_table", tenantId: String? = null,
                                                    variables: MutableMap<String, Any>): DecisionServiceActionStage {
    val builder = remoteService.evaluateDecisionTableByKey(decisionDefinitionKey).variables(variables)
    tenantId?.let { builder.decisionDefinitionTenantId(tenantId) } ?: builder.decisionDefinitionWithoutTenantId()
    decisionTableResult = builder.evaluate()
    return self()
  }


}

@JGivenStage
class DecisionServiceAssertStage : AssertStage<DecisionServiceAssertStage, DecisionService>() {

  @ProvidedScenarioState(resolution = ScenarioState.Resolution.TYPE)
  lateinit var decisionResult: DmnDecisionResult

  @ProvidedScenarioState(resolution = ScenarioState.Resolution.TYPE)
  lateinit var decisionTableResult: DmnDecisionTableResult

  fun decision_result_is_correct(variables: MutableMap<String, Any>): DecisionServiceAssertStage {
    assertThat(decisionResult.firstResult).containsAllEntriesOf(variables)
    return self()
  }

  fun decision_table_result_is_correct(variables: MutableMap<String, Any>): DecisionServiceAssertStage {
    assertThat(decisionTableResult.firstResult).containsAllEntriesOf(variables)
    return self()
  }

}



