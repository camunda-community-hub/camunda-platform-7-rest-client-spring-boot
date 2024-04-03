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
    processDefinitionId = "processDefinitionId",
    processDefinitionKey = "processDefinitionKey",
    businessKey = "businessKey",
    activityId = "activityId",
    executionId = "executionId",
    processInstanceId = "processInstanceId",
    suspensionState = SuspensionState.ACTIVE,
    incidentType = "incidentType",
    incidentId = "incidentId",
    incidentMessage = "incidentMessage",
    incidentMessageLike = "incidentMessageLike",
    eventSubscriptions = mutableListOf(EventSubscriptionQueryValue("eventSubscriptions", "message")),
  ).apply {
    this.tenantIdIn("tenantId")
    this.variableNamesIgnoreCase = true
    this.variableValuesIgnoreCase = true
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
