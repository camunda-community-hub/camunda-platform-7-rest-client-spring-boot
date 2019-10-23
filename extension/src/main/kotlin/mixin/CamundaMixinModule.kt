package org.camunda.bpm.extension.feign.mixin

import com.fasterxml.jackson.databind.module.SimpleModule
import org.camunda.bpm.engine.rest.dto.repository.ProcessDefinitionDto

class CamundaMixinModule : SimpleModule() {
  override fun setupModule(context: SetupContext?) {
    context?.setMixInAnnotations(ProcessDefinitionDto::class.java, ProcessDefinitionDtoMixin::class.java)
  }
}
