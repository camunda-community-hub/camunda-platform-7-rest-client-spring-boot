package org.camunda.community.rest.impl.query

import mu.KLogging
import org.camunda.bpm.engine.runtime.ProcessInstance
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery
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
class DelegatingProcessInstanceQuery(
  private val processInstanceApiClient: ProcessInstanceApiClient,
  var processInstanceId: String? = null,
  var processInstanceIds: Set<String>? = null,
  var businessKey: String? = null,
  var businessKeyLike: String? = null,
  var processDefinitionId: String? = null,
  var processDefinitionKey: String? = null,
  var processDefinitionKeys: Array<out String>? = null,
  var processDefinitionKeyNotIn: Array<out String>? = null,
  var deploymentId: String? = null,
  var superProcessInstanceId: String? = null,
  var subProcessInstanceId: String? = null,
  var suspensionState: SuspensionState? = null,
  var withIncident: Boolean = false,
  var incidentType: String? = null,
  var incidentId: String? = null,
  var incidentMessage: String? = null,
  var incidentMessageLike: String? = null,
  var caseInstanceId: String? = null,
  var superCaseInstanceId: String? = null,
  var subCaseInstanceId: String? = null,
  var activityIds: Array<out String>? = null,
  var isRootProcessInstances: Boolean = false,
  var isLeafProcessInstances: Boolean = false,
  var isProcessDefinitionWithoutTenantId: Boolean = false,
  var isOrQueryActive: Boolean = false
) : BaseVariableQuery<ProcessInstanceQuery, ProcessInstance>(), ProcessInstanceQuery {

  companion object : KLogging()

  override fun processInstanceId(processInstanceId: String?) = this.apply { this.processInstanceId = requireNotNull(processInstanceId) }

  override fun processInstanceIds(processInstanceIds: MutableSet<String>?) = this.apply { this.processInstanceIds = requireNotNull(processInstanceIds) }

  override fun processInstanceBusinessKey(processInstanceBusinessKey: String?) = this.apply { this.businessKey = requireNotNull(processInstanceBusinessKey) }

  override fun processInstanceBusinessKey(p0: String?, p1: String?) = this.apply { this.processInstanceId = requireNotNull(processInstanceId) }

  override fun processInstanceBusinessKeyLike(processInstanceBusinessKeyLike: String?) = this.apply { this.businessKeyLike = requireNotNull(processInstanceBusinessKeyLike) }

  override fun processDefinitionKey(processDefinitionKey: String?) = this.apply { this.processDefinitionKey = requireNotNull(processDefinitionKey) }

  override fun processDefinitionKeyIn(vararg processDefinitionKeyIn: String) = this.apply { this.processDefinitionKeys = processDefinitionKeyIn }

  override fun processDefinitionKeyNotIn(vararg processDefinitionKeyNotIn: String) = this.apply { this.processDefinitionKeyNotIn = processDefinitionKeyNotIn }

  override fun processDefinitionId(processDefinitionId: String?) = this.apply { this.processDefinitionId = requireNotNull(processDefinitionId) }

  override fun deploymentId(deploymentId: String?) = this.apply { this.deploymentId = requireNotNull(deploymentId) }

  override fun superProcessInstanceId(superProcessInstanceId: String?) = this.apply { this.superProcessInstanceId = requireNotNull(superProcessInstanceId) }

  override fun subProcessInstanceId(subProcessInstanceId: String?) = this.apply { this.subProcessInstanceId = requireNotNull(subProcessInstanceId) }

  override fun caseInstanceId(caseInstanceId: String?) = this.apply { this.caseInstanceId = requireNotNull(caseInstanceId) }

  override fun superCaseInstanceId(superCaseInstanceId: String?) = this.apply { this.superCaseInstanceId = requireNotNull(superCaseInstanceId) }

  override fun subCaseInstanceId(subCaseInstanceId: String?) = this.apply { this.subCaseInstanceId = requireNotNull(subCaseInstanceId) }

  override fun suspended() = this.apply { this.suspensionState = SuspensionState.SUSPENDED }

  override fun active() = this.apply { this.suspensionState = SuspensionState.ACTIVE }

  override fun withIncident() = this.apply { this.withIncident = true }

  override fun incidentType(incidentType: String?) = this.apply { this.incidentType = requireNotNull(incidentType) }

  override fun incidentId(incidentId: String?) = this.apply { this.incidentId = requireNotNull(incidentId) }

  override fun incidentMessage(incidentMessage: String?) = this.apply { this.incidentMessage = requireNotNull(incidentMessage) }

  override fun incidentMessageLike(incidentMessageLike: String?) = this.apply { this.incidentMessageLike = requireNotNull(incidentMessageLike) }

  override fun activityIdIn(vararg activityIdIn: String) = this.apply { this.activityIds = activityIdIn }

  override fun rootProcessInstances() = this.apply { this.isRootProcessInstances = true }

  override fun leafProcessInstances() = this.apply { this.isLeafProcessInstances = true }

  override fun processDefinitionWithoutTenantId() = this.apply { this.isProcessDefinitionWithoutTenantId = true }

  override fun orderByProcessInstanceId() = this.apply { orderBy("instanceId") }

  override fun orderByProcessDefinitionKey() = this.apply { orderBy("definitionKey") }

  override fun orderByProcessDefinitionId() = this.apply { orderBy("definitionId") }

  override fun orderByBusinessKey() = this.apply { orderBy("businessKey") }

  override fun or() = this.apply { isOrQueryActive = true }

  override fun endOr() = this

  override fun listPage(firstResult: Int, maxResults: Int): List<ProcessInstance> =
    processInstanceApiClient.queryProcessInstances(firstResult, maxResults, fillQueryDto()).body!!.map {
      ProcessInstanceAdapter(InstanceBean.fromProcessInstanceDto(it))
    }

  override fun count() = processInstanceApiClient.queryProcessInstancesCount(fillQueryDto()).body!!.count

  fun fillQueryDto() = ProcessInstanceQueryDto().apply {
    checkQueryOk()
    val dtoPropertiesByName = ProcessInstanceQueryDto::class.memberProperties.filterIsInstance<KMutableProperty1<ProcessInstanceQueryDto, Any?>>().associateBy { it.name }
    val queryPropertiesByName = DelegatingProcessInstanceQuery::class.memberProperties.associateBy { it.name }
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
            ids.plus(this@DelegatingProcessInstanceQuery.processInstanceId).toList()
          } else {
            if (ids.isEmpty()) null else ids.toList()
          }
        }
        "tenantIdIn" -> this@DelegatingProcessInstanceQuery.tenantIds?.toList()
        "withoutTenantId" -> this@DelegatingProcessInstanceQuery.tenantIdsSet && (this@DelegatingProcessInstanceQuery.tenantIds == null)
        "processDefinitionWithoutTenantId" -> this@DelegatingProcessInstanceQuery.isProcessDefinitionWithoutTenantId
        "processDefinitionKeyIn" -> this@DelegatingProcessInstanceQuery.processDefinitionKeys?.toList()
        "processDefinitionKeyNotIn" -> this@DelegatingProcessInstanceQuery.processDefinitionKeyNotIn?.toList()
        "activityIdIn" -> this@DelegatingProcessInstanceQuery.activityIds?.toList()
        "rootProcessInstances" -> this@DelegatingProcessInstanceQuery.isRootProcessInstances
        "leafProcessInstances" -> this@DelegatingProcessInstanceQuery.isLeafProcessInstances
        "variables" -> this@DelegatingProcessInstanceQuery.queryVariableValues.toDto()
        "orQueries" -> if (this@DelegatingProcessInstanceQuery.isOrQueryActive) throw UnsupportedOperationException("or-Queries are not supported") else null
        "sorting" -> this@DelegatingProcessInstanceQuery.orderingProperties.map { it.toProcessInstanceSorting() }.filter { it.sortBy != null }
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

