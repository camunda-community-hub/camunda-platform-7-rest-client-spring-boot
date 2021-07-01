package org.camunda.bpm.extension.rest.impl

import com.fasterxml.jackson.databind.ObjectMapper
import mu.KLogging
import org.camunda.bpm.engine.ProcessEngine
import org.camunda.bpm.engine.rest.dto.PatchVariablesDto
import org.camunda.bpm.engine.rest.dto.task.*
import org.camunda.bpm.engine.task.IdentityLink
import org.camunda.bpm.engine.task.Task
import org.camunda.bpm.engine.task.TaskQuery
import org.camunda.bpm.engine.variable.VariableMap
import org.camunda.bpm.engine.variable.Variables.createVariables
import org.camunda.bpm.engine.variable.value.TypedValue
import org.camunda.bpm.extension.rest.adapter.*
import org.camunda.bpm.extension.rest.client.TaskServiceClient
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
  private val taskServiceClient: TaskServiceClient,
  processEngine: ProcessEngine,
  objectMapper: ObjectMapper
) : AbstractTaskServiceAdapter() {

  companion object : KLogging()

  private val valueMapper: ValueMapper = ValueMapper(processEngine, objectMapper)

  override fun createTaskQuery(): TaskQuery {
    return DelegatingTaskQuery(taskServiceClient)
  }

  override fun claim(taskId: String, userId: String) {
    taskServiceClient.claimTask(taskId, UserIdDto().apply { this.userId = userId })
  }

  override fun deleteTask(taskId: String) {
    taskServiceClient.deleteTask(taskId)
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
      taskServiceClient.deleteTask(taskId)
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
    taskServiceClient.delegateTask(taskId, UserIdDto().apply { this.userId = userId })
  }

  override fun resolveTask(taskId: String) {
    taskServiceClient.resolveTask(taskId, CompleteTaskDto())
  }

  override fun resolveTask(taskId: String, variables: MutableMap<String, Any>) {
    taskServiceClient.resolveTask(taskId, CompleteTaskDto().apply {
      this.variables = valueMapper.mapValues(variables)
    })
  }

  override fun complete(taskId: String) {
    taskServiceClient.completeTask(taskId, CompleteTaskDto())
  }

  override fun complete(taskId: String, variables: MutableMap<String, Any>) {
    taskServiceClient.completeTask(taskId, CompleteTaskDto().apply {
      this.variables = valueMapper.mapValues(variables)
    })
  }

  override fun saveTask(task: Task) {
    taskServiceClient.updateTask(task.id, TaskDto.fromEntity(task))
  }

  override fun setAssignee(taskId: String, userId: String?) {
    taskServiceClient.setTaskAssignee(taskId, UserIdDto().apply { this.userId = userId })
  }

  override fun setOwner(taskId: String, userId: String?) {
    val task = taskServiceClient.getTask(taskId)
    taskServiceClient.updateTask(taskId, task.apply { this.owner = userId })
  }

  override fun setPriority(taskId: String, priority: Int) {
    val task = taskServiceClient.getTask(taskId)
    taskServiceClient.updateTask(taskId, task.apply { this.priority = priority })
  }

  // Variable handling.

  override fun setVariable(taskId: String, variableName: String, value: Any?) {
    return taskServiceClient.setVariable(taskId, variableName, valueMapper.mapValue(value))
  }

  override fun setVariables(taskId: String, variables: MutableMap<String, out Any>) {
    return taskServiceClient.changeVariables(taskId, PatchVariablesDto().apply {
      modifications = valueMapper.mapValues(variables)
    })
  }

  override fun setVariableLocal(taskId: String, variableName: String, value: Any?) {
    return taskServiceClient.setVariableLocal(taskId, variableName, valueMapper.mapValue(value))
  }

  override fun setVariablesLocal(taskId: String, variables: MutableMap<String, out Any>) {
    return taskServiceClient.changeVariablesLocal(taskId, PatchVariablesDto().apply {
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
    val dto = taskServiceClient.getVariable(taskId, variableName, deserializeValue)
    return valueMapper.mapDto(dto, deserializeValue)
  }

  override fun getVariableLocal(taskId: String, variableName: String): Any? {
    return getVariableLocalTyped<TypedValue>(taskId, variableName)?.value
  }

  override fun <T : TypedValue> getVariableLocalTyped(taskId: String, variableName: String): T? {
    return getVariableLocalTyped(taskId, variableName, true)
  }

  override fun <T : TypedValue> getVariableLocalTyped(taskId: String, variableName: String, deserializeValue: Boolean): T? {
    val dto = taskServiceClient.getVariableLocal(taskId, variableName, true)
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
    val variables = taskServiceClient.getVariables(taskId, deserializeValues)
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
    val variables = taskServiceClient.getVariablesLocal(taskId, deserializeValues)
      .filter { variableNames.isEmpty() || variableNames.contains(it.key) }
    return valueMapper.mapDtos(variables, deserializeValues)
  }

  override fun removeVariable(taskId: String, variableName: String) {
    taskServiceClient.deleteVariable(taskId, variableName)
  }

  override fun removeVariableLocal(taskId: String, variableName: String) {
    taskServiceClient.deleteVariableLocal(taskId, variableName)
  }

  override fun removeVariables(taskId: String, variableNames: MutableCollection<String>) {
    return taskServiceClient.changeVariables(taskId, PatchVariablesDto().apply {
      deletions = variableNames.toList()
    })
  }

  override fun removeVariablesLocal(taskId: String, variableNames: MutableCollection<String>) {
    return taskServiceClient.changeVariablesLocal(taskId, PatchVariablesDto().apply {
      deletions = variableNames.toList()
    })
  }

  // Identity links.

  override fun getIdentityLinksForTask(taskId: String): MutableList<IdentityLink> {
    return taskServiceClient.getIdentityLinks(taskId, null).map { IdentityLinkAdapter(IdentityLinkBean.fromDto(taskId, it)) }
      .toMutableList()
  }

  override fun addCandidateUser(taskId: String, userId: String) {
    taskServiceClient.addIdentityLink(
      taskId, IdentityLinkDto.fromIdentityLink(
        IdentityLinkAdapter(CandidateUserLinkBean(userId = userId, taskId = taskId))
      )
    )
  }

  override fun addCandidateGroup(taskId: String, groupId: String) {
    taskServiceClient.addIdentityLink(
      taskId, IdentityLinkDto.fromIdentityLink(
        IdentityLinkAdapter(GroupLinkBean(groupId = groupId, taskId = taskId))
      )
    )
  }

  override fun addUserIdentityLink(taskId: String, userId: String, identityLinkType: String) {
    taskServiceClient.addIdentityLink(
      taskId,
      IdentityLinkDto().apply {
        this.userId = userId
        this.type = identityLinkType
      }
    )
  }

  override fun addGroupIdentityLink(taskId: String, groupId: String, identityLinkType: String) {
    taskServiceClient.addIdentityLink(
      taskId,
      IdentityLinkDto().apply {
        this.groupId = groupId
        this.type = identityLinkType
      }
    )
  }

  override fun deleteCandidateUser(taskId: String, userId: String) {
    taskServiceClient.deleteIdentityLink(
      taskId,
      IdentityLinkDto.fromIdentityLink(
        IdentityLinkAdapter(CandidateUserLinkBean(userId = userId, taskId = taskId))
      )
    )
  }

  override fun deleteCandidateGroup(taskId: String, groupId: String) {
    taskServiceClient.deleteIdentityLink(
      taskId,
      IdentityLinkDto.fromIdentityLink(
        IdentityLinkAdapter(GroupLinkBean(groupId = groupId, taskId = taskId))
      )
    )
  }

  override fun deleteUserIdentityLink(taskId: String, userId: String, identityLinkType: String) {
    taskServiceClient.deleteIdentityLink(
      taskId,
      IdentityLinkDto().apply {
        this.userId = userId
        this.type = identityLinkType
      }
    )
  }

  override fun deleteGroupIdentityLink(taskId: String, groupId: String, identityLinkType: String) {
    taskServiceClient.deleteIdentityLink(
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
    taskServiceClient.handleBpmnError(taskId, TaskBpmnErrorDto().apply {
      this.errorCode = errorCode
      this.errorMessage = errorMessage
      this.variables = valueMapper.mapValues(variables)
    })
  }

  override fun handleEscalation(taskId: String, escalationCode: String) {
    handleEscalation(taskId, escalationCode, createVariables())
  }

  override fun handleEscalation(taskId: String, escalationCode: String, variables: MutableMap<String, Any>) {
    taskServiceClient.handleBpmnEscalation(taskId, TaskEscalationDto().apply {
      this.escalationCode = escalationCode
      this.variables = valueMapper.mapValues(variables)
    })
  }
}
