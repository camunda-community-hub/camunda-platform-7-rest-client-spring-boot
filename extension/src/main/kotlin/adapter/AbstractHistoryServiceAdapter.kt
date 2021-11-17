/*-
 * #%L
 * camunda-rest-client-spring-boot
 * %%
 * Copyright (C) 2021 Camunda Services GmbH
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

import org.camunda.bpm.engine.HistoryService
import org.camunda.bpm.engine.batch.Batch
import org.camunda.bpm.engine.batch.history.HistoricBatchQuery
import org.camunda.bpm.engine.history.*
import org.camunda.bpm.engine.runtime.Job
import org.camunda.bpm.extension.rest.impl.RemoteHistoryService
import org.camunda.bpm.extension.rest.impl.implementedBy

/**
 * Adapter for implementing history service.
 */
abstract class AbstractHistoryServiceAdapter : HistoryService {

  override fun createHistoricProcessInstanceQuery(): HistoricProcessInstanceQuery {
    implementedBy(RemoteHistoryService::class)
  }

  override fun createHistoricActivityInstanceQuery(): HistoricActivityInstanceQuery {
    TODO("Not yet implemented")
  }

  override fun createHistoricActivityStatisticsQuery(p0: String?): HistoricActivityStatisticsQuery {
    TODO("Not yet implemented")
  }

  override fun createHistoricCaseActivityStatisticsQuery(p0: String?): HistoricCaseActivityStatisticsQuery {
    TODO("Not yet implemented")
  }

  override fun createHistoricTaskInstanceQuery(): HistoricTaskInstanceQuery {
    TODO("Not yet implemented")
  }

  override fun createHistoricDetailQuery(): HistoricDetailQuery {
    TODO("Not yet implemented")
  }

  override fun createHistoricVariableInstanceQuery(): HistoricVariableInstanceQuery {
    TODO("Not yet implemented")
  }

  override fun createUserOperationLogQuery(): UserOperationLogQuery {
    TODO("Not yet implemented")
  }

  override fun createHistoricIncidentQuery(): HistoricIncidentQuery {
    TODO("Not yet implemented")
  }

  override fun createHistoricIdentityLinkLogQuery(): HistoricIdentityLinkLogQuery {
    TODO("Not yet implemented")
  }

  override fun createHistoricCaseInstanceQuery(): HistoricCaseInstanceQuery {
    TODO("Not yet implemented")
  }

  override fun createHistoricCaseActivityInstanceQuery(): HistoricCaseActivityInstanceQuery {
    TODO("Not yet implemented")
  }

  override fun createHistoricDecisionInstanceQuery(): HistoricDecisionInstanceQuery {
    TODO("Not yet implemented")
  }

  override fun deleteHistoricTaskInstance(p0: String?) {
    TODO("Not yet implemented")
  }

  override fun deleteHistoricProcessInstance(p0: String?) {
    TODO("Not yet implemented")
  }

  override fun deleteHistoricProcessInstanceIfExists(p0: String?) {
    TODO("Not yet implemented")
  }

  override fun deleteHistoricProcessInstances(p0: MutableList<String>?) {
    TODO("Not yet implemented")
  }

  override fun deleteHistoricProcessInstancesIfExists(p0: MutableList<String>?) {
    TODO("Not yet implemented")
  }

  override fun deleteHistoricProcessInstancesBulk(p0: MutableList<String>?) {
    TODO("Not yet implemented")
  }

  override fun cleanUpHistoryAsync(): Job {
    TODO("Not yet implemented")
  }

  override fun cleanUpHistoryAsync(p0: Boolean): Job {
    TODO("Not yet implemented")
  }

  override fun findHistoryCleanupJob(): Job {
    TODO("Not yet implemented")
  }

  override fun findHistoryCleanupJobs(): MutableList<Job> {
    TODO("Not yet implemented")
  }

  override fun deleteHistoricProcessInstancesAsync(p0: MutableList<String>?, p1: String?): Batch {
    TODO("Not yet implemented")
  }

  override fun deleteHistoricProcessInstancesAsync(p0: HistoricProcessInstanceQuery?, p1: String?): Batch {
    TODO("Not yet implemented")
  }

  override fun deleteHistoricProcessInstancesAsync(p0: MutableList<String>?, p1: HistoricProcessInstanceQuery?, p2: String?): Batch {
    TODO("Not yet implemented")
  }

  override fun deleteUserOperationLogEntry(p0: String?) {
    TODO("Not yet implemented")
  }

  override fun deleteHistoricCaseInstance(p0: String?) {
    TODO("Not yet implemented")
  }

  override fun deleteHistoricCaseInstancesBulk(p0: MutableList<String>?) {
    TODO("Not yet implemented")
  }

  override fun deleteHistoricDecisionInstance(p0: String?) {
    TODO("Not yet implemented")
  }

  override fun deleteHistoricDecisionInstancesBulk(p0: MutableList<String>?) {
    TODO("Not yet implemented")
  }

  override fun deleteHistoricDecisionInstanceByDefinitionId(p0: String?) {
    TODO("Not yet implemented")
  }

  override fun deleteHistoricDecisionInstanceByInstanceId(p0: String?) {
    TODO("Not yet implemented")
  }

  override fun deleteHistoricDecisionInstancesAsync(p0: MutableList<String>?, p1: String?): Batch {
    TODO("Not yet implemented")
  }

  override fun deleteHistoricDecisionInstancesAsync(p0: HistoricDecisionInstanceQuery?, p1: String?): Batch {
    TODO("Not yet implemented")
  }

  override fun deleteHistoricDecisionInstancesAsync(p0: MutableList<String>?, p1: HistoricDecisionInstanceQuery?, p2: String?): Batch {
    TODO("Not yet implemented")
  }

  override fun deleteHistoricVariableInstance(p0: String?) {
    TODO("Not yet implemented")
  }

  override fun deleteHistoricVariableInstancesByProcessInstanceId(p0: String?) {
    TODO("Not yet implemented")
  }

  override fun createNativeHistoricProcessInstanceQuery(): NativeHistoricProcessInstanceQuery {
    TODO("Not yet implemented")
  }

  override fun createNativeHistoricTaskInstanceQuery(): NativeHistoricTaskInstanceQuery {
    TODO("Not yet implemented")
  }

  override fun createNativeHistoricActivityInstanceQuery(): NativeHistoricActivityInstanceQuery {
    TODO("Not yet implemented")
  }

  override fun createNativeHistoricCaseInstanceQuery(): NativeHistoricCaseInstanceQuery {
    TODO("Not yet implemented")
  }

  override fun createNativeHistoricCaseActivityInstanceQuery(): NativeHistoricCaseActivityInstanceQuery {
    TODO("Not yet implemented")
  }

  override fun createNativeHistoricDecisionInstanceQuery(): NativeHistoricDecisionInstanceQuery {
    TODO("Not yet implemented")
  }

  override fun createNativeHistoricVariableInstanceQuery(): NativeHistoricVariableInstanceQuery {
    TODO("Not yet implemented")
  }

  override fun createHistoricJobLogQuery(): HistoricJobLogQuery {
    TODO("Not yet implemented")
  }

  override fun getHistoricJobLogExceptionStacktrace(p0: String?): String {
    TODO("Not yet implemented")
  }

  override fun createHistoricProcessInstanceReport(): HistoricProcessInstanceReport {
    TODO("Not yet implemented")
  }

  override fun createHistoricTaskInstanceReport(): HistoricTaskInstanceReport {
    TODO("Not yet implemented")
  }

  override fun createCleanableHistoricProcessInstanceReport(): CleanableHistoricProcessInstanceReport {
    TODO("Not yet implemented")
  }

  override fun createCleanableHistoricDecisionInstanceReport(): CleanableHistoricDecisionInstanceReport {
    TODO("Not yet implemented")
  }

  override fun createCleanableHistoricCaseInstanceReport(): CleanableHistoricCaseInstanceReport {
    TODO("Not yet implemented")
  }

  override fun createCleanableHistoricBatchReport(): CleanableHistoricBatchReport {
    TODO("Not yet implemented")
  }

  override fun createHistoricBatchQuery(): HistoricBatchQuery {
    TODO("Not yet implemented")
  }

  override fun deleteHistoricBatch(p0: String?) {
    TODO("Not yet implemented")
  }

  override fun createHistoricDecisionInstanceStatisticsQuery(p0: String?): HistoricDecisionInstanceStatisticsQuery {
    TODO("Not yet implemented")
  }

  override fun createHistoricExternalTaskLogQuery(): HistoricExternalTaskLogQuery {
    TODO("Not yet implemented")
  }

  override fun getHistoricExternalTaskLogErrorDetails(p0: String?): String {
    TODO("Not yet implemented")
  }

  override fun setRemovalTimeToHistoricProcessInstances(): SetRemovalTimeSelectModeForHistoricProcessInstancesBuilder {
    TODO("Not yet implemented")
  }

  override fun setRemovalTimeToHistoricDecisionInstances(): SetRemovalTimeSelectModeForHistoricDecisionInstancesBuilder {
    TODO("Not yet implemented")
  }

  override fun setRemovalTimeToHistoricBatches(): SetRemovalTimeSelectModeForHistoricBatchesBuilder {
    TODO("Not yet implemented")
  }

  override fun setAnnotationForOperationLogById(p0: String?, p1: String?) {
    TODO("Not yet implemented")
  }

  override fun clearAnnotationForOperationLogById(p0: String?) {
    TODO("Not yet implemented")
  }
}
