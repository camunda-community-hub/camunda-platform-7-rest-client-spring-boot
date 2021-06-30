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

import com.tngtech.jgiven.annotation.Hidden
import com.tngtech.jgiven.annotation.IsTag
import com.tngtech.jgiven.annotation.ProvidedScenarioState
import com.tngtech.jgiven.annotation.ScenarioState
import com.tngtech.jgiven.integration.spring.JGivenStage
import io.toolisticon.testing.jgiven.step
import org.assertj.core.api.Assertions.assertThat
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.TaskService
import org.camunda.bpm.engine.repository.ProcessDefinition
import org.camunda.bpm.engine.runtime.ProcessInstance
import org.camunda.bpm.engine.task.Task
import org.camunda.bpm.engine.task.TaskQuery
import org.camunda.bpm.model.bpmn.Bpmn
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier

@JGivenStage
class TaskServiceActionStage : ActionStage<TaskServiceActionStage, TaskService>() {

  @Autowired
  @ProvidedScenarioState
  lateinit var repositoryService: RepositoryService

  @Autowired
  @ProvidedScenarioState
  lateinit var runtimeService: RuntimeService

  @Autowired
  @Qualifier("remote")
  @ProvidedScenarioState(resolution = ScenarioState.Resolution.NAME)
  override lateinit var remoteService: TaskService

  @Autowired
  @Qualifier("taskService")
  @ProvidedScenarioState(resolution = ScenarioState.Resolution.NAME)
  override lateinit var localService: TaskService

  @ProvidedScenarioState(resolution = ScenarioState.Resolution.TYPE)
  lateinit var processDefinition: ProcessDefinition

  @ProvidedScenarioState(resolution = ScenarioState.Resolution.TYPE)
  lateinit var processInstance: ProcessInstance

  @ProvidedScenarioState(resolution = ScenarioState.Resolution.TYPE)
  lateinit var task: Task

  fun process_with_user_task_is_deployed(
    processDefinitionKey: String = "process_with_user_task",
    userTaskId: String = "user_task"
  ) = step {

    val modelInstance = Bpmn
      .createExecutableProcess(processDefinitionKey)
      .startEvent("start")
      .camundaAsyncAfter(true)
      .userTask(userTaskId)
      .endEvent("end")
      .done()

    val deployment = repositoryService
      .createDeployment()
      .addModelInstance("$processDefinitionKey.bpmn", modelInstance)
      .name("process_with_user_task")
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

}

@JGivenStage
class TaskServiceAssertStage : AssertStage<TaskServiceAssertStage, TaskService>() {

  @Autowired
  @Qualifier("remote")
  @ProvidedScenarioState(resolution = ScenarioState.Resolution.NAME)
  override lateinit var remoteService: TaskService

  @Autowired
  @Qualifier("taskService")
  @ProvidedScenarioState(resolution = ScenarioState.Resolution.NAME)
  override lateinit var localService: TaskService

  @ProvidedScenarioState
  var task: Task? = null

  fun task_exists(
    taskDefinitionKey: String? = null,
    taskId: String? = null,
    containingSimpleProcessVariables: Map<String, Any>? = null,
    taskAssertions: (Task, AssertStage<*, TaskService>) -> Unit = { _, _ -> }
  ): TaskServiceAssertStage = step {

    val query = localService.createTaskQuery().apply {
      if (taskId != null) {
        this.taskId(taskId)
      }
      if (taskDefinitionKey != null) {
        this.taskDefinitionKey(taskDefinitionKey)
      }
      containingSimpleProcessVariables?.entries?.forEach {
        this.processVariableValueEquals(it.key, it.value)
      }
    }
    val tasks = query.list()
    assertThat(tasks.size).`as`("expect to find exactly 1 task", taskDefinitionKey).isEqualTo(1)
    task = tasks[0]
    assertThat(task).isNotNull
    taskAssertions(task!!, this)
  }


  fun task_query_succeeds(
    @Hidden taskQueryAssertions: (TaskQuery, AssertStage<*, TaskService>) -> Unit = { _, _ -> }
  ) = step {
    val query = remoteService.createTaskQuery()
    taskQueryAssertions(query, this)
  }
}

@IsTag(name = "TaskService")
annotation class TaskServiceCategory


