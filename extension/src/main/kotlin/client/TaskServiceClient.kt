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
import org.camunda.bpm.engine.rest.dto.task.TaskDto
import org.camunda.bpm.engine.rest.dto.task.TaskQueryDto
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.cloud.openfeign.SpringQueryMap
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam

/**
 * Feign client accessing the methods of runtime service.
 */
@FeignClient(name = "remoteTaskService", url = "\${feign.client.config.remoteTaskService.url}")
interface TaskServiceClient {

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

}
