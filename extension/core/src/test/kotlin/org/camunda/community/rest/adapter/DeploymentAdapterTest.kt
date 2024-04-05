package org.camunda.community.rest.adapter

import org.assertj.core.api.Assertions
import org.camunda.community.rest.client.model.AtomLink
import org.camunda.community.rest.client.model.DeploymentDto
import org.junit.Test
import java.time.OffsetDateTime

class DeploymentAdapterTest {
  private val dto = DeploymentDto()
    .id("id")
    .tenantId("tenantId")
    .links(listOf(AtomLink()))
    .deploymentTime(OffsetDateTime.now())
    .source("source")
    .name("name")

  @Test
  fun `should delegate`() {
    val bean = DeploymentBean.fromDto(dto)
    val adapter = DeploymentAdapter(bean)
    Assertions.assertThat(adapter).usingRecursiveComparison().ignoringFields("deploymentBean").isEqualTo(bean)
  }

  @Test
  fun `should construct from dto`() {
    val bean = DeploymentBean.fromDto(dto)
    Assertions.assertThat(bean).usingRecursiveComparison().ignoringFields("deploymentTime", "deployedProcessDefinitions",
      "deployedCaseDefinitions", "deployedDecisionDefinitions", "deployedDecisionRequirementsDefinitions").isEqualTo(dto)
    Assertions.assertThat(bean.deploymentTime).isEqualTo(dto.deploymentTime.toInstant())
  }
}
