package org.camunda.bpm.extension.feign.client

import org.camunda.bpm.engine.rest.dto.SignalDto
import org.camunda.bpm.engine.rest.dto.message.CorrelationMessageDto
import org.camunda.bpm.engine.rest.dto.message.MessageCorrelationResultWithVariableDto
import org.camunda.bpm.engine.rest.dto.runtime.ExecutionTriggerDto
import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceWithVariablesDto
import org.camunda.bpm.engine.rest.dto.runtime.StartProcessInstanceDto
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod

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
}
