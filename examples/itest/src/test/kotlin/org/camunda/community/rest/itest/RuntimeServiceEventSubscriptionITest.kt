/*-
 * #%L
 * camunda-platform-7-rest-client-spring-boot-itest
 * %%
 * Copyright (C) 2022 Camunda Services GmbH
 * %%
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 *  under one or more contributor license agreements. See the NOTICE file
 *  distributed with this work for additional information regarding copyright
 *  ownership. Camunda licenses this file to you under the Apache License,
 *  Version 2.0; you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * #L%
 */
package org.camunda.community.rest.itest

import com.tngtech.jgiven.annotation.As
import io.toolisticon.testing.jgiven.AND
import io.toolisticon.testing.jgiven.GIVEN
import io.toolisticon.testing.jgiven.THEN
import io.toolisticon.testing.jgiven.WHEN
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.iterable.ThrowingExtractor
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.runtime.EventSubscription
import org.camunda.community.rest.itest.stages.CamundaRestClientITestBase
import org.camunda.community.rest.itest.stages.RuntimeServiceActionStage
import org.camunda.community.rest.itest.stages.RuntimeServiceAssertStage
import org.junit.jupiter.api.Test

@As("Event Subscription")
class RuntimeServiceEventSubscriptionITest :
  CamundaRestClientITestBase<RuntimeService, RuntimeServiceActionStage, RuntimeServiceAssertStage>() {

  @Test
  fun `should find event subscriptions for process instances`() {
    val processDefinitionKey1 = processDefinitionKey()
    val processDefinitionKey2 = processDefinitionKey()
    val processDefinitionKey3 = processDefinitionKey()
    val messageName1 = "myMessage1"
    val messageName2 = "myMessage2"
    val signalName = "mySignal"
    val userTaskId = "userTask"

    GIVEN
      .no_deployment_exists()

    WHEN
      .process_with_intermediate_message_catch_event_is_deployed(processDefinitionKey1, userTaskId, messageName1)
      .AND
      .process_with_intermediate_message_catch_event_is_deployed(processDefinitionKey2, userTaskId, messageName2)
      .AND
      .process_with_intermediate_signal_catch_event_is_deployed(processDefinitionKey3, userTaskId, signalName)
      .AND
      .process_is_started_by_key(processDefinitionKey1)
      .AND
      .process_is_started_by_key(processDefinitionKey1)
      .AND
      .process_is_started_by_key(processDefinitionKey3)
      .AND
      .process_is_started_by_key(processDefinitionKey2)

    THEN
      .event_subscription_query_succeeds { query, _ ->
        assertThat(query.processInstanceId(GIVEN.processInstance.id).count()).isEqualTo(1)
        assertThat(query.processInstanceId(GIVEN.processInstance.id).singleResult().eventType).isEqualTo("message")
      }
      .AND
      .event_subscription_query_succeeds { query, _ ->
        assertThat(query.eventType("message").count()).isEqualTo(3)
        assertThat(query.eventType("message").orderByCreated().desc().list())
          .hasSize(3)
          .isSortedAccordingTo { o1, o2 -> o2.created.compareTo(o1.created) }
        assertThat(query.eventType("signal").count()).isEqualTo(1)
      }
      .AND
      .event_subscription_query_succeeds { query, _ ->
        assertThat(query.eventName(messageName1).count()).isEqualTo(2)
        assertThat(query.eventName(signalName).count()).isEqualTo(1)
      }
  }

  @Test
  fun `should find event subscriptions for process definition`() {
    val processDefinitionKey1 = processDefinitionKey()
    val processDefinitionKey2 = processDefinitionKey()
    val messageName1 = "myMessage1"
    val messageName2 = "myMessage2"
    val userTaskId = "userTask"

    GIVEN
      .no_deployment_exists()

    WHEN
      .process_with_start_by_message_event_is_deployed(processDefinitionKey1, userTaskId, messageName1)
      .AND
      .process_with_start_by_message_event_is_deployed(processDefinitionKey2, userTaskId, messageName2)

    THEN
      .event_subscription_query_succeeds { query, _ ->
        assertThat(query.eventType("message").count()).isEqualTo(2)
        assertThat(query.eventType("message").list()).map(ThrowingExtractor { es: EventSubscription -> es.processInstanceId }).containsOnlyNulls()
        assertThat(query.eventType("signal").count()).isEqualTo(0)
      }
      .AND
      .event_subscription_query_succeeds { query, _ ->
        assertThat(query.eventName(messageName1).count()).isEqualTo(1)
      }

  }

}
