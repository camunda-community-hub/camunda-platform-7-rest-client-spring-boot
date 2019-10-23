package org.camunda.bpm.extension.feign.adapter

import org.assertj.core.api.Assertions.assertThat
import org.camunda.bpm.engine.rest.dto.runtime.ExecutionDto
import org.junit.Test

class ExecutionAdapterTest {

  val executionBean = ExecutionBean(id = "id",
    processInstanceId = "processInstanceId",
    ended = true,
    suspended = true,
    tenantId = "tenantId")

  @Test
  fun `should delegate`() {
    val executionAdapter = ExecutionAdapter(executionBean)
    assertThat(executionAdapter.isEnded).isEqualTo(executionBean.ended)
    assertThat(executionAdapter.isSuspended).isEqualTo(executionBean.suspended)
    assertThat(executionAdapter.processInstanceId).isEqualTo(executionBean.processInstanceId)
    assertThat(executionAdapter.tenantId).isEqualTo(executionBean.tenantId)
  }

  @Test
  fun `should construct from dto`() {
    val dto = ExecutionDto.fromExecution(ExecutionAdapter(executionBean))
    val bean = ExecutionBean.fromExecutionDto(dto)

    assertThat(dto.id).isEqualTo(bean.id)
    assertThat(dto.isEnded).isEqualTo(bean.ended)
    assertThat(dto.processInstanceId).isEqualTo(bean.processInstanceId)
    assertThat(dto.tenantId).isEqualTo(bean.tenantId)

  }
}
