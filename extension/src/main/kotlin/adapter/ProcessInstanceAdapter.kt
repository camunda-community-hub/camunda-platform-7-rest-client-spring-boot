package org.camunda.bpm.extension.feign.adapter

import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceDto
import org.camunda.bpm.engine.runtime.ProcessInstance

/**
 * Implementation of Camunda API Process Instance backed by a bean.
 */
class ProcessInstanceAdapter(private val instanceBean: InstanceBean) : ProcessInstance {

  override fun getProcessInstanceId(): String? = when (instanceBean.type) {
    InstanceType.CASE -> null
    InstanceType.PROCESS -> instanceBean.instanceId
  }

  override fun getBusinessKey(): String? = instanceBean.businessKey

  override fun getRootProcessInstanceId(): String? =
    when (instanceBean.type) {
      InstanceType.PROCESS -> instanceBean.rootProcessInstanceId
      InstanceType.CASE -> null
    }

  override fun getProcessDefinitionId(): String? =
    when (instanceBean.type) {
      InstanceType.PROCESS -> instanceBean.processDefinitionId
      InstanceType.CASE -> null
    }

  override fun isSuspended(): Boolean = instanceBean.suspended

  override fun getCaseInstanceId(): String? =
    when (instanceBean.type) {
      InstanceType.PROCESS -> null
      InstanceType.CASE -> instanceBean.instanceId
    }


  override fun isEnded(): Boolean = instanceBean.ended
  override fun getId(): String = instanceBean.id
  override fun getTenantId(): String? = instanceBean.tenantId
}

/**
 * Backing bean for process instance.
 */
data class InstanceBean(
  val id: String,
  val ended: Boolean,
  val suspended: Boolean,
  val type: InstanceType,
  val instanceId: String,
  val businessKey: String? = null,
  val tenantId: String? = null,
  val processDefinitionId: String? = null,
  val rootProcessInstanceId: String? = null
) {
  companion object {
    /**
     * Factory method to construct the bean from DTO.
     * @param dto: REST representation of process instance.
     */
    @JvmStatic
    fun fromProcessInstanceDto(processInstance: ProcessInstanceDto) =
      InstanceBean(
        id = processInstance.id,
        ended = processInstance.isEnded,
        suspended = processInstance.isSuspended,
        businessKey = processInstance.businessKey,
        tenantId = processInstance.tenantId,
        type = if (processInstance.caseInstanceId != null) {
          InstanceType.CASE
        } else {
          InstanceType.PROCESS
        },
        instanceId = if (processInstance.caseInstanceId != null) {
          processInstance.caseInstanceId
        } else {
          processInstance.id
        },
        processDefinitionId = processInstance.definitionId
      )
  }
}

/**
 * Instance type to cope with different ids.
 */
enum class InstanceType {
  /**
   * Process instance.
   */
  PROCESS,
  /**
   * Case instance.
   */
  CASE
}
