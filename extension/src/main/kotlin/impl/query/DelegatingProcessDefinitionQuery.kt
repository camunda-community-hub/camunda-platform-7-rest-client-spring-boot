package org.camunda.bpm.extension.feign.impl.query

import org.camunda.bpm.engine.ProcessEngineException
import org.camunda.bpm.engine.impl.ProcessDefinitionQueryImpl
import org.camunda.bpm.engine.impl.persistence.entity.SuspensionState
import org.camunda.bpm.engine.repository.ProcessDefinition
import org.camunda.bpm.engine.rest.dto.repository.ProcessDefinitionQueryDto
import org.camunda.bpm.extension.feign.adapter.ProcessDefinitionAdapter
import org.camunda.bpm.extension.feign.adapter.ProcessDefinitionBean
import org.camunda.bpm.extension.feign.client.RepositoryServiceClient

/**
 * Implementation of the process definition query.
 */
class DelegatingProcessDefinitionQuery(val repositoryServiceClient: RepositoryServiceClient) : ProcessDefinitionQueryImpl() {

  override fun list(): List<ProcessDefinition> {
    val definitions = repositoryServiceClient.getProcessDefinitions(fillQueryDto(), this.firstResult, this.maxResults)
    return definitions.map {
      ProcessDefinitionAdapter(ProcessDefinitionBean.fromDto(it))
    }
  }

  override fun listPage(firstResult: Int, maxResults: Int): List<ProcessDefinition> {
    val definitions = repositoryServiceClient.getProcessDefinitions(fillQueryDto(), firstResult, maxResults)
    return definitions.map {
      ProcessDefinitionAdapter(ProcessDefinitionBean.fromDto(it))
    }
  }

  override fun count(): Long {
    val count = repositoryServiceClient.countProcessDefinitions(fillQueryDto(), firstResult, maxResults)
    return count.count
  }

  override fun singleResult(): ProcessDefinition? {
    val results = list()
    return when {
      results.size == 1 -> results.get(0)
      results.size > 1 -> throw ProcessEngineException("Query return " + results.size.toString() + " results instead of max 1")
      else -> null
    }
  }

  /**
   * Fill the DTO from the builder.
   */
  fun fillQueryDto(): ProcessDefinitionQueryDto {
    val query = ProcessDefinitionQueryDto()
    query.setIncludeProcessDefinitionsWithoutTenantId(this.includeDefinitionsWithoutTenantId)
    query.setActive(this.suspensionState == SuspensionState.ACTIVE)
    query.setSuspended(this.suspensionState == SuspensionState.SUSPENDED)
    query.setCategory(this.category)
    query.setCategoryLike(this.categoryLike)
    query.setDeploymentId(this.deploymentId)
    query.setIncidentId(this.incidentId)
    query.setIncidentMessage(this.incidentMessage)
    query.setIncidentMessageLike(this.incidentMessageLike)
    query.setIncidentType(this.incidentType)
    query.setKey(this.key)
    query.setKeyLike(this.keyLike)
    // FIXME: check
    // query.setKeysIn()
    query.setLatestVersion(this.latest)
    query.setName(this.name)
    query.setNameLike(this.nameLike)
    query.setNotStartableInTasklist(this.isNotStartableInTasklist)
    query.setProcessDefinitionId(this.id)
    // FIXME: check
    if (this.ids != null) {
      query.setProcessDefinitionIdIn(this.ids.toList())
    }
    query.setResourceName(this.resourceName)
    query.setResourceNameLike(this.resourceNameLike)
    query.setStartableBy(this.authorizationUserId)
    query.setStartablePermissionCheck(this.startablePermissionCheck)
    if (this.tenantIds != null) {
      query.setTenantIdIn(this.tenantIds.toList())
    }
    query.setVersion(this.version)
    query.setVersionTag(this.versionTag)
    query.setVersionTagLike(this.versionTagLike)
    // FIXME
    // query.setWithoutTenantId()
    return query
  }
}
