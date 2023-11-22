package org.camunda.community.rest.impl.query

import mu.KLogging
import org.camunda.bpm.engine.exception.NotValidException
import org.camunda.bpm.engine.runtime.Execution
import org.camunda.bpm.engine.runtime.ExecutionQuery
import org.camunda.community.rest.adapter.ExecutionAdapter
import org.camunda.community.rest.adapter.ExecutionBean
import org.camunda.community.rest.client.api.ExecutionApiClient
import org.camunda.community.rest.client.model.ExecutionQueryDto
import org.camunda.community.rest.impl.toExecutionSorting
import org.camunda.community.rest.variables.toDto
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

class DelegatingExecutionQuery(
  private val executionApiClient: ExecutionApiClient,
  var processDefinitionId: String? = null,
  var processDefinitionKey: String? = null,
  var businessKey: String? = null,
  var activityId: String? = null,
  var executionId: String? = null,
  var processInstanceId: String? = null,
  var suspensionState: SuspensionState? = null,
  var incidentType: String? = null,
  var incidentId: String? = null,
  var incidentMessage: String? = null,
  var incidentMessageLike: String? = null,
  val eventSubscriptions: MutableList<EventSubscriptionQueryValue> = mutableListOf()
) : BaseVariableQuery<ExecutionQuery, Execution>(), ExecutionQuery {

  companion object : KLogging()

  override fun processDefinitionKey(processDefinitionKey: String?) = this.apply { this.processDefinitionKey = requireNotNull(processDefinitionKey) }

  override fun processDefinitionId(processDefinitionId: String?) = this.apply { this.processDefinitionId = requireNotNull(processDefinitionId) }

  override fun processInstanceId(processInstanceId: String?) = this.apply { this.processInstanceId = requireNotNull(processInstanceId) }

  override fun processInstanceBusinessKey(processInstanceBusinessKey: String?) = this.apply { this.businessKey = requireNotNull(processInstanceBusinessKey) }

  override fun executionId(executionId: String?) = this.apply { this.executionId = requireNotNull(executionId) }

  override fun activityId(activityId: String?) = this.apply { this.activityId = requireNotNull(activityId) }

  override fun processVariableValueEquals(name: String, value: Any?) = this.apply {
    queryVariableValues.add(QueryVariableValue(name = name, value = value, operator = QueryOperator.EQUALS, processVariable = true))
  }

  override fun processVariableValueNotEquals(name: String, value: Any?) = this.apply {
    queryVariableValues.add(QueryVariableValue(name = name, value = value, operator = QueryOperator.NOT_EQUALS, processVariable = true))
  }

  @Deprecated("Deprecated in Java")
  override fun signalEventSubscription(eventName: String?) = this.apply {
    eventSubscriptions.add(EventSubscriptionQueryValue(requireNotNull(eventName), "signal"))
  }

  override fun signalEventSubscriptionName(eventName: String?) = this.apply {
    eventSubscriptions.add(EventSubscriptionQueryValue(requireNotNull(eventName), "signal"))
  }

  override fun messageEventSubscriptionName(eventName: String?) = this.apply {
    eventSubscriptions.add(EventSubscriptionQueryValue(requireNotNull(eventName), "message"))
  }

  override fun messageEventSubscription() = this.apply {
    eventSubscriptions.add(EventSubscriptionQueryValue(null, "message"))
  }

  override fun suspended() = this.apply { this.suspensionState = SuspensionState.SUSPENDED }

  override fun active() = this.apply { this.suspensionState = SuspensionState.SUSPENDED }

  override fun incidentType(incidentType: String?) = this.apply { this.incidentType = requireNotNull(incidentType) }

  override fun incidentId(incidentId: String?) = this.apply { this.incidentId = requireNotNull(incidentId) }

  override fun incidentMessage(incidentMessage: String?) = this.apply { this.incidentMessage = requireNotNull(incidentMessage) }

  override fun incidentMessageLike(incidentMessageLike: String?) = this.apply { this.incidentMessageLike = requireNotNull(incidentMessageLike) }

  override fun orderByProcessInstanceId() = this.apply { orderBy("instanceId") }

  override fun orderByProcessDefinitionKey() = this.apply { orderBy("definitionKey") }

  override fun orderByProcessDefinitionId() = this.apply { orderBy("definitionId") }

  override fun listPage(firstResult: Int, maxResults: Int): List<Execution> =
    executionApiClient.queryExecutions(firstResult, maxResults, fillQueryDto()).body!!.map {
      ExecutionAdapter(ExecutionBean.fromExecutionDto(it))
    }

  override fun count() = executionApiClient.queryExecutionsCount(fillQueryDto()).body!!.count!!

  fun fillQueryDto() = ExecutionQueryDto().apply {
    validate()
    this@DelegatingExecutionQuery.eventSubscriptions.let {
      if (it.filter { s -> s.eventType == "signal" }.size > 1) {
        throw NotValidException("Only one signal name for event subscriptions allowed")
      }
      if (it.filter { s -> s.eventType == "message" }.size > 1) {
        throw NotValidException("Only one message name for event subscriptions allowed")
      }
    }
    val dtoPropertiesByName = ExecutionQueryDto::class.memberProperties.filterIsInstance<KMutableProperty1<ExecutionQueryDto, Any?>>().associateBy { it.name }
    dtoPropertiesByName.forEach {
      val valueToSet = when (it.key) {
        "signalEventSubscriptionName" -> this@DelegatingExecutionQuery.eventSubscriptions.filter { it.eventType == "signal" }
            .map { it.eventName }.firstOrNull()

        "messageEventSubscriptionName" -> this@DelegatingExecutionQuery.eventSubscriptions.filter { it.eventType == "message" }
            .map { it.eventName }.firstOrNull()

        "active" -> if (this@DelegatingExecutionQuery.suspensionState == SuspensionState.ACTIVE) true else null
        "suspended" -> if (this@DelegatingExecutionQuery.suspensionState == SuspensionState.SUSPENDED) true else null
        "tenantIdIn" -> this@DelegatingExecutionQuery.tenantIds?.toList()
        "withoutTenantId" -> this@DelegatingExecutionQuery.tenantIdsSet && (this@DelegatingExecutionQuery.tenantIds == null)
        "processVariables" -> this@DelegatingExecutionQuery.queryVariableValues.filter { it.processVariable }.toDto()
        "variables" -> this@DelegatingExecutionQuery.queryVariableValues.filter { !it.processVariable }.toDto()
        "variableNamesIgnoreCase" -> this@DelegatingExecutionQuery.variableNamesIgnoreCase
        "variableValuesIgnoreCase" -> this@DelegatingExecutionQuery.variableValuesIgnoreCase
        "sorting" -> this@DelegatingExecutionQuery.orderingProperties.map { it.toExecutionSorting() }.filter { it.sortBy != null }
        else -> valueForProperty(it.key, this@DelegatingExecutionQuery, it.value.returnType)
      }
      it.value.isAccessible = true
      it.value.set(this, valueToSet)
    }
  }

}

data class EventSubscriptionQueryValue(val eventName: String?, val eventType: String)
