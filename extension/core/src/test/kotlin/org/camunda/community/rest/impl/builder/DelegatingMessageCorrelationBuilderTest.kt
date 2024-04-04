package org.camunda.community.rest.impl.builder

import org.assertj.core.api.Assertions.assertThat
import org.camunda.bpm.engine.runtime.MessageCorrelationResultType
import org.camunda.community.rest.client.api.MessageApiClient
import org.camunda.community.rest.client.model.MessageCorrelationResultWithVariableDto
import org.camunda.community.rest.client.model.ProcessInstanceDto
import org.camunda.community.rest.variables.ValueMapper
import org.camunda.community.rest.variables.ValueTypeResolverImpl
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.ResponseEntity

class DelegatingMessageCorrelationBuilderTest {

  val messageApiClient = mock<MessageApiClient>()

  val builder = DelegatingMessageCorrelationBuilder(
    messageName = "messageName",
    messageApiClient = messageApiClient,
    valueMapper = ValueMapper(valueTypeResolver = ValueTypeResolverImpl())
  ).apply {
    this.localVariableEquals("localVar", "localValue")
    // this.processDefinitionId("processDefinitionId")
    this.processInstanceId("processInstanceId")
    this.processInstanceVariableEquals("processVar", "processValue")
    this.processInstanceBusinessKey("businessKey")
    this.processInstanceVariablesEqual(mutableMapOf("var" to "value"))
    this.setVariables(mutableMapOf("var2" to "value2"))
    this.tenantId("tenantId")
  }

  @Test
  fun correlateStartMessage() {
    whenever(messageApiClient.deliverMessage(any())).thenReturn(
      ResponseEntity.ok(listOf(MessageCorrelationResultWithVariableDto().processInstance(
        ProcessInstanceDto().id("processInstanceId").ended(false).suspended(false)
      )))
    )
    val result = builder.correlateStartMessage()
    assertThat(result).isNotNull
    assertThat(result.id).isEqualTo("processInstanceId")
  }

  @Test
  fun correlateWithResultAndVariables() {
    whenever(messageApiClient.deliverMessage(any())).thenReturn(
      ResponseEntity.ok(listOf(MessageCorrelationResultWithVariableDto()
        .resultType(MessageCorrelationResultWithVariableDto.ResultTypeEnum.EXECUTION)))
    )
    val result = builder.correlateWithResultAndVariables(true)
    assertThat(result).isNotNull
    assertThat(result.resultType).isEqualTo(MessageCorrelationResultType.Execution)
  }

  @Test
  fun correlateAllWithResultAndVariables() {
    whenever(messageApiClient.deliverMessage(any())).thenReturn(
      ResponseEntity.ok(listOf(MessageCorrelationResultWithVariableDto()
        .resultType(MessageCorrelationResultWithVariableDto.ResultTypeEnum.EXECUTION)))
    )
    val result = builder.correlateAllWithResultAndVariables(true)
    assertThat(result).isNotNull
    assertThat(result).hasSize(1)
  }

  @Test
  fun correlateAllWithResult() {
    whenever(messageApiClient.deliverMessage(any())).thenReturn(
      ResponseEntity.ok(listOf(MessageCorrelationResultWithVariableDto()
        .resultType(MessageCorrelationResultWithVariableDto.ResultTypeEnum.EXECUTION)))
    )
    val result = builder.correlateAllWithResult()
    assertThat(result).isNotNull
    assertThat(result).hasSize(1)
  }

  @Test
  fun correlateWithResult() {
    whenever(messageApiClient.deliverMessage(any())).thenReturn(
      ResponseEntity.ok(listOf(MessageCorrelationResultWithVariableDto()
        .resultType(MessageCorrelationResultWithVariableDto.ResultTypeEnum.PROCESSDEFINITION)))
    )
    val result = builder.correlateWithResult()
    assertThat(result).isNotNull
    assertThat(result.resultType).isEqualTo(MessageCorrelationResultType.ProcessDefinition)
  }

  @Test
  fun correlateExclusively() {
    whenever(messageApiClient.deliverMessage(any())).thenReturn(
      ResponseEntity.ok(listOf(MessageCorrelationResultWithVariableDto()
        .resultType(MessageCorrelationResultWithVariableDto.ResultTypeEnum.PROCESSDEFINITION)))
    )
    val result = builder.correlateExclusively()
    assertThat(result).isNotNull
  }

  @Test
  fun correlateAll() {
    whenever(messageApiClient.deliverMessage(any())).thenReturn(
      ResponseEntity.ok(listOf(MessageCorrelationResultWithVariableDto()
        .resultType(MessageCorrelationResultWithVariableDto.ResultTypeEnum.EXECUTION)))
    )
    builder.correlateAll()
    verify(messageApiClient).deliverMessage(any())
  }

  @Test
  fun correlate() {
    whenever(messageApiClient.deliverMessage(any())).thenReturn(
      ResponseEntity.ok(listOf(MessageCorrelationResultWithVariableDto()
        .resultType(MessageCorrelationResultWithVariableDto.ResultTypeEnum.EXECUTION)))
    )
    builder.correlate()
    verify(messageApiClient).deliverMessage(any())
  }

}
