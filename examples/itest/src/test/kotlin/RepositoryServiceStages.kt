/*-
 * #%L
 * camunda-rest-client-spring-boot-itest
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
package org.camunda.bpm.extension.rest.itest

import com.tngtech.jgiven.annotation.IsTag
import com.tngtech.jgiven.annotation.ProvidedScenarioState
import com.tngtech.jgiven.annotation.ScenarioState
import com.tngtech.jgiven.integration.spring.JGivenStage
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.repository.ProcessDefinition
import org.camunda.bpm.engine.repository.ProcessDefinitionQuery
import org.camunda.bpm.engine.runtime.ProcessInstance
import org.camunda.bpm.model.bpmn.Bpmn
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier

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
  lateinit var processDefinition: ProcessDefinition

  fun process_is_deployed(
    processDefinitionKey: String = "process_with_user_task"
  ): RepositoryServiceActionStage {

    val instance = Bpmn
      .createExecutableProcess(processDefinitionKey)
      .startEvent("start")
      .endEvent("end")
      .done()

    val deployment = localService
      .createDeployment()
      .addModelInstance("$processDefinitionKey.bpmn", instance)
      .name("deployment_name")
      .deploy()

    return self()
  }

}

@JGivenStage
class RepositoryServiceAssertStage : AssertStage<RepositoryServiceAssertStage, RepositoryService>() {

  @Autowired
  @Qualifier("repositoryService")
  @ProvidedScenarioState(resolution = ScenarioState.Resolution.NAME)
  override lateinit var localService: RepositoryService

  @Autowired
  @Qualifier("remote")
  @ProvidedScenarioState(resolution = ScenarioState.Resolution.NAME)
  override lateinit var remoteService: RepositoryService

  @ProvidedScenarioState(resolution = ScenarioState.Resolution.NAME)
  private lateinit var query: ProcessDefinitionQuery

  fun process_query_succeds(
    processDefinitionQueryAssertions: (ProcessDefinitionQuery, AssertStage<*, RepositoryService>) -> Unit = { _, _ -> }
  ): RepositoryServiceAssertStage {
    query = remoteService.createProcessDefinitionQuery()
    processDefinitionQueryAssertions(query!!, this)
    return self()
  }
}

@IsTag(name = "RepositoryService")
annotation class RepositoryServiceCategory


