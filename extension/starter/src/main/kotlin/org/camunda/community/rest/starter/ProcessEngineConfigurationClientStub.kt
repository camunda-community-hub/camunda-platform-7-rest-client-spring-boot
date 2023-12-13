package org.camunda.community.rest.starter

import org.camunda.bpm.engine.*

/**
 * Configuration for building a process engine abstraction for the client
 * which is not using the process engine itself, but is used just to provide
 * the remote services.
 */
internal class ProcessEngineConfigurationClientStub(
  private val decisionService: DecisionService,
  private val externalTaskService: ExternalTaskService,
  private val historyService: HistoryService,
  private val repositoryService: RepositoryService,
  private val runtimeService: RuntimeService,
  private val taskService: TaskService,
) : ProcessEngineConfiguration {

  fun buildProcessEngine(): ProcessEngine {
    return ProcessEngineClientStub(
      this,
      decisionService,
      externalTaskService,
      historyService,
      repositoryService,
      runtimeService,
      taskService
    )
  }

}

/**
 * Client process engine representation.
 */
internal class ProcessEngineClientStub(
  private val configuration: ProcessEngineConfiguration,
  private val decisionService: DecisionService,
  private val externalTaskService: ExternalTaskService,
  private val historyService: HistoryService,
  private val repositoryService: RepositoryService,
  private val runtimeService: RuntimeService,
  private val taskService: TaskService,
) : ProcessEngine {

  override fun getProcessEngineConfiguration() = configuration

  override fun getRepositoryService() = repositoryService

  override fun getTaskService() = taskService

  override fun getName() = "rest-client-engine-stub"

  override fun getCaseService(): CaseService {
    TODO("not implemented")
  }

  override fun getRuntimeService() = runtimeService

  override fun getDecisionService() = decisionService

  override fun getFormService(): FormService {
    TODO("not implemented")
  }

  override fun getFilterService(): FilterService {
    TODO("not implemented")
  }

  override fun getAuthorizationService(): AuthorizationService {
    TODO("not implemented")
  }

  override fun getHistoryService() = historyService

  override fun getIdentityService(): IdentityService {
    TODO("not implemented")
  }

  override fun getManagementService(): ManagementService {
    TODO("not implemented")
  }

  override fun getExternalTaskService() = externalTaskService

  override fun close() {
    // nothing to do here, but we need to implement this to comply with the contract.
  }

}

