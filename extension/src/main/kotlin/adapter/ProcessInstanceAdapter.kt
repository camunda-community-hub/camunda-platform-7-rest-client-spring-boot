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
package org.camunda.bpm.extension.rest.adapter

import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceDto
import org.camunda.bpm.engine.runtime.ProcessInstance

/**
 * Implementation of Camunda API Process Instance backed by a bean.
 */
class ProcessInstanceAdapter(private val instanceBean: InstanceBean) : ProcessInstance {

  override fun getProcessInstanceId(): String? = when (instanceBean.type) {
    InstanceType.CASE -> null
    InstanceType.PROCESS -> instanceBean.instanceId
  }

  override fun getBusinessKey(): String? = instanceBean.businessKey

  override fun getRootProcessInstanceId(): String? =
    when (instanceBean.type) {
      InstanceType.PROCESS -> instanceBean.rootProcessInstanceId
      InstanceType.CASE -> null
    }

  override fun getProcessDefinitionId(): String? =
    when (instanceBean.type) {
      InstanceType.PROCESS -> instanceBean.processDefinitionId
      InstanceType.CASE -> null
    }

  override fun isSuspended(): Boolean = instanceBean.suspended

  override fun getCaseInstanceId(): String? =
    when (instanceBean.type) {
      InstanceType.PROCESS -> null
      InstanceType.CASE -> instanceBean.instanceId
    }


  override fun isEnded(): Boolean = instanceBean.ended
  override fun getId(): String = instanceBean.id
  override fun getTenantId(): String? = instanceBean.tenantId
}

/**
 * Backing bean for process instance.
 */
data class InstanceBean(
  val id: String,
  val ended: Boolean,
  val suspended: Boolean,
  val type: InstanceType,
  val instanceId: String,
  val businessKey: String? = null,
  val tenantId: String? = null,
  val processDefinitionId: String? = null,
  val rootProcessInstanceId: String? = null
) {
  companion object {
    /**
     * Factory method to construct the bean from DTO.
     * @param dto: REST representation of process instance.
     */
    @JvmStatic
    fun fromProcessInstanceDto(processInstance: ProcessInstanceDto) =
      InstanceBean(
        id = processInstance.id,
        ended = processInstance.isEnded,
        suspended = processInstance.isSuspended,
        businessKey = processInstance.businessKey,
        tenantId = processInstance.tenantId,
        type = if (processInstance.caseInstanceId != null) {
          InstanceType.CASE
        } else {
          InstanceType.PROCESS
        },
        instanceId = if (processInstance.caseInstanceId != null) {
          processInstance.caseInstanceId
        } else {
          processInstance.id
        },
        processDefinitionId = processInstance.definitionId
      )
  }
}

/**
 * Instance type to cope with different ids.
 */
enum class InstanceType {
  /**
   * Process instance.
   */
  PROCESS,
  /**
   * Case instance.
   */
  CASE
}
