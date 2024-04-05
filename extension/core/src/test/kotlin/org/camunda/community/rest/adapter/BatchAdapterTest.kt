package org.camunda.community.rest.adapter

import org.assertj.core.api.Assertions
import org.camunda.community.rest.client.model.BatchDto
import org.junit.Test
import java.time.OffsetDateTime

class BatchAdapterTest {
  private val dto = BatchDto()
    .id("id")
    .tenantId("tenantId")
    .type("type")
    .totalJobs(1)
    .jobsCreated(1)
    .batchJobsPerSeed(1)
    .invocationsPerBatchJob(1)
    .batchJobDefinitionId("batchJobDefinitionId")
    .seedJobDefinitionId("seedJobDefinitionId")
    .monitorJobDefinitionId("monitorJobDefinitionId")
    .suspended(false)
    .createUserId("createUserId")
    .startTime(OffsetDateTime.now())
    .executionStartTime(OffsetDateTime.now())

  @Test
  fun `should delegate`() {
    val batchBean = BatchBean.fromDto(dto)
    val batchAdapter = BatchAdapter(batchBean)
    Assertions.assertThat(batchAdapter).usingRecursiveComparison().ignoringFields("batchBean").isEqualTo(batchBean)
  }

  @Test
  fun `should construct from dto`() {
    val bean = BatchBean.fromDto(dto)
    Assertions.assertThat(bean).usingRecursiveComparison().ignoringFields("startTime", "executionStartTime").isEqualTo(dto)
    Assertions.assertThat(bean.startTime).isEqualTo(dto.startTime.toInstant())
    Assertions.assertThat(bean.executionStartTime).isEqualTo(dto.executionStartTime.toInstant())
  }
}
