package org.camunda.bpm.extension.rest.starter.command

import org.camunda.bpm.engine.RuntimeService
import org.springframework.shell.standard.ShellComponent

@ShellComponent
class RuntimeServiceCommand(
  private val runtimeService: RuntimeService
) {
}
