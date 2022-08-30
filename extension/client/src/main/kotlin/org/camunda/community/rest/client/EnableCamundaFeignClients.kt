package org.camunda.community.rest.client

import org.springframework.context.annotation.Import

/**
 * Enables the registration of Feign clients for Camunda Rest API.
 */
@Import(FeignClientConfiguration::class)
annotation class EnableCamundaFeignClients
