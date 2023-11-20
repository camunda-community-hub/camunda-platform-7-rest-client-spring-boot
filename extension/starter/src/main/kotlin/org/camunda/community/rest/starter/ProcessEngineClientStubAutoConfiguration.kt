package org.camunda.community.rest.starter

import mu.KLogging
import org.camunda.bpm.engine.DecisionService
import org.camunda.bpm.engine.ExternalTaskService
import org.camunda.bpm.engine.HistoryService
import org.camunda.bpm.engine.ProcessEngine
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.TaskService
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Auto configuration providing client stub if no process engine is available.
 */
@Configuration
@AutoConfigureAfter(name = ["org.camunda.bpm.spring.boot.starter.CamundaBpmAutoConfiguration"])
class ProcessEngineClientStubAutoConfiguration {

  companion object : KLogging()

  /**
   * Sets up a fake engine if no engine is provided.
   */
  @Bean
  @ConditionalOnMissingBean(ProcessEngine::class)
  fun processEngineClientStub(
    decisionService: DecisionService,
    externalTaskService: ExternalTaskService,
    historyService: HistoryService,
    repositoryService: RepositoryService,
    runtimeService: RuntimeService,
    taskService: TaskService
  ): ProcessEngine {
    logger.info { "CAMUNDA-REST-STARTER-001: No existing process engine bean has been found. Providing a client-only stub." }
    return ProcessEngineConfigurationClientStub(
      decisionService,
      externalTaskService,
      historyService,
      repositoryService,
      runtimeService,
      taskService
    ).buildProcessEngine()
  }
}
