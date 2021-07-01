package org.camunda.bpm.extension.rest.example.standalone;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import feign.Logger;
import org.camunda.bpm.extension.rest.EnableCamundaRestClient;
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
    mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    mapper.disable(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS);
    mapper.disable(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS);
    return mapper;
  }

  public static void main(String[] args) {
    SpringApplication.run(CamundaRestClientExampleApplication.class, args);
  }
}
