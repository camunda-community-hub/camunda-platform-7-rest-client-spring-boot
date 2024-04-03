package org.camunda.community.rest.impl.query

import org.assertj.core.api.Assertions.assertThat
import org.camunda.community.rest.client.api.ProcessInstanceApiClient
import org.camunda.community.rest.client.model.CountResultDto
import org.camunda.community.rest.client.model.ProcessInstanceDto
import org.junit.Test

import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.http.ResponseEntity

class DelegatingProcessInstanceQueryTest {

  val processInstanceApiClient = mock<ProcessInstanceApiClient>()

  val query = DelegatingProcessInstanceQuery(
    processInstanceApiClient,
    processInstanceId = "processInstanceId",
    processInstanceIds = setOf("processInstanceIds"),
    businessKey = "businessKey",
    businessKeyLike = "businessKeyLike",
    processDefinitionId = "processDefinitionId",
    processDefinitionKey = "processDefinitionKey",
    processDefinitionKeys = arrayOf("processDefinitionKeys"),
    processDefinitionKeyNotIn = arrayOf("processDefinitionKeyNotIn"),
    deploymentId = "deploymentId",
    superProcessInstanceId = "superProcessInstanceId",
    subProcessInstanceId = "subProcessInstanceId",
    suspensionState = SuspensionState.ACTIVE,
    withIncident = true,
    incidentType = "incidentType",
    incidentId = "incidentId",
    incidentMessage = "incidentMessage",
    incidentMessageLike = "incidentMessageLike",
    caseInstanceId = "caseInstanceId",
    superCaseInstanceId = "superCaseInstanceId",
    subCaseInstanceId = "subCaseInstanceId",
    activityIds = arrayOf("activityIds"),
    isRootProcessInstances = false,
    isLeafProcessInstances = false,
    isProcessDefinitionWithoutTenantId = false,
    isOrQueryActive = false,
  ).apply {
    this.tenantIdIn("tenantId")
    this.variableNamesIgnoreCase = true
    this.variableValuesIgnoreCase = true
    this.variableValueEquals("var", "value")
    this.orderByProcessDefinitionKey().asc()
  }

  @Test
  fun listPage() {
    whenever(processInstanceApiClient.queryProcessInstances(eq(1), eq(10), any())).thenReturn(
      ResponseEntity.ok(listOf(ProcessInstanceDto().id("processInstanceId").ended(false).suspended(false)))
    )
    val result = query.listPage(1, 10)
    assertThat(result).hasSize(1)
  }

  @Test
  fun count() {
    whenever(processInstanceApiClient.queryProcessInstancesCount(any())).thenReturn(
      ResponseEntity.ok(CountResultDto().count(5))
    )
    val result = query.count()
    assertThat(result).isEqualTo(5)
  }

}
