/*-
 * #%L
 * camunda-platform-7-rest-client-spring-boot
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
package org.camunda.community.rest.adapter

import org.assertj.core.api.Assertions.assertThat
import org.camunda.community.rest.client.model.ExternalTaskDto
import org.junit.Test
import java.time.OffsetDateTime

class ExternalTaskAdapterTest {

  private val dto = ExternalTaskDto()
    .id("id")
    .processInstanceId("processInstanceId")
    .tenantId("tenantId")
    .activityId("activityId")
    .activityInstanceId("activityInstanceId")
    .businessKey("businessKey")
    .errorMessage("errorMessage")
    .executionId("executionId")
    .lockExpirationTime(OffsetDateTime.now())
    .priority(1L)
    .processDefinitionId("processDefinitionId")
    .processDefinitionKey("processDefinitionKey")
    .processDefinitionVersionTag("processDefinitionVersionTag")
    .retries(1)
    .suspended(true)
    .topicName("topicName")
    .workerId("workerId")

  @Test
  fun `should delegate`() {
    val externalTaskBean = ExternalTaskBean.fromDto(dto)
    val externalTaskAdapter = ExternalTaskAdapter(externalTaskBean)
    assertThat(externalTaskAdapter).usingRecursiveComparison().ignoringFields("externalTaskBean").isEqualTo(externalTaskBean)
  }

  @Test
  fun `should construct from dto`() {
    val bean = ExternalTaskBean.fromDto(dto)
    assertThat(bean).usingRecursiveComparison().ignoringFields("lockExpirationTime").isEqualTo(dto)
    assertThat(bean.lockExpirationTime).isEqualTo(dto.lockExpirationTime.toInstant())
  }
}
