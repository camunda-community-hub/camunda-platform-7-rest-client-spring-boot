package org.camunda.bpm.extension.rest.impl.model

import com.fasterxml.jackson.annotation.JsonProperty
import org.camunda.bpm.extension.rest.client.model.TaskQueryDto

/**
 * Patched version of the generated TaskQueryDto as the OpenAPI spec from camunda is missing the fields taskId and taskIdIn.
 * Bug is reported to camunda https://jira.camunda.com/browse/CAM-14522 and fix will be included in future versions.
 * To allow these fields to be used in the meantime, this class provides the fields for usage in the POST request.
 */
class PatchedTaskQueryDto : TaskQueryDto() {

  @JsonProperty("taskId")
  var taskId: String? = null
  @JsonProperty("taskIdIn")
  var taskIdIn: List<String>? = null

}
