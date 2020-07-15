package org.camunda.bpm.extension.rest.impl

import com.fasterxml.jackson.databind.ObjectMapper
import org.camunda.bpm.engine.ProcessEngine
import org.camunda.bpm.engine.rest.dto.externaltask.CompleteExternalTaskDto
import org.camunda.bpm.extension.rest.adapter.AbstractExternalTaskServiceAdapter
import org.camunda.bpm.extension.rest.client.ExternalTaskServiceClient
import org.camunda.bpm.extension.rest.variables.ValueMapper
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

@Component
@Qualifier("remote")
class RemoteExternalTaskService(
  private val externalTaskServiceClient: ExternalTaskServiceClient,
  processEngine: ProcessEngine,
  objectMapper: ObjectMapper
) : AbstractExternalTaskServiceAdapter() {

  private val valueMapper: ValueMapper = ValueMapper(processEngine, objectMapper)

  override fun complete(externalTaskId: String, workerId: String) {
    this.complete(externalTaskId, workerId, mutableMapOf())
  }

  override fun complete(externalTaskId: String, workerId: String, variables: MutableMap<String, Any>) {
    this.complete(externalTaskId, workerId, variables, mutableMapOf())
  }

  override fun complete(externalTaskId: String, workerId: String, variables: MutableMap<String, Any>, localVariables: MutableMap<String, Any>) {
    return externalTaskServiceClient.completeTask(externalTaskId, CompleteExternalTaskDto().apply {
      this.variables = valueMapper.mapValues(variables)
      this.localVariables = valueMapper.mapValues(localVariables)
      this.workerId = workerId
    })
  }
}
