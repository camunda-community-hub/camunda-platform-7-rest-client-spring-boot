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
    historyApiClient
  ).apply {
    this.processInstanceId("processInstanceId")
    this.processDefinitionId("processDefinitionId")
    this.processDefinitionName("processDefinitionName")
    this.processDefinitionNameLike("processDefinitionNameLike")
    this.processInstanceBusinessKey("businessKey")
    this.processInstanceBusinessKeyIn("businessKeyIn")
    this.processInstanceBusinessKeyLike("businessKeyLike")
    this.incidentType("incidentType")
    this.incidentStatus("incidentStatus")
    this.incidentMessage("incidentMessage")
    this.incidentMessageLike("incidentMessageLike")
    this.startedBy("startedBy")
    //this.rootProcessInstances()
    this.superProcessInstanceId("superProcessInstanceId")
    this.subProcessInstanceId("subProcessInstanceId")
    this.superCaseInstanceId("superCaseInstanceId")
    this.subCaseInstanceId("subCaseInstanceId")
    this.processDefinitionKeyNotIn(mutableListOf("processKeyNotIn"))
    this.finished()
    this.unfinished()
    this.withIncidents()
    this.withRootIncidents()
    this.startedBefore(Date())
    this.startedAfter(Date())
    this.finishedBefore(Date())
    this.finishedAfter(Date())
    this.executedActivityAfter(Date())
    this.executedActivityBefore(Date())
    this.executedJobAfter(Date())
    this.executedJobBefore(Date())
    this.active()
    this.suspended()
    this.internallyTerminated()
    this.externallyTerminated()
    this.processDefinitionKey("processDefinitionKey")
    this.processDefinitionKeyIn("processDefinitionKeys")
    this.processInstanceIds(mutableSetOf("processInstanceIds"))
    this.executedActivityIdIn("executedActivityIds")
    this.activeActivityIdIn("activeActivityIds")
    this.caseInstanceId("caseInstanceId")
    this.startDateBy(Date())
    this.startDateOn(Date())
    this.finishDateBy(Date())
    this.finishDateOn(Date())
    this.tenantIdIn("tenantId")
    this.matchVariableNamesIgnoreCase()
    this.matchVariableValuesIgnoreCase()
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
