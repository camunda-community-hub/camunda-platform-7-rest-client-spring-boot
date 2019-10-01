package org.camunda.bpm.extension.restclient.example

import feign.Logger
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.context.annotation.Bean
import org.springframework.scheduling.annotation.EnableScheduling

fun main() {
  SpringApplication.run(CamundaRestClientExampleApplication::class.java)
}

@SpringBootApplication
@EnableFeignClients
@EnableScheduling
class CamundaRestClientExampleApplication {

  // full debug of feign client
  @Bean
  fun feignLoggerLevel(): Logger.Level = Logger.Level.FULL

}
