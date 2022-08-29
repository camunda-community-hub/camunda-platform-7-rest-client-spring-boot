/*-
 * #%L
 * camunda-platform-7-rest-client-spring-boot-itest
 * %%
 * Copyright (C) 2019 Camunda Services GmbH
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
import org.camunda.bpm.engine.MismatchingMessageCorrelationException
import org.camunda.bpm.engine.RuntimeService
import org.camunda.community.rest.itest.stages.CamundaRestClientITestBase
import org.camunda.community.rest.itest.stages.RuntimeServiceActionStage
import org.camunda.community.rest.itest.stages.RuntimeServiceAssertStage
import org.junit.Test
import org.springframework.test.context.TestPropertySource

@As("Unwrapped Exceptions")
@TestPropertySource(
  properties = ["camunda.rest.client.error-decoding.wrap-exceptions=false"]
)
class UnwrappedExceptionsITest :
  CamundaRestClientITestBase<RuntimeService, RuntimeServiceActionStage, RuntimeServiceAssertStage>() {

  @Test
  fun `should fail to correlate message with waiting instance`() {
    val processDefinitionKey = processDefinitionKey()
    val messageName = "myEventMessage"
    val userTaskId = "user-task"
    GIVEN
      .process_with_intermediate_message_catch_event_is_deployed(processDefinitionKey, userTaskId, messageName)
      .AND
      .process_is_started_by_key(processDefinitionKey)
      .AND

    THEN
      .exception_is_thrown(
        clazz = MismatchingMessageCorrelationException::class.java,
        reason = "Cannot correlate message 'wrong-message': No process definition or execution matches the parameters"
      ) {
        WHEN
          .remoteService
          .correlateMessage("wrong-message")
      }
  }

}
