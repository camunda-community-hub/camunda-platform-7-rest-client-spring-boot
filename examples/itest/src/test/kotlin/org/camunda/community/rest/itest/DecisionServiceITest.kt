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
import io.toolisticon.testing.jgiven.GIVEN
import io.toolisticon.testing.jgiven.THEN
import io.toolisticon.testing.jgiven.WHEN
import org.camunda.bpm.engine.DecisionService
import org.camunda.community.rest.itest.stages.CamundaRestClientITestBase
import org.camunda.community.rest.itest.stages.DecisionServiceActionStage
import org.camunda.community.rest.itest.stages.DecisionServiceAssertStage
import org.junit.Test

@As("Incident")
class DecisionServiceITest : CamundaRestClientITestBase<DecisionService, DecisionServiceActionStage, DecisionServiceAssertStage>() {

  @Test
  fun `should evaluate decision by id`() {
    GIVEN
      .no_deployment_exists()
      .decision_table_is_deployed()
    WHEN
      .decision_is_evaluated_by_id(mutableMapOf(Pair("input1", "Rule1")))
    THEN
      .decision_result_is_correct(mutableMapOf(Pair("output1", "Rule1Output")))
  }

  @Test
  fun `should evaluate decision by key`() {
    GIVEN
      .no_deployment_exists()
      .decision_table_is_deployed()
    WHEN
      .decision_is_evaluated_by_key(variables = mutableMapOf(Pair("input1", "Rule1")))
    THEN
      .decision_result_is_correct(mutableMapOf(Pair("output1", "Rule1Output")))
  }

  @Test
  fun `should evaluate decision table by id`() {
    GIVEN
      .no_deployment_exists()
      .decision_table_is_deployed()
    WHEN
      .decision_table_is_evaluated_by_id(mutableMapOf(Pair("input1", "Rule1")))
    THEN
      .decision_table_result_is_correct(mutableMapOf(Pair("output1", "Rule1Output")))
  }

  @Test
  fun `should evaluate decision table by key`() {
    GIVEN
      .no_deployment_exists()
      .decision_table_is_deployed()
    WHEN
      .decision_table_is_evaluated_by_key(variables = mutableMapOf(Pair("input1", "Rule1")))
    THEN
      .decision_table_result_is_correct(mutableMapOf(Pair("output1", "Rule1Output")))
  }

  @Test
  fun `should evaluate decision table by key with tenant`() {
    GIVEN
      .no_deployment_exists()
      .decision_table_is_deployed(tenantId = "tenantId")
    WHEN
      .decision_table_is_evaluated_by_key_and_tenant(tenantId = "tenantId", variables = mutableMapOf(Pair("input1", "Rule1")))
    THEN
      .decision_table_result_is_correct(mutableMapOf(Pair("output1", "Rule1Output")))
  }

}
