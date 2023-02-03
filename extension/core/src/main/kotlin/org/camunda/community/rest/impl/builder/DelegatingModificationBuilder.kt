package org.camunda.community.rest.impl.builder

import org.camunda.bpm.engine.batch.Batch
import org.camunda.bpm.engine.impl.ProcessInstanceQueryImpl
import org.camunda.bpm.engine.runtime.ModificationBuilder
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery
import org.camunda.community.rest.adapter.BatchAdapter
import org.camunda.community.rest.adapter.BatchBean
import org.camunda.community.rest.client.api.ModificationApi
import org.camunda.community.rest.client.model.CorrelationMessageDto
import org.camunda.community.rest.client.model.ModificationDto
import org.camunda.community.rest.client.model.MultipleProcessInstanceModificationInstructionDto
import org.camunda.community.rest.impl.query.toDto

class DelegatingModificationBuilder(
  processDefinitionId: String,
  private val modificationApi: ModificationApi
) : ModificationBuilder {

  private val modificationDto: ModificationDto = ModificationDto().apply {
    this.processDefinitionId = processDefinitionId
    this.instructions = mutableListOf()
  }

  override fun startBeforeActivity(activityId: String): ModificationBuilder {
    modificationDto.instructions.add(
      MultipleProcessInstanceModificationInstructionDto()
        .activityId(activityId)
        .type(MultipleProcessInstanceModificationInstructionDto.TypeEnum.STARTBEFOREACTIVITY)
    )
    return this
  }

  override fun startAfterActivity(activityId: String): ModificationBuilder {
    modificationDto.instructions.add(
      MultipleProcessInstanceModificationInstructionDto()
        .activityId(activityId)
        .type(MultipleProcessInstanceModificationInstructionDto.TypeEnum.STARTAFTERACTIVITY)
    )
    return this
  }

  override fun startTransition(transitionId: String): ModificationBuilder {
    modificationDto.instructions.add(
      MultipleProcessInstanceModificationInstructionDto()
        .transitionId(transitionId)
        .type(MultipleProcessInstanceModificationInstructionDto.TypeEnum.STARTTRANSITION)
    )
    return this
  }

  override fun cancelAllForActivity(activityId: String): ModificationBuilder {
    modificationDto.instructions.add(
      MultipleProcessInstanceModificationInstructionDto()
        .activityId(activityId)
        .type(MultipleProcessInstanceModificationInstructionDto.TypeEnum.CANCEL)
    )
    return this
  }

  override fun cancelAllForActivity(activityId: String, cancelCurrentActiveActivityInstances: Boolean): ModificationBuilder {
    modificationDto.instructions.add(
      MultipleProcessInstanceModificationInstructionDto()
        .activityId(activityId)
        .cancelCurrentActiveActivityInstances(cancelCurrentActiveActivityInstances)
        .type(MultipleProcessInstanceModificationInstructionDto.TypeEnum.CANCEL)
    )
    return this
  }

  override fun processInstanceIds(processInstanceIds: List<String>): ModificationBuilder {
    modificationDto.processInstanceIds = processInstanceIds
    return this
  }

  override fun processInstanceIds(vararg processInstanceIds: String): ModificationBuilder {
    modificationDto.processInstanceIds = processInstanceIds.toList()
    return this
  }

  override fun processInstanceQuery(processInstanceQuery: ProcessInstanceQuery): ModificationBuilder {
    modificationDto.processInstanceQuery =
      if (processInstanceQuery is ProcessInstanceQueryImpl) processInstanceQuery.toDto() else throw IllegalArgumentException()
    return this
  }

  override fun skipCustomListeners(): ModificationBuilder {
    modificationDto.skipCustomListeners = true
    return this
  }

  override fun skipIoMappings(): ModificationBuilder {
    modificationDto.skipIoMappings = true
    return this
  }

  override fun setAnnotation(annotation: String): ModificationBuilder {
    modificationDto.annotation = annotation
    return this
  }

  override fun execute() {
    modificationApi.executeModification(modificationDto)
  }

  override fun executeAsync(): Batch {
    return BatchAdapter(BatchBean.fromDto(modificationApi.executeModificationAsync(modificationDto).body!!))
  }
}
