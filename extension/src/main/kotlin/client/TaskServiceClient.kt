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
package org.camunda.bpm.extension.rest.client

import org.camunda.bpm.engine.rest.dto.CountResultDto
import org.camunda.bpm.engine.rest.dto.PatchVariablesDto
import org.camunda.bpm.engine.rest.dto.VariableValueDto
import org.camunda.bpm.engine.rest.dto.task.*
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.cloud.openfeign.SpringQueryMap
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam

/**
 * Feign client accessing the methods of runtime service.
 */
@FeignClient(name = "remoteTaskService", url = "\${feign.client.config.remoteTaskService.url}")
interface TaskServiceClient {

  /**
   * Gets a task by id.
   * @see https://docs.camunda.org/manual/latest/reference/rest/task/get/
   */
  @RequestMapping(method = [RequestMethod.GET], value = ["/task/{id}"], consumes = ["application/json"])
  fun getTask(@PathVariable("id") id: String): TaskDto

  /**
   * Claims a task by id.
   * @see https://docs.camunda.org/manual/latest/reference/rest/task/claim/
   */
  @RequestMapping(method = [RequestMethod.POST], value = ["/task/{id}/claim"], consumes = ["application/json"])
  fun claimTask(@PathVariable("id") id: String, userId: UserIdDto)

  /**
   * Unclaims a task by id.
   * @see https://docs.camunda.org/manual/latest/reference/rest/task/unclaim/
   */
  @RequestMapping(method = [RequestMethod.POST], value = ["/task/{id}/unclaim"], consumes = ["application/json"])
  fun unclaimTask(@PathVariable("id") id: String)

  /**
   * Delegates a task by id.
   * @see https://docs.camunda.org/manual/latest/reference/rest/task/delegate/
   */
  @RequestMapping(method = [RequestMethod.POST], value = ["/task/{id}/delegate"], consumes = ["application/json"])
  fun delegateTask(@PathVariable("id") id: String, userId: UserIdDto)

  /**
   * Sets a task assingee by id.
   * @see https://docs.camunda.org/manual/latest/reference/rest/task/assignee/
   */
  @RequestMapping(method = [RequestMethod.POST], value = ["/task/{id}/assignee"], consumes = ["application/json"])
  fun setTaskAssignee(@PathVariable("id") id: String, userId: UserIdDto)

  /**
   * Deletes a task by id.
   * @see https://docs.camunda.org/manual/latest/reference/rest/task/delete/
   */
  @RequestMapping(method = [RequestMethod.DELETE], value = ["/task/{id}"], consumes = ["application/json"])
  fun deleteTask(@PathVariable("id") id: String)

  /**
   * Updates a task by id.
   * @see https://docs.camunda.org/manual/latest/reference/rest/task/put-update/
   */
  @RequestMapping(method = [RequestMethod.PUT], value = ["/task/{id}"], consumes = ["application/json"])
  fun updateTask(@PathVariable("id") id: String, task: TaskDto)

  /**
   * Resolves a task by id.
   * @see https://docs.camunda.org/manual/latest/reference/rest/task/resolve/
   */
  @RequestMapping(method = [RequestMethod.POST], value = ["/task/{id}/resolve"], consumes = ["application/json"])
  fun resolveTask(@PathVariable("id") id: String, completeTaskDto: CompleteTaskDto)

  /**
   * Completes a task by id.
   * @see https://docs.camunda.org/manual/latest/reference/rest/task/complete/
   */
  @RequestMapping(method = [RequestMethod.POST], value = ["/task/{id}/complete"], consumes = ["application/json"])
  fun completeTask(@PathVariable("id") id: String, completeTaskDto: CompleteTaskDto)

  /**
   * Retrieves the list of tasks.
   * @see https://docs.camunda.org/manual/latest/reference/rest/task/get-query/
   */
  @RequestMapping(method = [RequestMethod.GET], value = ["/task"], consumes = ["application/json"])
  fun getTasks(
    @SpringQueryMap query: TaskQueryDto,
    @RequestParam("firstResult") firstResult: Int,
    @RequestParam("maxResults") maxResults: Int
  ): List<TaskDto>

  /**
   * Retrieves the list of tasks.
   * @see https://docs.camunda.org/manual/latest/reference/rest/task/get-query/
   */
  @RequestMapping(method = [RequestMethod.GET], value = ["/task/count"], consumes = ["application/json"])
  fun getTaskCount(
    @SpringQueryMap query: TaskQueryDto,
    @RequestParam("firstResult") firstResult: Int,
    @RequestParam("maxResults") maxResults: Int
  ): CountResultDto


  /**
   * Retrieves all variables of a given task by id.
   * @see https://docs.camunda.org/manual/latest/reference/rest/task/local-variables/get-local-variables/
   */
  @RequestMapping(method = [RequestMethod.GET], value = ["/task/{id}/localVariables"], consumes = ["application/json"])
  fun getVariablesLocal(
    @PathVariable("id") taskId: String,
    @RequestParam("deserializeValues") deserializeValues: Boolean
  ): Map<String, VariableValueDto>

  /**
   * Updates or deletes the variables in the context of a task. Updates precede deletions. So, if a variable is updated AND deleted, the deletion overrides the update.
   * @see https://docs.camunda.org/manual/latest/reference/rest/task/local-variables/post-local-variables/
   */
  @RequestMapping(method = [RequestMethod.POST], value = ["/task/{id}/localVariables"], consumes = ["application/json"])
  fun changeVariablesLocal(@PathVariable("id") taskId: String, patch: PatchVariablesDto)

  /**
   * Retrieves a variable from the context of a given task by id.
   * @see https://docs.camunda.org/manual/latest/reference/rest/task/local-variables/get-local-variable/
   */
  @RequestMapping(method = [RequestMethod.GET], value = ["/task/{id}/localVariables/{varName}"], consumes = ["application/json"])
  fun getVariableLocal(
    @PathVariable("id") taskId: String,
    @PathVariable("varName") varName: String,
    @RequestParam("deserializeValue") deserializeValue: Boolean
  ): VariableValueDto

  /**
   * Sets a variable in the context of a given task.
   * @see https://docs.camunda.org/manual/latest/reference/rest/task/local-variables/put-local-variable/
   */
  @RequestMapping(method = [RequestMethod.PUT], value = ["/task/{id}/localVariables/{varName}"], consumes = ["application/json"])
  fun setVariableLocal(@PathVariable("id") taskId: String, @PathVariable("varName") varName: String, value: VariableValueDto)

  /**
   * Removes a local variable from a task by id.
   * @see https://docs.camunda.org/manual/latest/reference/rest/task/local-variables/delete-local-variable/
   */
  @RequestMapping(method = [RequestMethod.DELETE], value = ["/task/{id}/localVariables/{varName}"], consumes = ["application/json"])
  fun deleteVariableLocal(@PathVariable("id") taskId: String, @PathVariable("varName") varName: String)

  /**
   * Retrieves all variables visible from the task. A variable is visible from the task if it is a local task variable or declared in a
   * parent scope of the task. See documentation on visibility of variables.
   * @see https://docs.camunda.org/manual/latest/reference/rest/task/variables/get-variables/
   */
  @RequestMapping(method = [RequestMethod.GET], value = ["/task/{id}/variables"], consumes = ["application/json"])
  fun getVariables(
    @PathVariable("id") taskId: String,
    @RequestParam("deserializeValues") deserializeValues: Boolean
  ): Map<String, VariableValueDto>

  /**
   * Updates or deletes the variables visible from the task. Updates precede deletions. So, if a variable is updated AND deleted, the
   * deletion overrides the update. A variable is visible from the task if it is a local task variable or declared in a parent scope
   * of the task. See documentation on visibility of variables.
   * @see https://docs.camunda.org/manual/latest/reference/rest/task/variables/post-variables/
   */
  @RequestMapping(method = [RequestMethod.POST], value = ["/task/{id}/variables"], consumes = ["application/json"])
  fun changeVariables(@PathVariable("id") taskId: String, patch: PatchVariablesDto)

  /**
   * Retrieves a variable from the context of a given task. The variable must be visible from the task. It is visible from the task if it
   * is a local task variable or declared in a parent scope of the task. See documentation on visibility of variables.
   * @see https://docs.camunda.org/manual/latest/reference/rest/task/variables/get-variable/
   */
  @RequestMapping(method = [RequestMethod.GET], value = ["/task/{id}/variables/{varName}"], consumes = ["application/json"])
  fun getVariable(
    @PathVariable("id") taskId: String,
    @PathVariable("varName") varName: String,
    @RequestParam("deserializeValue") deserializeValue: Boolean
  ): VariableValueDto

  /**
   * Updates a process variable that is visible from the Task scope. A variable is visible from the task if it is a local task variable,
   * or declared in a parent scope of the task. See the documentation on variable scopes and visibility.
   * Note: If a variable doesn’t exist, the variable is created in the top-most scope visible from the task.
   * @see https://docs.camunda.org/manual/latest/reference/rest/task/variables/put-variable/
   */
  @RequestMapping(method = [RequestMethod.PUT], value = ["/task/{id}/variables/{varName}"], consumes = ["application/json"])
  fun setVariable(@PathVariable("id") taskId: String, @PathVariable("varName") varName: String, value: VariableValueDto)

  /**
   * Removes a variable that is visible to a task. A variable is visible to a task if it is a local task variable or declared in a parent
   * scope of the task. See documentation on visiblity of variables.
   * @see https://docs.camunda.org/manual/latest/reference/rest/task/variables/delete-variable/
   */
  @RequestMapping(method = [RequestMethod.DELETE], value = ["/task/{id}/variables/{varName}"], consumes = ["application/json"])
  fun deleteVariable(@PathVariable("id") taskId: String, @PathVariable("varName") varName: String)


  /**
   * Gets the identity links for a task by id, which are the users and groups that are in some relation to it (including assignee and owner).
   * @see https://docs.camunda.org/manual/latest/reference/rest/task/variables/get-variable/
   */
  @RequestMapping(method = [RequestMethod.GET], value = ["/task/{id}/identity-links"], consumes = ["application/json"])
  fun getIdentityLinks(@PathVariable("id") taskId: String, @RequestParam("type") type: String?): List<IdentityLinkDto>

  /**
   * Adds an identity link to a task by id. Can be used to link any user or group to a task and specify a relation.
   * @see https://docs.camunda.org/manual/latest/reference/rest/task/identity-links/post-identity-link/
   */
  @RequestMapping(method = [RequestMethod.POST], value = ["/task/{id}/identity-links"], consumes = ["application/json"])
  fun addIdentityLink(@PathVariable("id") taskId: String, identityLink: IdentityLinkDto)

  /**
   * Removes an identity link from a task by id.
   * @see https://docs.camunda.org/manual/latest/reference/rest/task/identity-links/post-delete-identity-link/
   */
  @RequestMapping(method = [RequestMethod.POST], value = ["/task/{id}/identity-links/delete"], consumes = ["application/json"])
  fun deleteIdentityLink(@PathVariable("id") taskId: String, identityLink: IdentityLinkDto)


  /**
   * Reports a business error in the context of a running task by id. The error code must be specified to identify the BPMN error handler.
   * See the documentation for Reporting Bpmn Error in User Tasks.
   * @see https://docs.camunda.org/manual/latest/reference/rest/task/post-bpmn-error/
   */
  @RequestMapping(method = [RequestMethod.POST], value = ["/task/{id}/bpmnError"], consumes = ["application/json"])
  fun handleBpmnError(@PathVariable("id") taskId: String, bpmnError: TaskBpmnErrorDto)

  /**
   * Reports an escalation in the context of a running task by id. The escalation code must be specified to identify the escalation handler.
   * See the documentation for Reporting Bpmn Escalation in User Tasks.
   * @see https://docs.camunda.org/manual/latest/reference/rest/task/post-bpmn-escalation/
   */
  @RequestMapping(method = [RequestMethod.POST], value = ["/task/{id}/bpmnEscalation"], consumes = ["application/json"])
  fun handleBpmnEscalation(@PathVariable("id") taskId: String, escalation: TaskEscalationDto)

}
