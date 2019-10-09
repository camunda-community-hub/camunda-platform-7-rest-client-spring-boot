package org.camunda.bpm.extension.feign.itest

import com.tngtech.jgiven.annotation.As
import org.assertj.core.api.Assertions.assertThat
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.variable.Variables.createVariables
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import java.util.*

@RuntimeServiceCategory
@As("Start Process By Id")
class RuntimeServiceStartProcessByIdITest : CamundaBpmFeignITestBase<RuntimeService, RuntimeServiceActionStage, RuntimeServiceAssertStage>() {

  @Test
  fun `should start process by id`() {
    val processDefinitionKey = processDefinitionKey()
    given()
      .process_with_user_task_is_deployed(processDefinitionKey)

    whenever()
      .remoteService
      .startProcessInstanceById(whenever().processDefinition.id)

    then()
      .process_instance_exists(processDefinitionId = whenever().processDefinition.id) { instance, stage ->
        assertThat(instance.businessKey).isNull()
        assertThat(stage.localService.getVariables(instance.id)).isEmpty()
      }
  }

  @Test
  fun `should start process by id with business key`() {
    val processDefinitionKey = processDefinitionKey()
    given()
      .process_with_user_task_is_deployed(processDefinitionKey)

    whenever()
      .remoteService
      .startProcessInstanceById(whenever().processDefinition.id, "businessKey")

    then()
      .process_instance_exists(processDefinitionId = whenever().processDefinition.id) { instance, stage ->
        assertThat(instance.businessKey).isEqualTo("businessKey")
        assertThat(stage.localService.getVariables(instance.id)).isEmpty()
      }
  }

  @Test
  fun `should start process by id with business id and variables`() {
    val processDefinitionKey = processDefinitionKey()
    given()
      .process_with_user_task_is_deployed(processDefinitionKey)

    whenever()
      .remoteService
      .startProcessInstanceById(whenever().processDefinition.id, "businessKey", createVariables().putValue("VAR_NAME", "var value"))

    then()
      .process_instance_exists(processDefinitionId = whenever().processDefinition.id) { instance, stage ->
        assertThat(instance.businessKey).isEqualTo("businessKey")
        assertThat(stage.localService.getVariables(instance.id)).isNotEmpty
        assertThat(stage.localService.getVariable(instance.id, "VAR_NAME")).isEqualTo("var value")
      }
  }

  @Test
  fun `should start process by id with business id and case instance id`() {
    val processDefinitionKey = processDefinitionKey()
    given()
      .process_with_user_task_is_deployed(processDefinitionKey)

    whenever()
      .remoteService
      .startProcessInstanceById(whenever().processDefinition.id, "businessKey", "caseInstanceId")

    then()
      .process_instance_exists(processDefinitionId = whenever().processDefinition.id) { instance, stage ->
        assertThat(instance.businessKey).isEqualTo("businessKey")
        assertThat(instance.caseInstanceId).isEqualTo("caseInstanceId")
        assertThat(stage.localService.getVariables(instance.id)).isEmpty()
      }
  }


  @Test
  fun `should start process by id with business key, case instance id and variables`() {
    val processDefinitionKey = processDefinitionKey()

    given()
      .process_with_user_task_is_deployed(processDefinitionKey)

    whenever()
      .remoteService
      .startProcessInstanceById(whenever().processDefinition.id, "businessKey", "caseInstanceId", createVariables().putValue("VAR_NAME", "var value"))

    then()
      .process_instance_exists(processDefinitionId = whenever().processDefinition.id) { instance, stage ->
        assertThat(instance.businessKey).isEqualTo("businessKey")
        assertThat(instance.caseInstanceId).isEqualTo("caseInstanceId")
        assertThat(stage.localService.getVariables(instance.id)).isNotEmpty
        assertThat(stage.localService.getVariable(instance.id, "VAR_NAME")).isEqualTo("var value")
      }
  }

  @Test
  fun `should start process by id with variables`() {
    val processDefinitionKey = processDefinitionKey()

    given()
      .process_with_user_task_is_deployed(processDefinitionKey)

    whenever()
      .remoteService
      .startProcessInstanceById(whenever().processDefinition.id, createVariables().putValue("VAR_NAME", "var value"))

    then()
      .process_instance_exists(processDefinitionId = whenever().processDefinition.id) { instance, stage ->
        assertThat(instance.businessKey).isNull()
        assertThat(stage.localService.getVariables(instance.id)).isNotEmpty
        assertThat(stage.localService.getVariable(instance.id, "VAR_NAME")).isEqualTo("var value")
      }
  }
}
