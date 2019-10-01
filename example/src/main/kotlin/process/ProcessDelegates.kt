package org.camunda.bpm.extension.restclient.example.process

import mu.KLogging
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.delegate.JavaDelegate
import org.camunda.bpm.engine.variable.Variables.createVariables
import org.camunda.bpm.engine.variable.Variables.stringValue
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.*

@Configuration
class ProcessDelegates {

  companion object : KLogging()

  @Bean
  fun loggingDelegate() = JavaDelegate {
    logger.info { "LOGGER-001: ${it.eventName.toGerund()} '${it.currentActivityName.toSinglePrettyString()}, payload: ${it.variables}" }
  }
}

@Component
class ProcessStarter(private val runtimeService: RuntimeService) {

  companion object : KLogging()

  @Scheduled(initialDelay = 10_000, fixedDelay = 5_000)
  fun startProcess() {
    val instance = runtimeService.startProcessInstanceByKey("process_messaging",
      "WAIT_FOR_MESSAGE", createVariables().putValueTyped("ID", stringValue("MESSAGING-${UUID.randomUUID()}"))
    )
    logger.trace { "SCHEDULER-001: Started instance ${instance.id}" }
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

