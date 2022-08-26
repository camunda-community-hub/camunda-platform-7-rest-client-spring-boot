The `camunda-platform-7-rest-client-spring-boot` uses HTTP to access a remote Camunda Platform 7 REST API. If any errors occur on
this access, Camunda Platform 7 REST API will send with corresponding HTTP error code and embed information about the error
into http response. `camunda-platform-7-rest-client-spring-boot` tries to parse this response and throw an exception on the client
side similar to the original exception thrown on the remote Camunda Platform 7 Engine.

By default, the library tries to decode HTTP codes and will throw a `RemoteProcessEngineException`. If the response decoding
was successful, the _cause_ of the thrown `RemoteProcessEngineException` will be the instance of the exception class thrown
on remote Camunda Platform 7 engine and the _reason_ of the latter exception will be the original reason from the server.
This behavior can be changed by configuration, so that the remotely thrown exception will be thrown locally, if the decoding was successful. For this the property `camunda.rest.client.error-decoding.wrap-exception` has to be set to false.

If anything goes wrong on HTTP error decoding, the `RemoteProcessEngineException` will contain a generic message extracted from
the REST call. If the error decoding is deactivated, `FeignException` is wrapping any exception occurring during the remote access.

## Configuration 

By default, the HTTP error decoding is switched on and the library reacts on HTTP codes 400 and 500. Also by default all exceptions will be wrapped in a RemoteProcessEngineException. Those defaults can be
changed by setting the following properties.

In order to configure it, a block of properties e.g. in `application.yml` is required. Here are the defaults:

```yml
camunda:
  rest:
    client:
      error-decoding:
        enabled: true
        http-codes: 400, 500
        wrap-exception: true
```

!!! info

      If you are using the remote version of the `ExternalTaskService` this will report HTTP 404 if you try
      to complete a non-existing task. By changing the `camunda.rest.client.error-decoding.http-codes` property you
      can cover this response too.

