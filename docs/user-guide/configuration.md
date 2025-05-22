## Spring Boot Configuration

Configuration of the extension can be performed by setting up the following configuration properties in your `application.yaml`:
```yaml
camunda:
  rest:
    client:
      enabled: true       # flag to switch the entire extension on/off
      error-decoding:
        enabled: true     # enable error decoding
        http-codes: 400, 500 # what HTTP status codes should be considered as errors
        wrap-exceptions: true # wrap exceptions into ProcessEngineExceptions
      deserialize-variables-on-server: false # should variables be deserialized on process engine side (require classes to be on classpath)
```

## Customization and usage 

By default, the extension will provide you with the remote implementations of Camunda Java API (see [Support Matrix](support-matrix.md)).
To use them, you can inject them into your code by:

```java

@Autowired
@Qualifier("remote")
private RuntimeService runtimeService;

```

In addition, there are beans responsible for variable mapping: `ValueMapper`, `SpinValueMapper` and `ValueTypeResolver` which will be 
constructed if missing. Since the `ValueMapper` requires a Jackson ObjectMapper to operate, you may want to provide your own by putting
this code into your configuration:

```java

import java.beans.BeanProperty;

@Bean
@Qualifier("customCamundaRestClientObjectMapper")
public ObjectMapper myCustomObjectMapper() {
  ObjectMapper om = new ObjectMapper();
  // custom setup of your object mapper
  om.enabled(...); 
  om.register(...);
  return om;
} 

@Bean
public ValueMapper myCustomValueMapper(
  @Qualifier("customCamundaRestClientObjectMapper")
  ObjectMapper myCustomObjectMapper,
  ValueTypeResolver valueTypeResolver,
  List<CustomValueMapper> customValueMappers
) {
  return new ValueMapper(myCustomObjectMapper, valueTypeResolver, customValueMappers);
}
```
