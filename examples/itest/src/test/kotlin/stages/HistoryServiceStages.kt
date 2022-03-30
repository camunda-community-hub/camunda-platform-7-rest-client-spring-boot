/*-
 * #%L
 * camunda-rest-client-spring-boot-itest
 * %%
 * Copyright (C) 2021 Camunda Services GmbH
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
package org.camunda.bpm.extension.rest.itest.stages

import com.tngtech.jgiven.annotation.IsTag
import com.tngtech.jgiven.annotation.ProvidedScenarioState
import com.tngtech.jgiven.annotation.ScenarioState
import com.tngtech.jgiven.integration.spring.JGivenStage
import io.toolisticon.testing.jgiven.step
import org.assertj.core.api.Assertions
import org.camunda.bpm.engine.HistoryService
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.TaskService
import org.camunda.bpm.engine.history.HistoricProcessInstanceQuery
import org.camunda.bpm.engine.repository.Deployment
import org.camunda.bpm.engine.runtime.ProcessInstance
import org.camunda.bpm.model.bpmn.Bpmn
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import java.util.*

@JGivenStage
class HistoryServiceActionStage : ActionStage<HistoryServiceActionStage, HistoryService>() {

  @Autowired
  @ProvidedScenarioState
  lateinit var repositoryService: RepositoryService

  @Autowired
  @ProvidedScenarioState
  lateinit var runtimeService: RuntimeService

  @Autowired
  @ProvidedScenarioState
  lateinit var taskService: TaskService

  @Autowired
  @Qualifier("remote")
  @ProvidedScenarioState(resolution = ScenarioState.Resolution.NAME)
  override lateinit var remoteService: HistoryService

  @Autowired
  @Qualifier("historyService")
  @ProvidedScenarioState(resolution = ScenarioState.Resolution.NAME)
  override lateinit var localService: HistoryService

  @ProvidedScenarioState(resolution = ScenarioState.Resolution.TYPE)
  lateinit var deployment: Deployment

  @ProvidedScenarioState(resolution = ScenarioState.Resolution.TYPE)
  lateinit var processInstance: ProcessInstance

  fun no_deployment_exists() = step {
    repositoryService.createDeploymentQuery().list().map {
      repositoryService.deleteDeployment(it.id)
    }
  }

  fun process_is_deployed(processDefinitionKey: String, versionTag: String? = null,
                          deploymentName: String = "deployment" + UUID.randomUUID().toString().replace("-", "")) = step {

    val instance = Bpmn
      .createExecutableProcess(processDefinitionKey)
      .camundaVersionTag(versionTag)
      .startEvent("start")
      .userTask("task")
      .endEvent("end")
      .done()

    deployment = repositoryService
      .createDeployment()
      .addModelInstance("$processDefinitionKey.bpmn", instance)
      .name(deploymentName)
      .deploy()
  }

  fun process_is_started_by_key(
    processDefinitionKey: String,
    businessKey: String? = null,
    variables: Map<String, Any>? = null
  ): HistoryServiceActionStage = step {

    processInstance = if (variables != null && businessKey != null) {
      runtimeService.startProcessInstanceByKey(processDefinitionKey, businessKey, variables)
    } else if (businessKey != null) {
      runtimeService.startProcessInstanceByKey(processDefinitionKey, businessKey)
    } else {
      runtimeService.startProcessInstanceByKey(processDefinitionKey)
    }

    // started instance
    Assertions.assertThat(processInstance).isNotNull

  }

  fun task_is_completed() {
    val task = taskService.createTaskQuery().processInstanceId(processInstance.id).singleResult()
    taskService.complete(task.id)
  }

}

@JGivenStage
class HistoryServiceAssertStage : AssertStage<HistoryServiceAssertStage, HistoryService>() {

  @Autowired
  @Qualifier("remote")
  @ProvidedScenarioState(resolution = ScenarioState.Resolution.NAME)
  override lateinit var remoteService: HistoryService

  @Autowired
  @Qualifier("historyService")
  @ProvidedScenarioState(resolution = ScenarioState.Resolution.NAME)
  override lateinit var localService: HistoryService

  @ProvidedScenarioState
  var processInstance: ProcessInstance? = null

  fun historic_process_instance_query_succeeds(
    processDefinitionQueryAssertions: (HistoricProcessInstanceQuery, AssertStage<*, HistoryService>) -> Unit = { _, _ -> }
  ): HistoryServiceAssertStage = step {
    val query = remoteService.createHistoricProcessInstanceQuery()
    processDefinitionQueryAssertions(query, this)
  }

}

@IsTag(name = "HistoryService")
annotation class HistoryServiceCategory


