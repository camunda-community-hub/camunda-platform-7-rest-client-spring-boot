package org.camunda.bpm.extension.rest.example.engine.client

import feign.Logger
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile



@Configuration
@ComponentScan
@ConditionalOnProperty("client.enabled")
class ProcessClientConfiguration {
  // full debug of feign client
  @Bean
  fun feignLoggerLevel(): Logger.Level = Logger.Level.FULL

}
