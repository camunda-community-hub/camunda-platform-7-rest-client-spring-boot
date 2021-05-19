package org.camunda.bpm.extension.rest.impl.builder

import org.camunda.bpm.engine.repository.Deployment
import org.camunda.bpm.engine.repository.DeploymentBuilder
import org.camunda.bpm.engine.repository.DeploymentWithDefinitions
import org.camunda.bpm.model.bpmn.BpmnModelInstance
import org.camunda.bpm.model.cmmn.CmmnModelInstance
import org.camunda.bpm.model.dmn.DmnModelInstance
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.*
import java.util.zip.ZipInputStream

class DelegatingDeploymentBuilder: DeploymentBuilder {

  val resources: MutableList<MultipartFile> = mutableListOf()


  override fun addInputStream(resourceName: String, inputStream: InputStream): DeploymentBuilder {
    val file = object: MultipartFile {
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

    return this
  }

  override fun addString(resourceName: String, text: String?): DeploymentBuilder {
    return this
  }

  override fun addModelInstance(resourceName: String, modelInstance: BpmnModelInstance): DeploymentBuilder {

    return this
  }

  override fun addModelInstance(resourceName: String, modelInstance: DmnModelInstance): DeploymentBuilder {
    return this
  }

  override fun addModelInstance(resourceName: String, modelInstance: CmmnModelInstance): DeploymentBuilder {
    return this
  }

  override fun addZipInputStream(zipInputStream: ZipInputStream?): DeploymentBuilder {
    return this
  }

  override fun addDeploymentResources(deploymentId: String): DeploymentBuilder {
    return this
  }

  override fun addDeploymentResourceById(deploymentId: String, resourceId: String): DeploymentBuilder {
    return this
  }

  override fun addDeploymentResourcesById(deploymentId: String, resourceIds: List<String>): DeploymentBuilder {
    return this
  }

  override fun addDeploymentResourceByName(deploymentId: String, resourceName: String): DeploymentBuilder {
    return this
  }

  override fun addDeploymentResourcesByName(deploymentId: String, resourceNames: List<String>): DeploymentBuilder {
    return this
  }

  override fun name(name: String): DeploymentBuilder {
    return this
  }

  override fun nameFromDeployment(deploymentId: String): DeploymentBuilder {
    return this
  }

  override fun enableDuplicateFiltering(): DeploymentBuilder {
    return this
  }

  override fun enableDuplicateFiltering(deployChangedOnly: Boolean): DeploymentBuilder {
    return this
  }

  override fun activateProcessDefinitionsOn(date: Date): DeploymentBuilder {
    return this
  }

  override fun source(source: String): DeploymentBuilder {
    return this
  }

  override fun deploy(): Deployment {
    TODO("Not yet implemented")
  }

  override fun deployWithResult(): DeploymentWithDefinitions {
    TODO("Not yet implemented")
  }

  override fun getResourceNames(): MutableCollection<String> {
    TODO("Not yet implemented")
  }

  override fun tenantId(tenantId: String?): DeploymentBuilder {
    TODO("Not yet implemented")
  }
}
