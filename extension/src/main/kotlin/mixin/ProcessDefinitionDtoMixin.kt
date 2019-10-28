package org.camunda.bpm.extension.feign.mixin

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Mixin to solve unorthodox mapping of ProcessDefinitionDTO mapping.
 */
@Suppress("unused")
abstract class ProcessDefinitionDtoMixin {

  /**
   * JSON startableInTasklist to Java isStartableInTasklist.
   */
  @JsonProperty("startableInTasklist")
  var isStartableInTasklist: Boolean = false
}
