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

import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.batch.Batch
import org.camunda.bpm.engine.history.HistoricProcessInstanceQuery
import org.camunda.bpm.engine.migration.MigrationPlan
import org.camunda.bpm.engine.migration.MigrationPlanBuilder
import org.camunda.bpm.engine.migration.MigrationPlanExecutionBuilder
import org.camunda.bpm.engine.runtime.*
import org.camunda.bpm.engine.variable.VariableMap
import org.camunda.bpm.engine.variable.value.TypedValue
import org.camunda.community.rest.impl.RemoteRuntimeService
import org.camunda.community.rest.impl.implementedBy
import java.util.*

/**
 * Adapter for implementing runtime service.
 */
abstract class AbstractRuntimeServiceAdapter : RuntimeService {

  override fun createVariableInstanceQuery(): VariableInstanceQuery {
    TODO("not implemented")
  }

  override fun getVariables(executionId: String): MutableMap<String, Any?> {
    implementedBy(RemoteRuntimeService::class)
  }

  override fun getVariables(executionId: String, variableNames: MutableCollection<String>): MutableMap<String, Any?> {
    implementedBy(RemoteRuntimeService::class)
  }

  override fun <T : TypedValue> getVariableTyped(executionId: String, variableName: String): T? {
    implementedBy(RemoteRuntimeService::class)
  }

  override fun <T : TypedValue> getVariableTyped(executionId: String, variableName: String, deserializeValue: Boolean): T? {
    implementedBy(RemoteRuntimeService::class)
  }

  override fun getVariablesTyped(executionId: String): VariableMap {
    implementedBy(RemoteRuntimeService::class)
  }

  override fun getVariablesTyped(executionId: String, deserializeValues: Boolean): VariableMap {
    implementedBy(RemoteRuntimeService::class)
  }

  override fun getVariablesTyped(executionId: String, variableNames: MutableCollection<String>, deserializeValues: Boolean): VariableMap {
    implementedBy(RemoteRuntimeService::class)
  }

  override fun getVariablesLocal(executionId: String): MutableMap<String, Any?> {
    implementedBy(RemoteRuntimeService::class)
  }

  override fun getVariablesLocal(executionId: String, variableNames: MutableCollection<String>): MutableMap<String, Any?> {
    implementedBy(RemoteRuntimeService::class)
  }

  override fun getVariableLocal(executionId: String, variableName: String): Any? {
    implementedBy(RemoteRuntimeService::class)
  }
  override fun getVariable(executionId: String, variableName: String): Any? {
    implementedBy(RemoteRuntimeService::class)
  }

  override fun getVariablesLocalTyped(executionId: String): VariableMap {
    implementedBy(RemoteRuntimeService::class)
  }

  override fun getVariablesLocalTyped(executionId: String, deserializeValues: Boolean): VariableMap {
    implementedBy(RemoteRuntimeService::class)
  }

  override fun getVariablesLocalTyped(executionId: String, variableNames: MutableCollection<String>, deserializeValues: Boolean): VariableMap {
    implementedBy(RemoteRuntimeService::class)
  }

  override fun <T : TypedValue> getVariableLocalTyped(executionId: String, variableName: String): T? {
    implementedBy(RemoteRuntimeService::class)
  }

  override fun <T : TypedValue> getVariableLocalTyped(executionId: String, variableName: String, deserializeValue: Boolean): T? {
    implementedBy(RemoteRuntimeService::class)
  }

  override fun removeVariables(executionId: String, variableNames: MutableCollection<String>) {
    implementedBy(RemoteRuntimeService::class)
  }

  override fun removeVariable(executionId: String, variableName: String) {
    implementedBy(RemoteRuntimeService::class)
  }

  override fun removeVariablesLocal(executionId: String, variableNames: MutableCollection<String>) {
    implementedBy(RemoteRuntimeService::class)
  }

  override fun removeVariableLocal(executionId: String, variableName: String) {
    implementedBy(RemoteRuntimeService::class)
  }

  override fun setVariableLocal(executionId: String, variableName: String, value: Any?) {
    implementedBy(RemoteRuntimeService::class)
  }

  override fun setVariablesLocal(executionId: String, variables: MutableMap<String, out Any>) {
    implementedBy(RemoteRuntimeService::class)
  }

  override fun setVariable(executionId: String, variableName: String, value: Any?) {
    implementedBy(RemoteRuntimeService::class)
  }

  override fun setVariables(executionId: String, variables: MutableMap<String, out Any>) {
    implementedBy(RemoteRuntimeService::class)
  }

  override fun correlateMessage(messageName: String) {
    implementedBy(RemoteRuntimeService::class)
  }

  override fun correlateMessage(messageName: String, businessKey: String) {
    implementedBy(RemoteRuntimeService::class)
  }

  override fun correlateMessage(messageName: String, correlationKeys: MutableMap<String, Any>) {
    implementedBy(RemoteRuntimeService::class)
  }

  override fun correlateMessage(messageName: String, businessKey: String, processVariables: MutableMap<String, Any>) {
    implementedBy(RemoteRuntimeService::class)
  }

  override fun correlateMessage(messageName: String, correlationKeys: MutableMap<String, Any>, processVariables: MutableMap<String, Any>) {
    implementedBy(RemoteRuntimeService::class)
  }

  override fun correlateMessage(messageName: String, businessKey: String, correlationKeys: MutableMap<String, Any>, processVariables: MutableMap<String, Any>) {
    implementedBy(RemoteRuntimeService::class)
  }

  override fun updateProcessInstanceSuspensionState(): UpdateProcessInstanceSuspensionStateSelectBuilder {
    implementedBy(RemoteRuntimeService::class)
  }
  override fun getActivityInstance(processInstanceId: String?): ActivityInstance {
    TODO("not implemented")
  }


  override fun createConditionEvaluation(): ConditionEvaluationBuilder {
    TODO("not implemented")
  }

  override fun resolveIncident(incidentId: String?) {
    implementedBy(RemoteRuntimeService::class)
  }

  override fun getActiveActivityIds(executionId: String?): MutableList<String> {
    TODO("not implemented")
  }
  override fun signal(executionId: String) {
    implementedBy(RemoteRuntimeService::class)
  }

  override fun signal(executionId: String, signalName: String, signalData: Any?, processVariables: MutableMap<String, Any>) {
    implementedBy(RemoteRuntimeService::class)
  }

  override fun signal(executionId: String, processVariables: MutableMap<String, Any>) {
    implementedBy(RemoteRuntimeService::class)
  }

  override fun signalEventReceived(signalName: String) {
    implementedBy(RemoteRuntimeService::class)
  }

  override fun signalEventReceived(signalName: String, processVariables: MutableMap<String, Any>) {
    implementedBy(RemoteRuntimeService::class)
  }

  override fun signalEventReceived(signalName: String, executionId: String) {
    implementedBy(RemoteRuntimeService::class)
  }

  override fun signalEventReceived(signalName: String, executionId: String, processVariables: MutableMap<String, Any>) {
    implementedBy(RemoteRuntimeService::class)
  }

  override fun createSignalEvent(signalName: String): SignalEventReceivedBuilder {
    implementedBy(RemoteRuntimeService::class)
  }

  override fun createNativeExecutionQuery(): NativeExecutionQuery {
    TODO("not implemented")
  }

  override fun newMigration(migrationPlan: MigrationPlan?): MigrationPlanExecutionBuilder {
    TODO("not implemented")
  }

  override fun createMessageCorrelation(messageName: String): MessageCorrelationBuilder {
    implementedBy(RemoteRuntimeService::class)
  }

  override fun messageEventReceived(messageName: String?, executionId: String?) {
    TODO("not implemented")
  }

  override fun messageEventReceived(messageName: String?, executionId: String?, processVariables: MutableMap<String, Any>?) {
    TODO("not implemented")
  }

  override fun createIncident(incidentType: String?, executionId: String?, configuration: String?): Incident {
    implementedBy(RemoteRuntimeService::class)
  }

  override fun createIncident(incidentType: String?, executionId: String?, configuration: String?, message: String?): Incident {
    implementedBy(RemoteRuntimeService::class)
  }

  override fun createMigrationPlan(sourceProcessDefinitionId: String?, targetProcessDefinitionId: String?): MigrationPlanBuilder {
    TODO("not implemented")
  }

  override fun deleteProcessInstancesAsync(processInstanceIds: MutableList<String>?,
                                           processInstanceQuery: ProcessInstanceQuery?,
                                           historicProcessInstanceQuery: HistoricProcessInstanceQuery?,
                                           deleteReason: String?,
                                           skipCustomListeners: Boolean,
                                           skipSubprocesses: Boolean): Batch {
    implementedBy(RemoteRuntimeService::class)
  }


  override fun deleteProcessInstancesAsync(processInstanceIds: MutableList<String>?, processInstanceQuery: ProcessInstanceQuery?, deleteReason: String?): Batch {
    implementedBy(RemoteRuntimeService::class)
  }

  override fun deleteProcessInstancesAsync(processInstanceIds: MutableList<String>?, processInstanceQuery: ProcessInstanceQuery?, deleteReason: String?, skipCustomListeners: Boolean): Batch {
    implementedBy(RemoteRuntimeService::class)
  }

  override fun deleteProcessInstancesAsync(processInstanceIds: MutableList<String>?, processInstanceQuery: ProcessInstanceQuery?, deleteReason: String?, skipCustomListeners: Boolean, skipSubprocesses: Boolean): Batch {
    implementedBy(RemoteRuntimeService::class)
  }

  override fun deleteProcessInstancesAsync(processInstanceQuery: ProcessInstanceQuery?, deleteReason: String?): Batch {
    implementedBy(RemoteRuntimeService::class)
  }

  override fun deleteProcessInstancesAsync(processInstanceIds: MutableList<String>?, deleteReason: String?): Batch {
    implementedBy(RemoteRuntimeService::class)
  }

  override fun deleteProcessInstanceIfExists(processInstanceId: String?, deleteReason: String?, skipCustomListeners: Boolean, externallyTerminated: Boolean, skipIoMappings: Boolean, skipSubprocesses: Boolean) {
    implementedBy(RemoteRuntimeService::class)
  }

  override fun suspendProcessInstanceById(processInstanceId: String?) {
    implementedBy(RemoteRuntimeService::class)
  }

  override fun deleteProcessInstances(processInstanceIds: MutableList<String>?, deleteReason: String?, skipCustomListeners: Boolean, externallyTerminated: Boolean) {
    implementedBy(RemoteRuntimeService::class)
  }

  override fun deleteProcessInstances(processInstanceIds: MutableList<String>?, deleteReason: String?, skipCustomListeners: Boolean, externallyTerminated: Boolean, skipSubprocesses: Boolean) {
    implementedBy(RemoteRuntimeService::class)
  }

  override fun activateProcessInstanceById(processInstanceId: String?) {
    implementedBy(RemoteRuntimeService::class)
  }

  override fun suspendProcessInstanceByProcessDefinitionKey(processDefinitionKey: String?) {
    implementedBy(RemoteRuntimeService::class)
  }

  override fun createProcessInstanceModification(processInstanceId: String?): ProcessInstanceModificationBuilder {
    TODO("not implemented")
  }

  override fun activateProcessInstanceByProcessDefinitionId(processDefinitionId: String?) {
    implementedBy(RemoteRuntimeService::class)
  }

  override fun createNativeProcessInstanceQuery(): NativeProcessInstanceQuery {
    TODO("not implemented")
  }

  override fun suspendProcessInstanceByProcessDefinitionId(processDefinitionId: String?) {
    implementedBy(RemoteRuntimeService::class)
  }

  override fun createProcessInstanceQuery(): ProcessInstanceQuery {
    implementedBy(RemoteRuntimeService::class)
  }

  override fun activateProcessInstanceByProcessDefinitionKey(processDefinitionKey: String?) {
    implementedBy(RemoteRuntimeService::class)
  }

  override fun deleteProcessInstancesIfExists(processInstanceIds: MutableList<String>?, deleteReason: String?, skipCustomListeners: Boolean, externallyTerminated: Boolean, skipSubprocesses: Boolean) {
    implementedBy(RemoteRuntimeService::class)
  }

  override fun createProcessInstanceById(processDefinitionId: String?): ProcessInstantiationBuilder {
    TODO("not implemented")
  }

  override fun createProcessInstanceByKey(processDefinitionKey: String): ProcessInstantiationBuilder {
    TODO("not implemented")
  }

  override fun deleteProcessInstance(processInstanceId: String?, deleteReason: String?) {
    implementedBy(RemoteRuntimeService::class)
  }

  override fun deleteProcessInstance(processInstanceId: String?, deleteReason: String?, skipCustomListeners: Boolean) {
    implementedBy(RemoteRuntimeService::class)
  }

  override fun deleteProcessInstance(processInstanceId: String?, deleteReason: String?, skipCustomListeners: Boolean, externallyTerminated: Boolean) {
    implementedBy(RemoteRuntimeService::class)
  }

  override fun deleteProcessInstance(processInstanceId: String?, deleteReason: String?, skipCustomListeners: Boolean, externallyTerminated: Boolean, skipIoMappings: Boolean) {
    implementedBy(RemoteRuntimeService::class)
  }

  override fun deleteProcessInstance(processInstanceId: String?, deleteReason: String?, skipCustomListeners: Boolean, externallyTerminated: Boolean, skipIoMappings: Boolean, skipSubprocesses: Boolean) {
    implementedBy(RemoteRuntimeService::class)
  }

  override fun restartProcessInstances(processDefinitionId: String?): RestartProcessInstanceBuilder {
    TODO("not implemented")
  }

  override fun createModification(processDefinitionId: String?): ModificationBuilder {
    TODO("not implemented")
  }

  override fun createExecutionQuery(): ExecutionQuery {
    implementedBy(RemoteRuntimeService::class)
  }

  override fun createEventSubscriptionQuery(): EventSubscriptionQuery {
    implementedBy(RemoteRuntimeService::class)
  }

  override fun createIncidentQuery(): IncidentQuery {
    implementedBy(RemoteRuntimeService::class)
  }

  override fun startProcessInstanceById(processDefinitionId: String): ProcessInstance {
    implementedBy(RemoteRuntimeService::class)
  }

  override fun startProcessInstanceById(processDefinitionId: String, businessKey: String): ProcessInstance {
    implementedBy(RemoteRuntimeService::class)
  }

  override fun startProcessInstanceById(processDefinitionId: String, businessKey: String, caseInstanceId: String): ProcessInstance {
    implementedBy(RemoteRuntimeService::class)
  }

  override fun startProcessInstanceById(processDefinitionId: String, variables: MutableMap<String, Any>): ProcessInstance {
    implementedBy(RemoteRuntimeService::class)
  }

  override fun startProcessInstanceById(processDefinitionId: String, businessKey: String, variables: MutableMap<String, Any>): ProcessInstance {
    implementedBy(RemoteRuntimeService::class)
  }

  override fun startProcessInstanceById(processDefinitionId: String, businessKey: String, caseInstanceId: String, variables: MutableMap<String, Any>): ProcessInstance {
    implementedBy(RemoteRuntimeService::class)
  }

  override fun startProcessInstanceByKey(processDefinitionKey: String): ProcessInstance {
    implementedBy(RemoteRuntimeService::class)
  }

  override fun startProcessInstanceByKey(processDefinitionKey: String, businessKey: String): ProcessInstance {
    implementedBy(RemoteRuntimeService::class)
  }

  override fun startProcessInstanceByKey(processDefinitionKey: String, businessKey: String, caseInstanceId: String): ProcessInstance {
    implementedBy(RemoteRuntimeService::class)
  }

  override fun startProcessInstanceByKey(processDefinitionKey: String, variables: MutableMap<String, Any>): ProcessInstance {
    implementedBy(RemoteRuntimeService::class)
  }

  override fun startProcessInstanceByKey(processDefinitionKey: String, businessKey: String, variables: MutableMap<String, Any>): ProcessInstance {
    implementedBy(RemoteRuntimeService::class)
  }

  override fun startProcessInstanceByKey(processDefinitionKey: String, businessKey: String, caseInstanceId: String, variables: MutableMap<String, Any>): ProcessInstance {
    implementedBy(RemoteRuntimeService::class)
  }

  override fun startProcessInstanceByMessage(messageName: String): ProcessInstance {
    implementedBy(RemoteRuntimeService::class)
  }

  override fun startProcessInstanceByMessage(messageName: String, businessKey: String): ProcessInstance {
    implementedBy(RemoteRuntimeService::class)
  }

  override fun startProcessInstanceByMessage(messageName: String, processVariables: MutableMap<String, Any>): ProcessInstance {
    implementedBy(RemoteRuntimeService::class)
  }

  override fun startProcessInstanceByMessage(messageName: String, businessKey: String, processVariables: MutableMap<String, Any>): ProcessInstance {
    implementedBy(RemoteRuntimeService::class)
  }

  override fun startProcessInstanceByMessageAndProcessDefinitionId(messageName: String, processDefinitionId: String): ProcessInstance {
    TODO("Not yet implemented")
  }

  override fun startProcessInstanceByMessageAndProcessDefinitionId(messageName: String, processDefinitionId: String, businessKey: String?): ProcessInstance {
    TODO("Not yet implemented")
  }

  override fun startProcessInstanceByMessageAndProcessDefinitionId(messageName: String, processDefinitionId: String, processVariables: MutableMap<String, Any>?): ProcessInstance {
    TODO("Not yet implemented")
  }

  override fun startProcessInstanceByMessageAndProcessDefinitionId(messageName: String, processDefinitionId: String, businessKey: String?, processVariables: MutableMap<String, Any>?): ProcessInstance {
    TODO("Not yet implemented")
  }

  /**
   * @since 7.14
   */
  override fun setVariablesAsync(processInstanceIds: MutableList<String>?, processInstanceQuery: ProcessInstanceQuery?, historicProcessInstanceQuery: HistoricProcessInstanceQuery?, processVariables: MutableMap<String, *>?): Batch {
    TODO("Not yet implemented")
  }

  override fun setVariablesAsync(processInstanceIds: MutableList<String>, processVariables: MutableMap<String, *>?): Batch =
    setVariablesAsync(processInstanceIds = processInstanceIds, processInstanceQuery = null, historicProcessInstanceQuery = null, processVariables = processVariables)

  override fun setVariablesAsync(processInstanceQuery: ProcessInstanceQuery?, processVariables: MutableMap<String, *>?): Batch =
    setVariablesAsync(processInstanceIds = null, processInstanceQuery = processInstanceQuery, historicProcessInstanceQuery = null, processVariables = processVariables)

  override fun setVariablesAsync(historicProcessInstanceQuery: HistoricProcessInstanceQuery?, processVariables: MutableMap<String, *>?): Batch =
    setVariablesAsync(processInstanceIds = null, processInstanceQuery = null, historicProcessInstanceQuery = historicProcessInstanceQuery, processVariables = processVariables)

  /**
   * @since 7.15
   */
  override fun setAnnotationForIncidentById(incidentId: String, annotation: String) {
    implementedBy(RemoteRuntimeService::class)
  }

  override fun clearAnnotationForIncidentById(incidentId: String) {
    implementedBy(RemoteRuntimeService::class)
  }

  /**
   * @since 7.16
   */
  override fun createMessageCorrelationAsync(messageName: String?): MessageCorrelationAsyncBuilder {
    TODO("Not yet implemented")
  }

  /**
   * @since 7.21
   */
  override fun deleteProcessInstances(processInstanceIds: MutableList<String>, deleteReason: String?, skipCustomListeners: Boolean,
                                      externallyTerminated: Boolean, skipSubprocesses: Boolean, skipIoMappings: Boolean) {
    implementedBy(RemoteRuntimeService::class)
  }

  override fun deleteProcessInstancesAsync(processInstanceIds: MutableList<String>?, processInstanceQuery: ProcessInstanceQuery?,
                                           historicProcessInstanceQuery: HistoricProcessInstanceQuery?, deleteReason: String?,
                                           skipCustomListeners: Boolean, skipSubprocesses: Boolean, skipIoMappings: Boolean): Batch {
    implementedBy(RemoteRuntimeService::class)
  }

}
