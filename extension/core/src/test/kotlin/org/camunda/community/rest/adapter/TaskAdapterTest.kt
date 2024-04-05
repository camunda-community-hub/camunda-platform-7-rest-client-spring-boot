package org.camunda.community.rest.adapter

import org.assertj.core.api.Assertions
import org.camunda.community.rest.client.model.CamundaFormRef
import org.camunda.community.rest.client.model.TaskDto
import org.junit.Test
import java.time.OffsetDateTime

class TaskAdapterTest {
  private val dto = TaskDto()
    .id("id")
    .tenantId("tenantId")
    .name("name")
    .assignee("assignee")
    .owner("owner")
    .created(OffsetDateTime.now())
    .lastUpdated(OffsetDateTime.now())
    .due(OffsetDateTime.now())
    .followUp(OffsetDateTime.now())
    .delegationState(TaskDto.DelegationStateEnum.RESOLVED)
    .description("description")
    .executionId("executionId")
    .parentTaskId("parentTaskId")
    .priority(1)
    .processDefinitionId("processDefinitionId")
    .processInstanceId("processInstanceId")
    .caseExecutionId("caseExecutionId")
    .caseDefinitionId("caseDefinitionId")
    .caseInstanceId("caseInstanceId")
    .taskDefinitionKey("taskDefinitionKey")
    .suspended(false)
    .formKey("formKey")
    .camundaFormRef(CamundaFormRef().key("key").binding("binding").version(1))


  @Test
  fun `should delegate`() {
    val bean = TaskBean.fromDto(dto)
    val adapter = TaskAdapter(bean)
    Assertions.assertThat(adapter).usingRecursiveComparison().ignoringFields("taskBean").isEqualTo(bean)
  }

  @Test
  fun `should construct from dto`() {
    val bean = TaskBean.fromDto(dto)
    Assertions.assertThat(bean).usingRecursiveComparison().ignoringFields("created", "lastUpdated", "due", "followUp", "processExecutionId").isEqualTo(dto)
    Assertions.assertThat(bean.created).isEqualTo(dto.created.toInstant())
    Assertions.assertThat(bean.lastUpdated).isEqualTo(dto.lastUpdated.toInstant())
    Assertions.assertThat(bean.due).isEqualTo(dto.due.toInstant())
    Assertions.assertThat(bean.followUp).isEqualTo(dto.followUp.toInstant())
  }
}
