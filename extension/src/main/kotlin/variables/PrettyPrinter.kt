package org.camunda.bpm.extension.feign.variables

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
