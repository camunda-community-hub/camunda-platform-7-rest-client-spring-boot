package org.camunda.community.rest.impl.builder

import org.camunda.bpm.engine.batch.Batch
import org.camunda.bpm.engine.runtime.ProcessInstanceModificationBuilder
import org.camunda.bpm.engine.runtime.ProcessInstanceModificationInstantiationBuilder
import org.camunda.community.rest.adapter.BatchAdapter
import org.camunda.community.rest.adapter.BatchBean
import org.camunda.community.rest.client.api.ProcessInstanceApi
import org.camunda.community.rest.client.model.ProcessInstanceModificationDto
import org.camunda.community.rest.client.model.ProcessInstanceModificationInstructionDto

class DelegatingProcessInstanceModificationBuilder(
  private val processInstanceId: String,
  private val processInstanceApi: ProcessInstanceApi
) : ProcessInstanceModificationBuilder, ProcessInstanceModificationInstantiationBuilder {

  private val modificationDto: ProcessInstanceModificationDto = ProcessInstanceModificationDto().apply {
    this.instructions = mutableListOf()
  }

  override fun startBeforeActivity(activityId: String): ProcessInstanceModificationInstantiationBuilder {
    modificationDto.instructions.add(
      ProcessInstanceModificationInstructionDto()
        .activityId(activityId)
        .type(ProcessInstanceModificationInstructionDto.TypeEnum.STARTBEFOREACTIVITY)
    )
    return this
  }

  override fun startBeforeActivity(activityId: String, ancestorActivityInstanceId: String): ProcessInstanceModificationInstantiationBuilder {
    modificationDto.instructions.add(
      ProcessInstanceModificationInstructionDto()
        .activityId(activityId)
        .ancestorActivityInstanceId(ancestorActivityInstanceId)
        .type(ProcessInstanceModificationInstructionDto.TypeEnum.STARTBEFOREACTIVITY)
    )
    return this
  }

  override fun startAfterActivity(activityId: String): ProcessInstanceModificationInstantiationBuilder {
    modificationDto.instructions.add(
      ProcessInstanceModificationInstructionDto()
        .activityId(activityId)
        .type(ProcessInstanceModificationInstructionDto.TypeEnum.STARTAFTERACTIVITY)
    )
    return this
  }

  override fun startAfterActivity(activityId: String, ancestorActivityInstanceId: String): ProcessInstanceModificationInstantiationBuilder {
    modificationDto.instructions.add(
      ProcessInstanceModificationInstructionDto()
        .activityId(activityId)
        .ancestorActivityInstanceId(ancestorActivityInstanceId)
        .type(ProcessInstanceModificationInstructionDto.TypeEnum.STARTAFTERACTIVITY)
    )
    return this
  }

  override fun startTransition(transitionId: String): ProcessInstanceModificationInstantiationBuilder {
    modificationDto.instructions.add(
      ProcessInstanceModificationInstructionDto()
        .transitionId(transitionId)
        .type(ProcessInstanceModificationInstructionDto.TypeEnum.STARTTRANSITION)
    )
    return this
  }

  override fun startTransition(transitionId: String, ancestorActivityInstanceId: String): ProcessInstanceModificationInstantiationBuilder {
    modificationDto.instructions.add(
      ProcessInstanceModificationInstructionDto()
        .transitionId(transitionId)
        .ancestorActivityInstanceId(ancestorActivityInstanceId)
        .type(ProcessInstanceModificationInstructionDto.TypeEnum.STARTTRANSITION)
    )
    return this
  }

  override fun cancelAllForActivity(activityId: String): ProcessInstanceModificationBuilder {
    modificationDto.instructions.add(
      ProcessInstanceModificationInstructionDto()
        .activityId(activityId)
        .type(ProcessInstanceModificationInstructionDto.TypeEnum.CANCEL)
    )
    return this
  }


  override fun cancelActivityInstance(activityInstanceId: String): ProcessInstanceModificationBuilder {
    modificationDto.instructions.add(
      ProcessInstanceModificationInstructionDto()
        .activityInstanceId(activityInstanceId)
        .type(ProcessInstanceModificationInstructionDto.TypeEnum.CANCEL)
    )
    return this
  }

  override fun cancelTransitionInstance(transitionInstanceId: String): ProcessInstanceModificationBuilder {
    modificationDto.instructions.add(
      ProcessInstanceModificationInstructionDto()
        .transitionInstanceId(transitionInstanceId)
        .type(ProcessInstanceModificationInstructionDto.TypeEnum.CANCEL)
    )
    return this
  }

  override fun cancellationSourceExternal(externallyTerminated: Boolean): ProcessInstanceModificationBuilder {
    TODO("Not yet implemented")
  }

  override fun setAnnotation(annotation: String): ProcessInstanceModificationBuilder {
    modificationDto.annotation = annotation
    return this
  }

  override fun setVariable(name: String, value: Any): ProcessInstanceModificationInstantiationBuilder {
    TODO("Not yet implemented")
  }

  override fun setVariableLocal(p0: String?, p1: Any?): ProcessInstanceModificationInstantiationBuilder {
    TODO("Not yet implemented")
  }

  override fun setVariables(p0: MutableMap<String, Any>?): ProcessInstanceModificationInstantiationBuilder {
    TODO("Not yet implemented")
  }

  override fun setVariablesLocal(p0: MutableMap<String, Any>?): ProcessInstanceModificationInstantiationBuilder {
    TODO("Not yet implemented")
  }

  override fun execute() = execute(skipCustomListeners = false, skipIoMappings = false)

  override fun execute(skipCustomListeners: Boolean, skipIoMappings: Boolean) {
    modificationDto.skipCustomListeners = skipCustomListeners
    modificationDto.skipIoMappings = skipIoMappings
    processInstanceApi.modifyProcessInstance(processInstanceId, modificationDto)
  }

  override fun executeAsync(): Batch = executeAsync(skipCustomListeners = false, skipIoMappings = false)

  override fun executeAsync(skipCustomListeners: Boolean, skipIoMappings: Boolean): Batch {
    modificationDto.skipCustomListeners = skipCustomListeners
    modificationDto.skipIoMappings = skipIoMappings
    return BatchAdapter(BatchBean.fromDto(processInstanceApi.modifyProcessInstanceAsyncOperation(processInstanceId, modificationDto).body!!))
  }

}
