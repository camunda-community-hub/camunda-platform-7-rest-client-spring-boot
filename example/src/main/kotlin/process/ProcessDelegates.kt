package org.camunda.bpm.extension.restclient.example.process

import mu.KLogging
import org.camunda.bpm.engine.delegate.JavaDelegate
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ProcessDelegates {

  companion object : KLogging()

  @Bean
  fun loggingDelegate() = JavaDelegate {
    logger.info { "Executed ${it.currentActivityName.toSinglePrettyString()}" }
  }

  // TODO: add scheduled process starter
}


fun String.toSinglePrettyString() = this
  .replace("\n", " ")
  .replace("\t", " ")
  .replace("  ", " ")
