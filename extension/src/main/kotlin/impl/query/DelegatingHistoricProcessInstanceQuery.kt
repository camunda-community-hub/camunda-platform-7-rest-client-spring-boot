package org.camunda.bpm.extension.rest.impl.query

import mu.KLogging
import org.camunda.bpm.engine.ProcessEngineException
import org.camunda.bpm.engine.history.HistoricProcessInstance
import org.camunda.bpm.engine.impl.HistoricProcessInstanceQueryImpl
import org.camunda.bpm.engine.impl.persistence.entity.SuspensionState
import org.camunda.bpm.extension.rest.adapter.HistoricInstanceBean
import org.camunda.bpm.extension.rest.adapter.HistoricProcessInstanceAdapter
import org.camunda.bpm.extension.rest.client.api.HistoricProcessInstanceApiClient
import org.camunda.bpm.extension.rest.client.model.HistoricProcessInstanceQueryDto
import org.camunda.bpm.extension.rest.impl.toProcessInstanceSorting
import org.camunda.bpm.extension.rest.variables.toDto
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

/**
 * Implementation of the process instance query.
 */
class DelegatingHistoricProcessInstanceQuery(private val historicProcessInstanceApiClient: HistoricProcessInstanceApiClient) : HistoricProcessInstanceQueryImpl() {

  companion object : KLogging()

  override fun list(): List<HistoricProcessInstance> =
    historicProcessInstanceApiClient.queryHistoricProcessInstances(this.firstResult, this.maxResults, fillQueryDto()).body!!.map {
      HistoricProcessInstanceAdapter(HistoricInstanceBean.fromHistoricProcessInstanceDto(it))
    }

  override fun listPage(firstResult: Int, maxResults: Int): List<HistoricProcessInstance> =
    historicProcessInstanceApiClient.queryHistoricProcessInstances(firstResult, maxResults, fillQueryDto()).body!!.map {
      HistoricProcessInstanceAdapter(HistoricInstanceBean.fromHistoricProcessInstanceDto(it))
    }

  override fun listIds(): List<String> {
    return list().map { it.id }
  }

  override fun unlimitedList(): List<HistoricProcessInstance> {
    // FIXME: best approximation so far.
    return list()
  }

  override fun count() = historicProcessInstanceApiClient.queryHistoricProcessInstancesCount(fillQueryDto()).body!!.count

  override fun singleResult(): HistoricProcessInstance? {
    val results = list()
    return when {
      results.size == 1 -> results[0]
      results.size > 1 -> throw ProcessEngineException("Query return " + results.size.toString() + " results instead of max 1")
      else -> null
    }
  }

  override fun ensureVariablesInitialized() = Unit

  fun fillQueryDto() = HistoricProcessInstanceQueryDto().apply {
    checkQueryOk()
    val dtoPropertiesByName = HistoricProcessInstanceQueryDto::class.memberProperties.filterIsInstance<KMutableProperty1<HistoricProcessInstanceQueryDto, Any?>>().associateBy { it.name }
    val queryPropertiesByName = HistoricProcessInstanceQueryImpl::class.memberProperties.associateBy { it.name }
    dtoPropertiesByName.forEach {
      val valueToSet = when (it.key) {
        "superProcessInstance" -> this@DelegatingHistoricProcessInstanceQuery.superProcessInstanceId
        "subProcessInstance" -> this@DelegatingHistoricProcessInstanceQuery.subProcessInstanceId
        "superCaseInstance" -> this@DelegatingHistoricProcessInstanceQuery.superCaseInstanceId
        "subCaseInstance" -> this@DelegatingHistoricProcessInstanceQuery.subCaseInstanceId
        "active" -> this@DelegatingHistoricProcessInstanceQuery.suspensionState?.let { it == SuspensionState.ACTIVE }
        "suspended" -> this@DelegatingHistoricProcessInstanceQuery.suspensionState?.let { it == SuspensionState.SUSPENDED }
        "processInstanceIds" -> {
          val ids = this@DelegatingHistoricProcessInstanceQuery.processInstanceIds?.toMutableSet() ?: mutableSetOf()
          if (this@DelegatingHistoricProcessInstanceQuery.processInstanceId != null) {
            ids.plus(this@DelegatingHistoricProcessInstanceQuery.processInstanceId)
          }
          if (ids.isEmpty()) null else ids.toList()
        }
        "tenantIdIn" -> this@DelegatingHistoricProcessInstanceQuery.tenantIds?.toList()
        "withoutTenantId" -> this@DelegatingHistoricProcessInstanceQuery.isTenantIdSet && (this@DelegatingHistoricProcessInstanceQuery.tenantIds == null)
        "processDefinitionWithoutTenantId" -> this@DelegatingHistoricProcessInstanceQuery.isProcessDefinitionWithoutTenantId
        "processDefinitionKeyIn" -> this@DelegatingHistoricProcessInstanceQuery.processDefinitionKeys?.toList()
        "processDefinitionKeyNotIn" -> this@DelegatingHistoricProcessInstanceQuery.processDefinitionKeyNotIn?.toList()
        "activityIdIn" -> this@DelegatingHistoricProcessInstanceQuery.activityIds?.toList()
        "rootProcessInstances" -> this@DelegatingHistoricProcessInstanceQuery.isRootProcessInstances
        "leafProcessInstances" -> this@DelegatingHistoricProcessInstanceQuery.isLeafProcessInstances
        "variables" -> this@DelegatingHistoricProcessInstanceQuery.queryVariableValues?.toDto()
        "orQueries" -> if (this@DelegatingHistoricProcessInstanceQuery.isOrQueryActive) throw UnsupportedOperationException("or-Queries are not supported") else null
        "sorting" -> this@DelegatingHistoricProcessInstanceQuery.orderingProperties.mapNotNull { it.toProcessInstanceSorting() }.filter { it.sortBy != null }
        else -> {
          val queryProperty = queryPropertiesByName[it.key]
          if (queryProperty == null) {
            throw IllegalArgumentException("no property found for ${it.key}")
          } else if (!queryProperty.returnType.isSubtypeOf(it.value.returnType)) {
            throw IllegalArgumentException("${queryProperty.returnType} is not assignable to ${it.value.returnType} for ${it.key}")
          } else {
            queryProperty.isAccessible = true
            queryProperty.get(this@DelegatingHistoricProcessInstanceQuery)
          }
        }
      }
      it.value.isAccessible = true
      it.value.set(this, valueToSet)
    }
  }

}

