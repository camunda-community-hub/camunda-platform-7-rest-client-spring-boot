package org.camunda.community.rest.impl.query

import org.assertj.core.api.Assertions.assertThat
import org.camunda.community.rest.client.api.IncidentApiClient
import org.camunda.community.rest.client.model.CountResultDto
import org.camunda.community.rest.client.model.IncidentDto
import org.junit.Test
import org.mockito.ArgumentMatchers.eq

import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.http.ResponseEntity
import java.time.OffsetDateTime
import java.util.*

class DelegatingIncidentQueryTest {

  val incidentApiClient = mock<IncidentApiClient>()

  val query = DelegatingIncidentQuery(
    incidentApiClient,
  ).apply {
    this.incidentId("id")
    this.incidentType("incidentType")
    this.incidentMessage("incidentMessage")
    this.incidentMessageLike("incidentMessageLike")
    this.executionId("executionId")
    this.incidentTimestampBefore(Date())
    this.incidentTimestampAfter(Date())
    this.activityId("activityId")
    this.failedActivityId("failedActivityId")
    this.processInstanceId("processInstanceId")
    this.processDefinitionId("processDefinitionId")
    this.processDefinitionKeyIn("processDefinitionKeys")
    this.causeIncidentId("causeIncidentId")
    this.rootCauseIncidentId("rootCauseIncidentId")
    this.configuration("configuration")
    this.jobDefinitionIdIn("jobDefinitionIds")
    this.tenantIdIn("tenantId")
    this.orderByIncidentType().asc()
  }

  @Test
  fun listPage() {
    whenever(incidentApiClient.getIncidents(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(),
      any(), any(), any(), any(), any(), any(), any(), any(), eq(1), eq(10)))
      .thenReturn(
        ResponseEntity.ok(listOf(IncidentDto().id("incidentId").incidentTimestamp(OffsetDateTime.now()).incidentType("incidentType")
          .executionId("executionId").activityId("activityId").processInstanceId("processInstanceId").processDefinitionId("processDefinitionId")
          .causeIncidentId("causeIncidentId").rootCauseIncidentId("rootCauseIncidentId")._configuration("configuration")))
      )
    val result = query.listPage(1, 10)
    assertThat(result).hasSize(1)
  }

  @Test
  fun count() {
    whenever(incidentApiClient.getIncidentsCount(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(),
      any(), any(), any(), any(), any(), any()))
      .thenReturn(
        ResponseEntity.ok(CountResultDto().count(5))
      )
    val result = query.count()
    assertThat(result).isEqualTo(5)
  }

}
