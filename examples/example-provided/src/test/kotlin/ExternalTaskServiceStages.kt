package org.camunda.bpm.extension.rest.itest

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
