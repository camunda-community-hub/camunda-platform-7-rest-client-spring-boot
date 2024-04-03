package org.camunda.community.rest.impl.query

import mu.KLogging
import org.camunda.bpm.engine.runtime.EventSubscription
import org.camunda.bpm.engine.runtime.EventSubscriptionQuery
import org.camunda.community.rest.adapter.EventSubscriptionAdapter
import org.camunda.community.rest.adapter.EventSubscriptionBean
import org.camunda.community.rest.client.api.EventSubscriptionApiClient
import org.springframework.web.bind.annotation.RequestParam
import kotlin.reflect.KParameter
import kotlin.reflect.full.declaredMemberProperties

class DelegatingEventSubscriptionQuery(
  private val eventSubscriptionApiClient: EventSubscriptionApiClient,
  var eventSubscriptionId: String? = null,
  var eventName: String? = null,
  var eventType: String? = null,
  var executionId: String? = null,
  var processInstanceId: String? = null,
  var activityId: String? = null,
  var includeEventSubscriptionsWithoutTenantId: Boolean = false
) : BaseQuery<EventSubscriptionQuery, EventSubscription>(), EventSubscriptionQuery {

  companion object : KLogging()

  override fun eventSubscriptionId(eventSubscriptionId: String?) = this.apply { this.eventSubscriptionId = requireNotNull(eventSubscriptionId) }

  override fun eventName(eventName: String?) = this.apply { this.eventName = requireNotNull(eventName) }

  override fun eventType(eventType: String?) = this.apply { this.eventType = requireNotNull(eventType) }

  override fun executionId(executionId: String?) = this.apply { this.executionId = requireNotNull(executionId) }

  override fun processInstanceId(processInstanceId: String?) = this.apply { this.processInstanceId = requireNotNull(processInstanceId) }

  override fun activityId(activityId: String?) = this.apply { this.activityId = requireNotNull(activityId) }

  override fun includeEventSubscriptionsWithoutTenantId() = this.apply { this.includeEventSubscriptionsWithoutTenantId = true }

  override fun orderByCreated() = this.apply { this.orderBy("created") }

  override fun listPage(firstResult: Int, maxResults: Int): List<EventSubscription> {
    validate()
    with(EventSubscriptionApiClient::getEventSubscriptions) {
      val result = callBy(parameters.associateWith { parameter ->
        when (parameter.kind) {
          KParameter.Kind.INSTANCE -> eventSubscriptionApiClient
          else -> {
            when (parameter.annotations.find { it is RequestParam }?.let { (it as RequestParam).value }) {
              "firstResult" -> firstResult
              "maxResults" -> maxResults
              else -> this@DelegatingEventSubscriptionQuery.getQueryParam(parameter)
            }
          }
        }
      })
      return result.body!!.map {
        EventSubscriptionAdapter(EventSubscriptionBean.fromDto(it))
      }
    }
  }

  override fun count(): Long {
    validate()
    with (EventSubscriptionApiClient::getEventSubscriptionsCount) {
      val result = callBy(parameters.associateWith { parameter ->
        when (parameter.kind) {
          KParameter.Kind.INSTANCE -> eventSubscriptionApiClient
          else -> this@DelegatingEventSubscriptionQuery.getQueryParam(parameter)
        }
      })
      return result.body!!.count
    }
  }

  private fun getQueryParam(parameter: KParameter): Any? {
    val value = parameter.annotations.find { it is RequestParam }?.let { (it as RequestParam).value }
    return when(value) {
      "id" -> eventSubscriptionId
      "tenantIdIn" -> tenantIds?.joinToString(",")
      "withoutTenantId" -> tenantIdsSet && tenantIds == null
      "sortBy" -> sortProperty()?.property
      "sortOrder" -> sortProperty()?.direction?.let { if (it == SortDirection.DESC) "desc" else "asc" }
      null -> throw IllegalArgumentException("value of RequestParam annotation is null")
      else -> valueForProperty(value, this, parameter.type)
    }
  }

}
