package org.camunda.community.rest.impl.query

import mu.KLogging
import org.camunda.bpm.engine.ProcessEngineException
import org.camunda.bpm.engine.impl.Direction
import org.camunda.bpm.engine.impl.EventSubscriptionQueryImpl
import org.camunda.bpm.engine.impl.EventSubscriptionQueryProperty
import org.camunda.bpm.engine.runtime.EventSubscription
import org.camunda.community.rest.adapter.EventSubscriptionAdapter
import org.camunda.community.rest.adapter.EventSubscriptionBean
import org.camunda.community.rest.client.api.EventSubscriptionApiClient
import org.springframework.web.bind.annotation.RequestParam
import kotlin.reflect.KParameter
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.jvm.isAccessible

class DelegatingEventSubscriptionQuery(
  private val eventSubscriptionApiClient: EventSubscriptionApiClient
) : EventSubscriptionQueryImpl() {

  companion object : KLogging()

  override fun list(): List<EventSubscription> = listPage(this.firstResult, this.maxResults)

  override fun listPage(firstResult: Int, maxResults: Int): List<EventSubscription> {
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

  override fun listIds(): List<String> {
    return list().map { it.id }
  }

  override fun unlimitedList(): List<EventSubscription> {
    // FIXME: best approximation so far.
    return list()
  }

  override fun count(): Long {
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

  override fun singleResult(): EventSubscription? {
    val results = list()
    return when {
      results.size == 1 -> results[0]
      results.size > 1 -> throw ProcessEngineException("Query return " + results.size.toString() + " results instead of expected maximum 1")
      else -> null
    }
  }

  private fun getQueryParam(parameter: KParameter): Any? {
    checkQueryOk()
    val value = parameter.annotations.find { it is RequestParam }?.let { (it as RequestParam).value }
    val propertiesByName = EventSubscriptionQueryImpl::class.declaredMemberProperties.associateBy { it.name }
    if (this.orderingProperties.size > 1) logger.warn { "sorting with more than one property not supported, ignoring all but first" }
    val sortProperty = this.orderingProperties.firstOrNull()
    return when(value) {
      "id" -> eventSubscriptionId
      "tenantIdIn" -> tenantIds?.toList()
      "withoutTenantId" -> isTenantIdSet && tenantIds == null
      "sortBy" -> when (sortProperty?.queryProperty) {
        EventSubscriptionQueryProperty.TENANT_ID -> "tenantId"
        EventSubscriptionQueryProperty.CREATED -> "created"
        null -> null
        else -> {
          logger.warn { "unknown query property ${sortProperty.queryProperty}, ignoring it" }
        }
      }
      "sortOrder" -> sortProperty?.direction?.let { if (it == Direction.DESCENDING) "desc" else "asc" }
      else -> {
        val property = propertiesByName[value]
        if (property == null) {
          throw IllegalArgumentException("no property found for $value")
        } else if (!property.returnType.isSubtypeOf(parameter.type)) {
          throw IllegalArgumentException("${property.returnType} is not assignable to ${parameter.type} for $value")
        } else {
          property.isAccessible = true
          val propValue = property.get(this)
          if (propValue is Collection<*>) propValue.joinToString(",") else propValue
        }
      }
    }
  }

}
