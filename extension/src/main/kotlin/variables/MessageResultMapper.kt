package org.camunda.bpm.extension.restclient.variables

import org.camunda.bpm.engine.rest.dto.message.MessageCorrelationResultDto
import org.camunda.bpm.engine.runtime.*
import org.camunda.bpm.engine.variable.VariableMap
import org.camunda.bpm.extension.restclient.adapter.ExecutionAdapter
import org.camunda.bpm.extension.restclient.adapter.ExecutionBean
import org.camunda.bpm.extension.restclient.adapter.InstanceBean
import org.camunda.bpm.extension.restclient.adapter.ProcessInstanceAdapter

fun messageCorrelationResultFromDto(dto: MessageCorrelationResultDto) = object : MessageCorrelationResult {
  override fun getProcessInstance(): ProcessInstance = ProcessInstanceAdapter(
    instanceBean = InstanceBean.fromProcessInstanceDto(dto.processInstance)
  )

  override fun getExecution(): Execution = ExecutionAdapter(ExecutionBean.fromExecutionDto(dto.execution))
  override fun getResultType(): MessageCorrelationResultType = dto.resultType
}

fun messageCorrelationResultWithVariablesFromDto(dto: MessageCorrelationResultDto, variables: VariableMap) = object : MessageCorrelationResultWithVariables {

  private val storedVariables: VariableMap = variables
  override fun getProcessInstance(): ProcessInstance = ProcessInstanceAdapter(
    instanceBean = InstanceBean.fromProcessInstanceDto(dto.processInstance)
  )

  override fun getVariables(): VariableMap = variables
  override fun getExecution(): Execution = ExecutionAdapter(ExecutionBean.fromExecutionDto(dto.execution))
  override fun getResultType(): MessageCorrelationResultType = dto.resultType
}
