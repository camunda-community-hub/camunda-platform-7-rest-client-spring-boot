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

package org.camunda.community.rest.adapter

import org.camunda.bpm.engine.repository.ProcessDefinition
import org.camunda.community.rest.client.model.ProcessDefinitionDto

/**
 * Implementation of process definition delegating to a simple bean.
 */
class ProcessDefinitionAdapter(private val processDefinitionBean: ProcessDefinitionBean) : ProcessDefinition {
  override fun getVersionTag() = processDefinitionBean.versionTag
  override fun getName() = processDefinitionBean.name
  override fun getId() = processDefinitionBean.id
  override fun getDeploymentId() = processDefinitionBean.deploymentId
  override fun isSuspended() = processDefinitionBean.suspended
  override fun getCategory() = processDefinitionBean.category
  override fun getKey() = processDefinitionBean.key
  override fun getVersion() = processDefinitionBean.version
  override fun getDescription() = processDefinitionBean.description
  override fun getTenantId() = processDefinitionBean.tenantId
  override fun getResourceName() = processDefinitionBean.resourceName
  override fun getDiagramResourceName() = processDefinitionBean.diagramResourceName
  override fun hasStartFormKey() = processDefinitionBean.hasStartFormKey
  override fun isStartableInTasklist() = processDefinitionBean.startableInTaskList
  override fun getHistoryTimeToLive() = processDefinitionBean.historyTimeToLive
}

/**
 * POJO to hold the values of process definition.
 */
data class ProcessDefinitionBean(
  val versionTag: String?,
  val id: String?,
  val name: String?,
  val key: String?,
  val category: String?,
  val deploymentId: String?,
  val suspended: Boolean,
  val historyTimeToLive: Int?,
  val startableInTaskList: Boolean,
  val hasStartFormKey: Boolean,
  val resourceName: String?,
  val diagramResourceName: String?,
  val tenantId: String?,
  val description: String?,
  val version: Int
) {
  companion object {
    /**
     * Factory method to create bean from REST represenation.
     */
    @JvmStatic
    fun fromDto(dto: ProcessDefinitionDto) = ProcessDefinitionBean(
      versionTag = dto.versionTag,
      id = dto.id,
      name = dto.name,
      key = dto.key,
      category = dto.category,
      deploymentId = dto.deploymentId,
      suspended = dto.suspended,
      historyTimeToLive = dto.historyTimeToLive,
      startableInTaskList = dto.startableInTasklist,
      hasStartFormKey = false, // FIXME
      resourceName = dto.resource,
      diagramResourceName = dto.diagram,
      tenantId = dto.tenantId,
      description = dto.description,
      version = dto.version
    )
  }
}
