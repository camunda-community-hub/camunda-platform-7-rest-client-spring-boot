package org.camunda.bpm.extension.rest.adapter

import org.camunda.bpm.engine.repository.*
import org.camunda.bpm.extension.rest.client.model.*
import java.util.*

class DeploymentAdapter(private val deploymentBean: DeploymentBean) : DeploymentWithDefinitions {

  override fun getId() = deploymentBean.id

  override fun getName() = deploymentBean.name

  override fun getDeploymentTime() = deploymentBean.deploymentTime

  override fun getSource() = deploymentBean.source

  override fun getTenantId() = deploymentBean.tenantId

  override fun getDeployedProcessDefinitions() = deploymentBean.deployedProcessDefinitions

  override fun getDeployedCaseDefinitions() = deploymentBean.deployedCaseDefinitions

  override fun getDeployedDecisionDefinitions() = deploymentBean.deployedDecisionDefinitions

  override fun getDeployedDecisionRequirementsDefinitions() = deploymentBean.deployedDecisionRequirementsDefinitions
}

/**
 * POJO to hold the values of a task.
 */
data class DeploymentBean(
  val id: String,
  val name: String?,
  var deploymentTime: Date?,
  var source: String?,
  var tenantId: String?,
  var deployedProcessDefinitions: List<ProcessDefinition>?,
  var deployedCaseDefinitions: List<CaseDefinition>?,
  var deployedDecisionDefinitions: List<DecisionDefinition>?,
  var deployedDecisionRequirementsDefinitions: List<DecisionRequirementsDefinition>?,
) {
  companion object {
    /**
     * Factory method to create bean from REST representation.
     */
    @JvmStatic
    fun fromDto(dto: DeploymentWithDefinitionsDto) = DeploymentBean(
      id = dto.id,
      name = dto.name,
      deploymentTime = dto.deploymentTime,
      source = dto.source,
      tenantId = dto.tenantId,
      deployedCaseDefinitions = dto.deployedCaseDefinitions?.map { it.value.toBean() },
      deployedProcessDefinitions = dto.deployedProcessDefinitions?.map { it.value.toBean() },
      deployedDecisionDefinitions = dto.deployedDecisionDefinitions?.map { it.value.toBean() },
      deployedDecisionRequirementsDefinitions = dto.deployedDecisionRequirementsDefinitions?.map { it.value.toBean() }
    )
  }
}

fun CaseDefinitionDto.toBean() = object : CaseDefinition {
  override fun getId() = this@toBean.id
  override fun getCategory() = this@toBean.category
  override fun getName() = this@toBean.name
  override fun getKey() = this@toBean.key
  override fun getVersion() = this@toBean.version
  override fun getResourceName() = this@toBean.resource
  override fun getDeploymentId() = this@toBean.deploymentId
  override fun getDiagramResourceName() = this@toBean.resource //FIXME
  override fun getTenantId() = this@toBean.tenantId
  override fun getHistoryTimeToLive() = this@toBean.historyTimeToLive
}

fun ProcessDefinitionDto.toBean() = object : ProcessDefinition {
  override fun getId() = this@toBean.id
  override fun getCategory() = this@toBean.category
  override fun getName() = this@toBean.name
  override fun getKey() = this@toBean.key
  override fun getVersion() = this@toBean.version
  override fun getResourceName() = this@toBean.resource
  override fun getDeploymentId() = this@toBean.deploymentId
  override fun getDiagramResourceName() = this@toBean.resource
  override fun getTenantId() = this@toBean.tenantId
  override fun getHistoryTimeToLive() = this@toBean.historyTimeToLive
  override fun getDescription() = this@toBean.description
  override fun hasStartFormKey() = false //FIXME
  override fun isSuspended() = this@toBean.suspended
  override fun getVersionTag() = this@toBean.versionTag
  override fun isStartableInTasklist() = this@toBean.startableInTasklist
}

fun DecisionDefinitionDto.toBean() = object : DecisionDefinition {
  override fun getId() = this@toBean.id
  override fun getCategory() = this@toBean.category
  override fun getName() = this@toBean.name
  override fun getKey() = this@toBean.key
  override fun getVersion() = this@toBean.version
  override fun getResourceName() = this@toBean.resource
  override fun getDeploymentId() = this@toBean.deploymentId
  override fun getDiagramResourceName() = this@toBean.resource //FIXME
  override fun getTenantId() = this@toBean.tenantId
  override fun getHistoryTimeToLive() = this@toBean.historyTimeToLive
  override fun getDecisionRequirementsDefinitionId() = this@toBean.decisionRequirementsDefinitionId
  override fun getDecisionRequirementsDefinitionKey() = this@toBean.decisionRequirementsDefinitionKey
  override fun getVersionTag() = this@toBean.versionTag
}

fun DecisionRequirementsDefinitionDto.toBean() = object : DecisionRequirementsDefinition {
  override fun getId() = this@toBean.id
  override fun getCategory() = this@toBean.category
  override fun getName() = this@toBean.name
  override fun getKey() = this@toBean.key
  override fun getVersion() = this@toBean.version
  override fun getResourceName() = this@toBean.resource
  override fun getDeploymentId() = this@toBean.deploymentId
  override fun getDiagramResourceName() = this@toBean.resource //FIXME
  override fun getTenantId() = this@toBean.tenantId
  override fun getHistoryTimeToLive() = null //FIXME
}
