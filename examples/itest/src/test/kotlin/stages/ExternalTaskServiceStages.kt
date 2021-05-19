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
package org.camunda.bpm.extension.rest.itest.stages

import com.tngtech.jgiven.annotation.ProvidedScenarioState
import com.tngtech.jgiven.annotation.ScenarioState
import com.tngtech.jgiven.integration.spring.JGivenStage
import org.assertj.core.api.Assertions.assertThat
import org.camunda.bpm.engine.ExternalTaskService
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.repository.ProcessDefinition
import org.camunda.bpm.engine.runtime.Execution
import org.camunda.bpm.engine.runtime.ProcessInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier

@JGivenStage
class ExternalTaskServiceActionStage : ActionStage<ExternalTaskServiceActionStage, ExternalTaskService>() {

  @Autowired
  @Qualifier("remote")
  @ProvidedScenarioState(resolution = ScenarioState.Resolution.NAME)
  override lateinit var remoteService: ExternalTaskService

  @Autowired
  @Qualifier("externalTaskService")
  @ProvidedScenarioState(resolution = ScenarioState.Resolution.NAME)
  override lateinit var localService: ExternalTaskService

  @Autowired
  @ProvidedScenarioState
  lateinit var repositoryService: RepositoryService

  @Autowired
  @ProvidedScenarioState(resolution = ScenarioState.Resolution.TYPE)
  lateinit var runtimeService: RuntimeService

  @ProvidedScenarioState(resolution = ScenarioState.Resolution.TYPE)
  lateinit var processDefinition: ProcessDefinition

  @ProvidedScenarioState(resolution = ScenarioState.Resolution.TYPE)
  lateinit var processInstance: ProcessInstance

  @ProvidedScenarioState(resolution = ScenarioState.Resolution.NAME)
  lateinit var externalTaskId: String

  fun process_from_a_resource_is_deployed(
    processModelFilename: String = "processModelFilename.bpmn"
  ): ExternalTaskServiceActionStage {

    val deployment = repositoryService
      .createDeployment()
      .addClasspathResource(processModelFilename)
      .name("deployemnt-$processModelFilename")
      .deploy()

    processDefinition = repositoryService
      .createProcessDefinitionQuery()
      .deploymentId(deployment.id)
      .singleResult()

    return self()
  }

  fun process_is_started_by_key(
    processDefinitionKey: String,
    businessKey: String? = null,
    caseInstanceId: String? = null,
    variables: Map<String, Any>? = null
  ): ExternalTaskServiceActionStage {

    processInstance = if (variables != null && businessKey != null && caseInstanceId != null) {
      runtimeService.startProcessInstanceByKey(processDefinitionKey, businessKey, caseInstanceId, variables)
    } else if (businessKey != null && caseInstanceId != null) {
      runtimeService.startProcessInstanceByKey(processDefinitionKey, businessKey, caseInstanceId)
    } else if (businessKey != null) {
      runtimeService.startProcessInstanceByKey(processDefinitionKey, businessKey)
    } else {
      runtimeService.startProcessInstanceByKey(processDefinitionKey)
    }

    // started instance
    assertThat(processInstance).isNotNull
    // waits in message event
    assertThat(runtimeService
      .createProcessInstanceQuery()
      .processInstanceId(processInstance.id)
      .singleResult()).isNotNull

    return self()
  }

  fun process_waits_in_external_task(topic: String): ExternalTaskServiceActionStage {
    localService.fetchAndLock(1, "worker-id")
      .topic(topic, 10)
      .execute().map {
        this.externalTaskId = it.id
      }

    assertThat(externalTaskId).isNotNull()

    return self()
  }


}

@JGivenStage
class ExternalTaskServiceAssertStage : AssertStage<ExternalTaskServiceAssertStage, ExternalTaskService>() {

  @Autowired
  @ProvidedScenarioState(resolution = ScenarioState.Resolution.TYPE)
  lateinit var runtimeService: RuntimeService

  @ProvidedScenarioState(resolution = ScenarioState.Resolution.TYPE)
  lateinit var processInstance: ProcessInstance

  @ProvidedScenarioState(resolution = ScenarioState.Resolution.TYPE)
  lateinit var execution: Execution


  fun execution_is_waiting_for_signal(): ExternalTaskServiceAssertStage {
    execution = runtimeService
      .createExecutionQuery()
      .executionId(runtimeService
        .createEventSubscriptionQuery()
        .processInstanceId(processInstance.id)
        .singleResult()
        .executionId
      ).singleResult()
    return self()
  }

}
