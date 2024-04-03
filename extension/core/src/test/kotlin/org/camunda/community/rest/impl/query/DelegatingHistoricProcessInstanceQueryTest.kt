package org.camunda.community.rest.impl.query

import org.assertj.core.api.Assertions.assertThat
import org.camunda.community.rest.client.api.HistoryApiClient
import org.camunda.community.rest.client.model.CountResultDto
import org.camunda.community.rest.client.model.HistoricProcessInstanceDto
import org.junit.Test
import org.mockito.ArgumentMatchers.eq
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.http.ResponseEntity
import java.time.OffsetDateTime
import java.util.*

class DelegatingHistoricProcessInstanceQueryTest {

  val historyApiClient = mock<HistoryApiClient>()

  val query = DelegatingHistoricProcessInstanceQuery(
    historyApiClient,
    processInstanceId = "processInstanceId",
    processDefinitionId = "processDefinitionId",
    processDefinitionName = "processDefinitionName",
    processDefinitionNameLike = "processDefinitionNameLike",
    businessKey = "businessKey",
    businessKeyIn = arrayOf("businessKeyIn"),
    businessKeyLike = "businessKeyLike",
    incidentType = "incidentType",
    incidentStatus = "incidentStatus",
    incidentMessage = "incidentMessage",
    incidentMessageLike = "incidentMessageLike",
    startedBy = "startedBy",
    isRootProcessInstances = true,
    superProcessInstanceId = "superProcessInstanceId",
    subProcessInstanceId = "subProcessInstanceId",
    superCaseInstanceId = "superCaseInstanceId",
    subCaseInstanceId = "subCaseInstanceId",
    processKeyNotIn = arrayOf("processKeyNotIn"),
    finished = true,
    unfinished = true,
    withIncidents = true,
    withRootIncidents = false,
    startedBefore = Date(),
    startedAfter = Date(),
    finishedBefore = Date(),
    finishedAfter = Date(),
    executedActivityAfter = Date(),
    executedActivityBefore = Date(),
    executedJobAfter = Date(),
    executedJobBefore = Date(),
    processDefinitionKey = "processDefinitionKey",
    processDefinitionKeys = arrayOf("processDefinitionKeys"),
    processInstanceIds = arrayOf("processInstanceIds"),
    executedActivityIds = arrayOf("executedActivityIds"),
    activeActivityIds = arrayOf("activeActivityIds"),
    state = "state",
    caseInstanceId = "caseInstanceId",
    startDateBy = Date(),
    startDateOn = Date(),
    finishDateBy = Date(),
    finishDateOn = Date(),
    startDateOnBegin = Date(),
    startDateOnEnd = Date(),
    finishDateOnBegin = Date(),
    finishDateOnEnd = Date(),
    isOrQueryActive = false,
  ).apply {
    this.tenantIdIn("tenantId")
    this.variableNamesIgnoreCase = false
    this.variableValuesIgnoreCase = true
    this.variableValueEquals("var", "value")
    this.orderByProcessInstanceDuration().asc()
  }

  @Test
  fun testListPage() {
    whenever(historyApiClient.queryHistoricProcessInstances(eq(1), eq(10), any())).thenReturn(
      ResponseEntity.ok(listOf(HistoricProcessInstanceDto()
        .id("processInstanceId")
        .processDefinitionId("processDefinitionId")
        .processDefinitionVersion(1)
        .startTime(OffsetDateTime.now())
        .state(HistoricProcessInstanceDto.StateEnum.ACTIVE)))
    )
    val result = query.listPage(1, 10)
    assertThat(result).hasSize(1)
  }

  @Test
  fun testCount() {
    whenever(historyApiClient.queryHistoricProcessInstancesCount(any())).thenReturn(
      ResponseEntity.ok(CountResultDto().count(5))
    )
    val result = query.count()
    assertThat(result).isEqualTo(5)
  }

}
