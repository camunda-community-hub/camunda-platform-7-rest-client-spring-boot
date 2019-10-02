package org.camunda.bpm.extension.feign.client

import org.camunda.bpm.engine.rest.dto.message.CorrelationMessageDto
import org.camunda.bpm.engine.rest.dto.message.MessageCorrelationResultDto
import org.camunda.bpm.engine.rest.dto.message.MessageCorrelationResultWithVariableDto
import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceWithVariablesDto
import org.camunda.bpm.engine.rest.dto.runtime.StartProcessInstanceDto
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod

@FeignClient(name = "remoteRuntimeService", url = "\${feign.client.config.remoteRuntimeService.url}")
interface RuntimeServiceClient {

  @RequestMapping(method = [RequestMethod.POST], value = ["/message"], consumes = ["application/json"])
  fun correlateMessage(correlationMessage: CorrelationMessageDto): List<MessageCorrelationResultWithVariableDto>

  @RequestMapping(method = [RequestMethod.POST], value = ["/process-definition/{id}/start"], consumes = ["application/json"])
  fun startProcessById(@PathVariable("id") id: String, startProcessInstance: StartProcessInstanceDto): ProcessInstanceWithVariablesDto

  @RequestMapping(method = [RequestMethod.POST], value = ["/process-definition/key/{key}/start"], consumes = ["application/json"])
  fun startProcessByKey(@PathVariable("key") processDefinitionKey: String, startProcessInstance: StartProcessInstanceDto): ProcessInstanceWithVariablesDto

  @RequestMapping(method = [RequestMethod.POST], value = ["/process-definition/key/{key}/tenant-id/{tenant-id}/start"], consumes = ["application/json"])
  fun startProcessByKeyForTenant(@PathVariable("key") processDefinitionKey: String, @PathVariable("tenant-id") tenantId: String, startProcessInstance: StartProcessInstanceDto): ProcessInstanceWithVariablesDto

}
