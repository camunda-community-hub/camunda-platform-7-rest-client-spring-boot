package org.camunda.community.rest.impl.builder

import org.assertj.core.api.Assertions.assertThat
import org.camunda.bpm.model.bpmn.Bpmn
import org.camunda.community.rest.client.api.DeploymentApiClient
import org.camunda.community.rest.client.model.DeploymentResourceDto
import org.camunda.community.rest.client.model.DeploymentWithDefinitionsDto
import org.camunda.community.rest.client.model.ProcessDefinitionDto
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.core.io.Resource
import org.springframework.http.ResponseEntity
import java.io.ByteArrayInputStream
import java.util.*

class DelegatingDeploymentBuilderTest {

  val deploymentApiClient = mock<DeploymentApiClient>()

  val builder = DelegatingDeploymentBuilder(deploymentApiClient).
    apply {
      this.deploymentName = "deploymentName"
      this.tenantId = "tenantId"
      this.deploymentSource = "deploymentSource"
      this.enableDuplicateFiltering = true
      this.deployChangedOnly = true
      this.activateProcessDefinitionsOn(Date())
      this.addClasspathResource("test-resource.txt")
      this.addString("stringResource", "unusedText")
      this.addInputStream("inputStream", ByteArrayInputStream("some-text".toByteArray()))
    }

  @Before
  fun setup() {
    whenever(deploymentApiClient.getDeploymentResources("anotherDeploymentId")).thenReturn(
      ResponseEntity.ok(listOf(DeploymentResourceDto().id("resourceId").name("name")))
    )
    whenever(deploymentApiClient.getDeploymentResource("anotherDeploymentId", "resourceId")).thenReturn(
      ResponseEntity.ok(DeploymentResourceDto().name("resourceName"))
    )
    val resourceMock = mock<Resource>()
    whenever(deploymentApiClient.getDeploymentResourceData("anotherDeploymentId", "resourceId")).thenReturn(
      ResponseEntity.ok(resourceMock)
    )
    whenever(resourceMock.inputStream).thenReturn(ByteArrayInputStream("someResourceText".toByteArray()))
    builder.addDeploymentResources("anotherDeploymentId")
    val model = Bpmn.createExecutableProcess().id("test").startEvent().endEvent()
    builder.addModelInstance("modelInstance", model.done())
  }

  @Test
  fun deploy() {
    whenever(deploymentApiClient.createDeployment(any(), any(), any(), any(), any(), any(), any())).thenReturn(
      ResponseEntity.ok(DeploymentWithDefinitionsDto().name("name").id("id"))
    )
    val result = builder.deploy()
    assertThat(result).isNotNull
    assertThat(result.id).isEqualTo("id")
  }

  @Test
  fun deployWithResult() {
    whenever(deploymentApiClient.createDeployment(any(), any(), any(), any(), any(), any(), any())).thenReturn(
      ResponseEntity.ok(DeploymentWithDefinitionsDto().name("name").id("id").deployedProcessDefinitions(
        mapOf("process" to ProcessDefinitionDto().id("processDefinitionId"))
      ))
    )
    val result = builder.deployWithResult()
    assertThat(result).isNotNull
    assertThat(result.id).isEqualTo("id")
    assertThat(result.deployedProcessDefinitions).hasSize(1)
  }

}
