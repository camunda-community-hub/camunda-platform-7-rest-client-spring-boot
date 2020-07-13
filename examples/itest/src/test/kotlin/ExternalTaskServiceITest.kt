/*-
 * #%L
 * camunda-rest-client-spring-boot-itest
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
package org.camunda.bpm.extension.rest.itest

import com.tngtech.jgiven.annotation.As
import org.camunda.bpm.engine.ExternalTaskService
import org.camunda.bpm.engine.variable.Variables.*
import org.junit.Test

@RuntimeServiceCategory
@As("External Task")
class ExternalTaskServiceITest : CamundaRestClientITestBase<ExternalTaskService, ExternalTaskServiceActionStage, ExternalTaskServiceAssertStage>() {

  @Test
  fun `should complete external task`() {

    given()
      .process_from_a_resource_is_deployed("test_external_task.bpmn")
      .and()
      .process_is_started_by_key("test_external_task", "my-business-key2", "caseInstanceId2",
        createVariables()
          .putValue("VAR1", "VAL1")
          .putValueTyped("VAR4", objectValue("My object value").create())
      )
      .and()
      .process_waits_in_external_task("topic")


    whenever()
      .remoteService.complete(given().externalTaskId, "worker-id",
      createVariables()
        .putValue("VAR1", "NEW-VAL1")
        .putValue("VAR2", "VAL2")
        .putValueTyped("VAR3", stringValue("VAL3")),
      createVariables()
        .putValue("LOCAL", "LOCAL-VAL")
    )

    then()
      .execution_is_waiting_for_signal()

  }
}
