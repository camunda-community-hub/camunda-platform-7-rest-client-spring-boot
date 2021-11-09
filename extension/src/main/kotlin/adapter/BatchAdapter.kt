package org.camunda.bpm.extension.rest.adapter

import org.camunda.bpm.engine.batch.Batch
import org.camunda.bpm.extension.rest.client.model.BatchDto

class BatchAdapter(private val batchBean: BatchBean) : Batch {

  override fun getId() = batchBean.id
  override fun getType() = batchBean.type
  override fun getTotalJobs() = batchBean.totalJobs
  override fun getJobsCreated() = batchBean.jobsCreated
  override fun getBatchJobsPerSeed() = batchBean.batchJobsPerSeed
  override fun getInvocationsPerBatchJob() = batchBean.invocationsPerBatchJob
  override fun getSeedJobDefinitionId() = batchBean.seedJobDefinitionId
  override fun getMonitorJobDefinitionId() = batchBean.monitorJobDefinitionId
  override fun getBatchJobDefinitionId() = batchBean.batchJobDefinitionId
  override fun getTenantId() = batchBean.tenantId
  override fun getCreateUserId() = batchBean.createUserId
  override fun isSuspended() = batchBean.suspended

}

/**
 * POJO to hold the values of a batch.
 */
data class BatchBean(
  val id: String,
  val type: String,
  val totalJobs: Int,
  val jobsCreated: Int,
  val batchJobsPerSeed: Int,
  val invocationsPerBatchJob: Int,
  val seedJobDefinitionId: String?,
  val monitorJobDefinitionId: String?,
  val batchJobDefinitionId: String?,
  val tenantId: String?,
  val createUserId: String,
  val suspended: Boolean
) {
  companion object {
    /**
     * Factory method to create bean from REST representation.
     */
    @JvmStatic
    fun fromDto(dto: BatchDto) = BatchBean(
      id = dto.id,
      type = dto.type,
      totalJobs = dto.totalJobs,
      jobsCreated = dto.jobsCreated,
      batchJobsPerSeed = dto.batchJobsPerSeed,
      invocationsPerBatchJob = dto.invocationsPerBatchJob,
      seedJobDefinitionId = dto.seedJobDefinitionId,
      monitorJobDefinitionId = dto.monitorJobDefinitionId,
      batchJobDefinitionId = dto.batchJobDefinitionId,
      tenantId = dto.tenantId,
      createUserId = dto.createUserId,
      suspended = dto.suspended
    )
  }
}

