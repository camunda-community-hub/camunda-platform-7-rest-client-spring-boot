package org.camunda.bpm.extension.restclient

import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

/**
 * Basi configuration of the extension.
 */
@Configuration
@ComponentScan
@EnableFeignClients
class CamundaBpmRestClientExtension


/**
 * Enables the registration of REST client beans.
 */
@Import(CamundaBpmRestClientExtension::class)
annotation class EnableCamundaRestClient
