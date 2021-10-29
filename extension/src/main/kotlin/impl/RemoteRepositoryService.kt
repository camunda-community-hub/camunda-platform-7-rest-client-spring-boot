/*-
 * #%L
 * camunda-rest-client-spring-boot
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

package org.camunda.bpm.extension.rest.impl

import org.camunda.bpm.engine.impl.util.EnsureUtil
import org.camunda.bpm.engine.repository.DeploymentQuery
import org.camunda.bpm.engine.repository.ProcessDefinitionQuery
import org.camunda.bpm.extension.rest.adapter.AbstractRepositoryServiceAdapter
import org.camunda.bpm.extension.rest.client.api.DecisionDefinitionApiClient
import org.camunda.bpm.extension.rest.client.api.DeploymentApiClient
import org.camunda.bpm.extension.rest.client.api.ProcessDefinitionApiClient
import org.camunda.bpm.extension.rest.client.model.HistoryTimeToLiveDto
import org.camunda.bpm.extension.rest.impl.builder.DelegatingDeploymentBuilder
import org.camunda.bpm.extension.rest.impl.builder.DelegatingUpdateProcessDefinitionSuspensionStateSelectBuilder
import org.camunda.bpm.extension.rest.impl.query.DelegatingDeploymentQuery
import org.camunda.bpm.extension.rest.impl.query.DelegatingProcessDefinitionQuery
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component


/**
 * Remote implementation of Camunda Core RepositoryService API, delegating
 * all request over HTTP to a remote Camunda Engine.
 */
@Component
@Qualifier("remote")
class RemoteRepositoryService(
  private val processDefinitionApiClient: ProcessDefinitionApiClient,
  private val decisionDefinitionApiClient: DecisionDefinitionApiClient,
  private val deploymentApiClient: DeploymentApiClient
) : AbstractRepositoryServiceAdapter() {

  override fun createProcessDefinitionQuery(): ProcessDefinitionQuery = DelegatingProcessDefinitionQuery(processDefinitionApiClient)

  override fun createDeploymentQuery(): DeploymentQuery = DelegatingDeploymentQuery(deploymentApiClient)

  override fun createDeployment() = DelegatingDeploymentBuilder(deploymentApiClient)

  override fun deleteDeployment(deploymentId: String?) =
    deleteDeployment(deploymentId, cascade = false, skipCustomListeners = false, skipIoMappings = false)

  override fun deleteDeploymentCascade(deploymentId: String?) =
    deleteDeployment(deploymentId, cascade = true)

  override fun deleteDeployment(deploymentId: String?, cascade: Boolean) =
    deleteDeployment(deploymentId, cascade = cascade, skipCustomListeners = false, skipIoMappings = false)

  override fun deleteDeployment(deploymentId: String?, cascade: Boolean, skipCustomListeners: Boolean) =
    deleteDeployment(deploymentId, cascade = cascade, skipCustomListeners = skipCustomListeners, skipIoMappings = false)

  override fun deleteDeployment(deploymentId: String?, cascade: Boolean, skipCustomListeners: Boolean, skipIoMappings: Boolean) {
    EnsureUtil.ensureNotNull("deploymentId", deploymentId)
    deploymentApiClient.deleteDeployment(deploymentId, cascade, skipCustomListeners, skipIoMappings)
  }

  override fun updateDecisionDefinitionHistoryTimeToLive(decisionDefinitionId: String?, historyTimeToLive: Int?) {
    decisionDefinitionApiClient.updateHistoryTimeToLiveByDecisionDefinitionId(decisionDefinitionId, HistoryTimeToLiveDto().historyTimeToLive(historyTimeToLive))
  }

  override fun updateProcessDefinitionHistoryTimeToLive(processDefinitionId: String?, historyTimeToLive: Int?) {
    processDefinitionApiClient.updateHistoryTimeToLiveByProcessDefinitionId(processDefinitionId, HistoryTimeToLiveDto().historyTimeToLive(historyTimeToLive))
  }

  override fun updateProcessDefinitionSuspensionState() = DelegatingUpdateProcessDefinitionSuspensionStateSelectBuilder(processDefinitionApiClient)

  override fun deleteProcessDefinition(processDefinitionId: String?) =
    deleteProcessDefinition(processDefinitionId, cascade = false, skipCustomListeners = false, skipIoMappings = false)

  override fun deleteProcessDefinition(processDefinitionId: String?, cascade: Boolean) =
    deleteProcessDefinition(processDefinitionId, cascade = cascade, skipCustomListeners = false, skipIoMappings = false)

  override fun deleteProcessDefinition(processDefinitionId: String?, cascade: Boolean, skipCustomListeners: Boolean) =
    deleteProcessDefinition(processDefinitionId, cascade, skipCustomListeners = skipCustomListeners, skipIoMappings = false)

  override fun deleteProcessDefinition(processDefinitionId: String?, cascade: Boolean, skipCustomListeners: Boolean, skipIoMappings: Boolean) {
    processDefinitionApiClient.deleteProcessDefinition(processDefinitionId, cascade, skipCustomListeners, skipIoMappings)
  }

}
