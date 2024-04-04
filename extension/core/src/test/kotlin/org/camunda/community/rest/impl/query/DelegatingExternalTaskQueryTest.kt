package org.camunda.community.rest.impl.query

import org.assertj.core.api.Assertions.assertThat
import org.camunda.community.rest.client.api.ExternalTaskApiClient
import org.camunda.community.rest.client.model.CountResultDto
import org.camunda.community.rest.client.model.ExternalTaskDto
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.http.ResponseEntity
import java.time.Instant
import java.util.*

class DelegatingExternalTaskQueryTest {

  val externalTaskApiClient = mock<ExternalTaskApiClient>()

  val query: DelegatingExternalTaskQuery = DelegatingExternalTaskQuery(
    externalTaskApiClient,
  ).apply {
    this.externalTaskId("externalTaskId")
    this.externalTaskIdIn(setOf("externalTaskId"))
    this.workerId("workerId")
    this.topicName("topicName")
    this.processDefinitionId("processDefinitionId")
    this.processInstanceId("processInstanceId")
    this.processInstanceIdIn("processInstanceIds")
    this.executionId("executionId")
    this.suspended()
    this.activityId("activityId")
    this.activityIdIn("activityIds")
    this.withRetriesLeft()
    this.noRetriesLeft()
    this.priorityHigherThanOrEquals(1)
    this.priorityLowerThanOrEquals(5)
    this.locked()
    this.notLocked()
    this.lockExpirationBefore(Date.from(Instant.now().plusSeconds(10)))
    this.lockExpirationAfter(Date.from(Instant.now().minusSeconds(10)))
  }

  @Test
  fun testListPage() {
    whenever(externalTaskApiClient.queryExternalTasks(eq(1), eq(10), any())).thenReturn(
      ResponseEntity.ok(listOf(ExternalTaskDto().id("id").priority(1).suspended(false)))
    )
    val result = query.listPage(1, 10)
    assertThat(result).hasSize(1)
  }

  @Test
  fun testCount() {
    whenever(externalTaskApiClient.queryExternalTasksCount(any())).thenReturn(
      ResponseEntity.ok(CountResultDto().count(5))
    )
    val result = query.count()
    assertThat(result).isEqualTo(5)
  }

}
