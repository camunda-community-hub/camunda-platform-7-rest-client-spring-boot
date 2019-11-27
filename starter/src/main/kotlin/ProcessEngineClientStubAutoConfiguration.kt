package org.camunda.bpm.extension.rest.starter

import org.camunda.bpm.engine.ProcessEngine
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Configuration

@Configuration
@AutoConfigureAfter(name = ["org.camunda.bpm.spring.boot.starter.CamundaBpmAutoConfiguration"])
class ProcessEngineClientStubAutoConfiguration {

  @ConditionalOnMissingBean(ProcessEngine::class)
  fun processEngineClientStub(): ProcessEngine {
    return ProcessEngineConfigurationClientStub().buildProcessEngine()
  }
}
