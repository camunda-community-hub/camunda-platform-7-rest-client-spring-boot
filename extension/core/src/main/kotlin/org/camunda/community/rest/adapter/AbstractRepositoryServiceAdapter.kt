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

import org.camunda.bpm.application.ProcessApplicationReference
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.repository.*
import org.camunda.bpm.engine.task.IdentityLink
import org.camunda.community.rest.impl.RemoteRepositoryService
import org.camunda.community.rest.impl.implementedBy
import org.camunda.bpm.model.bpmn.BpmnModelInstance
import org.camunda.bpm.model.cmmn.CmmnModelInstance
import org.camunda.bpm.model.dmn.DmnModelInstance
import java.io.InputStream
import java.util.*

/**
 * Adapter for implementing repository service.
 */
abstract class AbstractRepositoryServiceAdapter : RepositoryService {
  override fun updateDecisionDefinitionHistoryTimeToLive(decisionDefinitionId: String?, historyTimeToLive: Int?) {
    implementedBy(RemoteRepositoryService::class)
  }

  override fun updateProcessDefinitionHistoryTimeToLive(processDefinitionId: String?, historyTimeToLive: Int?) {
    implementedBy(RemoteRepositoryService::class)
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
    implementedBy(RemoteRepositoryService::class)
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
    implementedBy(RemoteRepositoryService::class)
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
    implementedBy(RemoteRepositoryService::class)
  }

  override fun getDecisionRequirementsModel(decisionRequirementsDefinitionId: String?): InputStream {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun createDeployment(): DeploymentBuilder {
    implementedBy(RemoteRepositoryService::class)
  }

  override fun createDeployment(processApplication: ProcessApplicationReference?): ProcessApplicationDeploymentBuilder {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun deleteDeployment(deploymentId: String?) {
    implementedBy(RemoteRepositoryService::class)
  }

  override fun deleteDeployment(deploymentId: String?, cascade: Boolean) {
    implementedBy(RemoteRepositoryService::class)
  }

  override fun deleteDeployment(deploymentId: String?, cascade: Boolean, skipCustomListeners: Boolean) {
    implementedBy(RemoteRepositoryService::class)
  }

  override fun deleteDeployment(deploymentId: String?, cascade: Boolean, skipCustomListeners: Boolean, skipIoMappings: Boolean) {
    implementedBy(RemoteRepositoryService::class)
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
    implementedBy(RemoteRepositoryService::class)
  }

  override fun deleteProcessDefinition(processDefinitionId: String?, cascade: Boolean) {
    implementedBy(RemoteRepositoryService::class)
  }

  override fun deleteProcessDefinition(processDefinitionId: String?, cascade: Boolean, skipCustomListeners: Boolean) {
    implementedBy(RemoteRepositoryService::class)
  }

  override fun deleteProcessDefinition(processDefinitionId: String?, cascade: Boolean, skipCustomListeners: Boolean, skipIoMappings: Boolean) {
    implementedBy(RemoteRepositoryService::class)
  }

  override fun updateCaseDefinitionHistoryTimeToLive(caseDefinitionId: String?, historyTimeToLive: Int?) {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun getProcessModel(processDefinitionId: String?): InputStream {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun createDeploymentQuery(): DeploymentQuery {
    implementedBy(RemoteRepositoryService::class)
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
    implementedBy(RepositoryService::class)
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

  /**
   * @since 7.16
   */
  override fun getStaticCalledProcessDefinitions(processDefinitionId: String?): MutableCollection<CalledProcessDefinition> {
    TODO("Not yet implemented")
  }

}
