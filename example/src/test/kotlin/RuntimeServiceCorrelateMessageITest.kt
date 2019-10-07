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
@As("Correlate Message")
class RuntimeServiceCorrelateMessageITest : CamundaBpmFeignITestBase<RuntimeService, RuntimeServiceActionStage, RuntimeServiceAssertStage>() {

  @Test
  fun `should correlate message with definition`() {
    val processDefinitionKey = processDefinitionKey()
    val messageName = "myStartMessage"
    val userTaskId = "user-task-in-process-started-by-message"
    given()
      .process_with_start_by_message_event_is_deployed(processDefinitionKey, userTaskId, messageName)
      .and()

    whenever()
      .remoteService
      .correlateMessage(messageName)

    then()
      .process_instance_exists(processDefinitionKey) { instance, stage ->
        assertThat(instance.businessKey).isNull()
        assertThat(
          stage.localService.createProcessInstanceQuery()
            .processInstanceId(instance.id)
            .activityIdIn(userTaskId)
            .singleResult()
        ).isNotNull
      }
  }

  @Test
  fun `should correlate message with waiting instance`() {
    val processDefinitionKey = processDefinitionKey()
    val messageName = "myEventMessage"
    val userTaskId = "user-task"
    given()
      .process_with_intermediate_message_catch_event_is_deployed(processDefinitionKey, userTaskId, messageName)
      .and()
      .process_is_started_by_key(processDefinitionKey)
      .and()

    whenever()
      .remoteService
      .correlateMessage(messageName)

    then()
      .process_instance_exists(processDefinitionKey) { instance, stage ->
        assertThat(instance.businessKey).isNull()
        assertThat(
          stage.localService.createProcessInstanceQuery()
            .processInstanceId(instance.id)
            .activityIdIn(userTaskId)
            .singleResult()
        ).isNotNull
      }
  }

  @Test
  fun `should correlate message and business key with waiting instance`() {
    val processDefinitionKey = processDefinitionKey()
    val messageName = "myEventMessage"
    val userTaskId = "user-task"
    given()
      .process_with_intermediate_message_catch_event_is_deployed(processDefinitionKey, userTaskId, messageName)
      .and()
      .process_is_started_by_key(processDefinitionKey, "my-business-key1")

    whenever()
      .remoteService
      .correlateMessage(messageName, "my-business-key1")

    then()
      .process_instance_exists(processDefinitionKey) { instance, stage ->
        assertThat(instance.businessKey).isEqualTo("my-business-key1")
        assertThat(
          stage.localService.createProcessInstanceQuery()
            .processInstanceId(instance.id)
            .activityIdIn(userTaskId)
            .singleResult()
        ).isNotNull
      }
  }

  @Test
  fun `should correlate message, business and case instance id key with waiting instance`() {
    val processDefinitionKey = processDefinitionKey()
    val messageName = "myEventMessage2"
    val userTaskId = "user-task"
    given()
      .process_with_intermediate_message_catch_event_is_deployed(processDefinitionKey, userTaskId, messageName)
      .and()
      .process_is_started_by_key(processDefinitionKey, "my-business-key1", "caseInstanceId1", createVariables().putValue("VAR1", "VAL1"))

    whenever()
      .remoteService
      .correlateMessage(messageName, "my-business-key1", createVariables().putValue("VAR2", "VAL2"))

    then()
      .process_instance_exists(processDefinitionKey) { instance, stage ->
        assertThat(instance.businessKey).isEqualTo("my-business-key1")
        assertThat(instance.caseInstanceId).isEqualTo("caseInstanceId1")
        assertThat(
          stage.localService.createProcessInstanceQuery()
            .processInstanceId(instance.id)
            .activityIdIn(userTaskId)
            .singleResult()
        ).isNotNull
        assertThat(
          stage.localService.getVariables(instance.id, listOf("VAR1", "VAR2"))
        ).containsValues("VAL1", "VAL2")

      }
  }

  @Test
  fun `should correlate message, business and case instance id key and variables with waiting instance`() {
    val processDefinitionKey = processDefinitionKey()
    val messageName = "myEventMessage3"
    val userTaskId = "user-task"
    val businessKey = "my-business-key3"
    val caseInstanceId = "caseInstanceId3"
    given()
      .process_with_intermediate_message_catch_event_is_deployed(processDefinitionKey, userTaskId, messageName)
      .and()
      .process_is_started_by_key(processDefinitionKey, businessKey, caseInstanceId, createVariables().putValue("VAR1", "VAL1"))
      .and()
      .process_is_started_by_key(processDefinitionKey, businessKey, caseInstanceId, createVariables().putValue("VAR2", "VAL2"))

    whenever()
      .remoteService
      .correlateMessage(messageName, businessKey, createVariables().putValue("VAR2", "VAL2"), createVariables().putValue("VAR-NEW", "VAL-NEW"))

    then()
      .process_instance_exists(processDefinitionKey, containingSimpleProcessVariables = createVariables().putValue("VAR-NEW", "VAL-NEW")) { instance, stage ->
        assertThat(instance.businessKey).isEqualTo(businessKey)
        assertThat(instance.caseInstanceId).isEqualTo(caseInstanceId)
        assertThat(
          stage.localService.createProcessInstanceQuery()
            .processInstanceId(instance.id)
            .activityIdIn(userTaskId)
            .singleResult()
        ).isNotNull
        assertThat(
          stage.localService.getVariables(instance.id, listOf("VAR2", "VAR-NEW"))
        ).containsValues("VAL2", "VAL-NEW")

      }
  }

  private fun processDefinitionKey() = "KEY" + UUID.randomUUID().toString().replace("-", "")

}
