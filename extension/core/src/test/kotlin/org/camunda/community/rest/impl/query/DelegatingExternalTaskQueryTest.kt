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
    externalTaskId = "externalTaskId",
    externalTaskIds = listOf("externalTaskId"),
    workerId = "workerId",
    topicName = "topicName",
    processDefinitionId = "processDefinitionId",
    processInstanceId = "processInstanceId",
    processInstanceIds = listOf("processInstanceIds"),
    executionId = "executionId",
    suspensionState = SuspensionState.ACTIVE,
    activityId = "activityId",
    activityIds = arrayOf("activityIds"),
    withRetriesLeft = true,
    noRetriesLeft = false,
    priorityHigherThanOrEquals = 1,
    priorityLowerThanOrEquals = 5,
    locked = false,
    notLocked = true,
    lockExpirationBefore = Date.from(Instant.now().plusSeconds(10)),
    lockExpirationAfter = Date.from(Instant.now().minusSeconds(10)),
  )

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
