package org.camunda.community.rest.impl.builder

import org.camunda.bpm.engine.batch.Batch
import org.camunda.bpm.engine.history.HistoricProcessInstanceQuery
import org.camunda.bpm.engine.impl.ProcessEngineLogger
import org.camunda.bpm.engine.impl.util.EnsureUtil
import org.camunda.bpm.engine.runtime.*
import org.camunda.community.rest.adapter.BatchAdapter
import org.camunda.community.rest.adapter.BatchBean
import org.camunda.community.rest.client.api.ProcessInstanceApiClient
import org.camunda.community.rest.client.model.ProcessInstanceSuspensionStateAsyncDto
import org.camunda.community.rest.client.model.ProcessInstanceSuspensionStateDto
import org.camunda.community.rest.impl.query.DelegatingHistoricProcessInstanceQuery
import org.camunda.community.rest.impl.query.DelegatingProcessInstanceQuery

class RemoteUpdateProcessInstanceSuspensionStateSelectBuilder(
  private val processInstanceApiClient: ProcessInstanceApiClient
) : UpdateProcessInstanceSuspensionStateSelectBuilder {

  override fun byProcessInstanceIds(processInstanceIds: MutableList<String>): UpdateProcessInstancesSuspensionStateBuilder =
    RemoteUpdateProcessInstanceSuspensionStateBuilder(processInstanceApiClient = processInstanceApiClient).apply { this.byProcessInstanceIds(processInstanceIds) }

  override fun byProcessInstanceIds(vararg processInstanceIds: String): UpdateProcessInstancesSuspensionStateBuilder =
    RemoteUpdateProcessInstanceSuspensionStateBuilder(processInstanceApiClient = processInstanceApiClient).apply { this.byProcessInstanceIds(processInstanceIds.toMutableList()) }

  override fun byProcessInstanceQuery(processInstanceQuery: ProcessInstanceQuery): UpdateProcessInstancesSuspensionStateBuilder =
    RemoteUpdateProcessInstanceSuspensionStateBuilder(processInstanceApiClient = processInstanceApiClient).apply { this.byProcessInstanceQuery(processInstanceQuery) }

  override fun byHistoricProcessInstanceQuery(historicProcessInstanceQuery: HistoricProcessInstanceQuery): UpdateProcessInstancesSuspensionStateBuilder =
    RemoteUpdateProcessInstanceSuspensionStateBuilder(processInstanceApiClient = processInstanceApiClient).apply { this.byHistoricProcessInstanceQuery(historicProcessInstanceQuery) }

  override fun byProcessInstanceId(processInstanceId: String): UpdateProcessInstanceSuspensionStateBuilder =
    RemoteUpdateProcessInstanceSuspensionStateBuilder(processInstanceApiClient = processInstanceApiClient).apply { this.byProcessInstanceIds(processInstanceId) }

  override fun byProcessDefinitionId(processDefinitionId: String?): UpdateProcessInstanceSuspensionStateBuilder =
    RemoteUpdateProcessInstanceSuspensionStateBuilder(processInstanceApiClient = processInstanceApiClient, processDefinitionId = processDefinitionId)

  override fun byProcessDefinitionKey(processDefinitionKey: String?): UpdateProcessInstanceSuspensionStateTenantBuilder =
    RemoteUpdateProcessInstanceSuspensionStateBuilder(processInstanceApiClient = processInstanceApiClient, processDefinitionKey = processDefinitionKey)

}


class RemoteUpdateProcessInstanceSuspensionStateBuilder(
  private val processInstanceApiClient: ProcessInstanceApiClient,
  private val processDefinitionId: String? = null,
  private val processDefinitionKey: String? = null
): UpdateProcessInstancesSuspensionStateBuilder, UpdateProcessInstanceSuspensionStateTenantBuilder {

  private var withoutTenant: Boolean? = null
  private var tenantId: String? = null
  private var processInstanceIds: MutableList<String>? = null
  private var processInstanceQuery: ProcessInstanceQuery? = null
  private var historicProcessInstanceQuery: HistoricProcessInstanceQuery? = null

  override fun processDefinitionWithoutTenantId() = this.apply {
    this.withoutTenant = true
  }

  override fun processDefinitionTenantId(tenantId: String?) = this.apply {
    this.tenantId = tenantId
  }

  override fun activate() = updateSuspensionState(false)

  override fun suspend() = updateSuspensionState(true)

  override fun byProcessInstanceIds(processInstanceIds: MutableList<String>?) = this.apply {
    this.processInstanceIds = (this.processInstanceIds ?: mutableListOf()).apply {
      processInstanceIds?.let { this.addAll(processInstanceIds) }
    }
  }

  override fun byProcessInstanceIds(vararg processInstanceIds: String) = this.apply {
    this.processInstanceIds = (this.processInstanceIds ?: mutableListOf()).apply { this.addAll(processInstanceIds.toList()) }
  }

  override fun byProcessInstanceQuery(processInstanceQuery: ProcessInstanceQuery) = this.apply {
    this.processInstanceQuery = processInstanceQuery
  }

  override fun byHistoricProcessInstanceQuery(historicProcessInstanceQuery: HistoricProcessInstanceQuery) = this.apply {
    this.historicProcessInstanceQuery = historicProcessInstanceQuery
  }

  override fun activateAsync() = updateSuspensionStateAsync(false)

  override fun suspendAsync() = updateSuspensionStateAsync(true)

  private fun updateSuspensionState(suspended: Boolean) {
    validateParameters()
    processInstanceApiClient.updateSuspensionState(
      ProcessInstanceSuspensionStateDto()
        .processDefinitionId(processDefinitionId)
        .processDefinitionKey(processDefinitionKey)
        .processDefinitionWithoutTenantId(withoutTenant)
        .processDefinitionTenantId(tenantId)
        .processInstanceIds(processInstanceIds)
        .processInstanceQuery(processInstanceQuery?.toDto())
        .historicProcessInstanceQuery(historicProcessInstanceQuery?.toDto())
        .suspended(suspended)
    )
  }

  private fun updateSuspensionStateAsync(suspended: Boolean): Batch {
    validateParameters()
    return BatchAdapter(BatchBean.fromDto(
      processInstanceApiClient.updateSuspensionStateAsyncOperation(
        ProcessInstanceSuspensionStateAsyncDto()
          .processInstanceIds(processInstanceIds)
          .processInstanceQuery(processInstanceQuery?.toDto())
          .historicProcessInstanceQuery(historicProcessInstanceQuery?.toDto())
          .suspended(suspended)
      ).body!!
    ))
  }


  private fun validateParameters() {
    EnsureUtil.ensureOnlyOneNotNull("Need to specify either a process instance id (or query), a process definition id or a process definition key.",
      processInstanceIds ?: processInstanceQuery ?: historicProcessInstanceQuery, processDefinitionId, processDefinitionKey)
    if ((withoutTenant != null || tenantId != null) && (processInstanceIds != null || processDefinitionId != null)) {
      throw ProcessEngineLogger.CMD_LOGGER.exceptionUpdateSuspensionStateForTenantOnlyByProcessDefinitionKey()
    }
  }

  private fun ProcessInstanceQuery.toDto() = if (this is DelegatingProcessInstanceQuery) this.fillQueryDto() else throw IllegalArgumentException()

  private fun HistoricProcessInstanceQuery.toDto() = if (this is DelegatingHistoricProcessInstanceQuery) this.fillQueryDto() else throw IllegalArgumentException()


}
