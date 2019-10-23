package org.camunda.bpm.extension.feign.mixin

import com.fasterxml.jackson.annotation.JsonProperty

abstract class ProcessDefinitionDtoMixin {
  @JsonProperty("startableInTasklist")
  var isStartableInTasklist: Boolean = false
}
