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

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.camunda.bpm.engine.variable.Variables
import org.camunda.community.rest.client.model.LockedExternalTaskDto
import org.camunda.community.rest.client.model.VariableValueDto
import org.camunda.community.rest.impl.toRequiredDate
import org.camunda.community.rest.variables.ValueMapper
import org.camunda.community.rest.variables.ValueTypeRegistration
import org.camunda.community.rest.variables.ValueTypeResolverImpl
import org.camunda.community.rest.variables.serialization.JsonValueSerializer
import org.junit.Test
import java.time.OffsetDateTime

class LockedExternalTaskAdapterTest {

  private val objectMapper = jacksonObjectMapper()
  private val typeResolver = ValueTypeResolverImpl()
  private val typeRegistration = ValueTypeRegistration()
  private val valueMapper = ValueMapper(
    objectMapper = objectMapper,
    valueTypeResolver = typeResolver,
    valueTypeRegistration = typeRegistration,
    valueSerializers = listOf(JsonValueSerializer(objectMapper)),
    serializationFormat = Variables.SerializationDataFormats.JSON,
    customValueSerializers = listOf()
  )

  private val dto = LockedExternalTaskDto()
    .id("id")
    .processInstanceId("processInstanceId")
    .tenantId("tenantId")
    .activityId("activityId")
    .activityInstanceId("activityInstanceId")
    .businessKey("businessKey")
    .errorDetails("errorDetails")
    .errorMessage("errorMessage")
    .executionId("executionId")
    .extensionProperties(mapOf("prop1" to "value1"))
    .lockExpirationTime(OffsetDateTime.now())
    .priority(1L)
    .processDefinitionId("processDefinitionId")
    .processDefinitionKey("processDefinitionKey")
    .processDefinitionVersionTag("processDefinitionVersionTag")
    .retries(1)
    .suspended(true)
    .topicName("topicName")
    .variables(mapOf("var1" to VariableValueDto().type("integer").value(1)))
    .workerId("workerId")
    .createTime(OffsetDateTime.now())

  @Test
  fun `should delegate`() {
    val lockedExternalTaskBean = LockedExternalTaskBean.fromDto(dto = dto, valueMapper = valueMapper)
    val lockedExternalTaskAdapter = LockedExternalTaskAdapter(lockedExternalTaskBean)
    assertThat(lockedExternalTaskAdapter).usingRecursiveComparison().ignoringFields("lockedExternalTaskBean")
      .isEqualTo(lockedExternalTaskBean)
  }

  @Test
  fun `should construct from dto`() {
    val bean = LockedExternalTaskBean.fromDto(dto = dto, valueMapper = valueMapper)
    assertThat(bean).usingRecursiveComparison().ignoringFields("lockExpirationTime", "variables", "createTime").isEqualTo(dto)
    assertThat(bean.lockExpirationTime).isEqualTo(dto.lockExpirationTime.toInstant())
    assertThat(bean.variables).containsEntry("var1", 1)
    assertThat(bean.createTime).isEqualTo(dto.createTime.toRequiredDate())
  }
}
