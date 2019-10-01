package org.camunda.bpm.extension.restclient.example.client

import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.variable.Variables.*
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.*

@Component
class ProcessClient(
  @Qualifier("remote") private val runtimeService: RuntimeService
) {

  @Scheduled(initialDelay = 12_000, fixedDelay = 5_000)
  fun correlateMessage() {
    val variables = createVariables()
    variables.putValueTyped("STRING", stringValue("my string"))
    variables.putValueTyped("CORRELATION_DATE", dateValue(Date.from(Instant.now())))
    variables.putValueTyped("SHORT", shortValue(1))
    variables.putValueTyped("INTEGER", integerValue(65800))
    variables.putValueTyped("LONG", longValue(1L + Integer.MAX_VALUE))
    variables.putValueTyped("BYTES", byteArrayValue("Hello!".toByteArray()))

    runtimeService.correlateMessage("message_received", "WAIT_FOR_MESSAGE", variables)
  }
}
