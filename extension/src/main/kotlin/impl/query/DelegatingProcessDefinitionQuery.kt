/*-
 * #%L
 * camunda-rest-client-spring-boot
 * %%
 * Copyright (C) 2019 Camunda Services GmbH
 * %%
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 *  under one or more contributor license agreements. See the NOTICE file
 *  distributed with this work for additional information regarding copyright
 *  ownership. Camunda licenses this file to you under the Apache License,
 *  Version 2.0; you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * #L%
 */
package org.camunda.bpm.extension.rest.impl.query

import org.camunda.bpm.engine.ProcessEngineException
import org.camunda.bpm.engine.impl.ProcessDefinitionQueryImpl
import org.camunda.bpm.engine.impl.persistence.entity.SuspensionState
import org.camunda.bpm.engine.repository.ProcessDefinition
import org.camunda.bpm.engine.rest.dto.repository.ProcessDefinitionQueryDto
import org.camunda.bpm.extension.rest.adapter.ProcessDefinitionAdapter
import org.camunda.bpm.extension.rest.adapter.TaskAdapter
import org.camunda.bpm.extension.rest.adapter.ProcessDefinitionBean
import org.camunda.bpm.extension.rest.client.RepositoryServiceClient

/**
 * Implementation of the process definition query.
 */
class DelegatingProcessDefinitionQuery(
  private val repositoryServiceClient: RepositoryServiceClient
) : ProcessDefinitionQueryImpl() {

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
    val count = repositoryServiceClient.countProcessDefinitions(fillQueryDto(), this.firstResult, this.maxResults)
    return count.count
  }

  override fun singleResult(): ProcessDefinition? {
    val results = list()
    return when {
      results.size == 1 -> results[0]
      results.size > 1 -> throw ProcessEngineException("Query return " + results.size.toString() + " results instead of expected maximum 1")
      else -> null
    }
  }

  /**
   * Fill the DTO from the builder.
   */
  private fun fillQueryDto(): ProcessDefinitionQueryDto {
    val queryDto = ProcessDefinitionQueryDto()
    queryDto.setIncludeProcessDefinitionsWithoutTenantId(this.includeDefinitionsWithoutTenantId)
    queryDto.setActive(this.suspensionState == SuspensionState.ACTIVE || this.suspensionState == null)
    queryDto.setSuspended(this.suspensionState == SuspensionState.SUSPENDED)
    queryDto.setCategory(this.category)
    queryDto.setCategoryLike(this.categoryLike)
    queryDto.setDeploymentId(this.deploymentId)
    queryDto.setIncidentId(this.incidentId)
    queryDto.setIncidentMessage(this.incidentMessage)
    queryDto.setIncidentMessageLike(this.incidentMessageLike)
    queryDto.setIncidentType(this.incidentType)
    queryDto.setKey(this.key)
    queryDto.setKeyLike(this.keyLike)
    if (this.keys != null) { // TODO: check
      queryDto.setKeysIn(this.keys.toList())
    }
    queryDto.setLatestVersion(this.latest)
    queryDto.setName(this.name)
    queryDto.setNameLike(this.nameLike)
    queryDto.setNotStartableInTasklist(this.isNotStartableInTasklist)
    queryDto.setProcessDefinitionId(this.id)
    if (this.ids != null) { // TODO: check
      queryDto.setProcessDefinitionIdIn(this.ids.toList())
    }
    queryDto.setResourceName(this.resourceName)
    queryDto.setResourceNameLike(this.resourceNameLike)
    queryDto.setStartableBy(this.authorizationUserId)
    queryDto.setStartablePermissionCheck(this.startablePermissionCheck)
    if (this.isTenantIdSet) { // TODO: check
      if (this.tenantIds != null) {
        queryDto.setTenantIdIn(this.tenantIds.toList())
      } else {
        queryDto.setWithoutTenantId(true)
      }
    }
    queryDto.setVersion(this.version)
    queryDto.setVersionTag(this.versionTag)
    queryDto.setVersionTagLike(this.versionTagLike)

    return queryDto
  }
}
