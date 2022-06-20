package org.camunda.community.rest.example.standalone;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import feign.Logger;
import org.camunda.community.rest.EnableCamundaRestClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.text.SimpleDateFormat;

/**
 * Application having no embedded engine.
 */
@SpringBootApplication
@EnableCamundaRestClient
@EnableScheduling
public class CamundaRestClientExampleApplication {
  // full debug of feign client
  @Bean
  public Logger.Level feignLoggerLevel() {
    return Logger.Level.FULL;
  }

  @Bean
  public ObjectMapper objectMapper() {
    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new Jdk8Module());
    mapper.registerModule(new JavaTimeModule());
    mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'hh:MM:ss.SSSz"));
    return mapper;
  }

  public static void main(String[] args) {
    SpringApplication.run(CamundaRestClientExampleApplication.class, args);
  }
}
