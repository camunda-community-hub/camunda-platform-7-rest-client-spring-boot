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

import org.camunda.bpm.engine.runtime.Incident
import org.camunda.community.rest.client.model.IncidentDto
import org.camunda.community.rest.impl.toDate
import java.util.*

/**
 * Implementation of incident delegating to a simple bean.
 */
class IncidentAdapter(private val incidentBean: IncidentBean) : Incident {
  override fun getId(): String? = incidentBean.id
  override fun getIncidentTimestamp(): Date = incidentBean.incidentTimestamp
  override fun getIncidentType(): String = incidentBean.incidentType
  override fun getIncidentMessage(): String? = incidentBean.incidentMessage
  override fun getExecutionId(): String = incidentBean.executionId
  override fun getActivityId(): String = incidentBean.activityId
  override fun getFailedActivityId(): String? = incidentBean.failedActivityId
  override fun getProcessInstanceId(): String = incidentBean.processInstanceId
  override fun getProcessDefinitionId(): String = incidentBean.processDefinitionId
  override fun getCauseIncidentId(): String = incidentBean.causeIncidentId
  override fun getRootCauseIncidentId(): String = incidentBean.rootCauseIncidentId
  override fun getConfiguration(): String = incidentBean.configuration
  override fun getTenantId(): String? = incidentBean.tenantId
  override fun getJobDefinitionId(): String? = incidentBean.jobDefinitionId
  override fun getHistoryConfiguration(): String? = incidentBean.historyConfiguration
  override fun getAnnotation(): String? = incidentBean.annotation
}

/**
 * Incident bean.
 */
data class IncidentBean(
  val id: String?,
  val incidentTimestamp: Date,
  val incidentType: String,
  val executionId: String,
  val activityId: String,
  val processInstanceId: String,
  val processDefinitionId: String,
  val causeIncidentId: String,
  val rootCauseIncidentId: String,
  val configuration: String,
  val incidentMessage: String?,
  val tenantId: String?,
  val jobDefinitionId: String?,
  val historyConfiguration: String?,
  val failedActivityId: String?,
  val annotation: String?,
) {
  companion object {
    /**
     * Constructs the bean from Incident DTO.
     * @param dto: REST representation of the incident.
     */
    @JvmStatic
    fun fromDto(dto: IncidentDto): IncidentBean =
      IncidentBean(
        id = dto.id,
        incidentTimestamp = dto.incidentTimestamp.toDate()!!,
        incidentType = dto.incidentType,
        executionId = dto.executionId,
        activityId = dto.activityId,
        processInstanceId = dto.processInstanceId,
        processDefinitionId = dto.processDefinitionId,
        causeIncidentId = dto.causeIncidentId,
        rootCauseIncidentId = dto.rootCauseIncidentId,
        configuration = dto.configuration,
        incidentMessage = dto.incidentMessage,
        tenantId = dto.tenantId,
        jobDefinitionId = dto.jobDefinitionId,
        historyConfiguration = null,
        failedActivityId = dto.failedActivityId,
        annotation = dto.annotation
      )
  }
}
