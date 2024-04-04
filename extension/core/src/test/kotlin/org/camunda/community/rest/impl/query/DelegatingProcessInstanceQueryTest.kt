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
  ).apply {
    this.processInstanceId("processInstanceId")
    this.processInstanceIds(mutableSetOf("processInstanceIds"))
    this.processInstanceBusinessKey("businessKey")
    this.processInstanceBusinessKeyLike("businessKeyLike")
    this.processDefinitionId("processDefinitionId")
    this.processDefinitionKey("processDefinitionKey")
    this.processDefinitionKeyIn("processDefinitionKeys")
    this.processDefinitionKeyNotIn("processDefinitionKeyNotIn")
    this.deploymentId("deploymentId")
    this.superProcessInstanceId("superProcessInstanceId")
    this.subProcessInstanceId("subProcessInstanceId")
    this.suspended()
    this.withIncident()
    this.incidentType("incidentType")
    this.incidentId("incidentId")
    this.incidentMessage("incidentMessage")
    this.incidentMessageLike("incidentMessageLike")
    this.caseInstanceId("caseInstanceId")
    this.superCaseInstanceId("superCaseInstanceId")
    this.subCaseInstanceId("subCaseInstanceId")
    this.activityIdIn("activityIds")
    //this.rootProcessInstances()
    this.leafProcessInstances()
    //this.withoutTenantId()
    this.tenantIdIn("tenantId")
    this.matchVariableNamesIgnoreCase()
    this.matchVariableValuesIgnoreCase()
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
