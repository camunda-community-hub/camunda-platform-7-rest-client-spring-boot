/*-
 * #%L
 * camunda-rest-client-spring-boot-example
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

import com.tngtech.jgiven.Stage
import com.tngtech.jgiven.base.ScenarioTestBase
import com.tngtech.jgiven.integration.spring.EnableJGiven
import com.tngtech.jgiven.integration.spring.SpringScenarioTest
import org.camunda.bpm.extension.rest.EnableCamundaRestClient
import org.junit.runner.RunWith
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import java.util.*

/**
 * Alias for the when
 */
fun <G, W, T> ScenarioTestBase<G, W, T>.whenever() = `when`()

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, classes = [TestApplication::class])
@ActiveProfiles("itest")
abstract class CamundaRestClientITestBase<SERVICE : Any, ACTION : ActionStage<ACTION, SERVICE>, ASSERT : AssertStage<ASSERT, SERVICE>> : SpringScenarioTest<ACTION, ACTION, ASSERT>() {
  internal fun processDefinitionKey() = "KEY" + UUID.randomUUID().toString().replace("-", "")
}

/**
 * Base action stage.
 */
abstract class ActionStage<SELF : ActionStage<SELF, SERVICE>, SERVICE : Any> : Stage<SELF>() {

  open lateinit var remoteService: SERVICE

  open lateinit var localService: SERVICE
}


/**
 * Base assert stage.
 */
abstract class AssertStage<SELF : AssertStage<SELF, SERVICE>, SERVICE : Any> : Stage<SELF>() {

  open lateinit var remoteService: SERVICE

  open lateinit var localService: SERVICE
}


@EnableJGiven
@EnableCamundaRestClient
@SpringBootApplication
class TestApplication
