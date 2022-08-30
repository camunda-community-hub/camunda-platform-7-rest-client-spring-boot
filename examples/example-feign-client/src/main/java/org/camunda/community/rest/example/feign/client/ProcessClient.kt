package org.camunda.community.rest.example.feign.client

import com.fasterxml.jackson.databind.ObjectMapper
import org.camunda.bpm.engine.variable.Variables
import org.camunda.community.rest.client.api.MessageApiClient
import org.camunda.community.rest.client.api.ProcessDefinitionApiClient
import org.camunda.community.rest.client.api.SignalApiClient
import org.camunda.community.rest.client.model.*
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.stream.Collectors

/**
 * Client speaking with a remote process engine.
 */
@Component
class ProcessClient
/**
 * Constructs the client.
 *
 * @param processDefinitionApiClient process definition api client.
 * @param messageApiClient message api client.
 * @param signalApiClient signal api client.
 */(
  private val processDefinitionApiClient: ProcessDefinitionApiClient,
  private val messageApiClient: MessageApiClient,
  private val signalApiClient: SignalApiClient,
  private val objectMapper: ObjectMapper
) {
  private val instances: MutableMap<String, String> = ConcurrentHashMap()

  companion object {
    private val LOGGER = LoggerFactory.getLogger(ProcessClient::class.java)
  }

  /**
   * Retrieves process definitions periodically.
   */
  @Scheduled(initialDelay = 8000, fixedRate = Int.MAX_VALUE.toLong())
  fun retrieveProcessDefinition() {
    LOGGER.info("CLIENT-90: Retrieving process definition")
    val count = processDefinitionApiClient.getProcessDefinitionsCount(
      null, null, null, null,
      null, null, null, null, null, null,
      null, null, null, null, null, null, null,
      null, null, null, null, null, null, null,
      null, null, null, null, null,
      null, null, null
    ).body.count
    LOGGER.info("CLIENT-91: Found {} deployed processes", count)
    val processDefinitions = processDefinitionApiClient.getProcessDefinitions(
      null, null, null, null,
      null, null, null, null, null, null,
      null, null, null, null, null, null, null,
      null, null, null, null, null, null, null,
      null, null, null, null, null,
      null, null, null, null, null, null, null
    ).body
    LOGGER.info(
      "CLIENT-92: Deployed process definition are {}",
      processDefinitions.stream().map { o: ProcessDefinitionDto? -> Objects.toString(o) }
        .collect(
          Collectors.toList()
        ))
  }

  /**
   * Starts processes periodically.
   */
  @Scheduled(initialDelay = 10000, fixedDelay = 5000)
  fun startProcess() {
    LOGGER.trace("CLIENT-100: Starting a process instance remote")
    val instance = processDefinitionApiClient.startProcessInstanceByKey(
      "process_messaging",
      StartProcessInstanceDto()
        .businessKey("WAIT_FOR_MESSAGE" + UUID.randomUUID())
        .putVariablesItem("ID", stringValue("MESSAGING-" + UUID.randomUUID()))
    ).body
    LOGGER.trace("CLIENT-101: Started instance {} - {}", instance.id, instance.businessKey)
    instances[instance.id] = instance.businessKey
  }

  /**
   * Fires signals periodically.
   */
  @Scheduled(initialDelay = 12500, fixedDelay = 5000)
  fun fireSignal() {
    LOGGER.info("CLIENT-200: Sending signal")
    signalApiClient
      .throwSignal(
        SignalDto().name("signal_received")
          .putVariablesItem("BYTES", VariableValueDto().value("World".toByteArray()).type("Bytes"))
      )
  }

  /**
   * Correlates messages periodically.
   */
  @Scheduled(initialDelay = 13500, fixedDelay = 5000)
  fun correlateMessage() {
    LOGGER.info("CLIENT-300: Correlating message")
    val variables: MutableMap<String, VariableValueDto> = HashMap()
    variables["STRING"] = stringValue("my string")
    variables["CORRELATION_DATE"] = dateValue(Date.from(Instant.now()))
    variables["SHORT"] = shortValue(120.toShort())
    variables["DOUBLE"] = doubleValue(1.0)
    variables["INTEGER"] = integerValue(65800)
    variables["LONG"] = longValue(1L + Int.MAX_VALUE)
    variables["BYTES"] = byteArrayValue("Hello!".toByteArray())
    variables["OBJECT"] = objectValue(MyDataStructure("string", 100))
    val instanceIterator: Iterator<Map.Entry<String, String>> = instances.entries.iterator()
    if (instanceIterator.hasNext()) {
      val (key, value) = instanceIterator.next()
      LOGGER.debug("Trying to message remote process instance {}", key)
      val result = messageApiClient
        .deliverMessage(
          CorrelationMessageDto()
            .messageName("message_received")
            .businessKey(value)
            .processVariables(variables)
            .all(true)
            .resultEnabled(true)
        ).body
      result.forEach {
        LOGGER.info(
          "CLIENT-301: {}",
          it.toString()
        )
      }
      instances.remove(key)
    } else {
      LOGGER.info("CLIENT-301: No instances to correlate with.")
    }
  }

  private fun stringValue(value: String): VariableValueDto = VariableValueDto().value(value).type("String")

  private fun integerValue(value: Int): VariableValueDto = VariableValueDto().value(value).type("Integer")

  private fun longValue(value: Long): VariableValueDto = VariableValueDto().value(value).type("Long")

  private fun shortValue(value: Short): VariableValueDto = VariableValueDto().value(value).type("Short")

  private fun doubleValue(value: Double): VariableValueDto = VariableValueDto().value(value).type("Double")

  private fun byteArrayValue(value: ByteArray): VariableValueDto = VariableValueDto().value(value).type("Bytes")

  private fun dateValue(value: Date): VariableValueDto = VariableValueDto().value(value).type("Date")

  private fun objectValue(value: Any): VariableValueDto {
    val valueInfo = mapOf<String, Any>(Pair("objectTypeName", value.javaClass.name), Pair("serializationDataFormat", Variables.SerializationDataFormats.JSON))
    return VariableValueDto().value(objectMapper.writeValueAsString(value)).type("Object").valueInfo(valueInfo)
  }

}
