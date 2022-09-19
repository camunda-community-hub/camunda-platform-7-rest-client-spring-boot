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

import com.tngtech.jgiven.annotation.Hidden
import com.tngtech.jgiven.annotation.ProvidedScenarioState
import com.tngtech.jgiven.annotation.ScenarioState
import com.tngtech.jgiven.integration.spring.JGivenStage
import io.toolisticon.testing.jgiven.step
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.camunda.bpm.engine.*
import org.camunda.bpm.engine.batch.Batch
import org.camunda.bpm.engine.repository.ProcessDefinition
import org.camunda.bpm.engine.runtime.*
import org.camunda.bpm.model.bpmn.Bpmn
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import java.time.Duration

@JGivenStage
class RuntimeServiceActionStage : ActionStage<RuntimeServiceActionStage, RuntimeService>() {

  @Autowired
  @ProvidedScenarioState
  lateinit var repositoryService: RepositoryService

  @Autowired
  @ProvidedScenarioState
  lateinit var taskService: TaskService

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
  lateinit var incident: Incident

  @ProvidedScenarioState(resolution = ScenarioState.Resolution.TYPE)
  lateinit var execution: Execution

  @ProvidedScenarioState(resolution = ScenarioState.Resolution.TYPE)
  lateinit var batch: Batch

  fun no_deployment_exists() = step {
    repositoryService.createDeploymentQuery().list().map {
      repositoryService.deleteDeployment(it.id, true)
    }
  }

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
  ): RuntimeServiceActionStage = step {

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
  }

  fun process_with_intermediate_signal_catch_event_is_deployed(
    processDefinitionKey: String = "process_with_signal_catch_event",
    userTaskId: String = "user-task",
    signalName: String = "my-signal"
  ): RuntimeServiceActionStage = step {

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
  }

  fun process_with_start_by_message_event_is_deployed(
    processDefinitionKey: String = "process_start_message",
    userTaskId: String = "user-task",
    messageName: String = "my-message"
  ): RuntimeServiceActionStage = step {

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
  }


  fun process_is_started_by_key(
    processDefinitionKey: String,
    businessKey: String? = null,
    caseInstanceId: String? = null,
    variables: Map<String, Any>? = null
  ): RuntimeServiceActionStage = step {

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
    assertThat(
      localService
        .createProcessInstanceQuery()
        .processInstanceId(processInstance.id)
        .singleResult()
    ).isNotNull

  }

  fun execution_is_waiting_in_user_task(): RuntimeServiceActionStage = step {
    await.atMost(Duration.ofSeconds(5)).until {
      taskService
        .createTaskQuery()
        .processInstanceId(processInstance.id)
        .count() != 0L
    }
  }

  fun execution_is_waiting_for_signal(): RuntimeServiceActionStage = step {
    execution = localService
      .createExecutionQuery()
      .processDefinitionKey(processDefinition.key)
      .executionId(
        localService
          .createEventSubscriptionQuery()
          .processInstanceId(processInstance.id)
          .singleResult()
          .executionId
      ).singleResult()
  }

  fun incident_is_created_locally(incidentType: String, configuration: String, message: String? = null) = step {
    incident = localService
      .createIncident(incidentType, processInstance.id, configuration, message)
  }

  fun incident_is_created(incidentType: String, configuration: String, message: String? = null) = step {
    incident = remoteService
      .createIncident(incidentType, processInstance.id, configuration, message)
  }

  fun incident_is_resolved() = step {
    remoteService.resolveIncident(incident.id)
  }

  fun annotation_is_set_on_incident(annotation: String) = step {
    remoteService.setAnnotationForIncidentById(incident.id, annotation)
  }

  fun annotation_is_cleared_on_incident() = step {
    remoteService.clearAnnotationForIncidentById(incident.id)
  }

  fun process_instance_is_deleted(processInstanceId: String) = step {
    remoteService.deleteProcessInstance(processInstanceId, "because")
  }

  fun process_instances_are_deleted(vararg processInstanceIds: String) = step {
    remoteService.deleteProcessInstances(processInstanceIds.toList(), "because", false, false)
  }

  fun process_instance_is_deleted_if_exists(processInstanceId: String) = step {
    remoteService.deleteProcessInstanceIfExists(processInstanceId, "because", false, false, false, false)
  }

  fun process_instances_are_deleted_if_existing(vararg processInstanceIds: String) = step {
    remoteService.deleteProcessInstancesIfExists(processInstanceIds.toList(), "because", false, false, false)
  }

  fun process_instance_is_deleted_async(processInstanceId: String) = step {
    batch = remoteService.deleteProcessInstancesAsync(listOf(processInstanceId), "because")
  }

  fun process_instances_are_deleted_async_by_process_definition_key(processDefinitionKey: String) = step {
    batch = remoteService.deleteProcessInstancesAsync(
      remoteService.createProcessInstanceQuery().processDefinitionKey(processDefinitionKey),
      "because"
    )
  }

}

@JGivenStage
class RuntimeServiceAssertStage : AssertStage<RuntimeServiceAssertStage, RuntimeService>() {

  @Autowired
  @Qualifier("remote")
  @ProvidedScenarioState(resolution = ScenarioState.Resolution.NAME)
  override lateinit var remoteService: RuntimeService

  @Autowired
  @Qualifier("runtimeService")
  @ProvidedScenarioState(resolution = ScenarioState.Resolution.NAME)
  override lateinit var localService: RuntimeService

  @Autowired
  @Qualifier("managementService")
  @ProvidedScenarioState(resolution = ScenarioState.Resolution.NAME)
  lateinit var managementService: ManagementService

  @Autowired
  @Qualifier("remote")
  @ProvidedScenarioState(resolution = ScenarioState.Resolution.NAME)
  lateinit var historyService: HistoryService

  @ProvidedScenarioState
  var processInstance: ProcessInstance? = null

  @ProvidedScenarioState
  lateinit var batch: Batch

  fun process_instance_exists(
    processDefinitionKey: String? = null,
    processDefinitionId: String? = null,
    containingSimpleProcessVariables: Map<String, Any>? = null,
    processInstanceAssertions: (ProcessInstance, AssertStage<*, RuntimeService>) -> Unit = { _, _ -> }
  ): RuntimeServiceAssertStage = step {

    val query = localService.createProcessInstanceQuery().apply {
      if (processDefinitionId != null) {
        this.processDefinitionId(processDefinitionId)
      }
      if (processDefinitionKey != null) {
        this.processDefinitionKey(processDefinitionKey)
      }
      containingSimpleProcessVariables?.entries?.forEach {
        this.variableValueEquals(it.key, it.value)
      }
    }
    val instances = query.list()
    assertThat(instances.size).`as`("expect to find exactly 1 instance", processDefinitionKey).isEqualTo(1)
    processInstance = instances[0]
    assertThat(processInstance).isNotNull
    processInstanceAssertions(processInstance!!, this)
  }

  fun subscription_exists(messageName: String) = step {
    assertThat(
      localService
        .createEventSubscriptionQuery()
        .eventType("message")
        .eventName(messageName)
        .singleResult()
    ).isNotNull
  }

  fun process_instance_is_suspended() = step {
    remoteService.suspendProcessInstanceById(processInstance!!.id)
  }

  fun process_instance_is_suspended_by_process_definition_key(processDefinitionKey: String) = step {
    remoteService.suspendProcessInstanceByProcessDefinitionKey(processDefinitionKey)
  }

  fun process_instance_is_activated() = step {
    remoteService.activateProcessInstanceById(processInstance!!.id)
  }

  fun process_instance_is_activated_by_process_definition_key(processDefinitionKey: String) = step {
    remoteService.activateProcessInstanceByProcessDefinitionKey(processDefinitionKey)
  }

  fun process_instance_query_succeeds(
    @Hidden processInstanceQueryAssertions: (ProcessInstanceQuery, AssertStage<*, RuntimeService>) -> Unit = { _, _ -> }
  ) = step {
    val query = remoteService.createProcessInstanceQuery()
    processInstanceQueryAssertions(query, this)
  }

  fun incident_query_succeeds(
    @Hidden incidentQueryAssertions: (IncidentQuery, AssertStage<*, RuntimeService>) -> Unit = { _, _ -> }
  ) = step {
    val query = remoteService.createIncidentQuery()
    incidentQueryAssertions(query, this)
  }

  fun event_subscription_query_succeeds(
    @Hidden eventSubscriptionQueryAssertions: (EventSubscriptionQuery, AssertStage<*, RuntimeService>) -> Unit = { _, _ -> }
  ) = step {
    val query = remoteService.createEventSubscriptionQuery()
    eventSubscriptionQueryAssertions(query, this)
  }

  fun execution_query_succeeds(
    @Hidden executionQueryAssertions: (ExecutionQuery, AssertStage<*, RuntimeService>) -> Unit = { _, _ -> }
  ) = step {
    val query = remoteService.createExecutionQuery()
    executionQueryAssertions(query, this)
  }

  fun batch_has_jobs(jobCount: Int) = step {
    assertThat(batch.totalJobs).isEqualTo(jobCount)
  }

  fun wait_for_batch() = step {
    await.atMost(Duration.ofSeconds(5)).until {
      managementService.createBatchQuery().batchId(batch.id).singleResult() == null
    }
  }
}

