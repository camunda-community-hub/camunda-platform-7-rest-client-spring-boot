package org.camunda.community.rest.impl.query

import org.assertj.core.api.Assertions
import org.camunda.community.rest.client.api.EventSubscriptionApiClient
import org.camunda.community.rest.client.model.CountResultDto
import org.camunda.community.rest.client.model.EventSubscriptionDto
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.http.ResponseEntity
import java.time.OffsetDateTime

class DelegatingEventSubscriptionQueryTest {

  val eventSubscriptionApiClient = mock<EventSubscriptionApiClient>()

  val query: DelegatingEventSubscriptionQuery = DelegatingEventSubscriptionQuery(
    eventSubscriptionApiClient,
  ).apply {
    this.eventSubscriptionId("eventSubscriptionId")
    this.eventName("eventName")
    this.eventType("eventType")
    this.executionId("executionId")
    this.processInstanceId("processInstanceId")
    this.activityId("activityId")
    this.tenantIdIn("tenantId")
    this.orderByCreated().asc()
  }

  @Test
  fun testListPage() {
    whenever(
      eventSubscriptionApiClient.getEventSubscriptions(
        any(),
        any(),
        any(),
        any(),
        any(),
        any(),
        any(),
        any(),
        any(),
        any(),
        any(),
        any(),
        any()
      )
    ).thenReturn(
      ResponseEntity.ok(listOf(EventSubscriptionDto().id("eventSubscriptionId").eventName("eventName").createdDate(OffsetDateTime.now())
        .eventType("eventType")))
    )
    val deployments = query.listPage(1, 10)
    Assertions.assertThat(deployments).hasSize(1)
  }

  @Test
  fun testCount() {
    whenever(eventSubscriptionApiClient.getEventSubscriptionsCount(any(), any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(
      ResponseEntity.ok(CountResultDto().count(5))
    )
    val count = query.count()
    Assertions.assertThat(count).isEqualTo(5)
  }

}
