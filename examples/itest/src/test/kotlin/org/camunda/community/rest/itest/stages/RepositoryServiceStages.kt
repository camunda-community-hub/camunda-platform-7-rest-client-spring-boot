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

import com.tngtech.jgiven.annotation.ExpectedScenarioState
import com.tngtech.jgiven.annotation.ProvidedScenarioState
import com.tngtech.jgiven.annotation.ScenarioState
import com.tngtech.jgiven.integration.spring.JGivenStage
import io.toolisticon.testing.jgiven.step
import org.assertj.core.api.Assertions.assertThat
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.repository.Deployment
import org.camunda.bpm.engine.repository.DeploymentQuery
import org.camunda.bpm.engine.repository.ProcessDefinition
import org.camunda.bpm.engine.repository.ProcessDefinitionQuery
import org.camunda.bpm.model.bpmn.Bpmn
import org.camunda.bpm.model.bpmn.BpmnModelInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import java.util.*

@JGivenStage
class RepositoryServiceActionStage : ActionStage<RepositoryServiceActionStage, RepositoryService>() {

  @Autowired
  @Qualifier("remote")
  @ProvidedScenarioState(resolution = ScenarioState.Resolution.NAME)
  override lateinit var remoteService: RepositoryService

  @Autowired
  @Qualifier("repositoryService")
  @ProvidedScenarioState(resolution = ScenarioState.Resolution.NAME)
  override lateinit var localService: RepositoryService

  @ProvidedScenarioState(resolution = ScenarioState.Resolution.TYPE)
  lateinit var deployment: Deployment

  @ProvidedScenarioState(resolution = ScenarioState.Resolution.TYPE)
  lateinit var processDefinition: ProcessDefinition

  fun no_deployment_exists() = step {
    localService.createDeploymentQuery().list().map {
      localService.deleteDeployment(it.id)
    }
  }

  fun process_is_deployed(
    processDefinitionKey: String, versionTag: String? = null,
    deploymentName: String = "deployment" + UUID.randomUUID().toString().replace("-", "")
  ) = step {

    val instance = Bpmn
      .createExecutableProcess(processDefinitionKey)
      .camundaVersionTag(versionTag)
      .startEvent("start")
      .endEvent("end")
      .done()

    deployment = localService
      .createDeployment()
      .addModelInstance("$processDefinitionKey.bpmn", instance)
      .name(deploymentName)
      .deploy()

    processDefinition = localService
      .createProcessDefinitionQuery()
      .deploymentId(deployment.id)
      .processDefinitionKey(processDefinitionKey)
      .singleResult()
  }

  fun process_definitions_are_deployed(
    deploymentName: String, processDefinitionKey: String, versionTag: String? = null, enableDuplicateFiltering: Boolean = false
  ) = step {
    val instance = Bpmn
      .createExecutableProcess(processDefinitionKey)
      .camundaVersionTag(versionTag)
      .startEvent("start")
      .endEvent("end")
      .done()

    val deployment = remoteService.createDeployment()
      .addModelInstance("$processDefinitionKey.bpmn", instance)
      .addClasspathResource("messages.bpmn")
      .source("itest")
      .name(deploymentName)

    if (enableDuplicateFiltering) {
      deployment.enableDuplicateFiltering(true)
    }

    deployment.deploy()
  }

  fun process_definition_is_suspended_by_key(processDefinitionKey: String) {
    remoteService.suspendProcessDefinitionByKey(processDefinitionKey)
  }

  fun process_definition_is_activated_by_key(processDefinitionKey: String) {
    remoteService.activateProcessDefinitionByKey(processDefinitionKey)
  }

  fun process_definition_is_suspended_by_id(processDefinitionId: String) {
    remoteService.suspendProcessDefinitionById(processDefinitionId)
  }

  fun process_definition_is_suspended_by_id() {
    remoteService.suspendProcessDefinitionById(processDefinition.id)
  }

  fun process_definition_is_activated_by_id(processDefinitionId: String) {
    remoteService.activateProcessDefinitionById(processDefinitionId)
  }

 fun process_definition_is_activated_by_id() {
    remoteService.activateProcessDefinitionById(processDefinition.id)
  }

}

@JGivenStage
class RepositoryServiceAssertStage : AssertStage<RepositoryServiceAssertStage, RepositoryService>() {

  @Autowired
  @Qualifier("remote")
  @ProvidedScenarioState(resolution = ScenarioState.Resolution.NAME)
  override lateinit var remoteService: RepositoryService

  @Autowired
  @Qualifier("repositoryService")
  @ProvidedScenarioState(resolution = ScenarioState.Resolution.NAME)
  override lateinit var localService: RepositoryService

  @ExpectedScenarioState(resolution = ScenarioState.Resolution.TYPE)
  lateinit var processDefinition: ProcessDefinition


  fun process_definition_query_succeeds(
    processDefinitionQueryAssertions: (ProcessDefinitionQuery, AssertStage<*, RepositoryService>) -> Unit = { _, _ -> }
  ): RepositoryServiceAssertStage = step {
    val query = remoteService.createProcessDefinitionQuery()
    processDefinitionQueryAssertions(query, this)
  }

  fun process_definition_is_activated_by_key() = step {
    remoteService.activateProcessDefinitionByKey(processDefinition.key)
  }

  fun process_definition_is_activated_by_id() = step {
    remoteService.activateProcessDefinitionById(processDefinition.id)
  }

  fun deployment_query_succeeds(
    deploymentQueryAssertions: (DeploymentQuery, AssertStage<*, RepositoryService>) -> Unit = { _, _ -> }
  ): RepositoryServiceAssertStage = step {
    val query = remoteService.createDeploymentQuery()
    deploymentQueryAssertions(query, this)
  }

  fun bpmn_model_query_succeeds(
    processDefinitionId: String,
    processDefinitionQueryAssertions: (BpmnModelInstance, AssertStage<*, RepositoryService>) -> Unit = { _, _ -> },
  ): RepositoryServiceAssertStage = step {
    assertThat(remoteService.getBpmnModelInstance(processDefinitionId)).isNotNull
    return self()
  }
}

