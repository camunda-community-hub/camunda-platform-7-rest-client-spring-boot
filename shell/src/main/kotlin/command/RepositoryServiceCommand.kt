package org.camunda.bpm.extension.rest.starter.command

import org.camunda.bpm.engine.FormService
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.repository.ProcessDefinition
import org.springframework.shell.standard.ShellComponent
import org.springframework.shell.standard.ShellMethod
import org.springframework.shell.standard.ShellOption
import org.springframework.shell.standard.ShellOption.*
import org.springframework.shell.table.BeanListTableModel
import org.springframework.shell.table.BorderStyle
import org.springframework.shell.table.TableBuilder
import org.springframework.shell.table.TableModel

@ShellComponent
class RepositoryServiceCommand(
  private val repositoryService: RepositoryService
) {

  @ShellMethod("list deployed process definitions")
  fun listDeployedProcessDefinitions(@ShellOption(value= ["--name"], defaultValue = NULL) name: String?): String {
    val query = repositoryService.createProcessDefinitionQuery().let {
      if (name != null) { it.processDefinitionNameLike(name)} else it
    }
    val definitions: List<ProcessDefinition> =  query.list()

    val headers = linkedMapOf<String,Any>(
      "id" to "Id",
      "name" to "Name",
      "description" to "Description",
      "key" to "Key",
      "version" to "Version",
      "versionTag" to "Version Tag",
      "deploymentId" to "DeploymentId",
      "tenantId" to "Tenant Id",
    )


//    /** Returns true if the process definition is in suspended state.  */
//    fun isSuspended(): Boolean
//
//    /** Returns true if the process definition is startable in Tasklist.  */
//    fun isStartableInTasklist(): Boolean


    val model: TableModel = BeanListTableModel(definitions, headers)

    val tableBuilder = TableBuilder(model)
    tableBuilder.addInnerBorder(BorderStyle.fancy_light)
    tableBuilder.addHeaderBorder(BorderStyle.fancy_double)

    return tableBuilder.build().render(80)
  }

}
