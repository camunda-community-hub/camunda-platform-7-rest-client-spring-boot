package org.camunda.bpm.extension.rest.adapter

import org.camunda.bpm.engine.ExternalTaskService
import org.camunda.bpm.engine.batch.Batch
import org.camunda.bpm.engine.externaltask.ExternalTaskQuery
import org.camunda.bpm.engine.externaltask.ExternalTaskQueryBuilder
import org.camunda.bpm.engine.externaltask.UpdateExternalTaskRetriesSelectBuilder
import org.camunda.bpm.extension.rest.impl.RemoteExternalTaskService
import org.camunda.bpm.extension.rest.impl.RemoteRuntimeService
import org.camunda.bpm.extension.rest.impl.implementedBy

/**
 * Adapter for implementing external task service.
 */
abstract class AbstractExternalTaskServiceAdapter : ExternalTaskService {

  override fun complete(externalTaskId: String, workerId: String) {
    implementedBy(RemoteExternalTaskService::class)
  }

  override fun complete(externalTaskId: String, workerId: String, variables: MutableMap<String, Any>) {
    implementedBy(RemoteExternalTaskService::class)
  }

  override fun complete(externalTaskId: String, workerId: String, variables: MutableMap<String, Any>, localVariables: MutableMap<String, Any>) {
    implementedBy(RemoteExternalTaskService::class)
  }

  override fun unlock(externalTaskId: String) {
    TODO("not implemented")
  }

  override fun getExternalTaskErrorDetails(externalTaskId: String?): String {
    TODO("not implemented")
  }

  override fun createExternalTaskQuery(): ExternalTaskQuery {
    TODO("not implemented")
  }

  override fun setRetries(externalTaskId: String?, retries: Int) {
    TODO("not implemented")
  }

  override fun setRetries(externalTaskIds: MutableList<String>?, retries: Int) {
    TODO("not implemented")
  }

  override fun extendLock(externalTaskId: String?, workerId: String?, newLockDuration: Long) {
    TODO("not implemented")
  }

  override fun setRetriesAsync(externalTaskIds: MutableList<String>?, externalTaskQuery: ExternalTaskQuery?, retries: Int): Batch {
    TODO("not implemented")
  }

  override fun updateRetries(): UpdateExternalTaskRetriesSelectBuilder {
    TODO("not implemented")
  }

  override fun handleFailure(externalTaskId: String, workerId: String, errorMessage: String, retries: Int, retryTimeout: Long) {
    implementedBy(RemoteExternalTaskService::class)
  }

  override fun handleFailure(externalTaskId: String, workerId: String, errorMessage: String?, errorDetails: String?, retries: Int, retryTimeout: Long) {
    implementedBy(RemoteExternalTaskService::class)
  }

  override fun fetchAndLock(maxTasks: Int, workerId: String?): ExternalTaskQueryBuilder {
    TODO("not implemented")
  }

  override fun fetchAndLock(maxTasks: Int, workerId: String?, usePriority: Boolean): ExternalTaskQueryBuilder {
    TODO("not implemented")
  }

  override fun handleBpmnError(externalTaskId: String, workerId: String, errorCode: String) {
    implementedBy(RemoteExternalTaskService::class)
  }

  override fun handleBpmnError(externalTaskId: String, workerId: String, errorCode: String, errorMessage: String) {
    implementedBy(RemoteExternalTaskService::class)
  }

  override fun handleBpmnError(externalTaskId: String, workerId: String, errorCode: String?, errorMessage: String?, variables: MutableMap<String, Any>) {
    implementedBy(RemoteExternalTaskService::class)
  }

  override fun setPriority(externalTaskId: String?, priority: Long) {
    TODO("not implemented")
  }

  override fun getTopicNames(): MutableList<String> {
    TODO("Not yet implemented")
  }

  override fun getTopicNames(withLockedTasks: Boolean, withUnlockedTasks: Boolean, withRetriesLeft: Boolean): MutableList<String> {
    TODO("Not yet implemented")
  }
}
