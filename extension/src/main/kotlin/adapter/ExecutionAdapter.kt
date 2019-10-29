/*-
 * #%L
 * camunda-bpm-feign
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
package org.camunda.bpm.extension.feign.adapter

import org.camunda.bpm.engine.rest.dto.runtime.ExecutionDto
import org.camunda.bpm.engine.runtime.Execution

/**
 * Implementation of Camunda API Execution backed by a bean.
 */
class ExecutionAdapter(private val executionBean: ExecutionBean) : Execution {
  override fun getProcessInstanceId(): String = executionBean.processInstanceId
  override fun isEnded(): Boolean = executionBean.ended
  override fun getId(): String = executionBean.id
  override fun isSuspended(): Boolean = executionBean.suspended
  override fun getTenantId(): String? = executionBean.tenantId
}

/**
 * Backing bean for the execution.
 */
data class ExecutionBean(
  val id: String,
  val processInstanceId: String,
  val ended: Boolean,
  val suspended: Boolean,
  val tenantId: String? = null
) {
  companion object {
    /**
     * Constructs the bean from Execution DTO.
     * @param dto: REST representation of the execution.
     */
    @JvmStatic
    fun fromExecutionDto(dto: ExecutionDto) =
      ExecutionBean(
        id = dto.id,
        processInstanceId = dto.processInstanceId,
        ended = dto.isEnded,
        suspended = false,
        tenantId = dto.tenantId
      )
  }
}
