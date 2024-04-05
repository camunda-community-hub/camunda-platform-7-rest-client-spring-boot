package org.camunda.community.rest.adapter

import org.assertj.core.api.Assertions
import org.camunda.community.rest.client.model.HistoricProcessInstanceDto
import org.junit.Test
import java.time.OffsetDateTime

class HistoricProcessInstanceAdapterTest {
  private val dto = HistoricProcessInstanceDto()
    .id("id")
    .tenantId("tenantId")
    .rootProcessInstanceId("rootProcessInstanceId")
    .superProcessInstanceId("superProcessInstanceId")
    .superCaseInstanceId("superCaseInstanceId")
    .caseInstanceId("caseInstanceId")
    .processDefinitionName("processDefinitionName")
    .processDefinitionKey("processDefinitionKey")
    .processDefinitionVersion(1)
    .processDefinitionId("processDefinitionId")
    .businessKey("businessKey")
    .startTime(OffsetDateTime.now())
    .endTime(OffsetDateTime.now())
    .removalTime(OffsetDateTime.now())
    .durationInMillis(1000)
    .startUserId("startUserId")
    .startActivityId("startActivityId")
    .deleteReason("deleteReason")
    .state(HistoricProcessInstanceDto.StateEnum.ACTIVE)


  @Test
  fun `should delegate`() {
    val bean = HistoricInstanceBean.fromHistoricProcessInstanceDto(dto)
    val adapter = HistoricProcessInstanceAdapter(bean)
    Assertions.assertThat(adapter).usingRecursiveComparison().ignoringFields("historicInstanceBean").isEqualTo(bean)
  }

  @Test
  fun `should construct from dto`() {
    val bean = HistoricInstanceBean.fromHistoricProcessInstanceDto(dto)
    Assertions.assertThat(bean).usingRecursiveComparison().ignoringFields("startTime", "endTime", "removalTime", "endActivityId", "state").isEqualTo(dto)
    Assertions.assertThat(bean.startTime).isEqualTo(dto.startTime.toInstant())
    Assertions.assertThat(bean.endTime).isEqualTo(dto.endTime.toInstant())
    Assertions.assertThat(bean.removalTime).isEqualTo(dto.removalTime.toInstant())
    Assertions.assertThat(bean.state).isEqualTo(dto.state.name)
  }
}
