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
    processDefinitionApiClient
  ).apply {
    this.processDefinitionId("id")
    this.processDefinitionIdIn("ids")
    this.processDefinitionCategory("category")
    this.processDefinitionCategoryLike("categoryLike")
    this.processDefinitionName("name")
    this.processDefinitionNameLike("nameLike")
    this.deploymentId("deploymentId")
    this.deployedAfter(Date())
    this.deployedAt(Date())
    this.processDefinitionKey("key")
    this.processDefinitionKeysIn("keys")
    this.processDefinitionKeyLike("keyLike")
    this.processDefinitionResourceName("resourceName")
    this.processDefinitionResourceNameLike("resourceNameLike")
    this.processDefinitionVersion(1)
    //this.latestVersion()
    this.suspended()
    this.startableByUser("authorizationUserId")
    this.incidentType("incidentType")
    this.incidentId("incidentId")
    this.incidentMessage("incidentMessage")
    this.incidentMessageLike("incidentMessageLike")
    this.messageEventSubscriptionName("eventSubscriptionName")
    //this.withoutTenantId()
    this.withoutVersionTag()
    this.versionTag("versionTag")
    this.versionTagLike("versionTagLike")
    this.startableInTasklist()
    this.notStartableInTasklist()
    this.startablePermissionCheck()
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
