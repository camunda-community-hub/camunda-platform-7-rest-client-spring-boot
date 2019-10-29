/*-
 * #%L
 * camunda-bpm-feign
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
package org.camunda.bpm.extension.feign.client

import org.camunda.bpm.engine.rest.dto.CountResultDto
import org.camunda.bpm.engine.rest.dto.repository.ProcessDefinitionDto
import org.camunda.bpm.engine.rest.dto.repository.ProcessDefinitionQueryDto
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.cloud.openfeign.SpringQueryMap
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam

/**
 * Feign client accessing the methods of repository service.
 */
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
