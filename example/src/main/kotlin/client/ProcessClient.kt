package org.camunda.bpm.extension.feign.example.client

import mu.KLogging
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.variable.Variables.*
import org.camunda.bpm.extension.feign.variables.toPrettyString
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.*

@Component
class ProcessClient(
  @Qualifier("remote") private val runtimeService: RuntimeService
) {

  companion object : KLogging()

  @Scheduled(initialDelay = 10_000, fixedDelay = 5_000)
  fun startProcess() {

    logger.trace { "CLIENT-100: Starting a process instance remote" }

    val variables = createVariables()
      .putValueTyped("ID", stringValue("MESSAGING-${UUID.randomUUID()}"))
    val instance = runtimeService.startProcessInstanceByKey("process_messaging", "WAIT_FOR_MESSAGE", variables)

    logger.trace { "CLIENT-101: Started instance ${instance.id} - ${instance.businessKey}" }
  }


  @Scheduled(initialDelay = 12_500, fixedDelay = 5_000)
  fun fireSignal() {

    logger.info { "CLIENT-200: Sending signal" }

    val variables = createVariables()
    variables.putValueTyped("BYTES", byteArrayValue("World".toByteArray()))

    val result = runtimeService
      .createSignalEvent("signal_received")
      .setVariables(variables)
      .send()
  }

  @Scheduled(initialDelay = 13_500, fixedDelay = 5_000)
  fun correlateMessage() {

    logger.info { "CLIENT-300: Correlating message" }

    val variables = createVariables()
    variables.putValueTyped("STRING", stringValue("my string"))
    variables.putValueTyped("CORRELATION_DATE", dateValue(Date.from(Instant.now())))
    variables.putValueTyped("SHORT", shortValue(1))
    variables.putValueTyped("INTEGER", integerValue(65800))
    variables.putValueTyped("LONG", longValue(1L + Integer.MAX_VALUE))
    variables.putValueTyped("BYTES", byteArrayValue("Hello!".toByteArray()))

    val result = runtimeService
      .createMessageCorrelation("message_received")
      .processInstanceBusinessKey("WAIT_FOR_MESSAGE")
      .setVariables(variables)
      .correlateAllWithResultAndVariables(true)

    result.forEach {
      logger.info { "CLIENT-301: ${it.toPrettyString()}" }
    }
  }

}
