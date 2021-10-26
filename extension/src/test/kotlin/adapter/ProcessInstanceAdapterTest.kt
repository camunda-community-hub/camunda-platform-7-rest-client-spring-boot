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
import org.camunda.bpm.extension.rest.client.model.ProcessInstanceDto
import org.junit.Test

class ProcessInstanceAdapterTest {

  private val processBean = InstanceBean(
    id = "id",
    ended = false,
    suspended = false,
    type = InstanceType.PROCESS,
    instanceId = "instanceId",
    businessKey = "businessKey",
    tenantId = "tenantId",
    processDefinitionId = "processDefId",
    rootProcessInstanceId = "root"
  )
  private val caseBean = InstanceBean(
    id = "id",
    ended = false,
    suspended = false,
    type = InstanceType.CASE,
    instanceId = "instanceId",
    businessKey = "businessKey",
    tenantId = "tenantId",
    processDefinitionId = "processDefId",
    rootProcessInstanceId = "root"
  )

  @Test
  fun `should construct process instance`() {
    val adapter = ProcessInstanceAdapter(processBean)
    assertThat(adapter.businessKey).isEqualTo(processBean.businessKey)
    assertThat(adapter.id).isEqualTo(processBean.id)
    assertThat(adapter.processDefinitionId).isEqualTo(processBean.processDefinitionId)
    assertThat(adapter.rootProcessInstanceId).isEqualTo(processBean.rootProcessInstanceId)
    assertThat(adapter.tenantId).isEqualTo(processBean.tenantId)
    assertThat(adapter.isEnded).isEqualTo(processBean.ended)
    assertThat(adapter.isSuspended).isEqualTo(processBean.suspended)
    assertThat(adapter.processInstanceId).isEqualTo(processBean.instanceId)
    assertThat(adapter.caseInstanceId).isNull()
  }

  @Test
  fun `should construct case instance`() {
    val adapter = ProcessInstanceAdapter(caseBean)
    assertThat(adapter.businessKey).isEqualTo(caseBean.businessKey)
    assertThat(adapter.id).isEqualTo(caseBean.id)
    assertThat(adapter.tenantId).isEqualTo(caseBean.tenantId)
    assertThat(adapter.isEnded).isEqualTo(caseBean.ended)
    assertThat(adapter.isSuspended).isEqualTo(caseBean.suspended)
    assertThat(adapter.caseInstanceId).isEqualTo(caseBean.instanceId)
    assertThat(adapter.rootProcessInstanceId).isNull()
    assertThat(adapter.processDefinitionId).isNull()
    assertThat(adapter.processInstanceId).isNull()
  }

  @Test
  fun `from dto`() {
    val dto = ProcessInstanceDto()
      .id("id")
      .businessKey("businessKey")
      .tenantId("tenantId")
      .ended(true)
      .suspended(false)
      .caseInstanceId(null)
    val bean = InstanceBean.fromProcessInstanceDto(dto)

    assertThat(dto.businessKey).isEqualTo(bean.businessKey)
    assertThat(dto.id).isEqualTo(bean.id)
    assertThat(dto.tenantId).isEqualTo(bean.tenantId)
    assertThat(dto.ended).isEqualTo(bean.ended)
    assertThat(dto.suspended).isEqualTo(bean.suspended)
    assertThat(dto.caseInstanceId).isNull()
  }
}
