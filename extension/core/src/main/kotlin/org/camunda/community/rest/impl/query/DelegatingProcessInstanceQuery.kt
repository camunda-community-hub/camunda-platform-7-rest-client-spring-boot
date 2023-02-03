package org.camunda.community.rest.impl.query

import mu.KLogging
import org.camunda.bpm.engine.ProcessEngineException
import org.camunda.bpm.engine.impl.ProcessInstanceQueryImpl
import org.camunda.bpm.engine.impl.persistence.entity.SuspensionState
import org.camunda.bpm.engine.runtime.ProcessInstance
import org.camunda.community.rest.adapter.InstanceBean
import org.camunda.community.rest.adapter.ProcessInstanceAdapter
import org.camunda.community.rest.client.api.ProcessInstanceApiClient
import org.camunda.community.rest.client.model.ProcessInstanceQueryDto
import org.camunda.community.rest.impl.toProcessInstanceSorting
import org.camunda.community.rest.variables.toDto
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

/**
 * Implementation of the process instance query.
 */
class DelegatingProcessInstanceQuery(private val processInstanceApiClient: ProcessInstanceApiClient) : ProcessInstanceQueryImpl() {

  companion object : KLogging()

  override fun list(): List<ProcessInstance> {
    checkQueryOk()
    return processInstanceApiClient.queryProcessInstances(this.firstResult, this.maxResults, toDto()).body!!.map {
      ProcessInstanceAdapter(InstanceBean.fromProcessInstanceDto(it))
    }
  }

  override fun listPage(firstResult: Int, maxResults: Int): List<ProcessInstance> {
    checkQueryOk()
    return processInstanceApiClient.queryProcessInstances(firstResult, maxResults, toDto()).body!!.map {
      ProcessInstanceAdapter(InstanceBean.fromProcessInstanceDto(it))
    }
  }

  override fun listIds(): List<String> {
    return list().map { it.processInstanceId }
  }

  override fun unlimitedList(): List<ProcessInstance> {
    // FIXME: best approximation so far.
    return list()
  }

  override fun count(): Long {
    checkQueryOk()
    return processInstanceApiClient.queryProcessInstancesCount(toDto()).body!!.count
  }

  override fun singleResult(): ProcessInstance? {
    val results = list()
    return when {
      results.size == 1 -> results[0]
      results.size > 1 -> throw ProcessEngineException("Query return " + results.size.toString() + " results instead of max 1")
      else -> null
    }
  }

  override fun ensureVariablesInitialized() = Unit

}

fun ProcessInstanceQueryImpl.toDto() =
  ProcessInstanceQueryDto().apply {
    val dtoPropertiesByName =
      ProcessInstanceQueryDto::class.memberProperties.filterIsInstance<KMutableProperty1<ProcessInstanceQueryDto, Any?>>()
        .associateBy { it.name }
    val queryPropertiesByName = ProcessInstanceQueryImpl::class.memberProperties.associateBy { it.name }
    dtoPropertiesByName.forEach {
      val valueToSet = when (it.key) {
        "superProcessInstance" -> this@toDto.superProcessInstanceId
        "subProcessInstance" -> this@toDto.subProcessInstanceId
        "superCaseInstance" -> this@toDto.superCaseInstanceId
        "subCaseInstance" -> this@toDto.subCaseInstanceId
        "active" -> this@toDto.suspensionState?.let { it == SuspensionState.ACTIVE }
        "suspended" -> this@toDto.suspensionState?.let { it == SuspensionState.SUSPENDED }
        "processInstanceIds" -> {
          val ids = this@toDto.processInstanceIds?.toMutableSet() ?: mutableSetOf()
          if (this@toDto.processInstanceId != null) {
            ids.plus(this@toDto.processInstanceId).toList()
          } else {
            if (ids.isEmpty()) null else ids.toList()
          }
        }

        "tenantIdIn" -> this@toDto.tenantIds?.toList()
        "withoutTenantId" -> this@toDto.isTenantIdSet && (this@toDto.tenantIds == null)
        "processDefinitionWithoutTenantId" -> this@toDto.isProcessDefinitionWithoutTenantId
        "processDefinitionKeyIn" -> this@toDto.processDefinitionKeys?.toList()
        "processDefinitionKeyNotIn" -> this@toDto.processDefinitionKeyNotIn?.toList()
        "activityIdIn" -> this@toDto.activityIds?.toList()
        "rootProcessInstances" -> this@toDto.isRootProcessInstances
        "leafProcessInstances" -> this@toDto.isLeafProcessInstances
        "variables" -> this@toDto.queryVariableValues?.toDto()
        "orQueries" -> if (this@toDto.isOrQueryActive) throw UnsupportedOperationException("or-Queries are not supported") else null
        "sorting" -> this@toDto.orderingProperties.mapNotNull { it.toProcessInstanceSorting() }.filter { it.sortBy != null }
        else -> {
          val queryProperty = queryPropertiesByName[it.key]
          if (queryProperty == null) {
            throw IllegalArgumentException("no property found for ${it.key}")
          } else if (!queryProperty.returnType.isSubtypeOf(it.value.returnType)) {
            throw IllegalArgumentException("${queryProperty.returnType} is not assignable to ${it.value.returnType} for ${it.key}")
          } else {
            queryProperty.isAccessible = true
            queryProperty.get(this@toDto)
          }
        }
      }
      it.value.isAccessible = true
      it.value.set(this, valueToSet)
    }
  }
