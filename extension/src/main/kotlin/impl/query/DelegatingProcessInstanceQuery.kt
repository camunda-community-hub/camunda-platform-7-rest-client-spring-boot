package org.camunda.bpm.extension.rest.impl.query

import mu.KLogging
import org.camunda.bpm.engine.ProcessEngineException
import org.camunda.bpm.engine.impl.ProcessInstanceQueryImpl
import org.camunda.bpm.engine.impl.persistence.entity.SuspensionState
import org.camunda.bpm.engine.runtime.ProcessInstance
import org.camunda.bpm.extension.rest.adapter.InstanceBean
import org.camunda.bpm.extension.rest.adapter.ProcessInstanceAdapter
import org.camunda.bpm.extension.rest.client.api.ProcessInstanceApiClient
import org.camunda.bpm.extension.rest.client.model.ProcessInstanceQueryDto
import org.camunda.bpm.extension.rest.impl.toProcessInstanceSorting
import org.camunda.bpm.extension.rest.variables.toDto
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

/**
 * Implementation of the process instance query.
 */
class DelegatingProcessInstanceQuery(private val processInstanceApiClient: ProcessInstanceApiClient) : ProcessInstanceQueryImpl() {

  companion object : KLogging()

  override fun list(): List<ProcessInstance> =
    processInstanceApiClient.queryProcessInstances(this.firstResult, this.maxResults, fillQueryDto()).body!!.map {
      ProcessInstanceAdapter(InstanceBean.fromProcessInstanceDto(it))
    }

  override fun listPage(firstResult: Int, maxResults: Int): List<ProcessInstance> =
    processInstanceApiClient.queryProcessInstances(firstResult, maxResults, fillQueryDto()).body!!.map {
      ProcessInstanceAdapter(InstanceBean.fromProcessInstanceDto(it))
    }

  override fun listIds(): List<String> {
    return list().map { it.processInstanceId }
  }

  override fun unlimitedList(): List<ProcessInstance> {
    // FIXME: best approximation so far.
    return list()
  }

  override fun count() = processInstanceApiClient.queryProcessInstancesCount(fillQueryDto()).body!!.count

  override fun singleResult(): ProcessInstance? {
    val results = list()
    return when {
      results.size == 1 -> results[0]
      results.size > 1 -> throw ProcessEngineException("Query return " + results.size.toString() + " results instead of max 1")
      else -> null
    }
  }

  override fun ensureVariablesInitialized() = Unit

  private fun fillQueryDto() = ProcessInstanceQueryDto().apply {
    checkQueryOk()
    val dtoPropertiesByName = ProcessInstanceQueryDto::class.memberProperties.filterIsInstance<KMutableProperty1<ProcessInstanceQueryDto, Any?>>().associateBy { it.name }
    val queryPropertiesByName = ProcessInstanceQueryImpl::class.memberProperties.associateBy { it.name }
    dtoPropertiesByName.forEach {
      val valueToSet = when (it.key) {
        "superProcessInstance" -> this@DelegatingProcessInstanceQuery.superProcessInstanceId
        "subProcessInstance" -> this@DelegatingProcessInstanceQuery.subProcessInstanceId
        "superCaseInstance" -> this@DelegatingProcessInstanceQuery.superCaseInstanceId
        "subCaseInstance" -> this@DelegatingProcessInstanceQuery.subCaseInstanceId
        "active" -> this@DelegatingProcessInstanceQuery.suspensionState?.let { it == SuspensionState.ACTIVE }
        "suspended" -> this@DelegatingProcessInstanceQuery.suspensionState?.let { it == SuspensionState.SUSPENDED }
        "processInstanceIds" -> {
          val ids = this@DelegatingProcessInstanceQuery.processInstanceIds?.toMutableSet() ?: mutableSetOf()
          if (this@DelegatingProcessInstanceQuery.processInstanceId != null) {
            ids.plus(this@DelegatingProcessInstanceQuery.processInstanceId)
          }
          if (ids.isEmpty()) null else ids.toList()
        }
        "tenantIdIn" -> this@DelegatingProcessInstanceQuery.tenantIds?.toList()
        "withoutTenantId" -> this@DelegatingProcessInstanceQuery.isTenantIdSet && (this@DelegatingProcessInstanceQuery.tenantIds == null)
        "processDefinitionWithoutTenantId" -> this@DelegatingProcessInstanceQuery.isProcessDefinitionWithoutTenantId
        "processDefinitionKeyIn" -> this@DelegatingProcessInstanceQuery.processDefinitionKeys?.toList()
        "processDefinitionKeyNotIn" -> this@DelegatingProcessInstanceQuery.processDefinitionKeyNotIn?.toList()
        "activityIdIn" -> this@DelegatingProcessInstanceQuery.activityIds?.toList()
        "rootProcessInstances" -> this@DelegatingProcessInstanceQuery.isRootProcessInstances
        "leafProcessInstances" -> this@DelegatingProcessInstanceQuery.isLeafProcessInstances
        "variables" -> this@DelegatingProcessInstanceQuery.queryVariableValues?.toDto()
        "orQueries" -> if (this@DelegatingProcessInstanceQuery.isOrQueryActive) throw UnsupportedOperationException("or-Queries are not supported") else null
        "sorting" -> this@DelegatingProcessInstanceQuery.orderingProperties.mapNotNull { it.toProcessInstanceSorting() }.filter { it.sortBy != null }
        else -> {
          val queryProperty = queryPropertiesByName[it.key]
          if (queryProperty == null) {
            throw IllegalArgumentException("no property found for ${it.key}")
          } else if (!queryProperty.returnType.isSubtypeOf(it.value.returnType)) {
            throw IllegalArgumentException("${queryProperty.returnType} is not assignable to ${it.value.returnType} for ${it.key}")
          } else {
            queryProperty.isAccessible = true
            queryProperty.get(this@DelegatingProcessInstanceQuery)
          }
        }
      }
      it.value.isAccessible = true
      it.value.set(this, valueToSet)
    }
  }

}

