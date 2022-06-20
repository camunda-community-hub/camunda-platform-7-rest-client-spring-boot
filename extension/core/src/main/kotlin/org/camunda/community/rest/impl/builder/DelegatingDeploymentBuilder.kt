package org.camunda.community.rest.impl.builder

import mu.KLogging
import org.camunda.bpm.engine.ProcessEngineException
import org.camunda.bpm.engine.impl.bpmn.deployer.BpmnDeployer
import org.camunda.bpm.engine.impl.cmmn.deployer.CmmnDeployer
import org.camunda.bpm.engine.impl.dmn.deployer.DecisionDefinitionDeployer
import org.camunda.bpm.engine.impl.util.EnsureUtil
import org.camunda.bpm.engine.impl.util.ReflectUtil
import org.camunda.bpm.engine.impl.util.StringUtil
import org.camunda.bpm.engine.repository.Deployment
import org.camunda.bpm.engine.repository.DeploymentBuilder
import org.camunda.bpm.engine.repository.DeploymentWithDefinitions
import org.camunda.community.rest.adapter.DeploymentAdapter
import org.camunda.community.rest.adapter.DeploymentBean
import org.camunda.community.rest.client.api.DeploymentApiClient
import org.camunda.bpm.model.bpmn.Bpmn
import org.camunda.bpm.model.bpmn.BpmnModelInstance
import org.camunda.bpm.model.cmmn.Cmmn
import org.camunda.bpm.model.cmmn.CmmnModelInstance
import org.camunda.bpm.model.dmn.Dmn
import org.camunda.bpm.model.dmn.DmnModelInstance
import org.springframework.web.multipart.MultipartFile
import java.io.*
import java.util.*
import java.util.zip.ZipInputStream

/**
 * Deployment builder, collecting all settings in the DTO sent to the REST endpoint later.
 */
class DelegatingDeploymentBuilder(
  private val deploymentApiClient: DeploymentApiClient
) : DeploymentBuilder {

  companion object : KLogging()

  var tenantId: String? = null
  var deploymentSource: String? = null
  var deploymentName: String? = null
  var deployChangedOnly: Boolean? = null
  var enableDuplicateFiltering: Boolean? = null

  val resources: MutableList<MultipartFile> = mutableListOf()

  override fun addInputStream(resourceName: String, inputStream: InputStream): DeploymentBuilder {
    val file = object : MultipartFile {
      override fun getInputStream(): InputStream = inputStream
      override fun getName(): String = resourceName
      override fun getOriginalFilename(): String = resourceName
      override fun getContentType(): String? = null
      override fun isEmpty(): Boolean = inputStream.available() != 0
      override fun getSize(): Long = inputStream.available().toLong()
      override fun getBytes(): ByteArray = inputStream.readBytes()
      override fun transferTo(file: File) {
        var outputStream: OutputStream? = null
        try {
          outputStream = FileOutputStream(file)
          outputStream.write(bytes)
        } finally {
          outputStream?.close()
        }
      }
    }
    resources.add(file)
    return this
  }

  override fun addClasspathResource(resource: String): DeploymentBuilder {
    val inputStream = ReflectUtil.getResourceAsStream(resource)
    EnsureUtil.ensureNotNull("resource '$resource' not found", "inputStream", inputStream)
    return addInputStream(resource, inputStream)
  }

  override fun addString(resourceName: String, text: String?): DeploymentBuilder {
    EnsureUtil.ensureNotNull("text", text)
    val bytes = text!!.toByteArray()
    return addInputStream(resourceName, ByteArrayInputStream(bytes))
  }

  override fun addModelInstance(resourceName: String, modelInstance: BpmnModelInstance): DeploymentBuilder {
    EnsureUtil.ensureNotNull("modelInstance", modelInstance)
    validateResouceName(resourceName, BpmnDeployer.BPMN_RESOURCE_SUFFIXES)
    val outputStream = ByteArrayOutputStream()
    Bpmn.writeModelToStream(outputStream, modelInstance)
    return addInputStream(resourceName, ByteArrayInputStream(outputStream.toByteArray()))
  }

  override fun addModelInstance(resourceName: String, modelInstance: DmnModelInstance): DeploymentBuilder {
    EnsureUtil.ensureNotNull("modelInstance", modelInstance)
    validateResouceName(resourceName, DecisionDefinitionDeployer.DMN_RESOURCE_SUFFIXES)
    val outputStream = ByteArrayOutputStream()
    Dmn.writeModelToStream(outputStream, modelInstance)
    return addInputStream(resourceName, ByteArrayInputStream(outputStream.toByteArray()))
  }

  override fun addModelInstance(resourceName: String, modelInstance: CmmnModelInstance): DeploymentBuilder {
    EnsureUtil.ensureNotNull("modelInstance", modelInstance)
    validateResouceName(resourceName, CmmnDeployer.CMMN_RESOURCE_SUFFIXES)
    val outputStream = ByteArrayOutputStream()
    Cmmn.writeModelToStream(outputStream, modelInstance)
    return addInputStream(resourceName, ByteArrayInputStream(outputStream.toByteArray()))
  }

  override fun addZipInputStream(zipInputStream: ZipInputStream): DeploymentBuilder {
    try {
      var entry = zipInputStream.nextEntry
      while (entry != null) {
        if (!entry.isDirectory) {
          val entryName = entry.name
          addInputStream(entryName, zipInputStream)
        }
        entry = zipInputStream.nextEntry
      }
    } catch (e: Exception) {
      throw ProcessEngineException("problem reading zip input stream", e)
    }
    return this
  }

  override fun addDeploymentResources(deploymentId: String): DeploymentBuilder {
    deploymentApiClient.getDeploymentResources(deploymentId).body!!.forEach {
      addDeploymentResourcesById(deploymentId, listOf(it.id))
    }
    return this
  }

  override fun addDeploymentResourceById(deploymentId: String, resourceId: String): DeploymentBuilder {
    val resourceName = deploymentApiClient.getDeploymentResource(deploymentId, resourceId).body!!.name
    val resource = deploymentApiClient.getDeploymentResourceData(deploymentId, resourceId).body!!
    addInputStream(resourceName, resource.inputStream)
    return this
  }

  override fun addDeploymentResourcesById(deploymentId: String, resourceIds: List<String>): DeploymentBuilder {
    resourceIds.forEach {
      addDeploymentResourceById(deploymentId, it)
    }
    return this
  }

  override fun addDeploymentResourceByName(deploymentId: String, resourceName: String): DeploymentBuilder {
    deploymentApiClient.getDeploymentResources(deploymentId).body!!.filter { it.name == resourceName }.forEach {
      addDeploymentResourceById(deploymentId, it.id)
    }
    return this
  }

  override fun addDeploymentResourcesByName(deploymentId: String, resourceNames: List<String>): DeploymentBuilder {
    deploymentApiClient.getDeploymentResources(deploymentId).body!!.filter { resourceNames.contains(it.name) }.forEach {
      addDeploymentResourceById(deploymentId, it.id)
    }
    return this
  }

  override fun name(name: String) = this.apply { this.deploymentName = name }

  override fun nameFromDeployment(deploymentId: String) = this.apply { this.deploymentName = deploymentApiClient.getDeployment(deploymentId).body!!.name }

  override fun enableDuplicateFiltering() = this.apply { this.enableDuplicateFiltering = true }

  override fun enableDuplicateFiltering(deployChangedOnly: Boolean) = this.apply {
    this.enableDuplicateFiltering = true
    this.deployChangedOnly = deployChangedOnly
  }

  override fun activateProcessDefinitionsOn(date: Date): DeploymentBuilder = throw UnsupportedOperationException("activation on a specific date is not yet supported")

  override fun source(source: String) = this.apply { this.deploymentSource = source }

  override fun deploy(): Deployment = deployWithResult()

  override fun deployWithResult() =
    DeploymentAdapter(DeploymentBean.fromDto(
      deploymentApiClient.createDeployment(tenantId, deploymentSource, deployChangedOnly, enableDuplicateFiltering, deploymentName, null, resources.toTypedArray()).body!!
    ))

  override fun getResourceNames(): MutableCollection<String> = this.resources.map { it.name }.toMutableList()

  override fun tenantId(tenantId: String?): DeploymentBuilder = this.apply { this.tenantId = tenantId }

  private fun validateResouceName(resourceName: String, resourceSuffixes: Array<String>) {
    if (!StringUtil.hasAnySuffix(resourceName, resourceSuffixes)) {
      logger.warn { "Deployment resource '$resourceName' will be ignored as its name must have one of suffixes $resourceSuffixes." }
    }
  }

}
