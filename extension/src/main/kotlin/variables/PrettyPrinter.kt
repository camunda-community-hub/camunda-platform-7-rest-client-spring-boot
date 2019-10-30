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

import org.camunda.bpm.engine.repository.ProcessDefinition
import org.camunda.bpm.engine.runtime.*

/**
 * Pretty print message correlation result with variables.
 */
fun MessageCorrelationResultWithVariables.toPrettyString(): String {
  return (this as MessageCorrelationResult).toPrettyString() +
    "\n Enclosed variables: $variables"
}

/**
 * Pretty print message correlation result.
 */
fun MessageCorrelationResult.toPrettyString(): String =
  when (resultType) {
    MessageCorrelationResultType.Execution -> "Correlated Execution: ${execution.toPrettyString()}"
    MessageCorrelationResultType.ProcessDefinition -> "Started new process instance: ${processInstance.toPrettyString()}"
    else -> "Unset result type."
  }

/**
 * Pretty print process instance.
 */
fun ProcessInstance.toPrettyString(): String =
  """{
    id: ${this.id},
    processDefinitionId: ${this.processDefinitionId},
    businessKey: ${this.businessKey},
    ended: ${this.isEnded}, 
    suspended: ${this.isSuspended} 
}"""

/**
 * Pretty print execution.
 */
fun Execution.toPrettyString(): String =
  """{
    id: ${this.id},
    processInstanceId: ${this.processInstanceId},
    ended: ${this.isEnded},
    suspended: ${this.isSuspended}
}"""

/**
 * Pretty print the process definition.
 */
fun ProcessDefinition.toPrettyString(): String =
  """
    id: ${this.id},
    key: ${this.key},
    name: ${this.name}
  """.trimIndent()
