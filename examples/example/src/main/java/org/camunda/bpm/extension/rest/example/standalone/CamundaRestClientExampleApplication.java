package org.camunda.bpm.extension.rest.example.standalone;

import feign.Logger;
import org.camunda.bpm.extension.rest.EnableCamundaRestClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableCamundaRestClient
@EnableScheduling
public class CamundaRestClientExampleApplication {
  // full debug of feign client
  @Bean
  public Logger.Level feignLoggerLevel() {
    return Logger.Level.FULL;
  }

  public static void main(String[] args) {
    SpringApplication.run(CamundaRestClientExampleApplication.class, args);
  }
}
