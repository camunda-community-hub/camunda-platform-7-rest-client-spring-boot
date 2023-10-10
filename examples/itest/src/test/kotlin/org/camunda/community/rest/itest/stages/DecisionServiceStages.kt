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
import io.toolisticon.testing.jgiven.step
import org.assertj.core.api.Assertions.assertThat
import org.camunda.bpm.dmn.engine.DmnDecisionResult
import org.camunda.bpm.dmn.engine.DmnDecisionTableResult
import org.camunda.bpm.engine.DecisionService
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.repository.DecisionDefinition
import org.camunda.bpm.engine.repository.Deployment
import org.camunda.bpm.model.dmn.Dmn
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import java.io.InputStream


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

  fun drd_is_deployed(): DecisionServiceActionStage {

    val fileName = "drd.dmn"

    this.javaClass.classLoader.getResourceAsStream("$fileName.tpl")?.let {
      deploy(it, fileName)
    } ?: throw IllegalStateException("$fileName.tpl not found")

    return self()
  }

  fun decision_table_is_deployed(
    version: String? = null,
    tenantId: String? = null
  ): DecisionServiceActionStage {

    val fileName = "decision_table_v${version ?: "1"}.dmn"

    val deployment = this.javaClass.classLoader.getResourceAsStream("$fileName.tpl")?.let {
      deploy(it, fileName, tenantId)
    } ?: throw IllegalStateException("$fileName.tpl not found")

    decisionDefinition = repositoryService
      .createDecisionDefinitionQuery()
      .deploymentId(deployment.id)
      .singleResult()

    return self()
  }

  private fun deploy(inputStream: InputStream, resourceName: String, tenantId: String? = null): Deployment {

    val modelInstance = Dmn.readModelFromStream(inputStream)

    return repositoryService
      .createDeployment()
      .addModelInstance(resourceName, modelInstance)
      .name("decision_deployment")
      .tenantId(tenantId)
      .deploy()

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

  fun decision_table_is_evaluated_by_key_and_version(decisionDefinitionKey: String = "decision_table", version: Int, variables: MutableMap<String, Any>): DecisionServiceActionStage {
    decisionTableResult = remoteService.evaluateDecisionTableByKeyAndVersion(decisionDefinitionKey, version, variables)
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



