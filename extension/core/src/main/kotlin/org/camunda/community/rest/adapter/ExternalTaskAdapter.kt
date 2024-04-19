package org.camunda.community.rest.adapter

import org.camunda.bpm.engine.externaltask.ExternalTask
import org.camunda.community.rest.client.model.ExternalTaskDto
import org.camunda.community.rest.impl.toDate
import java.util.*

/**
 * Implementation of Camunda API External Task backed by a bean.
 */
class ExternalTaskAdapter(
  private val externalTaskBean: ExternalTaskBean
) : ExternalTask {
  override fun getId() = externalTaskBean.id

  override fun getTopicName() = externalTaskBean.topicName

  override fun getWorkerId() = externalTaskBean.workerId

  override fun getLockExpirationTime() = externalTaskBean.lockExpirationTime

  override fun getProcessInstanceId() = externalTaskBean.processInstanceId

  override fun getExecutionId() = externalTaskBean.executionId

  override fun getActivityId() = externalTaskBean.activityId

  override fun getActivityInstanceId() = externalTaskBean.activityInstanceId

  override fun getProcessDefinitionId() = externalTaskBean.processDefinitionId

  override fun getProcessDefinitionKey() = externalTaskBean.processDefinitionKey

  override fun getProcessDefinitionVersionTag() = externalTaskBean.processDefinitionVersionTag

  override fun getRetries() = externalTaskBean.retries

  override fun getErrorMessage() = externalTaskBean.errorMessage
  override fun isSuspended() = externalTaskBean.suspended

  override fun getTenantId() = externalTaskBean.tenantId

  override fun getPriority() = externalTaskBean.priority

  override fun getBusinessKey() = externalTaskBean.businessKey

  override fun getExtensionProperties(): MutableMap<String, String> {
    throw UnsupportedOperationException("Extension properties not supported via REST")
  }

  /**
   * @since 7.21
   */
  override fun getCreateTime(): Date {
    throw UnsupportedOperationException("Create time not supported via REST")
  }

}

/**
 * Backing bean for the external task.
 */
data class ExternalTaskBean(
  val id: String?,
  val topicName: String?,
  val workerId: String?,
  val lockExpirationTime: Date?,
  val processInstanceId: String?,
  val executionId: String?,
  val activityId: String?,
  val activityInstanceId: String?,
  val processDefinitionId: String?,
  val processDefinitionKey: String?,
  val processDefinitionVersionTag: String?,
  val tenantId: String?,
  val retries: Int?,
  val errorMessage: String?,
  val businessKey: String?,
  val priority: Long,
  val suspended: Boolean
) {

  companion object {
    /**
     * Constructs the bean from ExternalTask DTO.
     * @param dto: REST representation of the external task.
     */
    @JvmStatic
    fun fromDto(dto: ExternalTaskDto): ExternalTaskBean =
      ExternalTaskBean(
        id = dto.id,
        topicName = dto.topicName,
        workerId = dto.workerId,
        executionId = dto.executionId,
        activityId = dto.activityId,
        activityInstanceId = dto.activityInstanceId,
        processInstanceId = dto.processInstanceId,
        processDefinitionId = dto.processDefinitionId,
        processDefinitionKey = dto.processDefinitionKey,
        processDefinitionVersionTag = dto.processDefinitionVersionTag,
        tenantId = dto.tenantId,
        lockExpirationTime = dto.lockExpirationTime.toDate(),
        errorMessage = dto.errorMessage,
        retries = dto.retries,
        priority = dto.priority,
        businessKey = dto.businessKey,
        suspended = dto.suspended
      )
  }

}
