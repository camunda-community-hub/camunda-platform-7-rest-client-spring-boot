!!! note

    If you are using the extension from an application containing Camunda BPM Engine classes on the classpath, please check the
    [Working Example](./examples/Usage_inside_a_process_application) section of our user guide.

!!! note

    You can also use the generated feign clients directly without using the Camunda Services.
    Please check out the [Feign Example](./examples/Usage_of_feign_clients) section of our user guide.

## Install Dependency

First install the extension dependency and configure Feign and Feign client:

```xml

<properties>
  <camunda-platform-7-rest-client-spring-boot.version>{{ POM_VERSION }}</camunda-platform-7-rest-client-spring-boot.version>
</properties>

<dependencies>
  <dependency>
    <groupId>org.camunda.community.rest</groupId>
    <artifactId>camunda-platform-7-rest-client-spring-boot-starter</artifactId>
    <version>${camunda-platform-7-rest-client-spring-boot.version}</version>
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
      default:
        url: "http://your-process-engine-host/engine-rest/"
```

There is also the possibility to configure a different URL for each feign client (even though this is a very uncommon setup):

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
      ...
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

