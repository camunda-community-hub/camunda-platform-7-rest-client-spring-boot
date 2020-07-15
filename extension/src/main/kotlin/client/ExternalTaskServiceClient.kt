package org.camunda.bpm.extension.rest.client

import org.camunda.bpm.engine.rest.dto.externaltask.CompleteExternalTaskDto
import org.camunda.bpm.engine.rest.dto.task.CompleteTaskDto
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod

@FeignClient(name = "remoteExternalTaskService", url = "\${feign.client.config.remoteExternalTaskService.url}")
interface ExternalTaskServiceClient {
  /**
   * Completes external task
   * @see https://docs.camunda.org/manual/latest/reference/rest/external-task/post-complete
   */
  @RequestMapping(method = [RequestMethod.POST], value = ["/external-task/{id}/complete"], consumes = ["application/json"])
  fun completeTask(@PathVariable("id") id: String, completeTask: CompleteExternalTaskDto)
}
