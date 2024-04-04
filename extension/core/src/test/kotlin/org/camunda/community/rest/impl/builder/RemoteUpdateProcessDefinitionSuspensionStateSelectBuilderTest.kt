package org.camunda.community.rest.impl.builder

import org.camunda.bpm.engine.repository.UpdateProcessDefinitionSuspensionStateSelectBuilder
import org.camunda.community.rest.client.api.ProcessDefinitionApiClient
import org.junit.Test

import org.junit.Assert.*
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.ResponseEntity
import java.util.Date

class RemoteUpdateProcessDefinitionSuspensionStateSelectBuilderTest {

  val processDefinitionApiClient = mock<ProcessDefinitionApiClient>()

  val builder = RemoteUpdateProcessDefinitionSuspensionStateSelectBuilder(
    processDefinitionApiClient = processDefinitionApiClient,
  )

  @Test
  fun byProcessDefinitionId() {
    whenever(processDefinitionApiClient.updateProcessDefinitionSuspensionStateById(eq("processDefinitionId"), any())).thenReturn(
      ResponseEntity.ok(null)
    )
    builder.byProcessDefinitionId("processDefinitionId")
      .includeProcessInstances(true)
      .executionDate(Date())
      .suspend()
    verify(processDefinitionApiClient).updateProcessDefinitionSuspensionStateById(eq("processDefinitionId"), any())
  }

  @Test
  fun byProcessDefinitionKey() {
    whenever(processDefinitionApiClient.updateProcessDefinitionSuspensionStateByKey(eq("processDefinitionKey"), any())).thenReturn(
      ResponseEntity.ok(null)
    )
    builder.byProcessDefinitionKey("processDefinitionKey")
      .includeProcessInstances(false)
      .executionDate(Date())
      .activate()
    verify(processDefinitionApiClient).updateProcessDefinitionSuspensionStateByKey(eq("processDefinitionKey"), any())
  }

}
