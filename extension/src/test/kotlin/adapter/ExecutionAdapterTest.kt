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
package org.camunda.bpm.extension.rest.adapter

import org.assertj.core.api.Assertions.assertThat
import org.camunda.bpm.extension.rest.client.model.ExecutionDto
import org.junit.Test

class ExecutionAdapterTest {

  private val dto = ExecutionDto()
    .id("id")
    .processInstanceId("processInstanceId")
    .ended(true)
    .tenantId("tenantId")

  @Test
  fun `should delegate`() {
    val executionBean = ExecutionBean.fromExecutionDto(dto)
    val executionAdapter = ExecutionAdapter(executionBean)
    assertThat(executionAdapter.isEnded).isEqualTo(executionBean.ended)
    assertThat(executionAdapter.isSuspended).isEqualTo(executionBean.suspended)
    assertThat(executionAdapter.processInstanceId).isEqualTo(executionBean.processInstanceId)
    assertThat(executionAdapter.tenantId).isEqualTo(executionBean.tenantId)
  }

  @Test
  fun `should construct from dto`() {
    val bean = ExecutionBean.fromExecutionDto(dto)

    assertThat(dto.id).isEqualTo(bean.id)
    assertThat(dto.ended).isEqualTo(bean.ended)
    assertThat(dto.processInstanceId).isEqualTo(bean.processInstanceId)
    assertThat(dto.tenantId).isEqualTo(bean.tenantId)

  }
}
