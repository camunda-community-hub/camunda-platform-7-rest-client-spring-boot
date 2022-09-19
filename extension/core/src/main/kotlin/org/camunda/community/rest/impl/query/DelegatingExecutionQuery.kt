package org.camunda.community.rest.impl.query

import mu.KLogging
import org.camunda.bpm.engine.ProcessEngineException
import org.camunda.bpm.engine.exception.NotValidException
import org.camunda.bpm.engine.impl.ExecutionQueryImpl
import org.camunda.bpm.engine.impl.persistence.entity.SuspensionState
import org.camunda.bpm.engine.runtime.Execution
import org.camunda.community.rest.adapter.ExecutionAdapter
import org.camunda.community.rest.adapter.ExecutionBean
import org.camunda.community.rest.client.api.ExecutionApiClient
import org.camunda.community.rest.client.model.ExecutionQueryDto
import org.camunda.community.rest.impl.toExecutionSorting
import org.camunda.community.rest.variables.toDto
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

class DelegatingExecutionQuery(
  private val executionApiClient: ExecutionApiClient
) : ExecutionQueryImpl() {

  companion object : KLogging()

  override fun list(): List<Execution> = listPage(this.firstResult, this.maxResults)

  override fun listPage(firstResult: Int, maxResults: Int): List<Execution> =
    executionApiClient.queryExecutions(this.firstResult, this.maxResults, fillQueryDto()).body!!.map {
      ExecutionAdapter(ExecutionBean.fromExecutionDto(it))
    }

  override fun listIds(): List<String> {
    return list().map { it.id }
  }

  override fun unlimitedList(): List<Execution> {
    // FIXME: best approximation so far.
    return list()
  }

  override fun count() = executionApiClient.queryExecutionsCount(fillQueryDto()).body!!.count!!

  override fun singleResult(): Execution? {
    val results = list()
    return when {
      results.size == 1 -> results[0]
      results.size > 1 -> throw ProcessEngineException("Query return " + results.size.toString() + " results instead of expected maximum 1")
      else -> null
    }
  }

  fun fillQueryDto() = ExecutionQueryDto().apply {
    checkQueryOk()
    this@DelegatingExecutionQuery.eventSubscriptions?.let {
      if (it.filter { s -> s.eventType == "signal" }.size > 1) {
        throw NotValidException("Only one signal name for event subscriptions allowed")
      }
      if (it.filter { s -> s.eventType == "message" }.size > 1) {
        throw NotValidException("Only one message name for event subscriptions allowed")
      }
    }
    val dtoPropertiesByName = ExecutionQueryDto::class.memberProperties.filterIsInstance<KMutableProperty1<ExecutionQueryDto, Any?>>().associateBy { it.name }
    val propertiesByName = ExecutionQueryImpl::class.declaredMemberProperties.associateBy { it.name }
    dtoPropertiesByName.forEach {
      val valueToSet = when (it.key) {
        "signalEventSubscriptionName" -> this@DelegatingExecutionQuery.eventSubscriptions?.filter { it.eventType == "signal" }
          ?.map { it.eventName }?.firstOrNull()

        "messageEventSubscriptionName" -> this@DelegatingExecutionQuery.eventSubscriptions?.filter { it.eventType == "message" }
          ?.map { it.eventName }?.firstOrNull()

        "active" -> if (this@DelegatingExecutionQuery.suspensionState == SuspensionState.ACTIVE) true else null
        "suspended" -> if (this@DelegatingExecutionQuery.suspensionState == SuspensionState.SUSPENDED) true else null
        "tenantIdIn" -> this@DelegatingExecutionQuery.tenantIds?.toList()
        "withoutTenantId" -> this@DelegatingExecutionQuery.isTenantIdSet && (this@DelegatingExecutionQuery.tenantIds == null)
        "processVariables" -> this@DelegatingExecutionQuery.queryVariableValues?.filter { !it.isLocal }?.toDto()
        "variables" -> this@DelegatingExecutionQuery.queryVariableValues?.filter { it.isLocal }?.toDto()
        "variableNamesIgnoreCase" -> this@DelegatingExecutionQuery.variableNamesIgnoreCase
        "variableValuesIgnoreCase" -> this@DelegatingExecutionQuery.variableValuesIgnoreCase
        "sorting" -> this@DelegatingExecutionQuery.orderingProperties.mapNotNull { it.toExecutionSorting() }.filter { it.sortBy != null }
        else -> {
          val queryProperty = propertiesByName[it.key]
          if (queryProperty == null) {
            throw IllegalArgumentException("no property found for ${it.key}")
          } else if (!queryProperty.returnType.isSubtypeOf(it.value.returnType)) {
            logger.warn { "${queryProperty.returnType} is not assignable to ${it.value.returnType} for ${it.key}" }
            null
            //            throw IllegalArgumentException("${queryProperty.returnType} is not assignable to ${it.value.returnType} for ${it.key}")
          } else {
            queryProperty.isAccessible = true
            queryProperty.get(this@DelegatingExecutionQuery)
          }
        }
      }
      it.value.isAccessible = true
      it.value.set(this, valueToSet)
    }
  }

}
