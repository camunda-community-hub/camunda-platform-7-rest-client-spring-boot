package org.camunda.community.rest.adapter

import org.assertj.core.api.Assertions
import org.camunda.community.rest.client.model.EventSubscriptionDto
import org.junit.Test
import java.time.OffsetDateTime

class EventSubscriptionAdapterTest {
  private val dto = EventSubscriptionDto()
    .id("id")
    .tenantId("tenantId")
    .eventType("eventType")
    .eventName("eventName")
    .executionId("executionId")
    .processInstanceId("processInstanceId")
    .activityId("activityId")
    .createdDate(OffsetDateTime.now())

  @Test
  fun `should delegate`() {
    val bean = EventSubscriptionBean.fromDto(dto)
    val adapter = EventSubscriptionAdapter(bean)
    Assertions.assertThat(adapter).usingRecursiveComparison().ignoringFields("eventSubscriptionBean").isEqualTo(bean)
  }

  @Test
  fun `should construct from dto`() {
    val bean = EventSubscriptionBean.fromDto(dto)
    Assertions.assertThat(bean).usingRecursiveComparison().ignoringFields("createdDate").isEqualTo(dto)
    Assertions.assertThat(bean.createdDate).isEqualTo(dto.createdDate.toInstant())
  }
}
