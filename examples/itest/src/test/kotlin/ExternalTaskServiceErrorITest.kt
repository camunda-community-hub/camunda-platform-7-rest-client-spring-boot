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
import io.toolisticon.testing.jgiven.WHEN
import org.camunda.bpm.engine.ExternalTaskService
import org.camunda.bpm.extension.rest.itest.stages.*
import org.junit.Test
import org.springframework.test.context.TestPropertySource

@RuntimeServiceCategory
@As("External Task")
@TestPropertySource(
  properties = [
    "camunda.rest.client.error-decoding.http-codes=404"
  ]
)
class ExternalTaskServiceErrorITest :
  CamundaRestClientITestBase<ExternalTaskService, ExternalTaskServiceActionStage, ExternalTaskServiceAssertStage>() {

  @Test
  fun `should fail completing non-existing external task`() {
    EXPECT.process_engine_exception_is_thrown_caused_by(
      reason = "REST-CLIENT-001 Error during remote Camunda engine invocation of ExternalTaskApiClient#completeExternalTaskResource(String,CompleteExternalTaskDto): "
    ) {
      WHEN
        .remoteService.complete("not-existing", "worker-id")
    }
  }
}
