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

import org.camunda.bpm.engine.history.HistoricProcessInstance
import org.camunda.bpm.extension.rest.client.model.HistoricProcessInstanceDto
import java.util.*

/**
 * Implementation of Camunda API Historic Process Instance backed by a bean.
 */
class HistoricProcessInstanceAdapter(private val historicInstanceBean: HistoricInstanceBean) : HistoricProcessInstance {

  override fun getBusinessKey(): String? = historicInstanceBean.businessKey
  override fun getProcessDefinitionKey(): String = historicInstanceBean.processDefinitionKey
  override fun getRootProcessInstanceId(): String? = historicInstanceBean.rootProcessInstanceId
  override fun getSuperCaseInstanceId(): String? = historicInstanceBean.superCaseInstanceId
  override fun getProcessDefinitionId(): String = historicInstanceBean.processDefinitionId
  override fun getProcessDefinitionName(): String = historicInstanceBean.processDefinitionName
  override fun getProcessDefinitionVersion(): Int = historicInstanceBean.processDefinitionVersion
  override fun getStartTime(): Date = historicInstanceBean.startTime
  override fun getEndTime(): Date = historicInstanceBean.endTime
  override fun getRemovalTime(): Date? = historicInstanceBean.removalTime
  override fun getDurationInMillis(): Long = historicInstanceBean.durationInMillis
  override fun getEndActivityId(): String? = historicInstanceBean.endActivityId
  override fun getStartUserId(): String = historicInstanceBean.startUserId
  override fun getStartActivityId(): String = historicInstanceBean.startActivityId
  override fun getDeleteReason(): String? = historicInstanceBean.deleteReason
  override fun getSuperProcessInstanceId(): String? = historicInstanceBean.superProcessInstanceId
  override fun getCaseInstanceId(): String? = historicInstanceBean.caseInstanceId
  override fun getId(): String = historicInstanceBean.id
  override fun getTenantId(): String? = historicInstanceBean.tenantId
  override fun getState(): String = historicInstanceBean.state
}

/**
 * Backing bean for historic process instance.
 */
data class HistoricInstanceBean(
  val id: String,
  val businessKey: String?,
  val processDefinitionKey: String,
  val processDefinitionId: String,
  val processDefinitionName: String,
  val processDefinitionVersion: Int,
  val startTime: Date,
  val endTime: Date,
  val removalTime: Date?,
  val durationInMillis: Long,
  val endActivityId: String?,
  val startUserId: String,
  val startActivityId: String,
  val deleteReason: String?,
  val superProcessInstanceId: String?,
  val rootProcessInstanceId: String?,
  val superCaseInstanceId: String?,
  val caseInstanceId: String?,
  val tenantId: String?,
  val state: String
) {
  companion object {
    /**
     * Factory method to construct the bean from DTO.
     * @param processInstance: REST representation of historic process instance.
     */
    @JvmStatic
    fun fromHistoricProcessInstanceDto(processInstance: HistoricProcessInstanceDto) =
      HistoricInstanceBean(
        id = processInstance.id,
        businessKey = processInstance.businessKey,
        processDefinitionKey = processInstance.processDefinitionKey,
        processDefinitionId = processInstance.processDefinitionId,
        processDefinitionName = processInstance.processDefinitionName,
        processDefinitionVersion = processInstance.processDefinitionVersion,
        startTime = processInstance.startTime,
        endTime = processInstance.endTime,
        removalTime = processInstance.removalTime,
        durationInMillis = processInstance.durationInMillis,
        endActivityId = null,
        startUserId = processInstance.startUserId,
        startActivityId = processInstance.startActivityId,
        deleteReason = processInstance.deleteReason,
        superProcessInstanceId = processInstance.superProcessInstanceId,
        rootProcessInstanceId = processInstance.rootProcessInstanceId,
        superCaseInstanceId = processInstance.superCaseInstanceId,
        caseInstanceId = processInstance.caseInstanceId,
        tenantId = processInstance.tenantId,
        state = when (processInstance.state) {
          HistoricProcessInstanceDto.StateEnum.ACTIVE -> HistoricProcessInstance.STATE_ACTIVE
          HistoricProcessInstanceDto.StateEnum.COMPLETED -> HistoricProcessInstance.STATE_COMPLETED
          HistoricProcessInstanceDto.StateEnum.SUSPENDED -> HistoricProcessInstance.STATE_SUSPENDED
          HistoricProcessInstanceDto.StateEnum.EXTERNALLY_TERMINATED -> HistoricProcessInstance.STATE_EXTERNALLY_TERMINATED
          HistoricProcessInstanceDto.StateEnum.INTERNALLY_TERMINATED -> HistoricProcessInstance.STATE_INTERNALLY_TERMINATED
          else -> throw IllegalStateException("unknow state for historic process instance")
        }
      )

  }
}
