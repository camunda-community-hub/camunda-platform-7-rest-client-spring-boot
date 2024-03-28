package org.camunda.community.rest.impl.builder

import org.camunda.bpm.engine.batch.Batch
import org.camunda.bpm.engine.externaltask.ExternalTaskQuery
import org.camunda.bpm.engine.externaltask.UpdateExternalTaskRetriesBuilder
import org.camunda.bpm.engine.history.HistoricProcessInstanceQuery
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery
import org.camunda.community.rest.adapter.BatchAdapter
import org.camunda.community.rest.adapter.BatchBean
import org.camunda.community.rest.client.api.ExternalTaskApiClient
import org.camunda.community.rest.client.model.SetRetriesForExternalTasksDto
import org.camunda.community.rest.impl.query.DelegatingExternalTaskQuery
import org.camunda.community.rest.impl.query.DelegatingHistoricProcessInstanceQuery
import org.camunda.community.rest.impl.query.DelegatingProcessInstanceQuery

class RemoteUpdateExternalTaskRetriesBuilder(
  private val externalTaskApiClient: ExternalTaskApiClient
) : UpdateExternalTaskRetriesBuilder {

  private val setRetriesForExternalTasksDto = SetRetriesForExternalTasksDto()

  override fun externalTaskIds(externalTaskIds: List<String>): UpdateExternalTaskRetriesBuilder {
    setRetriesForExternalTasksDto.externalTaskIds = externalTaskIds
    return this
  }

  override fun externalTaskIds(vararg externalTaskIds: String): UpdateExternalTaskRetriesBuilder {
    setRetriesForExternalTasksDto.externalTaskIds = externalTaskIds.toList()
    return this
  }

  override fun processInstanceIds(processInstanceIds: List<String>): UpdateExternalTaskRetriesBuilder {
    setRetriesForExternalTasksDto.processInstanceIds = processInstanceIds
    return this
  }

  override fun processInstanceIds(vararg processInstanceIds: String): UpdateExternalTaskRetriesBuilder {
    setRetriesForExternalTasksDto.processInstanceIds = processInstanceIds.toList()
    return this
  }

  override fun externalTaskQuery(externalTaskQuery: ExternalTaskQuery): UpdateExternalTaskRetriesBuilder {
    if (externalTaskQuery is DelegatingExternalTaskQuery) {
      setRetriesForExternalTasksDto.externalTaskQuery = externalTaskQuery.fillQueryDto()
      return this
    } else {
      throw IllegalStateException("delegating external task query needed")
    }
  }

  override fun processInstanceQuery(processInstanceQuery: ProcessInstanceQuery): UpdateExternalTaskRetriesBuilder {
    if (processInstanceQuery is DelegatingProcessInstanceQuery) {
      setRetriesForExternalTasksDto.processInstanceQuery = processInstanceQuery.fillQueryDto()
      return this
    } else {
      throw IllegalStateException("delegating process instance query needed")
    }
  }

  override fun historicProcessInstanceQuery(historicProcessInstanceQuery: HistoricProcessInstanceQuery): UpdateExternalTaskRetriesBuilder {
    if (historicProcessInstanceQuery is DelegatingHistoricProcessInstanceQuery) {
      setRetriesForExternalTasksDto.historicProcessInstanceQuery = historicProcessInstanceQuery.fillQueryDto()
      return this
    } else {
      throw IllegalStateException("delegating historic process instance query needed")
    }
  }

  override fun set(retries: Int) {
    setRetriesForExternalTasksDto.retries = retries
    externalTaskApiClient.setExternalTaskRetries(setRetriesForExternalTasksDto)
  }

  override fun setAsync(retries: Int): Batch {
    setRetriesForExternalTasksDto.retries = retries
    return BatchAdapter(BatchBean.fromDto(
      externalTaskApiClient.setExternalTaskRetriesAsyncOperation(setRetriesForExternalTasksDto).body!!
    ))
  }
}
