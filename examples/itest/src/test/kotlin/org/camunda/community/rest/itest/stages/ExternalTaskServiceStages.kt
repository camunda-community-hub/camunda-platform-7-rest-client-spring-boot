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

import com.tngtech.jgiven.annotation.AfterStage
import com.tngtech.jgiven.annotation.Hidden
import com.tngtech.jgiven.annotation.ProvidedScenarioState
import com.tngtech.jgiven.annotation.ScenarioState
import com.tngtech.jgiven.integration.spring.JGivenStage
import io.toolisticon.testing.jgiven.step
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.camunda.bpm.engine.ExternalTaskService
import org.camunda.bpm.engine.ManagementService
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.batch.Batch
import org.camunda.bpm.engine.externaltask.ExternalTaskQuery
import org.camunda.bpm.engine.externaltask.LockedExternalTask
import org.camunda.bpm.engine.repository.ProcessDefinition
import org.camunda.bpm.engine.runtime.Execution
import org.camunda.bpm.engine.runtime.ProcessInstance
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery
import org.camunda.community.rest.impl.query.DelegatingExternalTaskQuery
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import java.time.Duration
import java.time.Instant

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

  @ProvidedScenarioState(resolution = ScenarioState.Resolution.NAME)
  lateinit var lockedTasks: List<LockedExternalTask>

  @ProvidedScenarioState
  lateinit var batch: Batch

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
  ) = step {

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
    assertThat(
      runtimeService
        .createProcessInstanceQuery()
        .processInstanceId(processInstance.id)
        .singleResult()
    ).isNotNull
  }

  fun process_waits_in_external_task(topic: String) = step {
    localService.fetchAndLock(1, "worker-id")
      .topic(topic, 10)
      .execute().map {
        this.externalTaskId = it.id
      }

    assertThat(externalTaskId).isNotNull
  }

  fun fetch_and_lock_external_tasks(maxTasks: Int, topicName: String = "topic", lockDuration: Long = 1000) = step {
    lockedTasks = remoteService.fetchAndLock(maxTasks, "worker-id").topic(topicName, lockDuration).execute()
    if (lockedTasks.size == 1) {
      externalTaskId = lockedTasks[0].id
    }
  }

  fun extend_lock(lockDuration: Long) = step {
    remoteService.extendLock(externalTaskId, "worker-id", lockDuration)
  }

  fun unlock_external_task() = step {
    remoteService.unlock(externalTaskId)
  }

  fun lock_external_task(lockDuration: Long = 1000) = step {
    remoteService.lock(externalTaskId, "worker-id", lockDuration)
  }

  fun set_priority(priority: Long) = step {
    remoteService.setPriority(externalTaskId, priority)
  }

  fun handle_failure(errorDetails: String = "error-details", retries: Int = 1) = step {
    remoteService.handleFailure(externalTaskId, "worker-id", "error", errorDetails, retries, 1000,
      mapOf("var" to "value"), mapOf("local-var" to "local-value")
    )
  }

  fun set_retries(retries: Int) = step {
    remoteService.setRetries(externalTaskId, retries)
  }

  fun set_retries_async(retries: Int) = step {
    batch = remoteService.setRetriesAsync(listOf(externalTaskId), null, retries)
  }

  fun update_retries(retries: Int) = step {
    remoteService.updateRetries().externalTaskQuery(
      remoteService.createExternalTaskQuery().externalTaskId(externalTaskId)
    ).set(retries)
  }

}

@JGivenStage
class ExternalTaskServiceAssertStage : AssertStage<ExternalTaskServiceAssertStage, ExternalTaskService>() {

  @Autowired
  @ProvidedScenarioState(resolution = ScenarioState.Resolution.TYPE)
  lateinit var runtimeService: RuntimeService

  @Autowired
  @Qualifier("externalTaskService")
  @ProvidedScenarioState(resolution = ScenarioState.Resolution.NAME)
  override lateinit var localService: ExternalTaskService

  @Autowired
  @Qualifier("remote")
  @ProvidedScenarioState(resolution = ScenarioState.Resolution.NAME)
  override lateinit var remoteService: ExternalTaskService

  @Autowired
  @Qualifier("managementService")
  @ProvidedScenarioState(resolution = ScenarioState.Resolution.NAME)
  lateinit var managementService: ManagementService

  @ProvidedScenarioState(resolution = ScenarioState.Resolution.TYPE)
  lateinit var processInstance: ProcessInstance

  @ProvidedScenarioState(resolution = ScenarioState.Resolution.TYPE)
  lateinit var execution: Execution

  @ProvidedScenarioState(resolution = ScenarioState.Resolution.NAME)
  lateinit var externalTaskId: String

  @ProvidedScenarioState(resolution = ScenarioState.Resolution.NAME)
  lateinit var lockedTasks: List<LockedExternalTask>

  @ProvidedScenarioState
  lateinit var batch: Batch

  fun execution_is_waiting_for_signal(signalName: String) = step {

    val subscription = runtimeService
      .createEventSubscriptionQuery()
      .processInstanceId(processInstance.id)
      .eventName(signalName)
      .singleResult()


    execution = runtimeService
      .createExecutionQuery()
      .executionId(subscription.executionId)
      .singleResult()
  }

  fun process_waits_in_external_task(topic: String) = step {
    localService.fetchAndLock(1, "worker-id")
      .topic(topic, 10)
      .execute().map {
        this.externalTaskId = it.id
      }

    assertThat(externalTaskId).isNotNull
  }

  fun external_task_has_retries(retries: Int) = step {
    val externalTask = localService.createExternalTaskQuery().externalTaskId(externalTaskId).singleResult()

    assertThat(externalTask).isNotNull
    assertThat(externalTask.retries).isEqualTo(retries)
  }

  fun external_task_query_succeeds(
    @Hidden externalTaskQueryAssertions: (ExternalTaskQuery, AssertStage<*, ExternalTaskService>) -> Unit = { _, _ -> }
  ) = step {
    val query = remoteService.createExternalTaskQuery()
    externalTaskQueryAssertions(query, this)
  }

  fun locked_external_tasks_exist(
    count: Int, topicName: String
  ) = step {
    assertThat(lockedTasks).hasSize(count)
    assertThat(lockedTasks.map { it.topicName }).containsOnly(topicName)
  }

  fun external_task_is_locked(
    topicName: String, lockDuration: Long
  ) = step {
    val externalTask = localService.createExternalTaskQuery().externalTaskId(externalTaskId).singleResult()
    assertThat(externalTask).isNotNull
    assertThat(externalTask.topicName).isEqualTo(topicName)
    assertThat(externalTask.lockExpirationTime).isCloseTo(Instant.now().plusMillis(lockDuration), 100)
  }

  fun external_task_is_unlocked(
    topicName: String
  ) = step {
    val externalTask = localService.createExternalTaskQuery().externalTaskId(externalTaskId).singleResult()
    assertThat(externalTask).isNotNull
    assertThat(externalTask.topicName).isEqualTo(topicName)
    assertThat(externalTask.lockExpirationTime).isNull()
    assertThat(externalTask.workerId).isNull()
  }

  fun topic_names_exist(vararg topicNames: String) = step {
    assertThat(remoteService.topicNames).containsOnly(*topicNames)
  }

  fun topic_names_exist_for_locked_tasks(vararg topicNames: String) = step {
    assertThat(remoteService.getTopicNames(true, false, true)).containsOnly(*topicNames)
  }

  fun has_error_details(errorDetails: String = "error-details") = step {
    assertThat(remoteService.getExternalTaskErrorDetails(externalTaskId)).isEqualTo(errorDetails)
  }

  fun wait_for_batch() = step {
    await.atMost(Duration.ofSeconds(5)).until {
      managementService.createBatchQuery().batchId(batch.id).singleResult() == null
    }
  }

  @AfterStage
  fun stop_process() {
    val allInstances = runtimeService.createProcessInstanceQuery().list().map { it.id }
    runtimeService.deleteProcessInstancesIfExists(allInstances, "end of test", true, true, true)
  }

}
