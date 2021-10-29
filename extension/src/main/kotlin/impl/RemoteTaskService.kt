package org.camunda.bpm.extension.rest.impl

import com.fasterxml.jackson.databind.ObjectMapper
import mu.KLogging
import org.camunda.bpm.engine.ProcessEngine
import org.camunda.bpm.engine.task.IdentityLink
import org.camunda.bpm.engine.task.Task
import org.camunda.bpm.engine.task.TaskQuery
import org.camunda.bpm.engine.variable.VariableMap
import org.camunda.bpm.engine.variable.Variables.createVariables
import org.camunda.bpm.engine.variable.value.TypedValue
import org.camunda.bpm.extension.rest.adapter.*
import org.camunda.bpm.extension.rest.client.api.TaskApiClient
import org.camunda.bpm.extension.rest.client.api.TaskIdentityLinkApiClient
import org.camunda.bpm.extension.rest.client.api.TaskLocalVariableApiClient
import org.camunda.bpm.extension.rest.client.api.TaskVariableApiClient
import org.camunda.bpm.extension.rest.client.model.*
import org.camunda.bpm.extension.rest.impl.query.DelegatingTaskQuery
import org.camunda.bpm.extension.rest.variables.ValueMapper
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

/**
 * Remote implementation of Camunda Core TaskService API, delegating
 * all request over HTTP to a remote Camunda Engine.
 */
@Component
@Qualifier("remote")
class RemoteTaskService(
  private val taskApiClient: TaskApiClient,
  private val taskVariableApiClient: TaskVariableApiClient,
  private val taskLocalVariableApiClient: TaskLocalVariableApiClient,
  private val identityLinkApiClient: TaskIdentityLinkApiClient,
  processEngine: ProcessEngine,
  objectMapper: ObjectMapper
) : AbstractTaskServiceAdapter() {

  companion object : KLogging()

  private val valueMapper: ValueMapper = ValueMapper(processEngine, objectMapper)

  override fun createTaskQuery(): TaskQuery {
    return DelegatingTaskQuery(taskApiClient)
  }

  override fun claim(taskId: String, userId: String) {
    taskApiClient.claim(taskId, UserIdDto().apply { this.userId = userId })
  }

  override fun deleteTask(taskId: String) {
    taskApiClient.deleteTask(taskId)
  }

  override fun deleteTask(taskId: String, cascade: Boolean) {
    if (cascade) {
      logger.warn { "Cascade delete is not supported via REST API. The task will be deleted, but the cascade flag is ignored." }
    }
    this.deleteTask(taskId)
  }

  override fun deleteTask(taskId: String, deleteReason: String?) {
    if (deleteReason != null) {
      logger.warn { "Storage of the delete reason is not supported via REST API. The task will be deleted, but the deletion reason is ignored." }
    }
    this.deleteTask(taskId)
  }

  override fun deleteTasks(taskIds: MutableCollection<String>) {
    taskIds.forEach { taskId ->
      taskApiClient.deleteTask(taskId)
    }
  }

  override fun deleteTasks(taskIds: MutableCollection<String>, cascade: Boolean) {
    if (cascade) {
      logger.warn { "Cascade delete is not supported via REST API. The task will be deleted, but the cascade flag is ignored." }
    }
    this.deleteTasks(taskIds)
  }

  override fun deleteTasks(taskIds: MutableCollection<String>, deleteReason: String?) {
    if (deleteReason != null) {
      logger.warn { "Storage of the delete reason is not supported via REST API. The task will be deleted, but the deletion reason is ignored." }
    }
    this.deleteTasks(taskIds)
  }

  override fun delegateTask(taskId: String, userId: String) {
    taskApiClient.delegateTask(taskId, UserIdDto().apply { this.userId = userId })
  }

  override fun resolveTask(taskId: String) {
    taskApiClient.resolve(taskId, CompleteTaskDto())
  }

  override fun resolveTask(taskId: String, variables: MutableMap<String, Any>) {
    taskApiClient.resolve(taskId, CompleteTaskDto().apply {
      this.variables = valueMapper.mapValues(variables)
    })
  }

  override fun complete(taskId: String) {
    taskApiClient.complete(taskId, CompleteTaskDto())
  }

  override fun complete(taskId: String, variables: MutableMap<String, Any>) {
    taskApiClient.complete(taskId, CompleteTaskDto().apply {
      this.variables = valueMapper.mapValues(variables)
    })
  }

  override fun saveTask(task: Task) {
    taskApiClient.updateTask(task.id, task.toDto())
  }

  override fun setAssignee(taskId: String, userId: String?) {
    taskApiClient.setAssignee(taskId, UserIdDto().apply { this.userId = userId })
  }

  override fun setOwner(taskId: String, userId: String?) {
    val task = taskApiClient.getTask(taskId).body!!
    taskApiClient.updateTask(taskId, task.apply { this.owner = userId })
  }

  override fun setPriority(taskId: String, priority: Int) {
    val task = taskApiClient.getTask(taskId).body!!
    taskApiClient.updateTask(taskId, task.apply { this.priority = priority })
  }

  // Variable handling.

  override fun setVariable(taskId: String, variableName: String, value: Any?) {
    taskVariableApiClient.putTaskVariable(taskId, variableName, valueMapper.mapValue(value))
  }

  override fun setVariables(taskId: String, variables: MutableMap<String, out Any>) {
    taskVariableApiClient.modifyTaskVariables(taskId, PatchVariablesDto().apply {
      modifications = valueMapper.mapValues(variables)
    })
  }

  override fun setVariableLocal(taskId: String, variableName: String, value: Any?) {
    taskLocalVariableApiClient.putTaskLocalVariable(taskId, variableName, valueMapper.mapValue(value))
  }

  override fun setVariablesLocal(taskId: String, variables: MutableMap<String, out Any>) {
    taskLocalVariableApiClient.modifyTaskLocalVariables(taskId, PatchVariablesDto().apply {
      modifications = valueMapper.mapValues(variables)
    })
  }

  override fun getVariable(taskId: String, variableName: String): Any? {
    return getVariableTyped<TypedValue>(taskId, variableName)?.value
  }

  override fun <T : TypedValue> getVariableTyped(taskId: String, variableName: String): T? {
    return getVariableTyped(taskId, variableName, true)
  }

  override fun <T : TypedValue> getVariableTyped(taskId: String, variableName: String, deserializeValue: Boolean): T? {
    val dto = taskVariableApiClient.getTaskVariable(taskId, variableName, deserializeValue).body!!
    return valueMapper.mapDto(dto, deserializeValue)
  }

  override fun getVariableLocal(taskId: String, variableName: String): Any? {
    return getVariableLocalTyped<TypedValue>(taskId, variableName)?.value
  }

  override fun <T : TypedValue> getVariableLocalTyped(taskId: String, variableName: String): T? {
    return getVariableLocalTyped(taskId, variableName, true)
  }

  override fun <T : TypedValue> getVariableLocalTyped(taskId: String, variableName: String, deserializeValue: Boolean): T? {
    val dto = taskLocalVariableApiClient.getTaskLocalVariable(taskId, variableName, true).body!!
    return valueMapper.mapDto(dto, deserializeValue)
  }

  override fun getVariables(taskId: String): MutableMap<String, Any> {
    return getVariablesTyped(taskId, true)
  }

  override fun getVariables(taskId: String, variableNames: MutableCollection<String>): MutableMap<String, Any> {
    return getVariablesTyped(taskId, variableNames, true)
  }

  override fun getVariablesTyped(taskId: String): VariableMap {
    return getVariablesTyped(taskId, true)
  }

  override fun getVariablesTyped(taskId: String, deserializeValues: Boolean): VariableMap {
    return getVariablesTyped(taskId, mutableListOf(), deserializeValues)
  }

  override fun getVariablesTyped(taskId: String, variableNames: MutableCollection<String>, deserializeValues: Boolean): VariableMap {
    val variables = taskVariableApiClient.getTaskVariables(taskId, deserializeValues).body!!
      .filter { variableNames.isEmpty() || variableNames.contains(it.key) }
    return valueMapper.mapDtos(variables, deserializeValues)
  }

  override fun getVariablesLocal(taskId: String): MutableMap<String, Any> {
    return getVariablesLocalTyped(taskId, true)
  }

  override fun getVariablesLocal(taskId: String, variableNames: MutableCollection<String>): MutableMap<String, Any> {
    return getVariablesLocalTyped(taskId, variableNames, true)
  }

  override fun getVariablesLocalTyped(taskId: String): VariableMap {
    return getVariablesLocalTyped(taskId, true)
  }

  override fun getVariablesLocalTyped(taskId: String, deserializeValues: Boolean): VariableMap {
    return getVariablesLocalTyped(taskId, mutableListOf(), true)
  }

  override fun getVariablesLocalTyped(taskId: String, variableNames: MutableCollection<String>, deserializeValues: Boolean): VariableMap {
    val variables = taskLocalVariableApiClient.getTaskLocalVariables(taskId, deserializeValues).body!!
      .filter { variableNames.isEmpty() || variableNames.contains(it.key) }
    return valueMapper.mapDtos(variables, deserializeValues)
  }

  override fun removeVariable(taskId: String, variableName: String) {
    taskVariableApiClient.deleteTaskVariable(taskId, variableName)
  }

  override fun removeVariableLocal(taskId: String, variableName: String) {
    taskLocalVariableApiClient.deleteTaskLocalVariable(taskId, variableName)
  }

  override fun removeVariables(taskId: String, variableNames: MutableCollection<String>) {
    taskVariableApiClient.modifyTaskVariables(taskId, PatchVariablesDto().apply {
      deletions = variableNames.toList()
    })
  }

  override fun removeVariablesLocal(taskId: String, variableNames: MutableCollection<String>) {
    taskLocalVariableApiClient.modifyTaskLocalVariables(taskId, PatchVariablesDto().apply {
      deletions = variableNames.toList()
    })
  }

  // Identity links.

  override fun getIdentityLinksForTask(taskId: String): MutableList<IdentityLink> {
    return identityLinkApiClient.getIdentityLinks(taskId, null).body!!.map { IdentityLinkAdapter(IdentityLinkBean.fromDto(taskId, it)) }
      .toMutableList()
  }

  override fun addCandidateUser(taskId: String, userId: String) {
    identityLinkApiClient.addIdentityLink(
      taskId, IdentityLinkAdapter(CandidateUserLinkBean(userId = userId, taskId = taskId)).toDto()
    )
  }

  override fun addCandidateGroup(taskId: String, groupId: String) {
    identityLinkApiClient.addIdentityLink(
      taskId, IdentityLinkAdapter(GroupLinkBean(groupId = groupId, taskId = taskId)).toDto()
    )
  }

  override fun addUserIdentityLink(taskId: String, userId: String, identityLinkType: String) {
    identityLinkApiClient.addIdentityLink(
      taskId,
      IdentityLinkDto().apply {
        this.userId = userId
        this.type = identityLinkType
      }
    )
  }

  override fun addGroupIdentityLink(taskId: String, groupId: String, identityLinkType: String) {
    identityLinkApiClient.addIdentityLink(
      taskId,
      IdentityLinkDto().apply {
        this.groupId = groupId
        this.type = identityLinkType
      }
    )
  }

  override fun deleteCandidateUser(taskId: String, userId: String) {
    identityLinkApiClient.deleteIdentityLink(
      taskId,
      IdentityLinkAdapter(CandidateUserLinkBean(userId = userId, taskId = taskId)).toDto()
    )
  }

  override fun deleteCandidateGroup(taskId: String, groupId: String) {
    identityLinkApiClient.deleteIdentityLink(
      taskId,
      IdentityLinkAdapter(GroupLinkBean(groupId = groupId, taskId = taskId)).toDto()
    )
  }

  override fun deleteUserIdentityLink(taskId: String, userId: String, identityLinkType: String) {
    identityLinkApiClient.deleteIdentityLink(
      taskId,
      IdentityLinkDto().apply {
        this.userId = userId
        this.type = identityLinkType
      }
    )
  }

  override fun deleteGroupIdentityLink(taskId: String, groupId: String, identityLinkType: String) {
    identityLinkApiClient.deleteIdentityLink(
      taskId,
      IdentityLinkDto().apply {
        this.groupId = groupId
        this.type = identityLinkType
      }
    )
  }

  override fun handleBpmnError(taskId: String, errorCode: String) {
    handleBpmnError(taskId, errorCode, null)
  }

  override fun handleBpmnError(taskId: String, errorCode: String, errorMessage: String?) {
    handleBpmnError(taskId, errorCode, errorMessage, createVariables())
  }

  override fun handleBpmnError(taskId: String, errorCode: String, errorMessage: String?, variables: MutableMap<String, Any>) {
    taskApiClient.handleBpmnError(taskId, TaskBpmnErrorDto().apply {
      this.errorCode = errorCode
      this.errorMessage = errorMessage
      this.variables = valueMapper.mapValues(variables)
    })
  }

  override fun handleEscalation(taskId: String, escalationCode: String) {
    handleEscalation(taskId, escalationCode, createVariables())
  }

  override fun handleEscalation(taskId: String, escalationCode: String, variables: MutableMap<String, Any>) {
    taskApiClient.handleEscalation(taskId, TaskEscalationDto().apply {
      this.escalationCode = escalationCode
      this.variables = valueMapper.mapValues(variables)
    })
  }
}
