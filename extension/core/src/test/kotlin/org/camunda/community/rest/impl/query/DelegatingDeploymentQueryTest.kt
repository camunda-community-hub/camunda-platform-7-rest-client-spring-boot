package org.camunda.community.rest.impl.query

import org.assertj.core.api.Assertions.assertThat
import org.camunda.community.rest.client.api.DeploymentApiClient
import org.camunda.community.rest.client.model.CountResultDto
import org.camunda.community.rest.client.model.DeploymentDto
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.http.ResponseEntity
import java.time.Instant
import java.util.*

class DelegatingDeploymentQueryTest {

  val deploymentApiClient = mock<DeploymentApiClient>()

  val query: DelegatingDeploymentQuery = DelegatingDeploymentQuery(
    deploymentApiClient,
    deploymentId = "deploymentId",
    name = "name",
    nameLike = "nameLike",
    source = "source",
    deploymentBefore = Date.from(Instant.now()),
    deploymentAfter = Date.from(Instant.now().minusSeconds(5)),
  ).apply {
    this.tenantIdIn("tenantId")
    this.orderByDeploymentTime().asc()
  }

  @Test
  fun testListPage() {
    whenever(
      deploymentApiClient.getDeployments(
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
        any(),
        any()
      )
    ).thenReturn(
      ResponseEntity.ok(listOf(DeploymentDto().id("deploymentId").name("deploymentName")))
    )
    val deployments = query.listPage(1, 10)
    assertThat(deployments).hasSize(1)
  }

  @Test
  fun testCount() {
    whenever(deploymentApiClient.getDeploymentsCount(any(), any(), any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(
      ResponseEntity.ok(CountResultDto().count(5))
    )
    val count = query.count()
    assertThat(count).isEqualTo(5)
  }

}
