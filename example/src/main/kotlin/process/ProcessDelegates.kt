package org.camunda.bpm.extension.feign.example.process

import mu.KLogging
import org.camunda.bpm.engine.delegate.JavaDelegate
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ProcessDelegates {

  companion object : KLogging()

  @Bean
  fun loggingDelegate() = JavaDelegate {
    logger.info { "LOGGER-001: ${it.eventName.toGerund()} '${it.currentActivityName.toSinglePrettyString()}, payload: ${it.variables}" }
  }

  @Bean
  fun variableSetter() = JavaDelegate {
    // it.setVariableLocal("MY-VAR", stringValue("Example Value"))
  }
}

fun String.toSinglePrettyString() = this
  .replace("\n", " ")
  .replace("\t", " ")
  .replace("  ", " ")

fun String.toGerund() = when (this.length) {
  0 -> ""
  1 -> this.toUpperCase() + "ing"
  else -> "${this.substring(0, 1).toUpperCase()}${this.substring(1, this.length)}ing"
}

