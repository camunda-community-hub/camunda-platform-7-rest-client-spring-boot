package org.camunda.bpm.extension.rest.impl.builder

import org.camunda.bpm.engine.history.HistoricProcessInstanceQuery
import org.camunda.bpm.engine.runtime.*
import org.camunda.bpm.extension.rest.adapter.BatchAdapter
import org.camunda.bpm.extension.rest.adapter.BatchBean
import org.camunda.bpm.extension.rest.client.api.ProcessInstanceApiClient
import org.camunda.bpm.extension.rest.client.model.ProcessInstanceSuspensionStateAsyncDto
import org.camunda.bpm.extension.rest.client.model.ProcessInstanceSuspensionStateDto
import org.camunda.bpm.extension.rest.impl.query.DelegatingHistoricProcessInstanceQuery
import org.camunda.bpm.extension.rest.impl.query.DelegatingProcessInstanceQuery

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
  private val processInstanceIds = mutableListOf<String>()
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
    processInstanceIds?.let { this.processInstanceIds.addAll(processInstanceIds) }
  }

  override fun byProcessInstanceIds(vararg processInstanceIds: String) = this.apply {
    this.processInstanceIds.addAll(processInstanceIds.toList())
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

  private fun updateSuspensionStateAsync(suspended: Boolean) =
    BatchAdapter(BatchBean.fromDto(
      processInstanceApiClient.updateSuspensionStateAsyncOperation(
        ProcessInstanceSuspensionStateAsyncDto()
          .processInstanceIds(processInstanceIds)
          .processInstanceQuery(processInstanceQuery?.toDto())
          .historicProcessInstanceQuery(historicProcessInstanceQuery?.toDto())
          .suspended(suspended)
      ).body!!
    ))

  private fun ProcessInstanceQuery.toDto() = if (this is DelegatingProcessInstanceQuery) this.fillQueryDto() else throw IllegalArgumentException()

  private fun HistoricProcessInstanceQuery.toDto() = if (this is DelegatingHistoricProcessInstanceQuery) this.fillQueryDto() else throw IllegalArgumentException()


}
