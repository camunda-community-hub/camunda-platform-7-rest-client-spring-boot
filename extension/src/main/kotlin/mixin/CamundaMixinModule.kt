package org.camunda.bpm.extension.feign.mixin

import com.fasterxml.jackson.databind.module.SimpleModule
import org.camunda.bpm.engine.rest.dto.repository.ProcessDefinitionDto

/**
 * Jackson mixin module to configure mixins of unorthodox DTO-JSON mappings.
 */
class CamundaMixinModule : SimpleModule() {

  override fun setupModule(context: SetupContext?) {
    context?.setMixInAnnotations(ProcessDefinitionDto::class.java, ProcessDefinitionDtoMixin::class.java)
  }
}
