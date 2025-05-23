package org.camunda.community.rest.adapter

import org.camunda.bpm.engine.externaltask.LockedExternalTask
import org.camunda.bpm.engine.variable.VariableMap
import org.camunda.community.rest.client.model.LockedExternalTaskDto
import org.camunda.community.rest.impl.toDate
import org.camunda.community.rest.impl.toRequiredDate
import org.camunda.community.rest.variables.ValueMapper
import java.util.*

/**
 * Implementation of Camunda API Locked External Task backed by a bean.
 */
class LockedExternalTaskAdapter(
  private val lockedExternalTaskBean: LockedExternalTaskBean
) : LockedExternalTask {
  override fun getId() = lockedExternalTaskBean.id

  override fun getTopicName() = lockedExternalTaskBean.topicName

  override fun getWorkerId() = lockedExternalTaskBean.workerId

  override fun getLockExpirationTime() = lockedExternalTaskBean.lockExpirationTime

  override fun getProcessInstanceId() = lockedExternalTaskBean.processInstanceId

  override fun getExecutionId() = lockedExternalTaskBean.executionId

  override fun getActivityId() = lockedExternalTaskBean.activityId

  override fun getActivityInstanceId() = lockedExternalTaskBean.activityInstanceId

  override fun getProcessDefinitionId() = lockedExternalTaskBean.processDefinitionId

  override fun getProcessDefinitionKey() = lockedExternalTaskBean.processDefinitionKey

  override fun getProcessDefinitionVersionTag() = lockedExternalTaskBean.processDefinitionVersionTag

  override fun getRetries() = lockedExternalTaskBean.retries

  override fun getErrorMessage() = lockedExternalTaskBean.errorMessage

  override fun getErrorDetails() = lockedExternalTaskBean.errorDetails

  override fun getVariables() = lockedExternalTaskBean.variables

  override fun getTenantId() = lockedExternalTaskBean.tenantId

  override fun getPriority() = lockedExternalTaskBean.priority

  override fun getBusinessKey() = lockedExternalTaskBean.businessKey

  override fun getExtensionProperties(): Map<String, String>? = lockedExternalTaskBean.extensionProperties

  override fun getCreateTime(): Date = lockedExternalTaskBean.createTime
}

/**
 * Backing bean for the locked external task.
 */
data class LockedExternalTaskBean(
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
  val errorDetails: String?,
  val businessKey: String?,
  val priority: Long,
  val variables: VariableMap?,
  val extensionProperties: Map<String, String>?,
  val createTime: Date,
) {

  companion object {
    /**
     * Constructs the bean from LockedExternalTask DTO.
     * @param dto: REST representation of the locked external task.
     */
    @JvmStatic
    fun fromDto(dto: LockedExternalTaskDto, valueMapper: ValueMapper, deserializeValues: Boolean = false): LockedExternalTaskBean =
      LockedExternalTaskBean(
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
        errorDetails = dto.errorDetails,
        retries = dto.retries,
        priority = dto.priority,
        businessKey = dto.businessKey,
        variables = dto.variables?.let { valueMapper.mapDtos(it) },
        extensionProperties = dto.extensionProperties,
        createTime = dto.createTime.toRequiredDate(),
      )
  }

}
