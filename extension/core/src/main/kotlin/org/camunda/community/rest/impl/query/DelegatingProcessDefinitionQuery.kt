/*-
 * #%L
 * camunda-platform-7-rest-client-spring-boot
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
package org.camunda.community.rest.impl.query

import mu.KLogging
import org.camunda.bpm.engine.ProcessEngineException
import org.camunda.bpm.engine.impl.ProcessDefinitionQueryImpl
import org.camunda.bpm.engine.repository.ProcessDefinition
import org.camunda.bpm.engine.repository.ProcessDefinitionQuery
import org.camunda.community.rest.adapter.ProcessDefinitionAdapter
import org.camunda.community.rest.adapter.ProcessDefinitionBean
import org.camunda.community.rest.client.api.ProcessDefinitionApiClient
import org.springframework.web.bind.annotation.RequestParam
import java.util.*
import kotlin.reflect.KParameter

/**
 * Implementation of the process definition query.
 */
class DelegatingProcessDefinitionQuery(
  private val processDefinitionApiClient: ProcessDefinitionApiClient,
  var id: String? = null,
  var ids: Array<out String>? = null,
  var category: String? = null,
  var categoryLike: String? = null,
  var name: String? = null,
  var nameLike: String? = null,
  var deploymentId: String? = null,
  var deployedAfter: Date? = null,
  var deployedAt: Date? = null,
  var key: String? = null,
  var keys: Array<out String>? = null,
  var keyLike: String? = null,
  var resourceName: String? = null,
  var resourceNameLike: String? = null,
  var version: Int? = null,
  var latest: Boolean = false,
  var suspensionState: SuspensionState? = null,
  var authorizationUserId: String? = null,
  val procDefId: MutableList<String> = mutableListOf(),
  var incidentType: String? = null,
  var incidentId: String? = null,
  var incidentMessage: String? = null,
  var incidentMessageLike: String? = null,
  var eventSubscriptionName: String? = null,
  var eventSubscriptionType: String? = null,
  var includeDefinitionsWithoutTenantId: Boolean = false,
  var isVersionTagSet: Boolean = false,
  var versionTag: String? = null,
  var versionTagLike: String? = null,
  var isStartableInTasklist: Boolean = false,
  var isNotStartableInTasklist: Boolean = false,
  var startablePermissionCheck: Boolean = false
) : BaseQuery<ProcessDefinitionQuery, ProcessDefinition>(), ProcessDefinitionQuery {

  companion object : KLogging()

  override fun processDefinitionId(processDefinitionId: String?) = this.apply { this.id = requireNotNull(processDefinitionId) }

  override fun processDefinitionIdIn(vararg processDefinitionIdIn: String) = this.apply { this.ids = processDefinitionIdIn }

  override fun processDefinitionCategory(processDefinitionCategory: String?) = this.apply { this.category = requireNotNull(processDefinitionCategory) }

  override fun processDefinitionCategoryLike(processDefinitionCategoryLike: String?) = this.apply { this.categoryLike = requireNotNull(processDefinitionCategoryLike) }

  override fun processDefinitionName(processDefinitionName: String?) = this.apply { this.name = requireNotNull(processDefinitionName) }

  override fun processDefinitionNameLike(processDefinitionNameLike: String?) = this.apply { this.nameLike = requireNotNull(processDefinitionNameLike) }

  override fun deploymentId(deploymentId: String?) = this.apply { this.deploymentId = requireNotNull(deploymentId) }

  override fun deployedAfter(deployedAfter: Date?) = this.apply { this.deployedAfter = requireNotNull(deployedAfter) }

  override fun deployedAt(deployedAt: Date?) = this.apply { this.deployedAt = requireNotNull(deployedAt) }

  override fun processDefinitionKey(processDefinitionKey: String?) = this.apply { this.key = requireNotNull(processDefinitionKey) }

  @Deprecated("Deprecated in Java")
  override fun processDefinitionKeysIn(vararg processDefinitionKeysIn: String): ProcessDefinitionQueryImpl {
    this.keys = processDefinitionKeysIn
    return ProcessDefinitionQueryImpl()
  }

  /**
   * @since 7.21
   */
  override fun processDefinitionKeyIn(vararg processDefinitionKeys: String): ProcessDefinitionQuery = this.apply { this.keys = processDefinitionKeys }

  override fun processDefinitionKeyLike(processDefinitionKeyLike: String?) = this.apply { this.keyLike = requireNotNull(processDefinitionKeyLike) }

  override fun processDefinitionVersion(processDefinitionVersion: Int?) = this.apply { this.version = requireNotNull(processDefinitionVersion) }

  override fun latestVersion() = this.apply { this.latest = true }

  override fun processDefinitionResourceName(processDefinitionResourceName: String?) = this.apply { this.resourceName = requireNotNull(processDefinitionResourceName) }

  override fun processDefinitionResourceNameLike(processDefinitionResourceNameLike: String?) = this.apply { this.resourceNameLike = requireNotNull(processDefinitionResourceNameLike) }

  override fun startableByUser(startableByUser: String?) = this.apply { this.authorizationUserId = requireNotNull(startableByUser) }

  override fun suspended() = this.apply { this.suspensionState = SuspensionState.SUSPENDED }

  override fun active() = this.apply { this.suspensionState = SuspensionState.ACTIVE }

  override fun incidentType(incidentType: String?) = this.apply { this.incidentType = requireNotNull(incidentType) }

  override fun incidentId(incidentId: String?) = this.apply { this.incidentId = requireNotNull(incidentId) }

  override fun incidentMessage(incidentMessage: String?) = this.apply { this.incidentMessage = requireNotNull(incidentMessage) }

  override fun incidentMessageLike(incidentMessageLike: String?) = this.apply { this.incidentMessageLike = requireNotNull(incidentMessageLike) }

  override fun versionTag(versionTag: String?) = this.apply {
    this.isVersionTagSet = true
    this.versionTag = requireNotNull(versionTag)
  }

  override fun versionTagLike(versionTagLike: String?) = this.apply { this.versionTagLike = requireNotNull(versionTagLike) }

  override fun withoutVersionTag() = this.apply {
    this.isVersionTagSet = true
    this.versionTag = null
  }

  @Deprecated("Deprecated in Java")
  override fun messageEventSubscription(name: String?) = this.apply { this.messageEventSubscriptionName(name) }

  override fun messageEventSubscriptionName(name: String?) = this.apply {
    this.eventSubscriptionName = name
    this.eventSubscriptionType = "message"
  }

  override fun includeProcessDefinitionsWithoutTenantId() = this.apply { this.includeDefinitionsWithoutTenantId = true }

  override fun startableInTasklist() = this.apply { this.isStartableInTasklist = true }

  override fun notStartableInTasklist() = this.apply { this.isNotStartableInTasklist = true }

  override fun startablePermissionCheck() = this.apply { this.startablePermissionCheck = true }

  override fun orderByProcessDefinitionCategory() = this.apply { orderBy("category") }

  override fun orderByProcessDefinitionKey() = this.apply { orderBy("key") }

  override fun orderByProcessDefinitionId() = this.apply { orderBy("id") }

  override fun orderByProcessDefinitionVersion() = this.apply { orderBy("version") }

  override fun orderByProcessDefinitionName() = this.apply { orderBy("name") }

  override fun orderByDeploymentId() = this.apply { orderBy("deploymentId") }

  override fun orderByDeploymentTime() = this.apply { orderBy("deploymentTime") }

  override fun orderByVersionTag() = this.apply { orderBy("versionTag") }

  override fun listPage(firstResult: Int, maxResults: Int): List<ProcessDefinition> {
    validate()
    with(ProcessDefinitionApiClient::getProcessDefinitions) {
      val result = callBy(parameters.associateWith { parameter ->
        when (parameter.kind) {
          KParameter.Kind.INSTANCE -> processDefinitionApiClient
          else -> {
            when (parameter.annotations.find { it is RequestParam }?.let { (it as RequestParam).value }) {
              "firstResult" -> firstResult
              "maxResults" -> maxResults
              else -> this@DelegatingProcessDefinitionQuery.getQueryParam(parameter)
            }
          }
        }
      })
      return result.body!!.map {
        ProcessDefinitionAdapter(ProcessDefinitionBean.fromDto(it))
      }
    }
  }

  override fun count(): Long {
    validate()
    with (ProcessDefinitionApiClient::getProcessDefinitionsCount) {
      val result = callBy(parameters.associateWith { parameter ->
        when (parameter.kind) {
          KParameter.Kind.INSTANCE -> processDefinitionApiClient
          else -> this@DelegatingProcessDefinitionQuery.getQueryParam(parameter)
        }
      })
      return result.body!!.count
    }
  }

  override fun validate() {
    super.validate()
    if (latest && (id != null || version != null || deploymentId != null)) {
      throw ProcessEngineException("Calling latest() can only be used in combination with key(String) and keyLike(String) or name(String) and nameLike(String)")
    }
  }

  private fun getQueryParam(parameter: KParameter): Any? {
    val value = parameter.annotations.find { it is RequestParam }?.let { (it as RequestParam).value }
    return when(value) {
      "processDefinitionId" -> this.id
      "processDefinitionIdIn" -> this.ids?.joinToString(",")
      "keysIn" -> this.keys?.joinToString(",")
      "latestVersion" -> this.latest
      "startableBy" -> this.authorizationUserId
      "active" -> this.suspensionState?.let { it == SuspensionState.ACTIVE }
      "suspended" -> this.suspensionState?.let { it == SuspensionState.SUSPENDED }
      "tenantIdIn" -> this.tenantIds?.joinToString(",")
      "withoutTenantId" -> this.tenantIdsSet && (this.tenantIds == null)
      "withoutVersionTag" -> this.isVersionTagSet && (this.versionTag == null)
      "includeProcessDefinitionsWithoutTenantId" -> includeDefinitionsWithoutTenantId
      "notStartableInTasklist" -> isNotStartableInTasklist
      "startableInTasklist" -> isStartableInTasklist
      "sortBy" -> sortProperty()?.property
      "sortOrder" -> sortProperty()?.direction?.let { if (it == SortDirection.DESC) "desc" else "asc" }
      null -> throw IllegalArgumentException("value of RequestParam annotation is null")
      else -> valueForProperty(value, this, parameter.type)
    }
  }

}
