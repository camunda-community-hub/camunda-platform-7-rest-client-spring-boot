package org.camunda.community.rest.adapter

import org.camunda.bpm.engine.ExternalTaskService
import org.camunda.bpm.engine.batch.Batch
import org.camunda.bpm.engine.externaltask.ExternalTaskQuery
import org.camunda.bpm.engine.externaltask.ExternalTaskQueryBuilder
import org.camunda.bpm.engine.externaltask.FetchAndLockBuilder
import org.camunda.bpm.engine.externaltask.UpdateExternalTaskRetriesSelectBuilder
import org.camunda.community.rest.impl.RemoteExternalTaskService
import org.camunda.community.rest.impl.RemoteRuntimeService
import org.camunda.community.rest.impl.implementedBy
import java.util.*

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
    implementedBy(RemoteExternalTaskService::class)
  }

  override fun getExternalTaskErrorDetails(externalTaskId: String): String {
    implementedBy(RemoteExternalTaskService::class)
  }

  override fun createExternalTaskQuery(): ExternalTaskQuery {
    implementedBy(RemoteExternalTaskService::class)
  }

  override fun setRetries(externalTaskId: String, retries: Int) {
    implementedBy(RemoteExternalTaskService::class)
  }

  override fun setRetries(externalTaskIds: List<String>, retries: Int) {
    implementedBy(RemoteExternalTaskService::class)
  }

  override fun extendLock(externalTaskId: String, workerId: String, newLockDuration: Long) {
    implementedBy(RemoteExternalTaskService::class)
  }

  override fun setRetriesAsync(externalTaskIds: List<String>?, externalTaskQuery: ExternalTaskQuery?, retries: Int): Batch {
    implementedBy(RemoteExternalTaskService::class)
  }

  override fun updateRetries(): UpdateExternalTaskRetriesSelectBuilder {
    implementedBy(RemoteExternalTaskService::class)
  }

  override fun handleFailure(externalTaskId: String, workerId: String, errorMessage: String, retries: Int, retryTimeout: Long) {
    implementedBy(RemoteExternalTaskService::class)
  }

  override fun handleFailure(externalTaskId: String, workerId: String, errorMessage: String?, errorDetails: String?, retries: Int, retryTimeout: Long) {
    implementedBy(RemoteExternalTaskService::class)
  }

  override fun fetchAndLock(maxTasks: Int, workerId: String): ExternalTaskQueryBuilder {
    implementedBy(RemoteExternalTaskService::class)
  }

  override fun fetchAndLock(maxTasks: Int, workerId: String, usePriority: Boolean): ExternalTaskQueryBuilder {
    implementedBy(RemoteExternalTaskService::class)
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

  override fun setPriority(externalTaskId: String, priority: Long) {
    implementedBy(RemoteExternalTaskService::class)
  }

  override fun getTopicNames(): List<String> {
    implementedBy(RemoteExternalTaskService::class)
  }

  override fun getTopicNames(withLockedTasks: Boolean, withUnlockedTasks: Boolean, withRetriesLeft: Boolean): List<String> {
    implementedBy(RemoteExternalTaskService::class)
  }

  /**
   * @since 7.15
   */
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
    implementedBy(RemoteExternalTaskService::class)
  }

  /**
   * @since 7.15
   */
  override fun lock(externalTaskId: String, workerId: String, lockDuration: Long) {
    implementedBy(RemoteExternalTaskService::class)
  }

  /**
   * @since 7.21
   */
  override fun fetchAndLock(): FetchAndLockBuilder {
    TODO("Not yet implemented")
  }

}
