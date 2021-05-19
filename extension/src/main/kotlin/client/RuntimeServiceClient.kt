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

package org.camunda.bpm.extension.rest.client

import org.camunda.bpm.engine.rest.dto.CountResultDto
import org.camunda.bpm.engine.rest.dto.PatchVariablesDto
import org.camunda.bpm.engine.rest.dto.SignalDto
import org.camunda.bpm.engine.rest.dto.VariableValueDto
import org.camunda.bpm.engine.rest.dto.message.CorrelationMessageDto
import org.camunda.bpm.engine.rest.dto.message.MessageCorrelationResultWithVariableDto
import org.camunda.bpm.engine.rest.dto.runtime.*
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.*

/**
 * Feign client accessing the methods of runtime service.
 */
@FeignClient(name = "remoteRuntimeService", url = "\${feign.client.config.remoteRuntimeService.url}")
interface RuntimeServiceClient {

  /**
   * Correlates message.
   * @see https://docs.camunda.org/manual/latest/reference/rest/message/
   */
  @RequestMapping(method = [RequestMethod.POST], value = ["/message"], consumes = ["application/json"])
  fun correlateMessage(correlationMessage: CorrelationMessageDto): List<MessageCorrelationResultWithVariableDto>

  /**
   * Starts process instance by id
   * @see https://docs.camunda.org/manual/latest/reference/rest/process-definition/post-start-process-instance/
   */
  @RequestMapping(method = [RequestMethod.POST], value = ["/process-definition/{id}/start"], consumes = ["application/json"])
  fun startProcessById(@PathVariable("id") id: String, startProcessInstance: StartProcessInstanceDto): ProcessInstanceWithVariablesDto

  /**
   * Starts process instance by key
   * @see https://docs.camunda.org/manual/latest/reference/rest/process-definition/post-start-process-instance/
   */
  @RequestMapping(method = [RequestMethod.POST], value = ["/process-definition/key/{key}/start"], consumes = ["application/json"])
  fun startProcessByKey(@PathVariable("key") processDefinitionKey: String, startProcessInstance: StartProcessInstanceDto): ProcessInstanceWithVariablesDto

  /**
   * Starts process instance by key
   * @see https://docs.camunda.org/manual/latest/reference/rest/process-definition/post-start-process-instance/
   */
  @RequestMapping(method = [RequestMethod.POST], value = ["/process-definition/key/{key}/tenant-id/{tenant-id}/start"], consumes = ["application/json"])
  fun startProcessByKeyForTenant(@PathVariable("key") processDefinitionKey: String, @PathVariable("tenant-id") tenantId: String, startProcessInstance: StartProcessInstanceDto): ProcessInstanceWithVariablesDto

  /**
   * Throws a signal.
   * @see https://docs.camunda.org/manual/latest/reference/rest/signal/post-signal/
   */
  @RequestMapping(method = [RequestMethod.POST], value = ["/signal"], consumes = ["application/json"])
  fun signalEventReceived(signal: SignalDto)

  /**
   * Triggers execution.
   * @see https://docs.camunda.org/manual/latest/reference/rest/execution/post-signal/
   */
  @RequestMapping(method = [RequestMethod.POST], value = ["/execution/{id}/signal"], consumes = ["application/json"])
  fun triggerExecutionById(@PathVariable("id") executionId: String, trigger: ExecutionTriggerDto)

  /**
   * Retrieves all local variables of a given execution by id.
   * @see https://docs.camunda.org/manual/latest/reference/rest/execution/local-variables/get-local-variables/
   */
  @RequestMapping(method = [RequestMethod.GET], value = ["/execution/{id}/localVariables"], consumes = ["application/json"])
  fun getVariablesLocal(@PathVariable("id") executionId: String, @RequestParam("deserializeValues") deserializeValues: Boolean): Map<String, VariableValueDto>

  /**
   * Updates or deletes the variables in the context of an execution by id. The updates do not propagate upwards in the execution hierarchy.
   * Updates precede deletions. So, if a variable is updated AND deleted, the deletion overrides the update.
   * @see https://docs.camunda.org/manual/latest/reference/rest/execution/local-variables/post-local-variables/
   */
  @RequestMapping(method = [RequestMethod.POST], value = ["/execution/{id}/localVariables"], consumes = ["application/json"])
  fun changeVariablesLocal(@PathVariable("id") executionId: String, patch: PatchVariablesDto)

  /**
   * Retrieves local variable by id of a given execution by id.
   * @see https://docs.camunda.org/manual/latest/reference/rest/execution/local-variables/get-local-variable/
   */
  @RequestMapping(method = [RequestMethod.GET], value = ["/execution/{id}/localVariables/{varName}"], consumes = ["application/json"])
  fun getVariableLocal(@PathVariable("id") executionId: String, @PathVariable("varName") varName: String, @RequestParam("deserializeValue") deserializeValue: Boolean): VariableValueDto

  /**
   * Sets a variable in the context of a given execution by id. Update does not propagate upwards in the execution hierarchy.
   * @see https://docs.camunda.org/manual/latest/reference/rest/execution/local-variables/put-local-variable/
   */
  @RequestMapping(method = [RequestMethod.PUT], value = ["/execution/{id}/localVariables/{varName}"], consumes = ["application/json"])
  fun setVariableLocal(@PathVariable("id") executionId: String, @PathVariable("varName") varName: String, value: VariableValueDto)

  /**
   * Deletes a variable in the context of a given execution by id. Deletion does not propagate upwards in the execution hierarchy.
   * @see https://docs.camunda.org/manual/latest/reference/rest/execution/local-variables/delete-local-variable/
   */
  @RequestMapping(method = [RequestMethod.DELETE], value = ["/execution/{id}/localVariables/{varName}"], consumes = ["application/json"])
  fun deleteVariableLocal(@PathVariable("id") executionId: String, @PathVariable("varName") varName: String)

  /**
   * Retrieves all variables of a given process instance by id.
   * @see https://docs.camunda.org/manual/latest/reference/rest/process-instance/variables/get-variables/
   */
  @RequestMapping(method = [RequestMethod.GET], value = ["/process-instance/{id}/variables"], consumes = ["application/json"])
  fun getVariables(@PathVariable("id") processInstanceId: String, @RequestParam("deserializeValues") deserializeValues: Boolean): Map<String, VariableValueDto>

  /**
   * Updates or deletes the variables of a process instance by id. Updates precede deletions. So, if a variable is updated AND deleted,
   * the deletion overrides the update.
   * @see https://docs.camunda.org/manual/latest/reference/rest/process-instance/variables/post-variables/
   */
  @RequestMapping(method = [RequestMethod.POST], value = ["/process-instance/{id}/variables"], consumes = ["application/json"])
  fun changeVariables(@PathVariable("id") processInstanceId: String, patch: PatchVariablesDto)

  /**
   * Retrieves a variable of a given process instance by id.
   * @see https://docs.camunda.org/manual/latest/reference/rest/process-instance/variables/get-variable/
   */
  @RequestMapping(method = [RequestMethod.GET], value = ["/process-instance/{id}/variables/{varName}"], consumes = ["application/json"])
  fun getVariable(@PathVariable("id") processInstanceId: String, @PathVariable("varName") varName: String, @RequestParam("deserializeValue") deserializeValue: Boolean): VariableValueDto

  /**
   * Sets a variable of a given process instance by id.
   * @see https://docs.camunda.org/manual/latest/reference/rest/process-instance/variables/put-variable/
   */
  @RequestMapping(method = [RequestMethod.PUT], value = ["/process-instance/{id}/variables/{varName}"], consumes = ["application/json"])
  fun setVariable(@PathVariable("id") processInstanceId: String, @PathVariable("varName") varName: String, value: VariableValueDto)

  /**
   * Deletes a variable of a process instance by id.
   * @see https://docs.camunda.org/manual/latest/reference/rest/process-instance/variables/delete-variable/
   */
  @RequestMapping(method = [RequestMethod.DELETE], value = ["/process-instance/{id}/variables/{varName}"], consumes = ["application/json"])
  fun deleteVariable(@PathVariable("id") processInstanceId: String, @PathVariable("varName") varName: String)

  /**
   * Retrieves process instances.
   * @see https://docs.camunda.org/manual/latest/reference/rest/process-instance/post-query/
   */
  @RequestMapping(method = [RequestMethod.POST], value = ["/process-instance"], consumes = ["application/json"])
  fun getProcessInstances(@RequestBody query: ProcessInstanceQueryDto, @RequestParam("firstResult") firstResult: Int, @RequestParam("maxResults") maxResults: Int): List<ProcessInstanceDto>

  /**
   * Counts process instances.
   * @see https://docs.camunda.org/manual/latest/reference/rest/process-instance/post-query-count/
   */
  @RequestMapping(method = [RequestMethod.POST], value = ["/process-instance/count"], consumes = ["application/json"])
  fun countProcessInstances(@RequestBody query: ProcessInstanceQueryDto, @RequestParam("firstResult") firstResult: Int, @RequestParam("maxResults") maxResults: Int): CountResultDto

}
