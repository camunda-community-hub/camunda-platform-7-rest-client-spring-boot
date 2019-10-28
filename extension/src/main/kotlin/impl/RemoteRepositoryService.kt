package org.camunda.bpm.extension.feign.impl

import org.camunda.bpm.engine.repository.ProcessDefinitionQuery
import org.camunda.bpm.extension.feign.adapter.AbstractRepositoryServiceAdapter
import org.camunda.bpm.extension.feign.client.RepositoryServiceClient
import org.camunda.bpm.extension.feign.impl.query.DelegatingProcessDefinitionQuery
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component


/**
 * Remote implementation of Camunda Core RepositoryService API, delegating
 * all request over HTTP to a remote Camunda Engine.
 */
@Component
@Qualifier("remote")
class RemoteRepositoryService(
  private val repositoryServiceClient: RepositoryServiceClient
) : AbstractRepositoryServiceAdapter() {

  override fun createProcessDefinitionQuery(): ProcessDefinitionQuery {
    return DelegatingProcessDefinitionQuery(repositoryServiceClient)
  }
}
