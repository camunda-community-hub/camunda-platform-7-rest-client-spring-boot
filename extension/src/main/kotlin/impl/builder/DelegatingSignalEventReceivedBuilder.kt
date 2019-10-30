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

package org.camunda.bpm.extension.rest.impl.builder

import org.camunda.bpm.engine.rest.dto.SignalDto
import org.camunda.bpm.engine.runtime.SignalEventReceivedBuilder
import org.camunda.bpm.extension.rest.client.RuntimeServiceClient
import org.camunda.bpm.extension.rest.variables.ValueMapper

/**
 * Correlation builder, collecting all settings in the DTO sent to the REST endpoint later.
 */
class DelegatingSignalEventReceivedBuilder(
  signalName: String,
  private val runtimeServiceClient: RuntimeServiceClient,
  private val valueMapper: ValueMapper
) : SignalEventReceivedBuilder {

  private val signalDto = SignalDto().apply {
    this.name = signalName
    this.variables = mutableMapOf()
  }

  override fun setVariables(variables: MutableMap<String, Any>): SignalEventReceivedBuilder {
    signalDto.variables = valueMapper.mapValues(variables)
    return this
  }

  override fun tenantId(tenantId: String): SignalEventReceivedBuilder {
    signalDto.tenantId = tenantId
    return this
  }

  override fun executionId(executionId: String): SignalEventReceivedBuilder {
    signalDto.executionId = executionId
    return this
  }

  override fun withoutTenantId(): SignalEventReceivedBuilder {
    signalDto.isWithoutTenantId = true
    return this
  }

  override fun send() {
    runtimeServiceClient.signalEventReceived(signalDto)
  }

}
