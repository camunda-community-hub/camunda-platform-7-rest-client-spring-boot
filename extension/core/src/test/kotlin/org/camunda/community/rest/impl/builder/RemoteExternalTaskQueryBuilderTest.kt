package org.camunda.community.rest.impl.builder

import org.assertj.core.api.Assertions.assertThat
import org.camunda.community.rest.client.api.ExternalTaskApiClient
import org.camunda.community.rest.client.model.LockedExternalTaskDto
import org.camunda.community.rest.config.CamundaRestClientProperties
import org.camunda.community.rest.variables.ValueMapper
import org.camunda.community.rest.variables.ValueTypeResolverImpl
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.http.ResponseEntity

class RemoteExternalTaskQueryBuilderTest {

  val externalTaskApiClient = mock<ExternalTaskApiClient>()

  val camundaRestClientProperties = mock<CamundaRestClientProperties>()

  val builder = RemoteExternalTaskQueryBuilder(
    externalTaskApiClient,
    valueMapper = ValueMapper(valueTypeResolver = ValueTypeResolverImpl()),
    camundaRestClientProperties,
    workerId = "workerId",
    maxTasks = 10,
    usePriority = true
  )

  @Test
  fun execute() {
    whenever(externalTaskApiClient.fetchAndLock(any())).thenReturn(
      ResponseEntity.ok(listOf(LockedExternalTaskDto().id("id").priority(1)))
    )
    val result = builder
      .topic("topic", 5000)
      .tenantIdIn("tenantId")
      .businessKey("businessKey")
      .enableCustomObjectDeserialization()
      .includeExtensionProperties()
      .processDefinitionId("processDefinitionId")
      .topic("topic2", 5000)
      .withoutTenantId()
      .tenantIdIn("tenantId")
      .businessKey("businessKey")
      .enableCustomObjectDeserialization()
      .includeExtensionProperties()
      .processDefinitionId("processDefinitionId")
      .processDefinitionIdIn("processDefinitionIn")
      .processDefinitionKey("processDefinitionKey")
      .processDefinitionKeyIn("processDefinitionKeyIn")
      .processDefinitionVersionTag("processDefinitionVersionTag")
      .processInstanceVariableEquals("var", "value")
      .processInstanceVariableEquals(mapOf("var" to "value"))
      .variables("var")
      .variables(listOf("var"))
      .localVariables()
      .execute()
    assertThat(result).hasSize(1)
  }

}
