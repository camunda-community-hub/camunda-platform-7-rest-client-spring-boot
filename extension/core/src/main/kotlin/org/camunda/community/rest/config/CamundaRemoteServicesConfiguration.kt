package org.camunda.community.rest.config

import org.camunda.community.rest.impl.*
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@Import(
  RemoteExternalTaskService::class,
  RemoteHistoryService::class,
  RemoteRepositoryService::class,
  RemoteRuntimeService::class,
  RemoteTaskService::class,
  RemoteDecisionService::class
)
class CamundaRemoteServicesConfiguration
