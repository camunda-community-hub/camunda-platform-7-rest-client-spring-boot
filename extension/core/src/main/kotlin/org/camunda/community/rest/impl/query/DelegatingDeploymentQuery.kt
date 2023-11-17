package org.camunda.community.rest.impl.query

import mu.KLogging
import org.camunda.bpm.engine.ProcessEngineException
import org.camunda.bpm.engine.repository.Deployment
import org.camunda.bpm.engine.repository.DeploymentQuery
import org.camunda.community.rest.adapter.DeploymentAdapter
import org.camunda.community.rest.adapter.DeploymentBean
import org.camunda.community.rest.client.api.DeploymentApiClient
import org.springframework.web.bind.annotation.RequestParam
import java.util.*
import kotlin.reflect.KParameter
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

class DelegatingDeploymentQuery(
  private val deploymentApiClient: DeploymentApiClient,
  var deploymentId: String? = null,
  var name: String? = null,
  var nameLike: String? = null,
  var sourceQueryParamEnabled: Boolean = false,
  var source: String? = null,
  var deploymentBefore: Date? = null,
  var deploymentAfter: Date? = null,
  var includeDeploymentsWithoutTenantId: Boolean = false
) : BaseQuery<DeploymentQuery, Deployment>(), DeploymentQuery {

  companion object : KLogging()

  override fun deploymentId(deploymentId: String?) = this.apply { this.deploymentId = requireNotNull(deploymentId) }

  override fun deploymentName(deploymentName: String?) = this.apply { this.name = requireNotNull(deploymentName) }

  override fun deploymentNameLike(deploymentNameLike: String?) = this.apply { this.nameLike = requireNotNull(deploymentNameLike) }

  override fun deploymentSource(source: String?) = this.apply {
    this.source = source
    this.sourceQueryParamEnabled = true
  }

  override fun deploymentBefore(before: Date?) = this.apply { this.deploymentBefore = requireNotNull(before) }

  override fun deploymentAfter(after: Date?) = this.apply { this.deploymentAfter = requireNotNull(after) }

  override fun includeDeploymentsWithoutTenantId() = this.apply { this.includeDeploymentsWithoutTenantId = true }

  override fun orderByDeploymentId() = this.apply { orderBy("id") }

  override fun orderByDeploymentName() = this.apply { orderBy("name") }

  @Deprecated("Deprecated in Java", replaceWith = ReplaceWith("orderByDeploymentTime()"))
  override fun orderByDeploymenTime() = orderByDeploymentTime()

  override fun orderByDeploymentTime() = this.apply { orderBy("deploymentTime") }

  override fun listPage(firstResult: Int, maxResults: Int): List<Deployment> {
    checkQueryOk()
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

  override fun count(): Long {
    checkQueryOk()
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

  private fun getQueryParam(parameter: KParameter): Any? {
    val value = parameter.annotations.find { it is RequestParam }?.let { (it as RequestParam).value }
    val propertiesByName = DelegatingDeploymentQuery::class.declaredMemberProperties.associateBy { it.name }
    if (this.orderingProperties.size > 1) logger.warn { "sorting with more than one property not supported, ignoring all but first" }
    val sortProperty = this.orderingProperties.firstOrNull()
    return when(value) {
      "id" -> deploymentId
      "withoutSource" -> sourceQueryParamEnabled && source == null
      "tenantIdIn" -> tenantIds?.toList()
      "withoutTenantId" -> tenantIdsSet && tenantIds == null
      "after" -> deploymentAfter
      "before" -> deploymentBefore
      "sortBy" -> sortProperty?.property
      "sortOrder" -> sortProperty?.direction?.let { if (it == SortDirection.DESC) "desc" else "asc" }
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
