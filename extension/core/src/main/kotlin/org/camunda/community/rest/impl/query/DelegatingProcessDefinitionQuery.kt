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
import org.camunda.bpm.engine.impl.Direction
import org.camunda.bpm.engine.impl.ProcessDefinitionQueryImpl
import org.camunda.bpm.engine.impl.ProcessDefinitionQueryProperty
import org.camunda.bpm.engine.impl.persistence.entity.SuspensionState
import org.camunda.bpm.engine.repository.ProcessDefinition
import org.camunda.bpm.engine.repository.ProcessDefinitionQuery
import org.camunda.community.rest.adapter.ProcessDefinitionAdapter
import org.camunda.community.rest.adapter.ProcessDefinitionBean
import org.camunda.community.rest.client.api.ProcessDefinitionApiClient
import org.springframework.web.bind.annotation.RequestParam
import kotlin.reflect.KParameter
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.jvm.isAccessible

/**
 * Implementation of the process definition query.
 */
class DelegatingProcessDefinitionQuery(
  private val processDefinitionApiClient: ProcessDefinitionApiClient
) : BaseQuery<ProcessDefinitionQuery, ProcessDefinition>(), ProcessDefinitionQuery {

  companion object : KLogging()

  override fun listPage(firstResult: Int, maxResults: Int): List<ProcessDefinition> {
    checkQueryOk()
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
    checkQueryOk()
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

  private fun getQueryParam(parameter: KParameter): Any? {
    val value = parameter.annotations.find { it is RequestParam }?.let { (it as RequestParam).value }
    val propertiesByName = ProcessDefinitionQueryImpl::class.declaredMemberProperties.associateBy { it.name }
    if (this.orderingProperties.size > 1) logger.warn { "sorting with more than one property not supported, ignoring all but first" }
    val sortProperty = this.orderingProperties.firstOrNull()
    return when(value) {
      "processDefinitionId" -> this.id
      "processDefinitionIdIn" -> this.ids?.joinToString(",")
      "keysIn" -> this.keys?.joinToString(",")
      "latestVersion" -> this.latest
      "startableBy" -> this.authorizationUserId
      "active" -> this.suspensionState?.let { it == SuspensionState.ACTIVE }
      "suspended" -> this.suspensionState?.let { it == SuspensionState.SUSPENDED }
      "tenantIdIn" -> this.tenantIds?.joinToString(",")
      "withoutTenantId" -> this.isTenantIdSet && (this.tenantIds == null)
      "withoutVersionTag" -> this.isVersionTagSet && (this.versionTag == null)
      "includeProcessDefinitionsWithoutTenantId" -> includeDefinitionsWithoutTenantId
      "notStartableInTasklist" -> isNotStartableInTasklist
      "startableInTasklist" -> isStartableInTasklist
      "sortBy" -> when (sortProperty?.queryProperty) {
        ProcessDefinitionQueryProperty.PROCESS_DEFINITION_ID -> "id"
        ProcessDefinitionQueryProperty.PROCESS_DEFINITION_KEY -> "key"
        ProcessDefinitionQueryProperty.PROCESS_DEFINITION_CATEGORY -> "category"
        ProcessDefinitionQueryProperty.PROCESS_DEFINITION_NAME -> "name"
        ProcessDefinitionQueryProperty.PROCESS_DEFINITION_VERSION -> "version"
        ProcessDefinitionQueryProperty.DEPLOYMENT_ID -> "deploymentId"
        ProcessDefinitionQueryProperty.DEPLOY_TIME -> "deployTime"
        ProcessDefinitionQueryProperty.TENANT_ID -> "tenantId"
        ProcessDefinitionQueryProperty.VERSION_TAG -> "versionTag"
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
