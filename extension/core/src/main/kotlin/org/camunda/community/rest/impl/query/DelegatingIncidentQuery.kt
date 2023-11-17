package org.camunda.community.rest.impl.query

import mu.KLogging
import org.camunda.bpm.engine.ProcessEngineException
import org.camunda.bpm.engine.impl.Direction
import org.camunda.bpm.engine.impl.IncidentQueryImpl
import org.camunda.bpm.engine.impl.IncidentQueryProperty
import org.camunda.bpm.engine.runtime.Incident
import org.camunda.bpm.engine.runtime.IncidentQuery
import org.camunda.community.rest.adapter.*
import org.camunda.community.rest.client.api.IncidentApiClient
import org.springframework.web.bind.annotation.RequestParam
import kotlin.reflect.KParameter
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.jvm.isAccessible

class DelegatingIncidentQuery(
  private val incidentApiClient: IncidentApiClient
) : BaseQuery<IncidentQuery, Incident>(), IncidentQuery {

  companion object : KLogging()

  override fun listPage(firstResult: Int, maxResults: Int): List<Incident> {
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
    checkQueryOk()
    val value = parameter.annotations.find { it is RequestParam }?.let { (it as RequestParam).value }
    val propertiesByName = IncidentQueryImpl::class.declaredMemberProperties.associateBy { it.name }
    if (this.orderingProperties.size > 1) logger.warn { "sorting with more than one property not supported, ignoring all but first" }
    val sortProperty = this.orderingProperties.firstOrNull()
    return when(value) {
      "incidentId" -> this@DelegatingIncidentQuery.id
      "processDefinitionKeyIn" -> this@DelegatingIncidentQuery.processDefinitionKeys?.joinToString(",")
      "tenantIdIn" -> this@DelegatingIncidentQuery.tenantIds?.joinToString(",")
      "jobDefinitionIdIn" -> this@DelegatingIncidentQuery.jobDefinitionIds?.joinToString(",")
      "sortBy" -> when (sortProperty?.queryProperty) {
        IncidentQueryProperty.INCIDENT_ID -> "incidentId"
        IncidentQueryProperty.INCIDENT_MESSAGE -> "incidentMessage"
        IncidentQueryProperty.INCIDENT_TIMESTAMP -> "incidentTimestamp"
        IncidentQueryProperty.INCIDENT_TYPE -> "incidentType"
        IncidentQueryProperty.EXECUTION_ID -> "executionId"
        IncidentQueryProperty.ACTIVITY_ID -> "activityId"
        IncidentQueryProperty.PROCESS_INSTANCE_ID -> "processInstanceId"
        IncidentQueryProperty.PROCESS_DEFINITION_ID -> "processDefinitionId"
        IncidentQueryProperty.CAUSE_INCIDENT_ID -> "causeIncidentId"
        IncidentQueryProperty.ROOT_CAUSE_INCIDENT_ID -> "rootCauseIncidentId"
        IncidentQueryProperty.CONFIGURATION -> "configuration"
        IncidentQueryProperty.TENANT_ID -> "tenantId"
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
