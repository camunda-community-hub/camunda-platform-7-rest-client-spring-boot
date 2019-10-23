package org.camunda.bpm.extension.feign.example

import com.fasterxml.jackson.databind.ObjectMapper
import feign.Logger
import org.camunda.bpm.extension.feign.EnableCamundaFeign
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.context.annotation.Bean
import org.springframework.scheduling.annotation.EnableScheduling

fun main() {
  SpringApplication.run(CamundaBpmFeignExampleApplication::class.java)
}

@SpringBootApplication
@EnableFeignClients
@EnableScheduling
@EnableCamundaFeign
class CamundaBpmFeignExampleApplication {

  // full debug of feign client
  @Bean
  fun feignLoggerLevel(): Logger.Level = Logger.Level.FULL


  @Bean
  fun objectMapper(): ObjectMapper {
    val objectMapper = ObjectMapper()
    return JacksonDataFormatConfigurator.configureObjectMapper(objectMapper)
  }
}
