package org.camunda.bpm.extension.feign.adapter

import org.camunda.bpm.engine.repository.ProcessDefinition
import org.camunda.bpm.engine.rest.dto.repository.ProcessDefinitionDto

class ProcessDefinitionAdapter(private val processDefinitionBean: ProcessDefinitionBean) : ProcessDefinition {
  override fun getVersionTag() = processDefinitionBean.versionTag
  override fun getName() = processDefinitionBean.name
  override fun getId() = processDefinitionBean.id
  override fun getDeploymentId() = processDefinitionBean.deploymentId
  override fun isSuspended() = processDefinitionBean.suspended
  override fun getCategory() = processDefinitionBean.category
  override fun getKey() = processDefinitionBean.key
  override fun getVersion() = processDefinitionBean.version
  override fun getDescription() = processDefinitionBean.description
  override fun getTenantId() = processDefinitionBean.tenantId
  override fun getResourceName() = processDefinitionBean.resourceName
  override fun getDiagramResourceName() = processDefinitionBean.diagramResourceName
  override fun hasStartFormKey() = processDefinitionBean.hasStartFormKey
  override fun isStartableInTasklist() = processDefinitionBean.startableInTaskList
  override fun getHistoryTimeToLive() = processDefinitionBean.historyTimeToLive
}

data class ProcessDefinitionBean(
  val versionTag: String?,
  val id: String?,
  val name: String?,
  val key: String?,
  val category: String?,
  val deploymentId: String?,
  val suspended: Boolean,
  val historyTimeToLive: Int?,
  val startableInTaskList: Boolean,
  val hasStartFormKey: Boolean,
  val resourceName: String?,
  val diagramResourceName: String?,
  val tenantId: String?,
  val description: String?,
  val version: Int
) {
  companion object {
    fun fromDto(dto: ProcessDefinitionDto) = ProcessDefinitionBean(
      versionTag = dto.versionTag,
      id = dto.id,
      name = dto.name,
      key = dto.key,
      category = dto.category,
      deploymentId = dto.deploymentId,
      suspended = dto.isSuspended,
      historyTimeToLive = dto.historyTimeToLive,
      startableInTaskList = dto.isStartableInTasklist,
      hasStartFormKey = false, // FIXME
      resourceName = dto.resource,
      diagramResourceName = dto.diagram,
      tenantId = dto.tenantId,
      description = dto.description,
      version = dto.version
    )
  }
}
