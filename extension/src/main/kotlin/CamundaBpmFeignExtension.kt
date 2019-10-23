package org.camunda.bpm.extension.feign

import com.fasterxml.jackson.databind.ObjectMapper
import org.camunda.bpm.extension.feign.mixin.CamundaMixinModule
import org.springframework.beans.factory.annotation.Autowired
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
class CamundaBpmFeignExtension {

  @Autowired(required = false)
  fun configureJackson(objectMapper: ObjectMapper) {
    objectMapper.registerModule(CamundaMixinModule())
  }
}


/**
 * Enables the registration of REST client beans.
 */
@Import(CamundaBpmFeignExtension::class)
annotation class EnableCamundaFeign
