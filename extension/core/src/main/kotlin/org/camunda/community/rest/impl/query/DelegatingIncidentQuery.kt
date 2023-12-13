package org.camunda.community.rest.impl.query

import mu.KLogging
import org.camunda.bpm.engine.runtime.Incident
import org.camunda.bpm.engine.runtime.IncidentQuery
import org.camunda.community.rest.adapter.IncidentAdapter
import org.camunda.community.rest.adapter.IncidentBean
import org.camunda.community.rest.client.api.IncidentApiClient
import org.springframework.web.bind.annotation.RequestParam
import java.util.*
import kotlin.reflect.KParameter
import kotlin.reflect.full.declaredMemberProperties

class DelegatingIncidentQuery(
  private val incidentApiClient: IncidentApiClient,
  var id: String? = null,
  var incidentType: String? = null,
  var incidentMessage: String? = null,
  var incidentMessageLike: String? = null,
  var executionId: String? = null,
  var incidentTimestampBefore: Date? = null,
  var incidentTimestampAfter: Date? = null,
  var activityId: String? = null,
  var failedActivityId: String? = null,
  var processInstanceId: String? = null,
  var processDefinitionId: String? = null,
  var processDefinitionKeys: Array<out String>? = null,
  var causeIncidentId: String? = null,
  var rootCauseIncidentId: String? = null,
  var configuration: String? = null,
  var jobDefinitionIds: Array<out String>? = null
) : BaseQuery<IncidentQuery, Incident>(), IncidentQuery {

  companion object : KLogging()

  override fun incidentId(incidentId: String?) = this.apply { this.id = requireNotNull(incidentId) }

  override fun incidentType(incidentType: String?) = this.apply { this.incidentType = requireNotNull(incidentType) }

  override fun incidentMessage(incidentMessage: String?) = this.apply { this.incidentMessage = requireNotNull(incidentMessage) }

  override fun incidentMessageLike(incidentMessageLike: String?) = this.apply { this.incidentMessageLike = requireNotNull(incidentMessageLike) }

  override fun processDefinitionId(processDefinitionId: String?) = this.apply { this.processDefinitionId = requireNotNull(processDefinitionId) }

  override fun processDefinitionKeyIn(vararg processDefinitionKeyIn: String) = this.apply { this.processDefinitionKeys = processDefinitionKeyIn }

  override fun processInstanceId(processInstanceId: String?) = this.apply { this.processInstanceId = requireNotNull(processInstanceId) }

  override fun executionId(executionId: String?) = this.apply { this.executionId = executionId }

  override fun incidentTimestampBefore(incidentTimestampBefore: Date?) = this.apply { this.incidentTimestampBefore = incidentTimestampBefore }

  override fun incidentTimestampAfter(incidentTimestampAfter: Date?) = this.apply { this.incidentTimestampAfter = incidentTimestampAfter }

  override fun activityId(activityId: String?) = this.apply { this.activityId = activityId }

  override fun failedActivityId(failedActivityId: String?) = this.apply { this.failedActivityId = failedActivityId }

  override fun causeIncidentId(causeIncidentId: String?) = this.apply { this.causeIncidentId = causeIncidentId }

  override fun rootCauseIncidentId(rootCauseIncidentId: String?) = this.apply { this.rootCauseIncidentId = rootCauseIncidentId }

  override fun configuration(configuration: String?) = this.apply { this.configuration = configuration }

  override fun jobDefinitionIdIn(vararg jobDefinitionIdIn: String) = this.apply { this.jobDefinitionIds = jobDefinitionIdIn }

  override fun orderByIncidentId() = this.apply { orderBy("incidentId") }

  override fun orderByIncidentTimestamp() = this.apply { orderBy("incidentTimestamp") }


  override fun orderByIncidentMessage() = this.apply { orderBy("incidentMessage") }


  override fun orderByIncidentType() = this.apply { orderBy("incidentType") }


  override fun orderByExecutionId() = this.apply { orderBy("executionId") }


  override fun orderByActivityId() = this.apply { orderBy("activityId") }


  override fun orderByProcessInstanceId() = this.apply { orderBy("processInstanceId") }


  override fun orderByProcessDefinitionId() = this.apply { orderBy("processDefinitionId") }


  override fun orderByCauseIncidentId() = this.apply { orderBy("causeIncidentId") }


  override fun orderByRootCauseIncidentId() = this.apply { orderBy("rootCauseIncidentId") }


  override fun orderByConfiguration() = this.apply { orderBy("configuration") }


  override fun listPage(firstResult: Int, maxResults: Int): List<Incident> {
    validate()
    with(IncidentApiClient::getIncidents) {
      val result = callBy(parameters.associateWith { parameter ->
        when (parameter.kind) {
          KParameter.Kind.INSTANCE -> incidentApiClient
          else -> {
            when (parameter.annotations.find { it is RequestParam }?.let { (it as RequestParam).value }) {
              "firstResult" -> firstResult
              "maxResults" -> maxResults
              else -> this@DelegatingIncidentQuery.getQueryParam(parameter)
            }
          }
        }
      })
      return result.body!!.map {
        IncidentAdapter(IncidentBean.fromDto(it))
      }
    }
  }

  override fun count(): Long {
    validate()
    with (IncidentApiClient::getIncidentsCount) {
      val result = callBy(parameters.associateWith { parameter ->
        when (parameter.kind) {
          KParameter.Kind.INSTANCE -> incidentApiClient
          else -> this@DelegatingIncidentQuery.getQueryParam(parameter)
        }
      })
      return result.body!!.count
    }
  }

  private fun getQueryParam(parameter: KParameter): Any? {
    val value = parameter.annotations.find { it is RequestParam }?.let { (it as RequestParam).value }
    return when(value) {
      "incidentId" -> this@DelegatingIncidentQuery.id
      "processDefinitionKeyIn" -> this@DelegatingIncidentQuery.processDefinitionKeys?.joinToString(",")
      "tenantIdIn" -> this@DelegatingIncidentQuery.tenantIds?.joinToString(",")
      "jobDefinitionIdIn" -> this@DelegatingIncidentQuery.jobDefinitionIds?.joinToString(",")
      "sortBy" -> sortProperty()?.property
      "sortOrder" -> sortProperty()?.direction?.let { if (it == SortDirection.DESC) "desc" else "asc" }
      null -> throw IllegalArgumentException("value of RequestParam annotation is null")
      else -> valueForProperty(value, this, parameter.type)
    }
  }

}
