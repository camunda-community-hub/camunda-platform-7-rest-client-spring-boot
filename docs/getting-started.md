!!! note

    If you are using the extension from an application containing Camunda BPM Engine classes on the classpath, please check the
    [Working Example](./examples/sage_inside_a_process_application) section of our user guide.

## Install Dependency

First install the extension dependency and configure Feign and Feign client:

```xml

<properties>
  <camunda-platform-7-rest-client-spring-boot.version>0.0.3</camunda-platform-7-rest-client-spring-boot.version>
  <spring-cloud.version>Hoxton.SR2</spring-cloud.version>
</properties>

<dependencyManagement>
<dependencies>
  <dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-dependencies</artifactId>
    <version>${spring-cloud.version}</version>
    <type>pom</type>
    <scope>import</scope>
  </dependency>
</dependencies>
</dependencyManagement>
<dependencies>
<dependency>
  <groupId>org.camunda.community</groupId>
  <artifactId>camunda-platform-7-rest-client-spring-boot-starter</artifactId>
  <version>${camunda-platform-7-rest-client-spring-boot.version}</version>
</dependency>
<dependency>
  <groupId>org.springframework.cloud</groupId>
  <artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>
<dependency>
  <groupId>io.github.openfeign</groupId>
  <artifactId>feign-httpclient</artifactId>
</dependency>
</dependencies>
```

!!! note

    Please make sure your Spring Cloud version matches your Spring Boot version as described in the [Spring Cloud documentation](https://spring.io/projects/spring-cloud#release-trains)

## Configuration

In your client code, activate the usage of REST client by adding the following annotation to your configuration:

```java
@Configuration
@EnableCamundaRestClient
public class MyClientConfiguration {

}
```

In order to configure the Feign client, make sure to provide usual feign client configuration
(e.g. using `application.yml`). To set up the engine base URL, please set up the properties:

```yml
feign:
  client:
    config:
      processInstance:
        url: "http://your-process-engine-host/engine-rest/"
      processDefinition:
        url: "http://your-process-engine-host/engine-rest/"
      message:
        url: "http://your-process-engine-host/engine-rest/"
      signal:
        url: "http://your-process-engine-host/engine-rest/"
      execution:
        url: "http://your-process-engine-host/engine-rest/"
      task:
        url: "http://your-process-engine-host/engine-rest/"
      taskVariable:
        url: "http://your-process-engine-host/engine-rest/"
      taskLocalVariable:
        url: "http://your-process-engine-host/engine-rest/"
      taskIdentityLink:
        url: "http://your-process-engine-host/engine-rest/"
      externalTask:
        url: "http://your-process-engine-host/engine-rest/"
      incident:
        url: "http://your-process-engine-host/engine-rest/"
      historicProcessInstance:
        url: "http://your-process-engine-host/engine-rest/"
        
```

## Usage

To access the remote API, inject the remote API implementation:

```java
@Component
public class MyClient {

  private RuntimeService runtimeService;

  public MyClient(@Qualifier("remote") RuntimeService runtimeService) {
    this.runtimeService = runtimeService;
  }

  public void start() {
    this.runtimeService
      .startProcessInstanceByKey("my_process_key");
  }

  public void correlate() {
    this.runtimeService
      .createMessageCorrelation("message_received")
      .processInstanceBusinessKey("WAIT_FOR_MESSAGE")
      .correlateAllWithResult();
  }
}
```

