package org.camunda.community.rest.impl.query

import org.assertj.core.api.Assertions.assertThat
import org.camunda.community.rest.client.api.ExecutionApiClient
import org.camunda.community.rest.client.model.CountResultDto
import org.camunda.community.rest.client.model.ExecutionDto
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.http.ResponseEntity

class DelegatingExecutionQueryTest {

  val executionApiClient = mock<ExecutionApiClient>()

  val query = DelegatingExecutionQuery(
    executionApiClient,
  ).apply {
    this.processDefinitionId("processDefinitionId")
    this.processDefinitionKey("processDefinitionKey")
    this.processInstanceBusinessKey("businessKey")
    this.activityId("activityId")
    this.executionId("executionId")
    this.processInstanceId("processInstanceId")
    this.suspended()
    this.incidentType("incidentType")
    this.incidentId("incidentId")
    this.incidentMessage("incidentMessage")
    this.incidentMessageLike("incidentMessageLike")
    this.messageEventSubscriptionName("message")
    this.signalEventSubscriptionName("signal")
    this.tenantIdIn("tenantId")
    this.matchVariableNamesIgnoreCase()
    this.matchVariableValuesIgnoreCase()
    this.variableValueLike("var", "value")
    this.variableValueEquals("var2", "value2")
    this.orderByProcessDefinitionKey().asc()
  }

  @Test
  fun testListPage() {
    whenever(executionApiClient.queryExecutions(eq(1), eq(10), any())).thenReturn(
      ResponseEntity.ok(listOf(ExecutionDto().id("executionId").processInstanceId("processInstanceId").ended(false)))
    )
    val result = query.listPage(1, 10)
    assertThat(result).hasSize(1)
  }

  @Test
  fun testCount() {
    whenever(executionApiClient.queryExecutionsCount(any())).thenReturn(
      ResponseEntity.ok(CountResultDto().count(5))
    )
    val result = query.count()
    assertThat(result).isEqualTo(5)
  }

}
