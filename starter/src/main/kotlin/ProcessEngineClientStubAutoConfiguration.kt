package org.camunda.bpm.extension.rest.starter

import mu.KLogging
import org.camunda.bpm.engine.ProcessEngine
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

  companion object: KLogging()

  /**
   * Sets up a fake engine if no engine is provided.
   */
  @Bean
  @ConditionalOnMissingBean(ProcessEngine::class)
  fun processEngineClientStub(): ProcessEngine {
    logger.info { "CAMUNDA-REST-STARTER-001: No existing process engine bean has been found. Providing a client-only stub." }
    return ProcessEngineConfigurationClientStub().buildProcessEngine()
  }
}
