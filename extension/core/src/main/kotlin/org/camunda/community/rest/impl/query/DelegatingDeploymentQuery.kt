package org.camunda.community.rest.impl.query

import mu.KLogging
import org.camunda.bpm.engine.repository.Deployment
import org.camunda.bpm.engine.repository.DeploymentQuery
import org.camunda.community.rest.adapter.DeploymentAdapter
import org.camunda.community.rest.adapter.DeploymentBean
import org.camunda.community.rest.client.api.DeploymentApiClient
import org.camunda.community.rest.impl.toOffsetDateTime
import org.springframework.web.bind.annotation.RequestParam
import java.util.*
import kotlin.reflect.KParameter

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
    validate()
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
    validate()
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
    return when(value) {
      "id" -> deploymentId
      "withoutSource" -> sourceQueryParamEnabled && source == null
      "tenantIdIn" -> tenantIds?.joinToString(",")
      "withoutTenantId" -> tenantIdsSet && tenantIds == null
      "after" -> deploymentAfter.toOffsetDateTime()
      "before" -> deploymentBefore.toOffsetDateTime()
      "sortBy" -> sortProperty()?.property
      "sortOrder" -> sortProperty()?.direction?.let { if (it == SortDirection.DESC) "desc" else "asc" }
      null -> throw IllegalArgumentException("value of RequestParam annotation is null")
      else -> valueForProperty(value, this, parameter.type)
    }
  }

}
