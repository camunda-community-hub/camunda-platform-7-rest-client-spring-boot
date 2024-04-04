package org.camunda.community.rest.impl.builder

import org.assertj.core.api.Assertions.assertThat
import org.camunda.community.rest.client.api.ProcessInstanceApiClient
import org.camunda.community.rest.client.model.BatchDto
import org.camunda.community.rest.impl.query.DelegatingHistoricProcessInstanceQuery
import org.camunda.community.rest.impl.query.DelegatingProcessInstanceQuery
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.ResponseEntity

class RemoteUpdateProcessInstanceSuspensionStateSelectBuilderTest {

  val processInstanceApiClient = mock<ProcessInstanceApiClient>()

  val builder = RemoteUpdateProcessInstanceSuspensionStateSelectBuilder(
    processInstanceApiClient = processInstanceApiClient
  )

  @Test
  fun byProcessInstanceIds() {
    whenever(processInstanceApiClient.updateSuspensionState(any())).thenReturn(
      ResponseEntity.ok(null)
    )
    builder.byProcessInstanceIds("processInstanceId1", "processInstanceId2").suspend()
    verify(processInstanceApiClient).updateSuspensionState(any())
  }

  @Test
  fun byProcessInstanceQuery() {
    whenever(processInstanceApiClient.updateSuspensionStateAsyncOperation(any())).thenReturn(
      ResponseEntity.ok(
        BatchDto().id("batchId").type("type").totalJobs(1).jobsCreated(1).batchJobsPerSeed(1).invocationsPerBatchJob(1).suspended(false)
      )
    )
    val result = builder.byProcessInstanceQuery(DelegatingProcessInstanceQuery(processInstanceApiClient)).suspendAsync()
    assertThat(result).isNotNull
    verify(processInstanceApiClient).updateSuspensionStateAsyncOperation(any())
  }

  @Test
  fun byHistoricProcessInstanceQuery() {
    whenever(processInstanceApiClient.updateSuspensionStateAsyncOperation(any())).thenReturn(
      ResponseEntity.ok(
        BatchDto().id("batchId").type("type").totalJobs(1).jobsCreated(1).batchJobsPerSeed(1).invocationsPerBatchJob(1).suspended(false)
      )
    )
    val result = builder.byHistoricProcessInstanceQuery(DelegatingHistoricProcessInstanceQuery(mock())).activateAsync()
    assertThat(result).isNotNull
    verify(processInstanceApiClient).updateSuspensionStateAsyncOperation(any())
  }

  @Test
  fun byProcessInstanceId() {
    whenever(processInstanceApiClient.updateSuspensionState(any())).thenReturn(
      ResponseEntity.ok(null)
    )
    builder.byProcessInstanceId("processInstanceId").suspend()
    verify(processInstanceApiClient).updateSuspensionState(any())
  }

  @Test
  fun byProcessDefinitionId() {
    whenever(processInstanceApiClient.updateSuspensionState(any())).thenReturn(
      ResponseEntity.ok(null)
    )
    builder.byProcessDefinitionId("processDefinitionId").activate()
    verify(processInstanceApiClient).updateSuspensionState(any())
  }

  @Test
  fun byProcessDefinitionKey() {
    whenever(processInstanceApiClient.updateSuspensionState(any())).thenReturn(
      ResponseEntity.ok(null)
    )
    builder.byProcessDefinitionKey("processDefinitionKey").activate()
    verify(processInstanceApiClient).updateSuspensionState(any())
  }

}
