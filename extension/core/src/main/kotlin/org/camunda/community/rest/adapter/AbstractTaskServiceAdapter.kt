/*-
 * #%L
 * camunda-platform-7-rest-client-spring-boot
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
package org.camunda.community.rest.adapter

import org.camunda.bpm.engine.TaskService
import org.camunda.bpm.engine.task.*
import org.camunda.bpm.engine.variable.VariableMap
import org.camunda.bpm.engine.variable.value.TypedValue
import org.camunda.community.rest.impl.RemoteTaskService
import org.camunda.community.rest.impl.implementedBy
import java.io.InputStream
import java.util.*

/**
 * Adapter for implementing task service.
 */
abstract class AbstractTaskServiceAdapter : TaskService {
  override fun newTask(): Task {
    TODO("Not yet implemented")
  }

  override fun newTask(taskId: String): Task {
    TODO("Not yet implemented")
  }

  override fun saveTask(task: Task) {
    implementedBy(RemoteTaskService::class)
  }

  override fun deleteTask(taskId: String) {
    implementedBy(RemoteTaskService::class)
  }

  override fun deleteTask(taskId: String, cascade: Boolean) {
    implementedBy(RemoteTaskService::class)
  }

  override fun deleteTask(taskId: String, deleteReason: String?) {
    implementedBy(RemoteTaskService::class)
  }

  override fun deleteTasks(taskIds: MutableCollection<String>) {
    implementedBy(RemoteTaskService::class)
  }

  override fun deleteTasks(taskIds: MutableCollection<String>, cascade: Boolean) {
    implementedBy(RemoteTaskService::class)
  }

  override fun deleteTasks(taskIds: MutableCollection<String>, deleteReason: String?) {
    implementedBy(RemoteTaskService::class)
  }

  override fun claim(taskId: String, userId: String) {
    implementedBy(RemoteTaskService::class)
  }

  override fun complete(taskId: String) {
    implementedBy(RemoteTaskService::class)
  }

  override fun complete(taskId: String, variables: MutableMap<String, Any>) {
    implementedBy(RemoteTaskService::class)
  }

  override fun delegateTask(taskId: String, userId: String) {
    implementedBy(RemoteTaskService::class)
  }

  override fun resolveTask(taskId: String) {
    implementedBy(RemoteTaskService::class)
  }

  override fun resolveTask(taskId: String, variables: MutableMap<String, Any>) {
    implementedBy(RemoteTaskService::class)
  }

  override fun completeWithVariablesInReturn(
    taskId: String,
    variables: MutableMap<String, Any>?,
    deserializeValues: Boolean
  ): VariableMap {
    TODO("Not yet implemented")
  }

  override fun setAssignee(taskId: String, userId: String?) {
    implementedBy(RemoteTaskService::class)
  }

  override fun setOwner(taskId: String, userId: String?) {
    implementedBy(RemoteTaskService::class)
  }

  override fun getIdentityLinksForTask(taskId: String): MutableList<IdentityLink> {
    implementedBy(RemoteTaskService::class)
  }

  override fun addCandidateUser(taskId: String, userId: String) {
    implementedBy(RemoteTaskService::class)
  }

  override fun addCandidateGroup(taskId: String, groupId: String) {
    implementedBy(RemoteTaskService::class)
  }

  override fun addUserIdentityLink(taskId: String, userId: String, identityLinkType: String) {
    implementedBy(RemoteTaskService::class)
  }

  override fun addGroupIdentityLink(taskId: String, groupId: String, identityLinkType: String) {
    implementedBy(RemoteTaskService::class)
  }

  override fun deleteCandidateUser(taskId: String, userId: String) {
    implementedBy(RemoteTaskService::class)
  }

  override fun deleteCandidateGroup(taskId: String, groupId: String) {
    implementedBy(RemoteTaskService::class)
  }

  override fun deleteUserIdentityLink(taskId: String, userId: String, identityLinkType: String) {
    implementedBy(RemoteTaskService::class)
  }

  override fun deleteGroupIdentityLink(taskId: String, groupId: String, identityLinkType: String) {
    implementedBy(RemoteTaskService::class)
  }

  override fun setPriority(taskId: String, priority: Int) {
    implementedBy(RemoteTaskService::class)
  }

  override fun createTaskQuery(): TaskQuery {
    implementedBy(RemoteTaskService::class)
  }

  override fun createNativeTaskQuery(): NativeTaskQuery {
    TODO("Not yet implemented")
  }

  override fun setVariable(taskId: String, variableName: String, value: Any?) {
    implementedBy(RemoteTaskService::class)
  }

  override fun setVariables(taskId: String, variables: MutableMap<String, out Any>) {
    implementedBy(RemoteTaskService::class)
  }

  override fun setVariableLocal(taskId: String, variableName: String, value: Any?) {
    implementedBy(RemoteTaskService::class)
  }

  override fun setVariablesLocal(taskId: String, variables: MutableMap<String, out Any>) {
    implementedBy(RemoteTaskService::class)
  }

  override fun getVariable(taskId: String, variableName: String): Any? {
    implementedBy(RemoteTaskService::class)
  }

  override fun <T : TypedValue> getVariableTyped(taskId: String, variableName: String): T? {
    implementedBy(RemoteTaskService::class)
  }

  override fun <T : TypedValue> getVariableTyped(taskId: String, variableName: String, deserializeValue: Boolean): T? {
    implementedBy(RemoteTaskService::class)
  }

  override fun getVariableLocal(taskId: String, variableName: String): Any? {
    implementedBy(RemoteTaskService::class)
  }

  override fun <T : TypedValue> getVariableLocalTyped(taskId: String, variableName: String): T? {
    implementedBy(RemoteTaskService::class)
  }

  override fun <T : TypedValue> getVariableLocalTyped(taskId: String, variableName: String, deserializeValue: Boolean): T? {
    implementedBy(RemoteTaskService::class)
  }

  override fun getVariables(taskId: String): MutableMap<String, Any> {
    implementedBy(RemoteTaskService::class)
  }

  override fun getVariables(taskId: String, variableNames: MutableCollection<String>): MutableMap<String, Any> {
    implementedBy(RemoteTaskService::class)
  }

  override fun getVariablesTyped(taskId: String): VariableMap {
    implementedBy(RemoteTaskService::class)
  }

  override fun getVariablesTyped(taskId: String, deserializeValues: Boolean): VariableMap {
    implementedBy(RemoteTaskService::class)
  }

  override fun getVariablesTyped(taskId: String, variableNames: MutableCollection<String>, deserializeValues: Boolean): VariableMap {
    implementedBy(RemoteTaskService::class)
  }

  override fun getVariablesLocal(taskId: String): MutableMap<String, Any> {
    implementedBy(RemoteTaskService::class)
  }

  override fun getVariablesLocal(taskId: String, variableNames: MutableCollection<String>): MutableMap<String, Any> {
    implementedBy(RemoteTaskService::class)
  }

  override fun getVariablesLocalTyped(taskId: String): VariableMap {
    implementedBy(RemoteTaskService::class)
  }

  override fun getVariablesLocalTyped(taskId: String, deserializeValues: Boolean): VariableMap {
    implementedBy(RemoteTaskService::class)
  }

  override fun getVariablesLocalTyped(taskId: String, variableNames: MutableCollection<String>, deserializeValues: Boolean): VariableMap {
    implementedBy(RemoteTaskService::class)
  }

  override fun removeVariable(taskId: String, variableName: String) {
    implementedBy(RemoteTaskService::class)
  }

  override fun removeVariableLocal(taskId: String, variableName: String) {
    implementedBy(RemoteTaskService::class)
  }

  override fun removeVariables(taskId: String, variableNames: MutableCollection<String>) {
    implementedBy(RemoteTaskService::class)
  }

  override fun removeVariablesLocal(taskId: String, variableNames: MutableCollection<String>) {
    implementedBy(RemoteTaskService::class)
  }

  override fun addComment(taskId: String, processInstanceId: String?, message: String?) {
    TODO("Not yet implemented")
  }

  override fun createComment(taskId: String, processInstanceId: String?, message: String?): Comment {
    TODO("Not yet implemented")
  }

  override fun getTaskComments(taskId: String): MutableList<Comment> {
    TODO("Not yet implemented")
  }

  override fun getTaskComment(taskId: String, commentId: String?): Comment {
    TODO("Not yet implemented")
  }

  @Suppress("DEPRECATION")
  override fun getTaskEvents(taskId: String): MutableList<Event> {
    TODO("Not yet implemented")
  }

  override fun getProcessInstanceComments(processInstanceId: String?): MutableList<Comment> {
    TODO("Not yet implemented")
  }

  override fun createAttachment(
    attachmentType: String?,
    taskId: String,
    processInstanceId: String?,
    attachmentName: String?,
    attachmentDescription: String?,
    content: InputStream?
  ): Attachment {
    TODO("Not yet implemented")
  }

  override fun createAttachment(
    attachmentType: String?,
    taskId: String,
    processInstanceId: String?,
    attachmentName: String?,
    attachmentDescription: String?,
    url: String?
  ): Attachment {
    TODO("Not yet implemented")
  }

  override fun saveAttachment(attachment: Attachment?) {
    TODO("Not yet implemented")
  }

  override fun getAttachment(attachmentId: String?): Attachment {
    TODO("Not yet implemented")
  }

  override fun getTaskAttachment(taskId: String, attachmentId: String?): Attachment {
    TODO("Not yet implemented")
  }

  override fun getAttachmentContent(attachmentId: String?): InputStream {
    TODO("Not yet implemented")
  }

  override fun getTaskAttachmentContent(taskId: String, attachmentId: String?): InputStream {
    TODO("Not yet implemented")
  }

  override fun getTaskAttachments(taskId: String): MutableList<Attachment> {
    TODO("Not yet implemented")
  }

  override fun getProcessInstanceAttachments(processInstanceId: String?): MutableList<Attachment> {
    TODO("Not yet implemented")
  }

  override fun deleteAttachment(attachmentId: String?) {
    TODO("Not yet implemented")
  }

  override fun deleteTaskAttachment(taskId: String, attachmentId: String?) {
    TODO("Not yet implemented")
  }

  override fun getSubTasks(parenttaskId: String): MutableList<Task> {
    TODO("Not yet implemented")
  }

  override fun createTaskReport(): TaskReport {
    TODO("Not yet implemented")
  }

  override fun handleBpmnError(taskId: String, errorCode: String) {
    implementedBy(RemoteTaskService::class)
  }

  override fun handleBpmnError(taskId: String, errorCode: String, errorMessage: String?) {
    implementedBy(RemoteTaskService::class)
  }

  override fun handleBpmnError(taskId: String, errorCode: String, errorMessage: String?, variables: MutableMap<String, Any>) {
    implementedBy(RemoteTaskService::class)
  }

  override fun handleEscalation(taskId: String, escalationCode: String) {
    implementedBy(RemoteTaskService::class)
  }

  override fun handleEscalation(taskId: String, escalationCode: String, variables: MutableMap<String, Any>) {
    implementedBy(RemoteTaskService::class)
  }

  // 7.20

  override fun setName(p0: String?, p1: String?) {
    TODO("Not yet implemented")
  }

  override fun setDescription(p0: String?, p1: String?) {
    TODO("Not yet implemented")
  }

  override fun setDueDate(p0: String?, p1: Date?) {
    TODO("Not yet implemented")
  }

  override fun setFollowUpDate(p0: String?, p1: Date?) {
    TODO("Not yet implemented")
  }

}
