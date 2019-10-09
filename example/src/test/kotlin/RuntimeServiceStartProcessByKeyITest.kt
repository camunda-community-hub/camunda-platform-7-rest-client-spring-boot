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
@As("Start Process By Key")
class RuntimeServiceStartProcessByKeyITest : CamundaBpmFeignITestBase<RuntimeService, RuntimeServiceActionStage, RuntimeServiceAssertStage>() {

  @Test
  fun `should start process by key`() {
    val processDefinitionKey = processDefinitionKey()
    given()
      .process_with_user_task_is_deployed(processDefinitionKey)
    whenever()
      .remoteService
      .startProcessInstanceByKey(processDefinitionKey)

    then()
      .process_instance_exists(processDefinitionKey = processDefinitionKey) { instance, stage ->
        assertThat(instance.businessKey).isNull()
        assertThat(stage.localService.getVariables(instance.id)).isEmpty()
      }
  }

  @Test
  fun `should start process by key with business key`() {
    val processDefinitionKey = processDefinitionKey()
    given()
      .process_with_user_task_is_deployed(processDefinitionKey)

    whenever()
      .remoteService
      .startProcessInstanceByKey(processDefinitionKey, "businessKey")

    then()
      .process_instance_exists(processDefinitionKey = processDefinitionKey) { instance, stage ->
        assertThat(instance.businessKey).isEqualTo("businessKey")
        assertThat(stage.localService.getVariables(instance.id)).isEmpty()
      }
  }

  @Test
  fun `should start process by key with business key and variables`() {
    val processDefinitionKey = processDefinitionKey()
    given()
      .process_with_user_task_is_deployed(processDefinitionKey)

    whenever()
      .remoteService
      .startProcessInstanceByKey(processDefinitionKey, "businessKey", createVariables().putValue("VAR_NAME", "var value"))

    then()
      .process_instance_exists(processDefinitionKey = processDefinitionKey) { instance, stage ->
        assertThat(instance.businessKey).isEqualTo("businessKey")
        assertThat(stage.localService.getVariables(instance.id)).isNotEmpty
        assertThat(stage.localService.getVariable(instance.id, "VAR_NAME")).isEqualTo("var value")
      }
  }

  @Test
  fun `should start process by key with business key and case instance id`() {
    val processDefinitionKey = processDefinitionKey()
    given()
      .process_with_user_task_is_deployed(processDefinitionKey)

    whenever()
      .remoteService
      .startProcessInstanceByKey(processDefinitionKey, "businessKey", "caseInstanceId")

    then()
      .process_instance_exists(processDefinitionKey = processDefinitionKey) { instance, stage ->
        assertThat(instance.businessKey).isEqualTo("businessKey")
        assertThat(instance.caseInstanceId).isEqualTo("caseInstanceId")
        assertThat(stage.localService.getVariables(instance.id)).isEmpty()
      }
  }


  @Test
  fun `should start process by key with business key, case instance id and variables`() {
    val processDefinitionKey = processDefinitionKey()
    given()
      .process_with_user_task_is_deployed(processDefinitionKey)

    whenever()
      .remoteService
      .startProcessInstanceByKey(processDefinitionKey, "businessKey", "caseInstanceId", createVariables().putValue("VAR_NAME", "var value"))

    then()
      .process_instance_exists(processDefinitionKey = processDefinitionKey) { instance, stage ->
        assertThat(instance.businessKey).isEqualTo("businessKey")
        assertThat(instance.caseInstanceId).isEqualTo("caseInstanceId")
        assertThat(stage.localService.getVariables(instance.id)).isNotEmpty
        assertThat(stage.localService.getVariable(instance.id, "VAR_NAME")).isEqualTo("var value")
      }
  }

  @Test
  fun `should start process by key with variables`() {
    val processDefinitionKey = processDefinitionKey()
    given()
      .process_with_user_task_is_deployed(processDefinitionKey)

    whenever()
      .remoteService
      .startProcessInstanceByKey(processDefinitionKey, createVariables().putValue("VAR_NAME", "var value"))

    then()
      .process_instance_exists(processDefinitionKey = processDefinitionKey) { instance, stage ->
        assertThat(instance.businessKey).isNull()
        assertThat(stage.localService.getVariables(instance.id)).isNotEmpty
        assertThat(stage.localService.getVariable(instance.id, "VAR_NAME")).isEqualTo("var value")
      }
  }
}
