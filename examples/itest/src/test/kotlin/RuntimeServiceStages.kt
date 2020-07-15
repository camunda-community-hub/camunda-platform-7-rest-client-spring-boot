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
import org.assertj.core.api.Assertions.assertThat
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.repository.ProcessDefinition
import org.camunda.bpm.engine.runtime.Execution
import org.camunda.bpm.engine.runtime.ProcessInstance
import org.camunda.bpm.model.bpmn.Bpmn
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier

@JGivenStage
class RuntimeServiceActionStage : ActionStage<RuntimeServiceActionStage, RuntimeService>() {

  @Autowired
  @ProvidedScenarioState
  lateinit var repositoryService: RepositoryService

  @Autowired
  @Qualifier("remote")
  @ProvidedScenarioState(resolution = ScenarioState.Resolution.NAME)
  override lateinit var remoteService: RuntimeService

  @Autowired
  @Qualifier("runtimeService")
  @ProvidedScenarioState(resolution = ScenarioState.Resolution.NAME)
  override lateinit var localService: RuntimeService

  @ProvidedScenarioState(resolution = ScenarioState.Resolution.TYPE)
  lateinit var processDefinition: ProcessDefinition

  @ProvidedScenarioState(resolution = ScenarioState.Resolution.TYPE)
  lateinit var processInstance: ProcessInstance

  @ProvidedScenarioState(resolution = ScenarioState.Resolution.TYPE)
  lateinit var execution: Execution

  fun process_with_user_task_is_deployed(
    processDefinitionKey: String = "process_with_user_task",
    userTaskId: String = "user_task"
  ): RuntimeServiceActionStage {

    val instance = Bpmn
      .createExecutableProcess(processDefinitionKey)
      .startEvent("start")
      .camundaAsyncAfter(true)
      .userTask(userTaskId)
      .endEvent("end")
      .done()

    val deployment = repositoryService
      .createDeployment()
      .addModelInstance("$processDefinitionKey.bpmn", instance)
      .name("process_with_user_task")
      .deploy()

    processDefinition = repositoryService
      .createProcessDefinitionQuery()
      .deploymentId(deployment.id)
      .singleResult()

    return self()
  }

  fun process_with_intermediate_message_catch_event_is_deployed(
    processDefinitionKey: String = "process_with_message_catch_event",
    userTaskId: String = "user-task",
    messageName: String = "my-message"
  ): RuntimeServiceActionStage {

    val instance = Bpmn
      .createExecutableProcess(processDefinitionKey)
      .startEvent("start")
      .intermediateCatchEvent().message(messageName)
      .userTask(userTaskId)
      .endEvent("end")
      .done()

    val deployment = repositoryService
      .createDeployment()
      .addModelInstance("$processDefinitionKey.bpmn", instance)
      .name("process_with_message_catch_event")
      .deploy()

    processDefinition = repositoryService
      .createProcessDefinitionQuery()
      .deploymentId(deployment.id)
      .singleResult()

    return self()
  }

  fun process_with_intermediate_signal_catch_event_is_deployed(
    processDefinitionKey: String = "process_with_signal_catch_event",
    userTaskId: String = "user-task",
    signalName: String = "my-signal"
  ): RuntimeServiceActionStage {

    val instance = Bpmn
      .createExecutableProcess(processDefinitionKey)
      .startEvent("start")
      .intermediateCatchEvent().signal(signalName)
      .userTask(userTaskId)
      .endEvent("end")
      .done()

    val deployment = repositoryService
      .createDeployment()
      .addModelInstance("$processDefinitionKey.bpmn", instance)
      .name("process_with_signal_catch_event")
      .deploy()

    processDefinition = repositoryService
      .createProcessDefinitionQuery()
      .deploymentId(deployment.id)
      .singleResult()

    return self()
  }

  fun process_with_start_by_message_event_is_deployed(
    processDefinitionKey: String = "process_start_message",
    userTaskId: String = "user-task",
    messageName: String = "my-message"
  ): RuntimeServiceActionStage {

    val instance = Bpmn
      .createExecutableProcess(processDefinitionKey)
      .startEvent()
      .message(messageName)
      .userTask(userTaskId)
      .endEvent("end")
      .done()

    val deployment = repositoryService
      .createDeployment()
      .addModelInstance("$processDefinitionKey.bpmn", instance)
      .name("process_start_message")
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
  ): RuntimeServiceActionStage {

    processInstance = if (variables != null && businessKey != null && caseInstanceId != null) {
      localService.startProcessInstanceByKey(processDefinitionKey, businessKey, caseInstanceId, variables)
    } else if (businessKey != null && caseInstanceId != null) {
      localService.startProcessInstanceByKey(processDefinitionKey, businessKey, caseInstanceId)
    } else if (businessKey != null) {
      localService.startProcessInstanceByKey(processDefinitionKey, businessKey)
    } else {
      localService.startProcessInstanceByKey(processDefinitionKey)
    }

    // started instance
    assertThat(processInstance).isNotNull
    // waits in message event
    assertThat(localService
      .createProcessInstanceQuery()
      .processInstanceId(processInstance.id)
      .singleResult()).isNotNull

    return self()
  }

  fun execution_is_waiting_for_signal(): RuntimeServiceActionStage {
    execution = localService
      .createExecutionQuery()
      .processDefinitionKey(processDefinition.key)
      .executionId(localService
        .createEventSubscriptionQuery()
        .processInstanceId(processInstance.id)
        .singleResult()
        .executionId
      ).singleResult()
    return self()
  }
}

@JGivenStage
class RuntimeServiceAssertStage : AssertStage<RuntimeServiceAssertStage, RuntimeService>() {

  @Autowired
  @Qualifier("runtimeService")
  @ProvidedScenarioState(resolution = ScenarioState.Resolution.NAME)
  override lateinit var localService: RuntimeService

  @Autowired
  @Qualifier("remoteRuntimeService")
  @ProvidedScenarioState(resolution = ScenarioState.Resolution.NAME)
  override lateinit var remoteService: RuntimeService

  @ProvidedScenarioState
  var processInstance: ProcessInstance? = null

  fun process_instance_exists(
    processDefinitionKey: String? = null,
    processDefinitionId: String? = null,
    containingSimpleProcessVariables: Map<String, Any>? = null,
    processInstanceAssertions: (ProcessInstance, AssertStage<*, RuntimeService>) -> Unit = { _, _ -> }
  ): RuntimeServiceAssertStage {

    val query = localService.createProcessInstanceQuery().apply {
      if (processDefinitionId != null) {
        this.processDefinitionId(processDefinitionId)
      }
      if (processDefinitionKey != null) {
        this.processDefinitionKey(processDefinitionKey)
      }
      if (containingSimpleProcessVariables != null) {
        containingSimpleProcessVariables.entries.forEach {
          this.variableValueEquals(it.key, it.value)
        }
      }
    }
    val instances = query.list()
    assertThat(instances.size).`as`("expect to find exactly 1 instance", processDefinitionKey).isEqualTo(1)
    processInstance = instances[0]
    assertThat(processInstance).isNotNull
    processInstanceAssertions(processInstance!!, this)
    return self()
  }

  fun subscription_exists(messageName: String): RuntimeServiceAssertStage {

    assertThat(localService
      .createEventSubscriptionQuery()
      .eventType("message")
      .eventName(messageName)
      .singleResult()
    ).isNotNull

    return self()
  }
}

@IsTag(name = "RuntimeService")
annotation class RuntimeServiceCategory


