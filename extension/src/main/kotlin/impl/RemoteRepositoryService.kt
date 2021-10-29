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

package org.camunda.bpm.extension.rest.impl

import org.camunda.bpm.engine.repository.ProcessDefinitionQuery
import org.camunda.bpm.extension.rest.adapter.AbstractRepositoryServiceAdapter
import org.camunda.bpm.extension.rest.client.api.ProcessDefinitionApiClient
import org.camunda.bpm.extension.rest.impl.query.DelegatingProcessDefinitionQuery
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component


/**
 * Remote implementation of Camunda Core RepositoryService API, delegating
 * all request over HTTP to a remote Camunda Engine.
 */
@Component
@Qualifier("remote")
class RemoteRepositoryService(
  private val processDefinitionApiClient: ProcessDefinitionApiClient
) : AbstractRepositoryServiceAdapter() {

  override fun createProcessDefinitionQuery(): ProcessDefinitionQuery {
    return DelegatingProcessDefinitionQuery(processDefinitionApiClient)
  }
}
