package impl

import org.camunda.bpm.engine.rest.dto.VariableValueDto

class CompleteTaskDto {
  var workerId: String? = null
  var variables: Map<String, VariableValueDto>? = null
  var localVariables: Map<String, VariableValueDto>? = null
}
