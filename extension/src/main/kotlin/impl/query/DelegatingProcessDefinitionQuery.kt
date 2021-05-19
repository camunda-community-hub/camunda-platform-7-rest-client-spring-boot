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
import org.camunda.bpm.extension.rest.adapter.ProcessDefinitionBean
import org.camunda.bpm.extension.rest.client.RepositoryServiceClient

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
      results.size == 1 -> results[0]
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
    query.setActive(this.suspensionState == SuspensionState.ACTIVE || this.suspensionState == null)
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
