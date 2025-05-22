package org.camunda.community.rest.example.feign

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import feign.Logger
import org.camunda.community.rest.client.EnableCamundaFeignClients
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.scheduling.annotation.EnableScheduling
import java.text.SimpleDateFormat

/**
 * Starts example application.
 */
fun main(args: Array<String>) {
  runApplication<CamundaRestClientFeignExampleApplication>(*args)
}

/**
 * Application directly using feign clients.
 */
@SpringBootApplication
@EnableCamundaFeignClients
@EnableScheduling
class CamundaRestClientFeignExampleApplication {
  // full debug of feign client
  @Bean
  fun feignLoggerLevel(): Logger.Level {
    return Logger.Level.FULL
  }

  @Bean
  fun objectMapper(): ObjectMapper {
    val mapper = ObjectMapper()
    mapper.registerModule(Jdk8Module())
    mapper.registerModule(JavaTimeModule())
    mapper.dateFormat = SimpleDateFormat("yyyy-MM-dd'T'hh:MM:ss.SSSz")
    return mapper
  }
}
