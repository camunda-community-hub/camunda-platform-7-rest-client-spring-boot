package org.camunda.community.rest.impl.builder

import org.camunda.bpm.engine.externaltask.ExternalTaskQueryTopicBuilder
import org.camunda.bpm.engine.externaltask.LockedExternalTask
import org.camunda.community.rest.adapter.LockedExternalTaskAdapter
import org.camunda.community.rest.adapter.LockedExternalTaskBean
import org.camunda.community.rest.client.api.ExternalTaskApiClient
import org.camunda.community.rest.client.model.FetchExternalTaskTopicDto
import org.camunda.community.rest.client.model.FetchExternalTasksDto
import org.camunda.community.rest.variables.ValueMapper

class RemoteExternalTaskQueryBuilder(
  private val externalTaskApiClient: ExternalTaskApiClient,
  private val valueMapper: ValueMapper,
  workerId: String,
  maxTasks: Int,
  usePriority: Boolean? = null
) : ExternalTaskQueryTopicBuilder {

  private val fetchExternalTasksDto = FetchExternalTasksDto().apply {
    this.workerId = workerId
    this.maxTasks = maxTasks
    this.usePriority = usePriority
    this.topics = mutableListOf()
  }

  private var currentTopic: FetchExternalTaskTopicDto? = null

  override fun topic(topicName: String, lockDuration: Long): ExternalTaskQueryTopicBuilder {
    currentTopic = FetchExternalTaskTopicDto(topicName, lockDuration)
    fetchExternalTasksDto.topics.add(currentTopic)
    return this
  }

  override fun execute(): List<LockedExternalTask> {
    return externalTaskApiClient.fetchAndLock(fetchExternalTasksDto).body!!
      .map { LockedExternalTaskAdapter(LockedExternalTaskBean.fromDto(it, valueMapper)) }
  }

  override fun variables(vararg variables: String): ExternalTaskQueryTopicBuilder {
    currentTopic!!.variables = variables.toList()
    return this
  }

  override fun variables(variables: List<String>): ExternalTaskQueryTopicBuilder {
    currentTopic!!.variables = variables
    return this
  }

  override fun processInstanceVariableEquals(variables: Map<String, Any>): ExternalTaskQueryTopicBuilder {
    currentTopic!!.processVariables = variables
    return this
  }

  override fun processInstanceVariableEquals(name: String, value: Any): ExternalTaskQueryTopicBuilder {
    currentTopic!!.processVariables = mapOf(name to value)
    return this
  }

  override fun businessKey(businessKey: String): ExternalTaskQueryTopicBuilder {
    currentTopic!!.businessKey = businessKey
    return this
  }

  override fun processDefinitionId(processDefinitionId: String): ExternalTaskQueryTopicBuilder {
    currentTopic!!.processDefinitionId = processDefinitionId
    return this
  }

  override fun processDefinitionIdIn(vararg processDefinitionIds: String): ExternalTaskQueryTopicBuilder {
    currentTopic!!.processDefinitionIdIn = processDefinitionIds.toList()
    return this
  }

  override fun processDefinitionKey(processDefinitionKey: String): ExternalTaskQueryTopicBuilder {
    currentTopic!!.processDefinitionKey = processDefinitionKey
    return this
  }

  override fun processDefinitionKeyIn(vararg processDefinitionKeys: String): ExternalTaskQueryTopicBuilder {
    currentTopic!!.processDefinitionKeyIn = processDefinitionKeys.toList()
    return this
  }

  override fun processDefinitionVersionTag(versionTag: String): ExternalTaskQueryTopicBuilder {
    currentTopic!!.processDefinitionVersionTag = versionTag
    return this
  }

  override fun withoutTenantId(): ExternalTaskQueryTopicBuilder {
    currentTopic!!.withoutTenantId = true
    return this
  }

  override fun tenantIdIn(vararg tenantIds: String): ExternalTaskQueryTopicBuilder {
    currentTopic!!.tenantIdIn = tenantIds.toList()
    return this
  }

  override fun enableCustomObjectDeserialization(): ExternalTaskQueryTopicBuilder {
    currentTopic!!.deserializeValues = true
    return this
  }

  override fun localVariables(): ExternalTaskQueryTopicBuilder {
    currentTopic!!.localVariables = true
    return this
  }

  override fun includeExtensionProperties(): ExternalTaskQueryTopicBuilder {
    currentTopic!!.includeExtensionProperties = true
    return this
  }
}
