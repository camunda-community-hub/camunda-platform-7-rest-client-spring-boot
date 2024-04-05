package org.camunda.community.rest.impl.builder

import org.assertj.core.api.Assertions.assertThat
import org.camunda.bpm.model.bpmn.Bpmn
import org.camunda.bpm.model.cmmn.Cmmn
import org.camunda.bpm.model.dmn.Dmn
import org.camunda.bpm.model.dmn.instance.Definitions
import org.camunda.bpm.model.xml.test.AbstractModelElementInstanceTest.modelInstance
import org.camunda.community.rest.client.api.DeploymentApiClient
import org.camunda.community.rest.client.model.DeploymentDto
import org.camunda.community.rest.client.model.DeploymentResourceDto
import org.camunda.community.rest.client.model.DeploymentWithDefinitionsDto
import org.camunda.community.rest.client.model.ProcessDefinitionDto
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.core.io.Resource
import org.springframework.http.ResponseEntity
import java.io.ByteArrayInputStream
import java.util.*
import java.util.zip.ZipInputStream


class DelegatingDeploymentBuilderTest {

  val deploymentApiClient = mock<DeploymentApiClient>()

  val builder = DelegatingDeploymentBuilder(deploymentApiClient).
    apply {
      this.name("deploymentName")
      this.tenantId("tenantId")
      this.source("deploymentSource")
      this.enableDuplicateFiltering()
      this.enableDuplicateFiltering(true)
      this.activateProcessDefinitionsOn(Date())
      this.addClasspathResource("test-resource.txt")
      this.addString("stringResource", "unusedText")
      this.addInputStream("inputStream", ByteArrayInputStream("some-text".toByteArray()))
    }

  @Before
  fun setup() {
    whenever(deploymentApiClient.getDeploymentResources("anotherDeploymentId")).thenReturn(
      ResponseEntity.ok(listOf(DeploymentResourceDto().id("resourceId").name("resourceName")))
    )
    whenever(deploymentApiClient.getDeployment("anotherDeploymentId")).thenReturn(
      ResponseEntity.ok(DeploymentDto().id("deploymentId").name("deploymentName"))
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
    builder.addDeploymentResourcesByName("anotherDeploymentId", listOf("resourceName"))
    builder.addDeploymentResourceByName("anotherDeploymentId", "resourceName")
    val model = Bpmn.createExecutableProcess().id("test").startEvent().endEvent()
    builder.addModelInstance("modelInstance", model.done())
    val dmnModel = Dmn.createEmptyModel()
    val definitions = dmnModel.newInstance(Definitions::class.java)
    definitions.namespace = "http://camunda.org/schema/1.0/dmn"
    definitions.name = "definitions"
    definitions.id = "definitions"
    dmnModel.definitions = definitions
    builder.addModelInstance("dmnModel", dmnModel)
    val cmmnModel = Cmmn.createEmptyModel()
    val cmmnDefinitions = cmmnModel.newInstance(org.camunda.bpm.model.cmmn.instance.Definitions::class.java)
    cmmnDefinitions.targetNamespace = "http://camunda.org/schema/1.0/cmmn"
    cmmnDefinitions.name = "definitions"
    cmmnDefinitions.id = "definitions"
    cmmnModel.definitions = cmmnDefinitions
    builder.addModelInstance("cmmnModel", cmmnModel)
    builder.addZipInputStream(ZipInputStream(javaClass.classLoader.getResourceAsStream("test-resource.zip")!!))
    builder.nameFromDeployment("anotherDeploymentId")
  }

  @Test
  fun deploy() {
    whenever(deploymentApiClient.createDeployment(any(), any(), any(), any(), any(), any(), any())).thenReturn(
      ResponseEntity.ok(DeploymentWithDefinitionsDto().name("name").id("id"))
    )
    val names = builder.resourceNames
    assertThat(names).isNotEmpty()
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
