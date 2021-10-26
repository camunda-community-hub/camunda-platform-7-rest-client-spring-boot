/*-
 * #%L
 * camunda-rest-client-spring-boot
 * %%
 * Copyright (C) 2019 Camunda Services GmbH
 * %%
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 *  under one or more contributor license agreements. See the NOTICE file
 *  distributed with this work for additional information regarding copyright
 *  ownership. Camunda licenses this file to you under the Apache License,
 *  Version 2.0; you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * #L%
 */

package org.camunda.bpm.extension.rest.variables

import org.camunda.bpm.engine.runtime.*
import org.camunda.bpm.engine.variable.VariableMap
import org.camunda.bpm.engine.variable.Variables.createVariables
import org.camunda.bpm.extension.rest.adapter.ExecutionAdapter
import org.camunda.bpm.extension.rest.adapter.ExecutionBean
import org.camunda.bpm.extension.rest.adapter.InstanceBean
import org.camunda.bpm.extension.rest.adapter.ProcessInstanceAdapter
import org.camunda.bpm.extension.rest.client.model.ExecutionDto
import org.camunda.bpm.extension.rest.client.model.MessageCorrelationResultWithVariableDto
import org.camunda.bpm.extension.rest.client.model.ProcessInstanceDto
import org.camunda.bpm.extension.rest.client.model.VariableValueDto

/**
 * Create result from DTO.
 * @param valueMapper to to map variable values.
 */
fun MessageCorrelationResultWithVariableDto.fromDto(valueMapper: ValueMapper): MessageCorrelationResultWithVariables {

  val execution: ExecutionDto? by lazy { this.execution }
  val processInstance: ProcessInstanceDto? by lazy { this.processInstance }
  val resultType by lazy { this.resultType }
  val variables: MutableMap<String, VariableValueDto>? by lazy { this.variables }

  return object : MessageCorrelationResultWithVariables {
    override fun getExecution(): Execution? = if (execution != null) ExecutionAdapter(ExecutionBean.fromExecutionDto(execution!!)) else null
    override fun getProcessInstance(): ProcessInstance? = if (processInstance != null) ProcessInstanceAdapter(instanceBean = InstanceBean.fromProcessInstanceDto(processInstance!!)) else null
    override fun getResultType(): MessageCorrelationResultType = when (resultType) {
      MessageCorrelationResultWithVariableDto.ResultTypeEnum.EXECUTION -> MessageCorrelationResultType.Execution
      MessageCorrelationResultWithVariableDto.ResultTypeEnum.PROCESSDEFINITION -> MessageCorrelationResultType.ProcessDefinition
      null -> throw IllegalArgumentException("Result type should not be null")
    }
    override fun getVariables(): VariableMap? = if (variables != null) valueMapper.mapDtos(variables!!.toMap()) else createVariables()
  }
}
