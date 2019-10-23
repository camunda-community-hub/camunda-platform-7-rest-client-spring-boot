package org.camunda.bpm.extension.feign.adapter

import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.batch.Batch
import org.camunda.bpm.engine.migration.MigrationPlan
import org.camunda.bpm.engine.migration.MigrationPlanBuilder
import org.camunda.bpm.engine.migration.MigrationPlanExecutionBuilder
import org.camunda.bpm.engine.runtime.*
import org.camunda.bpm.engine.variable.VariableMap
import org.camunda.bpm.engine.variable.value.TypedValue
import org.camunda.bpm.extension.feign.impl.RemoteRuntimeService
import org.camunda.bpm.extension.feign.impl.implementedBy

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
    TODO("not implemented")
  }
  override fun getActivityInstance(processInstanceId: String?): ActivityInstance {
    TODO("not implemented")
  }


  override fun createConditionEvaluation(): ConditionEvaluationBuilder {
    TODO("not implemented")
  }

  override fun resolveIncident(incidentId: String?) {
    TODO("not implemented")
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
    TODO("not implemented")
  }

  override fun createIncident(incidentType: String?, executionId: String?, configuration: String?, message: String?): Incident {
    TODO("not implemented")
  }

  override fun createMigrationPlan(sourceProcessDefinitionId: String?, targetProcessDefinitionId: String?): MigrationPlanBuilder {
    TODO("not implemented")
  }

  override fun deleteProcessInstancesAsync(processInstanceIds: MutableList<String>?, processInstanceQuery: ProcessInstanceQuery?, deleteReason: String?): Batch {
    TODO("not implemented")
  }

  override fun deleteProcessInstancesAsync(processInstanceIds: MutableList<String>?, processInstanceQuery: ProcessInstanceQuery?, deleteReason: String?, skipCustomListeners: Boolean): Batch {
    TODO("not implemented")
  }

  override fun deleteProcessInstancesAsync(processInstanceIds: MutableList<String>?, processInstanceQuery: ProcessInstanceQuery?, deleteReason: String?, skipCustomListeners: Boolean, skipSubprocesses: Boolean): Batch {
    TODO("not implemented")
  }

  override fun deleteProcessInstancesAsync(processInstanceQuery: ProcessInstanceQuery?, deleteReason: String?): Batch {
    TODO("not implemented")
  }

  override fun deleteProcessInstancesAsync(processInstanceIds: MutableList<String>?, deleteReason: String?): Batch {
    TODO("not implemented")
  }

  override fun deleteProcessInstanceIfExists(processInstanceId: String?, deleteReason: String?, skipCustomListeners: Boolean, externallyTerminated: Boolean, skipIoMappings: Boolean, skipSubprocesses: Boolean) {
    TODO("not implemented")
  }

  override fun suspendProcessInstanceById(processInstanceId: String?) {
    TODO("not implemented")
  }

  override fun deleteProcessInstances(processInstanceIds: MutableList<String>?, deleteReason: String?, skipCustomListeners: Boolean, externallyTerminated: Boolean) {
    TODO("not implemented")
  }

  override fun deleteProcessInstances(processInstanceIds: MutableList<String>?, deleteReason: String?, skipCustomListeners: Boolean, externallyTerminated: Boolean, skipSubprocesses: Boolean) {
    TODO("not implemented")
  }

  override fun activateProcessInstanceById(processInstanceId: String?) {
    TODO("not implemented")
  }

  override fun suspendProcessInstanceByProcessDefinitionKey(processDefinitionKey: String?) {
    TODO("not implemented")
  }

  override fun createProcessInstanceModification(processInstanceId: String?): ProcessInstanceModificationBuilder {
    TODO("not implemented")
  }

  override fun activateProcessInstanceByProcessDefinitionId(processDefinitionId: String?) {
    TODO("not implemented")
  }

  override fun createNativeProcessInstanceQuery(): NativeProcessInstanceQuery {
    TODO("not implemented")
  }

  override fun suspendProcessInstanceByProcessDefinitionId(processDefinitionId: String?) {
    TODO("not implemented")
  }

  override fun createProcessInstanceQuery(): ProcessInstanceQuery {
    TODO("not implemented")
  }

  override fun activateProcessInstanceByProcessDefinitionKey(processDefinitionKey: String?) {
    TODO("not implemented")
  }

  override fun deleteProcessInstancesIfExists(processInstanceIds: MutableList<String>?, deleteReason: String?, skipCustomListeners: Boolean, externallyTerminated: Boolean, skipSubprocesses: Boolean) {
    TODO("not implemented")
  }

  override fun createProcessInstanceById(processDefinitionId: String?): ProcessInstantiationBuilder {
    TODO("not implemented")
  }

  override fun createProcessInstanceByKey(processDefinitionKey: String): ProcessInstantiationBuilder {
    TODO("not implemented")
  }

  override fun deleteProcessInstance(processInstanceId: String?, deleteReason: String?) {
    TODO("not implemented")
  }

  override fun deleteProcessInstance(processInstanceId: String?, deleteReason: String?, skipCustomListeners: Boolean) {
    TODO("not implemented")
  }

  override fun deleteProcessInstance(processInstanceId: String?, deleteReason: String?, skipCustomListeners: Boolean, externallyTerminated: Boolean) {
    TODO("not implemented")
  }

  override fun deleteProcessInstance(processInstanceId: String?, deleteReason: String?, skipCustomListeners: Boolean, externallyTerminated: Boolean, skipIoMappings: Boolean) {
    TODO("not implemented")
  }

  override fun deleteProcessInstance(processInstanceId: String?, deleteReason: String?, skipCustomListeners: Boolean, externallyTerminated: Boolean, skipIoMappings: Boolean, skipSubprocesses: Boolean) {
    TODO("not implemented")
  }

  override fun restartProcessInstances(processDefinitionId: String?): RestartProcessInstanceBuilder {
    TODO("not implemented")
  }

  override fun createModification(processDefinitionId: String?): ModificationBuilder {
    TODO("not implemented")
  }

  override fun createExecutionQuery(): ExecutionQuery {
    TODO("not implemented")
  }

  override fun createEventSubscriptionQuery(): EventSubscriptionQuery {
    TODO("not implemented")
  }

  override fun createIncidentQuery(): IncidentQuery {
    TODO("not implemented")
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

  override fun startProcessInstanceByMessage(messageName: String?): ProcessInstance {
    TODO("not implemented")
  }

  override fun startProcessInstanceByMessage(messageName: String?, businessKey: String?): ProcessInstance {
    TODO("not implemented")
  }

  override fun startProcessInstanceByMessage(messageName: String?, processVariables: MutableMap<String, Any>?): ProcessInstance {
    TODO("not implemented")
  }

  override fun startProcessInstanceByMessage(messageName: String?, businessKey: String?, processVariables: MutableMap<String, Any>?): ProcessInstance {
    TODO("not implemented")
  }

  override fun startProcessInstanceByMessageAndProcessDefinitionId(messageName: String?, processDefinitionId: String?): ProcessInstance {
    TODO("not implemented")
  }

  override fun startProcessInstanceByMessageAndProcessDefinitionId(messageName: String?, processDefinitionId: String?, businessKey: String?): ProcessInstance {
    TODO("not implemented")
  }

  override fun startProcessInstanceByMessageAndProcessDefinitionId(messageName: String?, processDefinitionId: String?, processVariables: MutableMap<String, Any>?): ProcessInstance {
    TODO("not implemented")
  }

  override fun startProcessInstanceByMessageAndProcessDefinitionId(messageName: String?, processDefinitionId: String?, businessKey: String?, processVariables: MutableMap<String, Any>?): ProcessInstance {
    TODO("not implemented")
  }

}
