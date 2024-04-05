package org.camunda.community.rest.adapter

import org.assertj.core.api.Assertions
import org.camunda.community.rest.client.model.IncidentDto
import org.junit.Test
import java.time.OffsetDateTime

class IncidentAdapterTest {
  private val dto = IncidentDto()
    .id("id")
    .tenantId("tenantId")
    .processDefinitionId("processDefinitionId")
    .processInstanceId("processInstanceId")
    .executionId("executionId")
    .incidentTimestamp(OffsetDateTime.now())
    .incidentType("incidentType")
    .activityId("activityId")
    .failedActivityId("failedActivityId")
    .causeIncidentId("causeIncidentId")
    .rootCauseIncidentId("rootCauseIncidentId")
    ._configuration("_configuration")
    .tenantId("tenantId")
    .incidentMessage("incidentMessage")
    .jobDefinitionId("jobDefinitionId")
    .annotation("annotation")

  @Test
  fun `should delegate`() {
    val bean = IncidentBean.fromDto(dto)
    val adapter = IncidentAdapter(bean)
    Assertions.assertThat(adapter).usingRecursiveComparison().ignoringFields("incidentBean").isEqualTo(bean)
  }

  @Test
  fun `should construct from dto`() {
    val bean = IncidentBean.fromDto(dto)
    Assertions.assertThat(bean).usingRecursiveComparison().ignoringFields("incidentTimestamp", "configuration", "historyConfiguration").isEqualTo(dto)
    Assertions.assertThat(bean.incidentTimestamp).isEqualTo(dto.incidentTimestamp.toInstant())
  }
}
