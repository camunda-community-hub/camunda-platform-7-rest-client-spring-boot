package org.camunda.bpm.extension.restclient.client

import org.camunda.bpm.engine.rest.dto.message.CorrelationMessageDto
import org.camunda.bpm.engine.rest.dto.message.MessageCorrelationResultDto
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod

@FeignClient(name = "remoteRuntimeService", url = "\${feign.client.config.remoteRuntimeService.url}")
interface RuntimeServiceClient {

  @RequestMapping(method = [RequestMethod.POST], value = ["/message"], consumes = ["application/json"])
  fun correlateMessage(correlationMessage: CorrelationMessageDto): List<MessageCorrelationResultDto>
}
