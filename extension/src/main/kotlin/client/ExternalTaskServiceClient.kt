package org.camunda.bpm.extension.rest.client

import org.camunda.bpm.engine.rest.dto.externaltask.CompleteExternalTaskDto
import org.camunda.bpm.engine.rest.dto.externaltask.ExternalTaskBpmnError
import org.camunda.bpm.engine.rest.dto.externaltask.ExternalTaskFailureDto
import org.camunda.bpm.engine.rest.dto.task.CompleteTaskDto
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod

/**
 * Feign client accessing the methods of external task service.
 */
@FeignClient(name = "remoteExternalTaskService", url = "\${feign.client.config.remoteExternalTaskService.url}")
interface ExternalTaskServiceClient {
  /**
   * Completes external task
   * @see https://docs.camunda.org/manual/latest/reference/rest/external-task/post-complete
   */
  @RequestMapping(method = [RequestMethod.POST], value = ["/external-task/{id}/complete"], consumes = ["application/json"])
  fun completeTask(@PathVariable("id") id: String, completeTask: CompleteExternalTaskDto)

  /**
   * Handle BPMN Error
   * @see https://docs.camunda.org/manual/latest/reference/rest/external-task/post-bpmn-error
   */
  @RequestMapping(method = [RequestMethod.POST], value = ["/external-task/{id}/bpmnError"], consumes = ["application/json"])
  fun handleBpmnError(@PathVariable("id") id: String, bpmnError: ExternalTaskBpmnError)

  /**
   * Handle BPMN Error
   * @see https://docs.camunda.org/manual/latest/reference/rest/external-task/post-failure
   */
  @RequestMapping(method = [RequestMethod.POST], value = ["/external-task/{id}/failure"], consumes = ["application/json"])
  fun handleFailure(@PathVariable("id") id: String, bpmnFailure: ExternalTaskFailureDto)

}
