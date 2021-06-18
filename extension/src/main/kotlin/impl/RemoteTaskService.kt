package org.camunda.bpm.extension.rest.impl

import com.fasterxml.jackson.databind.ObjectMapper
import org.camunda.bpm.engine.ProcessEngine
import org.camunda.bpm.engine.task.TaskQuery
import org.camunda.bpm.extension.rest.adapter.AbstractTaskServiceAdapter
import org.camunda.bpm.extension.rest.client.TaskServiceClient
import org.camunda.bpm.extension.rest.impl.query.DelegatingTaskQuery
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

@Component
@Qualifier("remote")
class RemoteTaskService(
  private val taskServiceClient: TaskServiceClient,
  processEngine: ProcessEngine,
  objectMapper: ObjectMapper
) : AbstractTaskServiceAdapter() {

  override fun createTaskQuery(): TaskQuery {
    return DelegatingTaskQuery(taskServiceClient)
  }

}
