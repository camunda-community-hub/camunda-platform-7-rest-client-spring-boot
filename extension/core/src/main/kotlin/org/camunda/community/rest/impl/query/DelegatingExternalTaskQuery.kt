package org.camunda.community.rest.impl.query

import mu.KLogging
import org.camunda.bpm.engine.externaltask.ExternalTask
import org.camunda.bpm.engine.externaltask.ExternalTaskQuery
import org.camunda.community.rest.adapter.ExternalTaskAdapter
import org.camunda.community.rest.adapter.ExternalTaskBean
import org.camunda.community.rest.client.api.ExternalTaskApiClient
import org.camunda.community.rest.client.model.ExternalTaskQueryDto
import org.camunda.community.rest.impl.toExternalTaskSorting
import java.util.*
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

/**
 * Implementation of the external task query.
 */
class DelegatingExternalTaskQuery(
  private val externalTaskApiClient: ExternalTaskApiClient,
  var externalTaskId: String? = null,
  var externalTaskIds: List<String>? = null,
  var workerId: String? = null,
  var topicName: String? = null,
  var processInstanceId: String? = null,
  var processInstanceIds: List<String>? = null,
  var processDefinitionId: String? = null,
  var executionId: String? = null,
  var suspensionState: SuspensionState? = null,
  var activityId: String? = null,
  var activityIds: Array<out String>? = null,
  var withRetriesLeft: Boolean? = null,
  var noRetriesLeft: Boolean? = null,
  var priorityHigherThanOrEquals: Long? = null,
  var priorityLowerThanOrEquals: Long? = null,
  var locked: Boolean? = null,
  var notLocked: Boolean? = null,
  var lockExpirationBefore: Date? = null,
  var lockExpirationAfter: Date? = null,
) : BaseQuery<ExternalTaskQuery, ExternalTask>(), ExternalTaskQuery {

  companion object : KLogging()

  override fun processInstanceId(processInstanceId: String?) = this.apply { this.processInstanceId = requireNotNull(processInstanceId) }
  override fun processInstanceIdIn(vararg processInstanceIdIn: String) = this.apply { this.processInstanceIds = processInstanceIdIn.toList() }
  override fun processDefinitionId(processDefinitionId: String?) = this.apply { this.processDefinitionId = requireNotNull(processDefinitionId) }
  override fun activityId(activityId: String?) = this.apply { this.activityId = requireNotNull(activityId) }

  override fun suspended() = this.apply { this.suspensionState = SuspensionState.SUSPENDED }

  override fun active() = this.apply { this.suspensionState = SuspensionState.ACTIVE }
  override fun withRetriesLeft() = this.apply { this.withRetriesLeft = true }

  override fun noRetriesLeft() = this.apply { this.noRetriesLeft = true }

  override fun activityIdIn(vararg activityIdIn: String) = this.apply { this.activityIds = activityIdIn }
  override fun priorityHigherThanOrEquals(priority: Long) = this.apply { this.priorityHigherThanOrEquals = priority }

  override fun priorityLowerThanOrEquals(priority: Long) = this.apply { this.priorityLowerThanOrEquals = priority }

  override fun externalTaskId(externalTaskId: String?) = this.apply { this.externalTaskId = requireNotNull(externalTaskId) }

  override fun externalTaskIdIn(externalTaskIds: Set<String>?) = this.apply { this.externalTaskIds = requireNotNull(externalTaskIds).toList() }

  override fun workerId(workerId: String?) = this.apply { this.workerId = requireNotNull(workerId) }

  override fun lockExpirationBefore(lockExpirationDate: Date?) = this.apply { this.lockExpirationBefore = requireNotNull(lockExpirationDate) }

  override fun lockExpirationAfter(lockExpirationDate: Date?) = this.apply { this.lockExpirationAfter = requireNotNull(lockExpirationDate) }

  override fun topicName(topicName: String?) = this.apply { this.topicName = requireNotNull(topicName) }

  override fun locked() = this.apply { this.locked = true }

  override fun notLocked() = this.apply { this.notLocked = true }

  override fun executionId(executionId: String?) = this.apply { this.executionId = requireNotNull(executionId) }

  override fun orderById() = this.apply { orderBy("id") }

  override fun orderByLockExpirationTime() = this.apply { orderBy("lockExpirationTime") }

  override fun orderByProcessInstanceId() = this.apply { orderBy("processInstanceId") }

  override fun orderByProcessDefinitionKey() = this.apply { orderBy("processDefinitionKey") }
  override fun orderByPriority() = this.apply { orderBy("taskPriority") }
  override fun orderByProcessDefinitionId() = this.apply { orderBy("processDefinitionId") }

  override fun listPage(firstResult: Int, maxResults: Int): List<ExternalTask> =
    externalTaskApiClient.queryExternalTasks(firstResult, maxResults, fillQueryDto()).body!!.map {
      ExternalTaskAdapter(ExternalTaskBean.fromDto(it))
    }


  override fun count() = externalTaskApiClient.queryExternalTasksCount(fillQueryDto()).body!!.count

  /**
   * Mapping for the query DTO, which will be sent to the REST endpoint.
   */
  fun fillQueryDto() = ExternalTaskQueryDto().apply {
    validate()
    val dtoPropertiesByName = ExternalTaskQueryDto::class.memberProperties.filterIsInstance<KMutableProperty1<ExternalTaskQueryDto, Any?>>().associateBy { it.name }
    dtoPropertiesByName.forEach {
      val valueToSet = when (it.key) {
        "externalTaskIdIn" -> this@DelegatingExternalTaskQuery.externalTaskIds
        "processInstanceIdIn" -> this@DelegatingExternalTaskQuery.processInstanceIds
        "active" -> this@DelegatingExternalTaskQuery.suspensionState?.let { it == SuspensionState.ACTIVE }
        "suspended" -> this@DelegatingExternalTaskQuery.suspensionState?.let { it == SuspensionState.SUSPENDED }
        "tenantIdIn" -> this@DelegatingExternalTaskQuery.tenantIds?.toList()
        "activityIdIn" -> this@DelegatingExternalTaskQuery.activityIds?.toList()
        "sorting" -> this@DelegatingExternalTaskQuery.orderingProperties.map { it.toExternalTaskSorting() }.filter { it.sortBy != null }
        else -> valueForProperty(it.key, this@DelegatingExternalTaskQuery, it.value.returnType)
      }
      it.value.isAccessible = true
      it.value.set(this, valueToSet)
    }
  }

}

