package org.camunda.community.rest.impl

import com.fasterxml.jackson.databind.ObjectMapper
import org.camunda.bpm.engine.batch.Batch
import org.camunda.bpm.engine.externaltask.ExternalTaskQuery
import org.camunda.bpm.engine.externaltask.UpdateExternalTaskRetriesSelectBuilder
import org.camunda.bpm.engine.variable.type.ValueTypeResolver
import org.camunda.community.rest.adapter.AbstractExternalTaskServiceAdapter
import org.camunda.community.rest.client.api.ExternalTaskApiClient
import org.camunda.community.rest.client.model.CompleteExternalTaskDto
import org.camunda.community.rest.client.model.ExtendLockOnExternalTaskDto
import org.camunda.community.rest.client.model.ExternalTaskBpmnError
import org.camunda.community.rest.client.model.ExternalTaskFailureDto
import org.camunda.community.rest.client.model.LockExternalTaskDto
import org.camunda.community.rest.client.model.PriorityDto
import org.camunda.community.rest.client.model.RetriesDto
import org.camunda.community.rest.client.model.SetRetriesForExternalTasksDto
import org.camunda.community.rest.config.CamundaRestClientProperties
import org.camunda.community.rest.impl.builder.RemoteExternalTaskQueryBuilder
import org.camunda.community.rest.impl.builder.RemoteUpdateExternalTaskRetriesBuilder
import org.camunda.community.rest.impl.query.DelegatingExternalTaskQuery
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
  private val camundaRestClientProperties: CamundaRestClientProperties,
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

  override fun fetchAndLock(maxTasks: Int, workerId: String) =
    RemoteExternalTaskQueryBuilder(externalTaskApiClient, valueMapper, camundaRestClientProperties,
      workerId = workerId, maxTasks = maxTasks)

  override fun fetchAndLock(maxTasks: Int, workerId: String, usePriority: Boolean) =
    RemoteExternalTaskQueryBuilder(externalTaskApiClient, valueMapper, camundaRestClientProperties,
      workerId = workerId, maxTasks = maxTasks, usePriority = usePriority)

  override fun extendLock(externalTaskId: String, workerId: String, newLockDuration: Long) {
    externalTaskApiClient.extendLock(externalTaskId, ExtendLockOnExternalTaskDto().apply {
      this.workerId = workerId
      this.newDuration = newLockDuration
    })
  }

  override fun lock(externalTaskId: String, workerId: String, lockDuration: Long) {
    externalTaskApiClient.lock(externalTaskId, LockExternalTaskDto().apply {
      this.workerId = workerId
      this.lockDuration = lockDuration
    })
  }

  override fun unlock(externalTaskId: String) {
    externalTaskApiClient.unlock(externalTaskId)
  }

  override fun getTopicNames(): List<String> =
    getTopicNames(withLockedTasks = false, withUnlockedTasks = false, withRetriesLeft = false)

  override fun getTopicNames(withLockedTasks: Boolean, withUnlockedTasks: Boolean, withRetriesLeft: Boolean): List<String> =
    externalTaskApiClient.getTopicNames(withLockedTasks, withUnlockedTasks, withRetriesLeft).body!!

  override fun getExternalTaskErrorDetails(externalTaskId: String): String =
    externalTaskApiClient.getExternalTaskErrorDetails(externalTaskId).body!!

  override fun setPriority(externalTaskId: String, priority: Long) {
    externalTaskApiClient.setExternalTaskResourcePriority(externalTaskId, PriorityDto().priority(priority))
  }

  override fun handleFailure(
    externalTaskId: String,
    workerId: String,
    errorMessage: String,
    errorDetails: String,
    retries: Int,
    retryDuration: Long,
    variables: MutableMap<String, Any>,
    localVariables: MutableMap<String, Any>
  ) {
    externalTaskApiClient.handleFailure(externalTaskId, ExternalTaskFailureDto().apply {
      this.workerId = workerId
      this.retries = retries
      this.retryTimeout = retryDuration
      this.errorDetails = errorDetails
      this.errorMessage = errorMessage
      this.variables = valueMapper.mapValues(variables)
      this.localVariables = valueMapper.mapValues(localVariables)
    })
  }

  override fun setRetries(externalTaskId: String, retries: Int) {
    externalTaskApiClient.setExternalTaskResourceRetries(externalTaskId, RetriesDto().retries(retries))
  }

  override fun setRetries(externalTaskIds: List<String>, retries: Int) {
    externalTaskApiClient.setExternalTaskRetries(SetRetriesForExternalTasksDto().apply {
      this.externalTaskIds = externalTaskIds
      this.retries = retries
    })
  }

  override fun setRetriesAsync(externalTaskIds: List<String>?, externalTaskQuery: ExternalTaskQuery?, retries: Int): Batch {
    return updateRetries()
      .externalTaskIds(externalTaskIds)
      .externalTaskQuery(externalTaskQuery)
      .setAsync(retries)
  }

  override fun updateRetries(): UpdateExternalTaskRetriesSelectBuilder =
    RemoteUpdateExternalTaskRetriesBuilder(externalTaskApiClient)

  override fun createExternalTaskQuery(): ExternalTaskQuery =
    DelegatingExternalTaskQuery(externalTaskApiClient)

}
