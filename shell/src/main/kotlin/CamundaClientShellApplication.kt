package org.camunda.bpm.extension.rest.starter

import org.camunda.bpm.extension.rest.EnableCamundaRestClient
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.shell.standard.ShellComponent
import org.springframework.shell.standard.ShellMethod

fun main(args: Array<String>) = runApplication<CamundaClientShellApplication>().let {  }

@SpringBootApplication
@EnableCamundaRestClient
class CamundaClientShellApplication {



}

@ShellComponent
class HelloWorld {

  @ShellMethod(value = "description: this is the say hello command", key = ["foo"])
  fun sayHello(name: String) = "Hallo $name!"

}
