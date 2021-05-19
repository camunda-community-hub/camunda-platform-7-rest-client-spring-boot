package org.camunda.bpm.extension.rest.client

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.multipart.MultipartFile

/**
 * Feign client accessing the methods of runtime service.
 */
@FeignClient(name = "remoteDeploymentService", url = "\${feign.client.config.remoteDeploymentService.url}")
interface DeploymentServiceClient {

  /**
   * Creates a deployment.
   * @see https://docs.camunda.org/manual/latest/reference/rest/deployment/post-deployment/
   * @see https://medium.com/comsystoreply/sending-multipart-requests-using-spring-boot-and-feign-20d5602d0f21
   */
  @RequestMapping(method = [RequestMethod.POST], value = ["/deployment/create"], consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
  fun createDeployment(
    @PathVariable("deployment-name") deploymentName: String,
    @PathVariable("enable-duplicate-filtering") enableDuplicateFiltering: Boolean,
    @PathVariable("") deployChangedOnly: Boolean,
    @PathVariable("") deploymentSource: String,
    @PathVariable("tenant-id") tenantId: String?,
    files: List<MultipartFile>
  )
}
