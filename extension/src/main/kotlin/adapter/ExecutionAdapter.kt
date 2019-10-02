package org.camunda.bpm.extension.feign.adapter

import org.camunda.bpm.engine.rest.dto.runtime.ExecutionDto
import org.camunda.bpm.engine.runtime.Execution

class ExecutionAdapter(
  val executionBean: ExecutionBean
) : Execution {
  override fun getProcessInstanceId(): String = executionBean.processInstanceId
  override fun isEnded(): Boolean = executionBean.ended
  override fun getId(): String = executionBean.id
  override fun isSuspended(): Boolean = executionBean.suspended
  override fun getTenantId(): String? = executionBean.tenantId
}

data class ExecutionBean(
  val id: String,
  val processInstanceId: String,
  val ended: Boolean,
  val suspended: Boolean,
  val tenantId: String? = null
) {
  companion object {
    @JvmStatic
    fun fromExecutionDto(dto: ExecutionDto) =
      ExecutionBean(
        id = dto.id,
        processInstanceId = dto.processInstanceId,
        ended = dto.isEnded,
        suspended = false,
        tenantId = dto.tenantId
      )
  }
}
