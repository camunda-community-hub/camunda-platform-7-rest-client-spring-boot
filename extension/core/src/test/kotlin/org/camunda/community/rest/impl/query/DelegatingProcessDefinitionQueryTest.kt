package org.camunda.community.rest.impl.query

import org.assertj.core.api.Assertions.assertThat
import org.camunda.community.rest.client.api.ProcessDefinitionApiClient
import org.camunda.community.rest.client.model.CountResultDto
import org.camunda.community.rest.client.model.ProcessDefinitionDto
import org.junit.Test

import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.http.ResponseEntity
import java.util.*

class DelegatingProcessDefinitionQueryTest {

  val processDefinitionApiClient = mock<ProcessDefinitionApiClient>()

  val query = DelegatingProcessDefinitionQuery(
    processDefinitionApiClient,
    id = "id",
    ids = arrayOf("ids"),
    category = "category",
    categoryLike = "categoryLike",
    name = "name",
    nameLike = "nameLike",
    deploymentId = "deploymentId",
    deployedAfter = Date(),
    deployedAt = Date(),
    key = "key",
    keys = arrayOf("keys"),
    keyLike = "keyLike",
    resourceName = "resourceName",
    resourceNameLike = "resourceNameLike",
    version = 1,
    latest = false,
    suspensionState = SuspensionState.ACTIVE,
    authorizationUserId = "authorizationUserId",
    procDefId = mutableListOf("procDefId"),
    incidentType = "incidentType",
    incidentId = "incidentId",
    incidentMessage = "incidentMessage",
    incidentMessageLike = "incidentMessageLike",
    eventSubscriptionName = "eventSubscriptionName",
    eventSubscriptionType = "eventSubscriptionType",
    includeDefinitionsWithoutTenantId = true,
    isVersionTagSet = true,
    versionTag = "versionTag",
    versionTagLike = "versionTagLike",
    isStartableInTasklist = true,
    isNotStartableInTasklist = false,
    startablePermissionCheck = true,
  ).apply {
    this.tenantIdIn("tenantId")
    this.orderByProcessDefinitionName().asc()
  }

  @Test
  fun listPage() {
    whenever(
      processDefinitionApiClient.getProcessDefinitions(
        any(), any(), any(), any(), any(), any(), any(), any(), any(),
        any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(),
        any(), any(), any(), any(), any(), any(), eq(1), eq(10)
      )
    ).thenReturn(
      ResponseEntity.ok(listOf(ProcessDefinitionDto().id("processDefinitionId").suspended(false).startableInTasklist(true).version(1)))
    )
    val result = query.listPage(1, 10)
    assertThat(result).hasSize(1)
  }

  @Test
  fun count() {
    whenever(
      processDefinitionApiClient.getProcessDefinitionsCount(
        any(), any(), any(), any(), any(), any(), any(), any(), any(),
        any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(),
        any(), any(), any(), any()
      )
    ).thenReturn(
      ResponseEntity.ok(CountResultDto().count(5))
    )
    val result = query.count()
    assertThat(result).isEqualTo(5)
  }

}
