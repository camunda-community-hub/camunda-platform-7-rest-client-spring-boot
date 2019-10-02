package org.camunda.bpm.extension.feign.example.process

import mu.KLogging
import org.camunda.bpm.engine.delegate.JavaDelegate
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Instant
import java.util.*

@Configuration
class ProcessDelegates {

  companion object : KLogging()

  @Bean
  fun loggingDelegate() = JavaDelegate {
    logger.info { "PROCESS-LOGGER-001: ${it.eventName.toGerund()} '${it.currentActivityName.toSinglePrettyString()}, payload: ${it.variables}" }
  }

  @Bean
  fun variableSetter() = JavaDelegate {
    // setting untyped variable
    it.setVariable("MY_UNTYPED_STRING", "Example Value")
    it.setVariable("MY_UNTYPED_OBJ", Payload())
  }

  data class Payload(val time: Instant = Instant.now(), val field: String = UUID.randomUUID().toString())
}

fun String.toSinglePrettyString() = this
  .replace("\n", " ")
  .replace("\t", " ")
  .replace("  ", " ")

fun String.toGerund() = when (this.length) {
  0 -> ""
  1 -> "${this.toUpperCase()}ing"
  else -> "${this.substring(0, 1).toUpperCase()}${this.substring(1, this.length)}ing"
}

