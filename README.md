# Camunda BPM REST client

This library provides a Camunda BPM REST client based on SpringCloud Feign, by implementing 
a set of Camunda Engine API interfaces, invoking remote process engine.   

## Quick start

Add the following Maven dependency to your project:

### Dependency
``` 
<dependency>
  <groupId>org.camunda.bpm.extension.restclient</groupId>
  <artifactId>camunda-bpm-rest-client</artifactId>
  <version>${camunda-bpm-rest-client.version}</version>
</dependency>
```

### Configuration
In your client code, activate the usage of REST client by adding the following annotation
to your configuration:

``` 
@Configuration
@EnableCamundaRestClient
public class MyClientConfiguration {

}
```

In order to configure the feign client, make sure to provide usual feign client configuration 
(e.g. using `application.yml`). To set-up the engine base URL, please set-up the property:

```
feign:
  client:
    config:
      remoteRuntimeService:
        url: "http://your-process-enginehost/rest/engine/default/"

```

### Usage
To access the remote API, inject the remote API implementation:

``` 
@Component
public class MyClient {
    
    private RuntimeService runtimeService;

    public MyClient(@Qulifier("remote") RuntimeService runtimeService) {
        this.runtimeService = runtimeService;
    }

    public void correlate() {
        this.runtimeService
            .createMessageCorrelation("message_received")
            .processInstanceBusinessKey("WAIT_FOR_MESSAGE")
            .correlateAllWithResult();
    }
}
```

### Implemented API
* `RuntimeService`
  * Message correlation: `#correlateMessage`, `#createMessageCorrelation()` 

## Example

Please check [example](./example) project for demonstration and use cases.

## Contributing

Please check the [Contribution Guide](CONTRIBUTING.md) if you want to support the project, file a new issue or
submit a pull request.

## Maintainer

* [Simon Zambrovski](https://gihub.com/zambrovski)

## License

* [Apache License 2.0](LICENSE)
