During the development of Camunda Platform 7 process applications you have to choose if your applications is either
_using_ Camunda Platform 7 Engine or _is_ Camunda Platform 7 Engine. Depending on this decision, you are accessing Camunda Platform 7 via REST
or Java API.

While Camunda Engine Core API provides well-designed and easy-accessible programming interface for Java, the
usage of REST interface requires additional development. In order to enable the usage of REST API from Java
and allow for easy integration into Spring Boot applications, the Camunda Platform 7 REST Client Spring-Boot library has
been developed.

So instead of usage a hand-written or generated client, this library provides you the same access to a remote Camunda Platform 7 engine
as the Java API provides to local Camunda. The difference between _using_ and _being_ disappears. 
