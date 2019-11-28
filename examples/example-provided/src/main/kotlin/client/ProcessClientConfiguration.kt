package org.camunda.bpm.extension.rest.example.processapplication.client

import feign.Logger
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.RuntimeService
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration


@Configuration
@ComponentScan
class ProcessClientConfiguration {
  // full debug of feign client
  @Bean
  fun feignLoggerLevel(): Logger.Level = Logger.Level.FULL


  @ConditionalOnProperty("client.enabled")
  @Bean
  fun createProcessClient(
    @Qualifier("remote") runtimeService: RuntimeService,
    @Qualifier("remote") repositoryService: RepositoryService
  ): ProcessClient {
    return ProcessClient(runtimeService = runtimeService, repositoryService = repositoryService)
  }
}
