package org.camunda.community.rest.impl

import com.fasterxml.jackson.databind.ObjectMapper
import org.camunda.bpm.engine.variable.type.ValueTypeResolver
import org.camunda.community.rest.adapter.AbstractExternalTaskServiceAdapter
import org.camunda.community.rest.client.api.ExternalTaskApiClient
import org.camunda.community.rest.client.model.CompleteExternalTaskDto
import org.camunda.community.rest.client.model.ExternalTaskBpmnError
import org.camunda.community.rest.client.model.ExternalTaskFailureDto
import org.camunda.community.rest.variables.ValueMapper
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component


/**
 * Remote implementation of Camunda Core ExternalTaskService API, delegating
 * all request over HTTP to a remote Camunda Engine.
 */
@Component
@Qualifier("remote")
class RemoteExternalTaskService(
  private val externalTaskApiClient: ExternalTaskApiClient,
  valueTypeResolver: ValueTypeResolver,
  objectMapper: ObjectMapper
) : AbstractExternalTaskServiceAdapter() {

  private val valueMapper: ValueMapper = ValueMapper(objectMapper, valueTypeResolver)

  override fun complete(externalTaskId: String, workerId: String) {
    this.complete(externalTaskId, workerId, mutableMapOf())
  }

  override fun complete(externalTaskId: String, workerId: String, variables: MutableMap<String, Any>) {
    this.complete(externalTaskId, workerId, variables, mutableMapOf())
  }

  override fun complete(
    externalTaskId: String,
    workerId: String,
    variables: MutableMap<String, Any>,
    localVariables: MutableMap<String, Any>
  ) {
    externalTaskApiClient.completeExternalTaskResource(externalTaskId, CompleteExternalTaskDto().apply {
      this.variables = valueMapper.mapValues(variables)
      this.localVariables = valueMapper.mapValues(localVariables)
      this.workerId = workerId
    })
  }

  override fun handleBpmnError(externalTaskId: String, workerId: String, errorCode: String) {
    this.handleBpmnError(externalTaskId, workerId, errorCode, null, mutableMapOf())
  }

  override fun handleBpmnError(externalTaskId: String, workerId: String, errorCode: String, errorMessage: String) {
    this.handleBpmnError(externalTaskId, workerId, errorCode, errorMessage, mutableMapOf())
  }

  override fun handleBpmnError(
    externalTaskId: String,
    workerId: String,
    errorCode: String?,
    errorMessage: String?,
    variables: MutableMap<String, Any>
  ) {
    externalTaskApiClient.handleExternalTaskBpmnError(externalTaskId, ExternalTaskBpmnError().apply {
      this.workerId = workerId
      this.errorCode = errorCode
      this.errorMessage = errorMessage
      this.variables = valueMapper.mapValues(variables)
    })
  }

  override fun handleFailure(externalTaskId: String, workerId: String, errorMessage: String, retries: Int, retryTimeout: Long) {
    this.handleFailure(externalTaskId, workerId, errorMessage, null, retries, retryTimeout)
  }

  override fun handleFailure(
    externalTaskId: String,
    workerId: String,
    errorMessage: String?,
    errorDetails: String?,
    retries: Int,
    retryTimeout: Long
  ) {
    externalTaskApiClient.handleFailure(externalTaskId, ExternalTaskFailureDto().apply {
      this.workerId = workerId
      this.retries = retries
      this.retryTimeout = retryTimeout
      this.errorDetails = errorDetails
      this.errorMessage = errorMessage
    })
  }
}
