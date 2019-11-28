package org.camunda.bpm.extension.rest.starter

import org.camunda.bpm.engine.*
import org.camunda.bpm.engine.impl.variable.ValueTypeResolverImpl

/**
 * Configuration for building a process engine abstraction for the client
 * which is not using the process engine itself, but is used just to provide
 * the facade to encapsulate required facilities:
 * <ul>
 *   <li>ValueTypeResolver</li>
 * </ul>
 */
internal class ProcessEngineConfigurationClientStub : ProcessEngineConfiguration() {

  init {
      this.valueTypeResolver = ValueTypeResolverImpl()
  }

  override fun buildProcessEngine(): ProcessEngine {
    return ProcessEngineClientStub(this)
  }
}

/**
 * Client process engine representation.
 */
internal class ProcessEngineClientStub(
  private val configuration: ProcessEngineConfiguration
): ProcessEngine {

  override fun getProcessEngineConfiguration(): ProcessEngineConfiguration {
    return configuration
  }

  override fun getRepositoryService(): RepositoryService {
    TODO("not implemented")
  }

  override fun getTaskService(): TaskService {
    TODO("not implemented")
  }

  override fun getName(): String {
    TODO("not implemented")
  }

  override fun getCaseService(): CaseService {
    TODO("not implemented")
  }

  override fun getRuntimeService(): RuntimeService {
    TODO("not implemented")
  }

  override fun getDecisionService(): DecisionService {
    TODO("not implemented")
  }

  override fun getFormService(): FormService {
    TODO("not implemented")
  }

  override fun getFilterService(): FilterService {
    TODO("not implemented")
  }

  override fun getAuthorizationService(): AuthorizationService {
    TODO("not implemented")
  }

  override fun getHistoryService(): HistoryService {
    TODO("not implemented")
  }

  override fun getIdentityService(): IdentityService {
    TODO("not implemented")
  }

  override fun getManagementService(): ManagementService {
    TODO("not implemented")
  }

  override fun getExternalTaskService(): ExternalTaskService {
    TODO("not implemented")
  }

  override fun close() {
    TODO("not implemented")
  }

}

