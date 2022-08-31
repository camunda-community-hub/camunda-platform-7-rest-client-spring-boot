package org.camunda.community.rest.adapter

import org.camunda.bpm.engine.runtime.EventSubscription
import org.camunda.community.rest.client.model.EventSubscriptionDto
import java.util.*

class EventSubscriptionAdapter(private val eventSubscriptionBean: EventSubscriptionBean) : EventSubscription {
  override fun getId() = eventSubscriptionBean.id

  override fun getEventType() = eventSubscriptionBean.eventType

  override fun getEventName() = eventSubscriptionBean.eventName

  override fun getExecutionId() = eventSubscriptionBean.executionId

  override fun getProcessInstanceId() = eventSubscriptionBean.processInstanceId

  override fun getActivityId() = eventSubscriptionBean.activityId

  override fun getTenantId() = eventSubscriptionBean.tenantId

  override fun getCreated() = eventSubscriptionBean.createdDate
}

/**
 * POJO to hold the values of a task.
 */
data class EventSubscriptionBean(
  val id: String,
  val eventName: String?,
  val eventType: String,
  var createdDate: Date,
  var tenantId: String?,
  var activityId: String?,
  var processInstanceId: String?,
  var executionId: String?,
) {
  companion object {
    /**
     * Factory method to create bean from REST representation.
     */
    @JvmStatic
    fun fromDto(dto: EventSubscriptionDto) = EventSubscriptionBean(
      id = dto.id,
      eventName = dto.eventName,
      eventType = dto.eventType,
      createdDate = dto.createdDate,
      tenantId = dto.tenantId,
      executionId = dto.executionId,
      processInstanceId = dto.processInstanceId,
      activityId = dto.activityId
    )
  }
}
