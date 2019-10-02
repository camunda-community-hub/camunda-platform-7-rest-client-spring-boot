package org.camunda.bpm.extension.feign.variables

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.camunda.bpm.engine.ProcessEngine
import org.camunda.bpm.engine.ProcessEngines
import org.camunda.bpm.engine.rest.dto.VariableValueDto
import org.camunda.bpm.engine.rest.dto.message.MessageCorrelationResultDto
import org.camunda.bpm.engine.rest.dto.message.MessageCorrelationResultWithVariableDto
import org.camunda.bpm.engine.rest.dto.runtime.ExecutionDto
import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceDto
import org.camunda.bpm.engine.runtime.*
import org.camunda.bpm.engine.variable.VariableMap
import org.camunda.bpm.engine.variable.Variables.createVariables
import org.camunda.bpm.extension.feign.adapter.ExecutionAdapter
import org.camunda.bpm.extension.feign.adapter.ExecutionBean
import org.camunda.bpm.extension.feign.adapter.InstanceBean
import org.camunda.bpm.extension.feign.adapter.ProcessInstanceAdapter

/**
 * Create result from DTO.
 */
fun MessageCorrelationResultDto.fromDto(): MessageCorrelationResult {

  val processInstance: ProcessInstanceDto? by lazy { this.processInstance }
  val resultType by lazy { this.resultType }
  val execution: ExecutionDto? by lazy { this.execution }

  return object : MessageCorrelationResult {
    override fun getProcessInstance(): ProcessInstance? = if (processInstance != null) ProcessInstanceAdapter(instanceBean = InstanceBean.fromProcessInstanceDto(processInstance!!)) else null
    override fun getExecution(): Execution? = if (execution != null) ExecutionAdapter(ExecutionBean.fromExecutionDto(execution!!)) else null
    override fun getResultType(): MessageCorrelationResultType = resultType
  }
}

/**
 * Create result from DTO.
 * @param processEngine to retrieve the getValueTypeResolver
 * @param objectMapper for deserialization of complex structures.
 */
fun MessageCorrelationResultWithVariableDto.fromDto(
  processEngine: ProcessEngine = ProcessEngines.getDefaultProcessEngine(),
  objectMapper: ObjectMapper = jacksonObjectMapper()
): MessageCorrelationResultWithVariables {

  val processInstance: ProcessInstanceDto? by lazy { this.processInstance }
  val variables: MutableMap<String, VariableValueDto>? by lazy { this.variables }
  val execution: ExecutionDto? by lazy { this.execution }
  val resultType by lazy { this.resultType }

  return object : MessageCorrelationResultWithVariables {
    override fun getProcessInstance(): ProcessInstance? = if (processInstance != null) ProcessInstanceAdapter(instanceBean = InstanceBean.fromProcessInstanceDto(processInstance!!)) else null
    override fun getVariables(): VariableMap? = if (variables != null) VariableValueDto.toMap(variables, processEngine, objectMapper) else createVariables()
    override fun getExecution(): Execution? = if (execution != null) ExecutionAdapter(ExecutionBean.fromExecutionDto(execution!!)) else null
    override fun getResultType(): MessageCorrelationResultType = resultType
  }
}
