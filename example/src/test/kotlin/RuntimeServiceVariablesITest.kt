package org.camunda.bpm.extension.feign.itest

import com.tngtech.jgiven.annotation.As
import org.assertj.core.api.Assertions.assertThat
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.variable.Variables.*
import org.camunda.bpm.engine.variable.value.ObjectValue
import org.camunda.bpm.engine.variable.value.StringValue
import org.junit.Test

@RuntimeServiceCategory
@As("Variables")
class RuntimeServiceVariablesITest : CamundaBpmFeignITestBase<RuntimeService, RuntimeServiceActionStage, RuntimeServiceAssertStage>() {

  @Test
  fun `should add new, update and delete existing variables`() {
    val processDefinitionKey = processDefinitionKey()
    val signalName = "var_process_blocker_1"
    val userTaskId = "user-task"
    val structure = ComplexDataStructure("string", 17)

    given()
      .process_with_intermediate_signal_catch_event_is_deployed(processDefinitionKey, userTaskId, signalName)
      .and()
      .process_is_started_by_key(processDefinitionKey, "my-business-key1", "caseInstanceId1",
        createVariables()
          .putValue("VAR1", "VAL1")
          .putValue("VAR2", "VAL2")
          .putValue("TO_REMOVE1", "TO_REMOVE")
          .putValue("TO_REMOVE2", "TO_REMOVE")
          .putValueTyped("VAR3", stringValue("VAL3"))
          .putValueTyped("VAR4", stringValue("My object value"))
          .putValueTyped("VAR7",
            objectValue(structure)
              .serializationDataFormat("application/json")
              .create()
          )
      )

    whenever()
      .remoteService
      .removeVariable(given().processInstance.id, "VAR2")

    whenever()
      .remoteService
      .removeVariables(given().processInstance.id, listOf("TO_REMOVE1", "TO_REMOVE2"))

    whenever()
      .remoteService
      .setVariable(given().processInstance.id, "VAR1", "NEW VALUE")

    whenever()
      .remoteService
      .setVariable(given().processInstance.id, "VAR5", "untyped")

    whenever()
      .remoteService
      .setVariables(given().processInstance.id, createVariables().putValueTyped("VAR6", stringValue("typed")))


    then()
      .process_instance_exists(processDefinitionKey) { instance, stage ->
        assertThat(instance.businessKey).isEqualTo("my-business-key1")
        assertThat(instance.caseInstanceId).isEqualTo("caseInstanceId1")

        assertThat(stage.remoteService.getVariables(instance.id)).containsKeys("VAR1", "VAR3", "VAR4", "VAR5", "VAR6")
        assertThat(stage.remoteService.getVariables(instance.id, listOf("VAR1", "VAR2", "VAR6"))).containsKeys("VAR1", "VAR6")
        assertThat(stage.remoteService.getVariable(instance.id, "VAR1")).isEqualTo("NEW VALUE")

        assertThat(stage.remoteService.getVariableTyped<ObjectValue>(instance.id, "VAR7").value).isEqualTo(structure)
        assertThat(stage.remoteService.getVariableTyped<StringValue>(instance.id, "VAR4").value).isEqualTo("My object value")
        assertThat(stage.remoteService.getVariableTyped<StringValue>(instance.id, "VAR6")).isEqualTo(stringValue("typed"))
        assertThat(stage.remoteService.getVariableTyped<StringValue>(instance.id, "VAR6")).isEqualTo(stringValue("typed"))
        assertThat(stage.remoteService.getVariableTyped<StringValue>(instance.id, "VAR6", true)).isEqualTo(stringValue("typed"))

        assertThat(stage.remoteService.getVariablesTyped(instance.id)).containsKeys("VAR1", "VAR3", "VAR4", "VAR5", "VAR6")
        assertThat(stage.remoteService.getVariablesTyped(instance.id, true)).containsValues("NEW VALUE", "VAL3", "My object value", "untyped", "typed")
        assertThat(stage.remoteService.getVariablesTyped(instance.id, listOf("VAR1", "VAR6"), true)).containsValues("NEW VALUE", "typed")

        assertThat(stage.localService.getVariables(instance.id)).containsKeys("VAR1", "VAR3", "VAR4", "VAR5", "VAR6")
        assertThat(stage.localService.getVariable(instance.id, "VAR1")).isEqualTo("NEW VALUE")
        assertThat(stage.localService.getVariable(instance.id, "VAR3")).isEqualTo("VAL3")
        assertThat(stage.localService.getVariable(instance.id, "VAR4")).isEqualTo("My object value")
        assertThat(stage.localService.getVariable(instance.id, "VAR5")).isEqualTo("untyped")
        assertThat(stage.localService.getVariableTyped<StringValue>(instance.id, "VAR6")).isEqualTo(stringValue("typed"))
      }
  }
}

data class ComplexDataStructure(val string: String, val integer: Int)
