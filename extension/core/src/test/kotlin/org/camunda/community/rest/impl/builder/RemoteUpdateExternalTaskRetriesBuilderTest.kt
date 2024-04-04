package org.camunda.community.rest.impl.builder

import org.assertj.core.api.Assertions.assertThat
import org.camunda.community.rest.client.api.ExternalTaskApiClient
import org.camunda.community.rest.client.api.HistoryApiClient
import org.camunda.community.rest.client.api.ProcessInstanceApiClient
import org.camunda.community.rest.client.model.BatchDto
import org.camunda.community.rest.impl.query.DelegatingExternalTaskQuery
import org.camunda.community.rest.impl.query.DelegatingHistoricProcessInstanceQuery
import org.camunda.community.rest.impl.query.DelegatingProcessInstanceQuery
import org.junit.Test

import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.http.ResponseEntity

class RemoteUpdateExternalTaskRetriesBuilderTest {

  val externalTaskApiClient = mock<ExternalTaskApiClient>()
  val historyApiClient = mock<HistoryApiClient>()
  val processInstanceApiClient = mock<ProcessInstanceApiClient>()

  val builder = RemoteUpdateExternalTaskRetriesBuilder(externalTaskApiClient)

  @Test
  fun set() {
    whenever(externalTaskApiClient.setExternalTaskRetries(any())).thenReturn(
      ResponseEntity.ok(null)
    )
    builder
      .externalTaskIds("externalTaskId")
      .externalTaskQuery(DelegatingExternalTaskQuery(externalTaskApiClient, topicName = "topic"))
      .historicProcessInstanceQuery(DelegatingHistoricProcessInstanceQuery(historyApiClient, businessKey = "businessKey"))
      .processInstanceIds("processInstanceId")
      .processInstanceQuery(DelegatingProcessInstanceQuery(processInstanceApiClient, processDefinitionKey = "processDefinitionKey"))
      .set(5)
  }

  @Test
  fun setAsync() {
    whenever(externalTaskApiClient.setExternalTaskRetriesAsyncOperation(any())).thenReturn(
      ResponseEntity.ok(BatchDto().id("batchId").type("type").totalJobs(1).jobsCreated(1).batchJobsPerSeed(1).invocationsPerBatchJob(1).suspended(false))
    )
    val result = builder
      .externalTaskIds("externalTaskId")
      .externalTaskQuery(DelegatingExternalTaskQuery(externalTaskApiClient, topicName = "topic"))
      .historicProcessInstanceQuery(DelegatingHistoricProcessInstanceQuery(historyApiClient, businessKey = "businessKey"))
      .processInstanceIds("processInstanceId")
      .processInstanceQuery(DelegatingProcessInstanceQuery(processInstanceApiClient, processDefinitionKey = "processDefinitionKey"))
      .setAsync(5)
    assertThat(result.id).isEqualTo("batchId")
  }

}
