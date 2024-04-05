package org.camunda.community.rest.adapter

import org.assertj.core.api.Assertions
import org.camunda.community.rest.client.model.ProcessDefinitionDto
import org.junit.Test

class ProcessDefinitionAdapterTest {
  private val dto = ProcessDefinitionDto()
    .id("id")
    .tenantId("tenantId")
    .key("key")
    .category("category")
    .description("description")
    .name("name")
    .version(1)
    .resource("resource")
    .deploymentId("deploymentId")
    .diagram("diagram")
    .suspended(false)
    .tenantId("tenantId")
    .versionTag("versionTag")
    .historyTimeToLive(1)
    .startableInTasklist(true)


  @Test
  fun `should delegate`() {
    val bean = ProcessDefinitionBean.fromDto(dto)
    val adapter = ProcessDefinitionAdapter(bean)
    Assertions.assertThat(adapter).usingRecursiveComparison().ignoringFields("processDefinitionBean").isEqualTo(bean)
  }

  @Test
  fun `should construct from dto`() {
    val bean = ProcessDefinitionBean.fromDto(dto)
    Assertions.assertThat(bean).usingRecursiveComparison().ignoringFields("resourceName", "diagramResourceName", "startableInTaskList", "hasStartFormKey").isEqualTo(dto)
    Assertions.assertThat(bean.startableInTaskList).isEqualTo(dto.startableInTasklist)
  }
}
