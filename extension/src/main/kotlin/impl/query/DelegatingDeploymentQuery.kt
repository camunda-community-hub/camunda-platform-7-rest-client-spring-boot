package org.camunda.bpm.extension.rest.impl.query

import mu.KLogging
import org.camunda.bpm.engine.ProcessEngineException
import org.camunda.bpm.engine.impl.DeploymentQueryImpl
import org.camunda.bpm.engine.impl.DeploymentQueryProperty
import org.camunda.bpm.engine.impl.Direction
import org.camunda.bpm.engine.repository.Deployment
import org.camunda.bpm.extension.rest.adapter.DeploymentAdapter
import org.camunda.bpm.extension.rest.adapter.DeploymentBean
import org.camunda.bpm.extension.rest.client.api.DeploymentApiClient
import org.springframework.web.bind.annotation.RequestParam
import kotlin.reflect.KParameter
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.jvm.isAccessible

class DelegatingDeploymentQuery(
  private val deploymentApiClient: DeploymentApiClient
) : DeploymentQueryImpl() {

  companion object : KLogging()

  override fun list(): List<Deployment> = listPage(this.firstResult, this.maxResults)

  override fun listPage(firstResult: Int, maxResults: Int): List<Deployment> {
    with(DeploymentApiClient::getDeployments) {
      val result = callBy(parameters.associateWith { parameter ->
        when (parameter.kind) {
          KParameter.Kind.INSTANCE -> deploymentApiClient
          else -> {
            when (parameter.annotations.find { it is RequestParam }?.let { (it as RequestParam).value }) {
              "firstResult" -> firstResult
              "maxResults" -> maxResults
              else -> this@DelegatingDeploymentQuery.getQueryParam(parameter)
            }
          }
        }
      })
      return result.body!!.map {
        DeploymentAdapter(DeploymentBean.fromDto(it))
      }
    }
  }

  override fun listIds(): List<String> {
    return list().map { it.id }
  }

  override fun unlimitedList(): List<Deployment> {
    // FIXME: best approximation so far.
    return list()
  }

  override fun count(): Long {
    with (DeploymentApiClient::getDeploymentsCount) {
      val result = callBy(parameters.associateWith { parameter ->
        when (parameter.kind) {
          KParameter.Kind.INSTANCE -> deploymentApiClient
          else -> this@DelegatingDeploymentQuery.getQueryParam(parameter)
        }
      })
      return result.body!!.count
    }
  }

  override fun singleResult(): Deployment? {
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
    val propertiesByName = DeploymentQueryImpl::class.declaredMemberProperties.associateBy { it.name }
    if (this.orderingProperties.size > 1) logger.warn { "sorting with more than one property not supported, ignoring all but first" }
    val sortProperty = this.orderingProperties.firstOrNull()
    return when(value) {
      "id" -> deploymentId
      "withoutSource" -> sourceQueryParamEnabled && source == null
      "tenantIdIn" -> tenantIds?.toList()
      "withoutTenantId" -> isTenantIdSet && tenantIds == null
      "after" -> deploymentAfter
      "before" -> deploymentBefore
      "sortBy" -> when (sortProperty?.queryProperty) {
        DeploymentQueryProperty.DEPLOYMENT_ID -> "id"
        DeploymentQueryProperty.DEPLOYMENT_NAME -> "name"
        DeploymentQueryProperty.DEPLOY_TIME -> "deploymentTime"
        DeploymentQueryProperty.TENANT_ID -> "tenantId"
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
