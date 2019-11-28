package org.camunda.bpm.extension.rest.starter

import mu.KLogging
import org.camunda.bpm.engine.ProcessEngine
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@AutoConfigureAfter(name = ["org.camunda.bpm.spring.boot.starter.CamundaBpmAutoConfiguration"])
class ProcessEngineClientStubAutoConfiguration {

  companion object: KLogging()

  @Bean
  @ConditionalOnMissingBean(ProcessEngine::class)
  fun processEngineClientStub(): ProcessEngine {
    logger.info { "CAMUNDA-REST-STARTER-001: No existing process engine bean has been found. Providing a client-only stub." }
    return ProcessEngineConfigurationClientStub().buildProcessEngine()
  }
}
