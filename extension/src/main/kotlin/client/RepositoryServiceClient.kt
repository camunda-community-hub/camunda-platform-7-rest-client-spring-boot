package org.camunda.bpm.extension.feign.client

import org.camunda.bpm.engine.rest.dto.CountResultDto
import org.camunda.bpm.engine.rest.dto.repository.ProcessDefinitionDto
import org.camunda.bpm.engine.rest.dto.repository.ProcessDefinitionQueryDto
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.cloud.openfeign.SpringQueryMap
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam

@FeignClient(name = "remoteRepositoryService", url = "\${feign.client.config.remoteRepositoryService.url}")
interface RepositoryServiceClient {

  /**
   * Retrieves the list of process definitions.
   * @see https://docs.camunda.org/manual/latest/reference/rest/process-definition/get-query/
   */
  @RequestMapping(method = [RequestMethod.GET], value = ["/process-definition"], consumes = ["application/json"])
  fun getProcessDefinitions(@SpringQueryMap query: ProcessDefinitionQueryDto, @RequestParam("firstResult") firstResult: Int, @RequestParam("maxResults") maxResults: Int): List<ProcessDefinitionDto>

  /**
   * Retrieves the list of process definitions.
   * @see https://docs.camunda.org/manual/latest/reference/rest/process-definition/get-query/
   */
  @RequestMapping(method = [RequestMethod.GET], value = ["/process-definition/count"], consumes = ["application/json"])
  fun countProcessDefinitions(@SpringQueryMap query: ProcessDefinitionQueryDto, @RequestParam("firstResult") firstResult: Int, @RequestParam("maxResults") maxResults: Int): CountResultDto

}
