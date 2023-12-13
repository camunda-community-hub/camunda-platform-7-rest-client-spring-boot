package org.camunda.community.rest.impl.query

import mu.KLogging
import org.camunda.bpm.engine.BadUserRequestException
import org.camunda.bpm.engine.history.HistoricProcessInstance
import org.camunda.bpm.engine.history.HistoricProcessInstanceQuery
import org.camunda.community.rest.adapter.HistoricInstanceBean
import org.camunda.community.rest.adapter.HistoricProcessInstanceAdapter
import org.camunda.community.rest.client.api.HistoricProcessInstanceApiClient
import org.camunda.community.rest.client.model.HistoricProcessInstanceQueryDto
import org.camunda.community.rest.impl.toHistoricProcessInstanceSorting
import org.camunda.community.rest.variables.toDto
import java.util.*
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

/**
 * Implementation of the process instance query.
 */
class DelegatingHistoricProcessInstanceQuery(
  private val historicProcessInstanceApiClient: HistoricProcessInstanceApiClient,
  var processInstanceId: String? = null,
  var processDefinitionId: String? = null,
  var processDefinitionName: String? = null,
  var processDefinitionNameLike: String? = null,
  var businessKey: String? = null,
  var businessKeyIn: Array<out String>? = null,
  var businessKeyLike: String? = null,
  var incidentType: String? = null,
  var incidentStatus: String? = null,
  var incidentMessage: String? = null,
  var incidentMessageLike: String? = null,
  var startedBy: String? = null,
  var isRootProcessInstances: Boolean = false,
  var superProcessInstanceId: String? = null,
  var subProcessInstanceId: String? = null,
  var superCaseInstanceId: String? = null,
  var subCaseInstanceId: String? = null,
  var processKeyNotIn: Array<out String>? = null,
  var finished: Boolean = false,
  var unfinished: Boolean = false,
  var withIncidents: Boolean = false,
  var withRootIncidents: Boolean = false,
  var startedBefore: Date? = null,
  var startedAfter: Date? = null,
  var finishedBefore: Date? = null,
  var finishedAfter: Date? = null,
  var executedActivityAfter: Date? = null,
  var executedActivityBefore: Date? = null,
  var executedJobAfter: Date? = null,
  var executedJobBefore: Date? = null,
  var processDefinitionKey: String? = null,
  var processDefinitionKeys: Array<out String>? = null,
  var processInstanceIds: Array<out String>? = null,
  var executedActivityIds: Array<out String>? = null,
  var activeActivityIds: Array<out String>? = null,
  var state: String? = null,
  var caseInstanceId: String? = null,
  var startDateBy: Date? = null,
  var startDateOn: Date? = null,
  var finishDateBy: Date? = null,
  var finishDateOn: Date? = null,
  var startDateOnBegin: Date? = null,
  var startDateOnEnd: Date? = null,
  var finishDateOnBegin: Date? = null,
  var finishDateOnEnd: Date? = null,
  var isOrQueryActive: Boolean = false
) : BaseVariableQuery<HistoricProcessInstanceQuery, HistoricProcessInstance>(), HistoricProcessInstanceQuery {

  companion object : KLogging()

  override fun processInstanceId(processInstanceId: String?) = this.apply { this.processInstanceId = requireNotNull(processInstanceId) }

  override fun processInstanceIds(processInstanceIds: MutableSet<String>?) = this.apply { this.processInstanceIds = requireNotNull(processInstanceIds).toTypedArray() }

  override fun processDefinitionId(processDefinitionId: String?) = this.apply { this.processDefinitionId = requireNotNull(processDefinitionId) }

  override fun processDefinitionKey(processDefinitionKey: String?) = this.apply { this.processDefinitionKey = requireNotNull(processDefinitionKey) }

  override fun processDefinitionKeyIn(vararg processDefinitionKeyIn: String) = this.apply { this.processDefinitionKeys = processDefinitionKeyIn }


  override fun processDefinitionKeyNotIn(processDefinitionKeyNotIn: MutableList<String>) = this.apply { this.processKeyNotIn = processDefinitionKeyNotIn.toTypedArray() }

  override fun processDefinitionName(processDefinitionName: String?) = this.apply { this.processDefinitionName = requireNotNull(processDefinitionName) }

  override fun processDefinitionNameLike(processDefinitionNameLike: String?) = this.apply { this.processDefinitionNameLike = requireNotNull(processDefinitionNameLike) }

  override fun processInstanceBusinessKey(processInstanceBusinessKey: String?) = this.apply { this.businessKey = requireNotNull(processInstanceBusinessKey) }

  override fun processInstanceBusinessKeyIn(vararg processInstanceBusinessKeyIn: String) = this.apply { this.businessKeyIn = processInstanceBusinessKeyIn }

  override fun processInstanceBusinessKeyLike(processInstanceBusinessKeyLike: String?) = this.apply { this.businessKeyLike = requireNotNull(processInstanceBusinessKeyLike) }

  override fun finished() = this.apply { this.finished = true }

  override fun unfinished() = this.apply { this.unfinished = true }

  override fun withIncidents() = this.apply { this.withIncidents = true }

  override fun withRootIncidents() = this.apply { this.withRootIncidents = true }

  override fun incidentStatus(incidentStatus: String?) = this.apply { this.incidentStatus = requireNotNull(incidentStatus) }

  override fun incidentType(incidentType: String?) = this.apply { this.incidentType = requireNotNull(incidentType) }

  override fun incidentMessage(incidentMessage: String?) = this.apply { this.incidentMessage = requireNotNull(incidentMessage) }

  override fun incidentMessageLike(incidentMessageLike: String?) = this.apply { this.incidentMessageLike = requireNotNull(incidentMessageLike) }

  override fun caseInstanceId(caseInstanceId: String?) = this.apply { this.caseInstanceId = requireNotNull(caseInstanceId) }

  override fun startedBefore(startedBefore: Date?) = this.apply { this.startedBefore = requireNotNull(startedBefore) }

  override fun startedAfter(startedAfter: Date?) = this.apply { this.startedAfter = requireNotNull(startedAfter) }

  override fun finishedBefore(finishedBefore: Date?) = this.apply {
    this.finishedBefore = requireNotNull(finishedBefore)
    this.finished = true
  }

  override fun finishedAfter(finishedAfter: Date?) = this.apply {
    this.finishedAfter = requireNotNull(finishedAfter)
    this.finished = true
  }

  override fun startedBy(startedBy: String?) = this.apply { this.startedBy = requireNotNull(startedBy) }

  override fun orderByProcessInstanceId() = this.apply { this.orderBy("instanceId") }

  override fun orderByProcessDefinitionId() = this.apply { this.orderBy("definitionId") }

  override fun orderByProcessDefinitionKey() = this.apply { this.orderBy("definitionKey") }

  override fun orderByProcessDefinitionName() = this.apply { this.orderBy("definitionName") }

  override fun orderByProcessDefinitionVersion() = this.apply { this.orderBy("definitionVersion") }

  override fun orderByProcessInstanceBusinessKey() = this.apply { this.orderBy("businessKey") }

  override fun orderByProcessInstanceStartTime() = this.apply { this.orderBy("startTime") }

  override fun orderByProcessInstanceEndTime() = this.apply { this.orderBy("endTime") }

  override fun orderByProcessInstanceDuration()  = this.apply { this.orderBy("duration") }

  override fun rootProcessInstances() = this.apply {
    if (superProcessInstanceId != null) {
      throw BadUserRequestException("Invalid query usage: cannot set both rootProcessInstances and superProcessInstanceId")
    }
    if (superCaseInstanceId != null) {
      throw BadUserRequestException("Invalid query usage: cannot set both rootProcessInstances and superCaseInstanceId")
    }
    this.isRootProcessInstances = true
  }

  override fun superProcessInstanceId(superProcessInstanceId: String?) = this.apply {
    if (isRootProcessInstances) {
      throw BadUserRequestException("Invalid query usage: cannot set both rootProcessInstances and superProcessInstanceId")
    }
    this.superProcessInstanceId = requireNotNull(superProcessInstanceId)
  }

  override fun subProcessInstanceId(subProcessInstanceId: String?) = this.apply { this.subProcessInstanceId = requireNotNull(subProcessInstanceId) }

  override fun superCaseInstanceId(superCaseInstanceId: String?) = this.apply {
    if (isRootProcessInstances) {
      throw BadUserRequestException("Invalid query usage: cannot set both rootProcessInstances and superCaseInstanceId")
    }
    this.superCaseInstanceId = requireNotNull(superCaseInstanceId)
  }

  override fun subCaseInstanceId(subCaseInstanceId: String?) = this.apply { this.subCaseInstanceId = requireNotNull(subCaseInstanceId) }

  @Deprecated("Deprecated in Java")
  override fun startDateBy(startDateBy: Date?) = this.apply { this.startDateBy = requireNotNull(startDateBy) }

  @Deprecated("Deprecated in Java")
  override fun startDateOn(startDateOn: Date?) = this.apply {
    this.startDateOn = requireNotNull(startDateOn)
    this.startDateOnBegin = this.calculateMidnight(startDateOn)
    this.startDateOnEnd = this.calculateBeforeMidnight(startDateOn)

  }

  @Deprecated("Deprecated in Java")
  override fun finishDateBy(finishDateBy: Date?) = this.apply { this.finishDateBy = requireNotNull(finishDateBy) }

  @Deprecated("Deprecated in Java")
  override fun finishDateOn(finishDateOn: Date?) = this.apply {
    this.finishDateOn = requireNotNull(finishDateOn)
    this.finishDateOnBegin = this.calculateMidnight(finishDateOn)
    this.finishDateOnEnd = this.calculateBeforeMidnight(finishDateOn)
  }

  override fun executedActivityAfter(executedActivityAfter: Date?) = this.apply { this.executedActivityAfter = requireNotNull(executedActivityAfter) }

  override fun executedActivityBefore(executedActivityBefore: Date?) = this.apply { this.executedActivityBefore = requireNotNull(executedActivityBefore) }

  override fun executedActivityIdIn(vararg executedActivityIdIn: String) = this.apply { this.executedActivityIds = executedActivityIdIn }

  override fun activeActivityIdIn(vararg activeActivityIdIn: String) = this.apply { this.activeActivityIds = activeActivityIdIn }

  override fun executedJobAfter(executedJobAfter: Date?) = this.apply { this.executedJobAfter = requireNotNull(executedJobAfter) }

  override fun executedJobBefore(executedJobBefore: Date?) = this.apply { this.executedJobBefore = requireNotNull(executedJobBefore) }

  override fun active() = this.apply { this.state = HistoricProcessInstance.STATE_ACTIVE }

  override fun suspended() = this.apply { this.state = HistoricProcessInstance.STATE_SUSPENDED }

  override fun completed() = this.apply { this.state = HistoricProcessInstance.STATE_COMPLETED }

  override fun externallyTerminated() = this.apply { this.state = HistoricProcessInstance.STATE_EXTERNALLY_TERMINATED }

  override fun internallyTerminated() = this.apply { this.state = HistoricProcessInstance.STATE_INTERNALLY_TERMINATED }

  override fun or() = this.apply {this.isOrQueryActive = true }

  override fun endOr() = this

  override fun listPage(firstResult: Int, maxResults: Int): List<HistoricProcessInstance> =
    historicProcessInstanceApiClient.queryHistoricProcessInstances(firstResult, maxResults, fillQueryDto()).body!!.map {
      HistoricProcessInstanceAdapter(HistoricInstanceBean.fromHistoricProcessInstanceDto(it))
    }

  override fun count() = historicProcessInstanceApiClient.queryHistoricProcessInstancesCount(fillQueryDto()).body!!.count

  fun fillQueryDto() = HistoricProcessInstanceQueryDto().apply {
    validate()
    val dtoPropertiesByName = HistoricProcessInstanceQueryDto::class.memberProperties.filterIsInstance<KMutableProperty1<HistoricProcessInstanceQueryDto, Any?>>().associateBy { it.name }
    dtoPropertiesByName.forEach {
      val valueToSet = when (it.key) {
        "processInstanceIds" -> this@DelegatingHistoricProcessInstanceQuery.processInstanceIds?.toList()
        "processDefinitionKeyIn" -> this@DelegatingHistoricProcessInstanceQuery.processDefinitionKeys?.toList()
        "processDefinitionKeyNotIn" -> this@DelegatingHistoricProcessInstanceQuery.processKeyNotIn?.toList()
        "processInstanceBusinessKey" -> this@DelegatingHistoricProcessInstanceQuery.businessKey
        "processInstanceBusinessKeyIn" -> this@DelegatingHistoricProcessInstanceQuery.businessKeyIn?.toList()
        "processInstanceBusinessKeyLike" -> this@DelegatingHistoricProcessInstanceQuery.businessKeyLike
        "rootProcessInstances" -> this@DelegatingHistoricProcessInstanceQuery.isRootProcessInstances
        "incidentStatus" -> this@DelegatingHistoricProcessInstanceQuery.incidentStatus?.let { HistoricProcessInstanceQueryDto.IncidentStatusEnum.fromValue(it) }
        "tenantIdIn" -> this@DelegatingHistoricProcessInstanceQuery.tenantIds?.toList()
        "withoutTenantId" -> this@DelegatingHistoricProcessInstanceQuery.tenantIdsSet && this@DelegatingHistoricProcessInstanceQuery.tenantIds == null
        "executedActivityIdIn" -> this@DelegatingHistoricProcessInstanceQuery.executedActivityIds?.toList()
        "activeActivityIdIn" -> this@DelegatingHistoricProcessInstanceQuery.activeActivityIds?.toList()
        "active" -> this@DelegatingHistoricProcessInstanceQuery.state == HistoricProcessInstance.STATE_ACTIVE
        "suspended" -> this@DelegatingHistoricProcessInstanceQuery.state == HistoricProcessInstance.STATE_SUSPENDED
        "completed" -> this@DelegatingHistoricProcessInstanceQuery.state == HistoricProcessInstance.STATE_COMPLETED
        "externallyTerminated" -> this@DelegatingHistoricProcessInstanceQuery.state == HistoricProcessInstance.STATE_EXTERNALLY_TERMINATED
        "internallyTerminated" -> this@DelegatingHistoricProcessInstanceQuery.state == HistoricProcessInstance.STATE_INTERNALLY_TERMINATED
        "variables" -> this@DelegatingHistoricProcessInstanceQuery.queryVariableValues.toDto()
        "orQueries" -> if (this@DelegatingHistoricProcessInstanceQuery.isOrQueryActive) throw UnsupportedOperationException("or-Queries are not supported") else null
        "sorting" -> this@DelegatingHistoricProcessInstanceQuery.orderingProperties.map { it.toHistoricProcessInstanceSorting() }.filter { it.sortBy != null }
        else -> valueForProperty(it.key, this@DelegatingHistoricProcessInstanceQuery, it.value.returnType)
      }
      it.value.isAccessible = true
      it.value.set(this, valueToSet)
    }
  }

  @Deprecated("")
  private fun calculateBeforeMidnight(date: Date): Date {
    val cal = Calendar.getInstance()
    cal.setTime(date)
    cal.add(Calendar.DAY_OF_MONTH, 1)
    cal.add(Calendar.SECOND, -1)
    return cal.time
  }

  @Deprecated("")
  private fun calculateMidnight(date: Date): Date {
    val cal = Calendar.getInstance()
    cal.setTime(date)
    cal[Calendar.MILLISECOND] = 0
    cal[Calendar.SECOND] = 0
    cal[Calendar.MINUTE] = 0
    cal[Calendar.HOUR] = 0
    return cal.time
  }

}

