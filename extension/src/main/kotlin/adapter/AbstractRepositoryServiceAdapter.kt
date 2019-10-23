package org.camunda.bpm.extension.feign.adapter

import org.camunda.bpm.application.ProcessApplicationReference
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.repository.*
import org.camunda.bpm.engine.task.IdentityLink
import org.camunda.bpm.model.bpmn.BpmnModelInstance
import org.camunda.bpm.model.cmmn.CmmnModelInstance
import org.camunda.bpm.model.dmn.DmnModelInstance
import java.io.InputStream
import java.util.*

abstract class AbstractRepositoryServiceAdapter : RepositoryService {
  override fun updateDecisionDefinitionHistoryTimeToLive(decisionDefinitionId: String?, historyTimeToLive: Int?) {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun updateProcessDefinitionHistoryTimeToLive(processDefinitionId: String?, historyTimeToLive: Int?) {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun addCandidateStarterUser(processDefinitionId: String?, userId: String?) {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun deleteCandidateStarterUser(processDefinitionId: String?, userId: String?) {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun getDecisionRequirementsDefinition(decisionRequirementsDefinitionId: String?): DecisionRequirementsDefinition {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun getCmmnModelInstance(caseDefinitionId: String?): CmmnModelInstance {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun deleteDeploymentCascade(deploymentId: String?) {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun getProcessDefinition(processDefinitionId: String?): ProcessDefinition {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun activateProcessDefinitionByKey(processDefinitionKey: String?) {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun activateProcessDefinitionByKey(processDefinitionKey: String?, activateProcessInstances: Boolean, activationDate: Date?) {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun getBpmnModelInstance(processDefinitionId: String?): BpmnModelInstance {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun getDmnModelInstance(decisionDefinitionId: String?): DmnModelInstance {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun getDecisionModel(decisionDefinitionId: String?): InputStream {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun getResourceAsStream(deploymentId: String?, resourceName: String?): InputStream {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun getProcessDiagramLayout(processDefinitionId: String?): DiagramLayout {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun suspendProcessDefinitionById(processDefinitionId: String?) {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun suspendProcessDefinitionById(processDefinitionId: String?, suspendProcessInstances: Boolean, suspensionDate: Date?) {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun getDecisionDiagram(decisionDefinitionId: String?): InputStream {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun activateProcessDefinitionById(processDefinitionId: String?) {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun activateProcessDefinitionById(processDefinitionId: String?, activateProcessInstances: Boolean, activationDate: Date?) {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun getResourceAsStreamById(deploymentId: String?, resourceId: String?): InputStream {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun addCandidateStarterGroup(processDefinitionId: String?, groupId: String?) {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun getDecisionDefinition(decisionDefinitionId: String?): DecisionDefinition {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun updateProcessDefinitionSuspensionState(): UpdateProcessDefinitionSuspensionStateSelectBuilder {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun getDecisionRequirementsModel(decisionRequirementsDefinitionId: String?): InputStream {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun createDeployment(): DeploymentBuilder {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun createDeployment(processApplication: ProcessApplicationReference?): ProcessApplicationDeploymentBuilder {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun deleteDeployment(deploymentId: String?) {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun deleteDeployment(deploymentId: String?, cascade: Boolean) {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun deleteDeployment(deploymentId: String?, cascade: Boolean, skipCustomListeners: Boolean) {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun deleteDeployment(deploymentId: String?, cascade: Boolean, skipCustomListeners: Boolean, skipIoMappings: Boolean) {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun getProcessDiagram(processDefinitionId: String?): InputStream {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun deleteProcessDefinitions(): DeleteProcessDefinitionsSelectBuilder {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun createDecisionDefinitionQuery(): DecisionDefinitionQuery {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun getDecisionRequirementsDiagram(decisionRequirementsDefinitionId: String?): InputStream {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun suspendProcessDefinitionByKey(processDefinitionKey: String?) {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun suspendProcessDefinitionByKey(processDefinitionKey: String?, suspendProcessInstances: Boolean, suspensionDate: Date?) {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun getIdentityLinksForProcessDefinition(processDefinitionId: String?): MutableList<IdentityLink> {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun deleteProcessDefinition(processDefinitionId: String?) {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun deleteProcessDefinition(processDefinitionId: String?, cascade: Boolean) {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun deleteProcessDefinition(processDefinitionId: String?, cascade: Boolean, skipCustomListeners: Boolean) {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun deleteProcessDefinition(processDefinitionId: String?, cascade: Boolean, skipCustomListeners: Boolean, skipIoMappings: Boolean) {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun updateCaseDefinitionHistoryTimeToLive(caseDefinitionId: String?, historyTimeToLive: Int?) {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun getProcessModel(processDefinitionId: String?): InputStream {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun createDeploymentQuery(): DeploymentQuery {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun deleteCandidateStarterGroup(processDefinitionId: String?, groupId: String?) {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun getDeploymentResources(deploymentId: String?): MutableList<Resource> {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun createCaseDefinitionQuery(): CaseDefinitionQuery {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun getCaseDefinition(caseDefinitionId: String?): CaseDefinition {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun createProcessDefinitionQuery(): ProcessDefinitionQuery {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun getDeploymentResourceNames(deploymentId: String?): MutableList<String> {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun getCaseDiagram(caseDefinitionId: String?): InputStream {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun createDecisionRequirementsDefinitionQuery(): DecisionRequirementsDefinitionQuery {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun getCaseModel(caseDefinitionId: String?): InputStream {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

}
