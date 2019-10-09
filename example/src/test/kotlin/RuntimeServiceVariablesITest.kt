package org.camunda.bpm.extension.feign.itest

import com.tngtech.jgiven.annotation.As
import org.assertj.core.api.Assertions.assertThat
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.variable.Variables.createVariables
import org.camunda.bpm.engine.variable.Variables.stringValue
import org.junit.Test
import java.util.*

@RuntimeServiceCategory
@As("Variables")
class RuntimeServiceVariablesITest : CamundaBpmFeignITestBase<RuntimeService, RuntimeServiceActionStage, RuntimeServiceAssertStage>() {

  @Test
  fun `should add new, update and delete existing variables`() {
    val processDefinitionKey = processDefinitionKey()
    val signalName = "var_process_blocker_1"
    val userTaskId = "user-task"
    given()
      .process_with_intermediate_signal_catch_event_is_deployed(processDefinitionKey, userTaskId, signalName)
      .and()
      .process_is_started_by_key(processDefinitionKey, "my-business-key1", "caseInstanceId1",
        createVariables()
          .putValue("VAR1", "VAL1")
          .putValue("VAR2", "VAL2")
          .putValueTyped("VAR3", stringValue("VAL3"))
      )

    whenever()
      .remoteService
      .removeVariable(given().processInstance.id, "VAR2")

    whenever()
      .remoteService
      .setVariable(given().processInstance.id, "VAR1", "NEW VALUE")

    whenever()
      .remoteService
      .setVariable(given().processInstance.id, "VAR4", "untyped")

    whenever()
      .remoteService
      .setVariables(given().processInstance.id, createVariables().putValueTyped("VAR5", stringValue("typed")))


    then()
      .process_instance_exists(processDefinitionKey) { instance, stage ->
        assertThat(instance.businessKey).isEqualTo("my-business-key1")
        assertThat(instance.caseInstanceId).isEqualTo("caseInstanceId1")
        assertThat(
          stage.localService.getVariables(instance.id, listOf("VAR1", "VAR3", "VAR4", "VAR5"))
        ).containsValues("NEW VALUE", "VAL3", "untyped", "typed")
      }
  }
}
