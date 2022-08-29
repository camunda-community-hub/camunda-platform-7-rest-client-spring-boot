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
package org.camunda.community.rest.itest.stages

import com.fasterxml.jackson.databind.ObjectMapper
import com.tngtech.jgiven.Stage
import com.tngtech.jgiven.integration.spring.EnableJGiven
import com.tngtech.jgiven.integration.spring.SpringScenarioTest
import io.toolisticon.testing.jgiven.THEN
import org.assertj.core.api.Assertions.assertThat
import org.camunda.community.rest.EnableCamundaRestClient
import org.camunda.community.rest.exception.RemoteProcessEngineException
import org.junit.jupiter.api.fail
import org.junit.runner.RunWith
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Bean
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import java.util.*

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, classes = [TestApplication::class])
@ActiveProfiles("itest")
@DirtiesContext
abstract class CamundaRestClientITestBase<SERVICE : Any, ACTION : ActionStage<ACTION, SERVICE>, ASSERT : AssertStage<ASSERT, SERVICE>> :
  SpringScenarioTest<ACTION, ACTION, ASSERT>() {
  /**
   * Generates a new process definition key.
   */
  internal fun processDefinitionKey() = "KEY" + UUID.randomUUID().toString().replace("-", "")

  internal fun taskDefinitionKey() = "task_" + UUID.randomUUID().toString().replace("-", "")
}

/**
 * Another name for the assertion stage.
 */
val <G : Stage<G>, W : Stage<W>, T : Stage<T>> com.tngtech.jgiven.base.ScenarioTestBase<G, W, T>.EXPECT: T get() = THEN

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

  fun process_engine_exception_is_thrown_caused_by(clazz: Class<out Throwable>? = null, reason: String? = null, callable: () -> Unit) {
    try {
      callable.invoke()
      fail { "Expecting exception caused by $clazz" }
    } catch (e: Exception) {
      assertThat(e).isInstanceOf(RemoteProcessEngineException::class.java)
      if (clazz != null) {
        assertThat((e as RemoteProcessEngineException).cause).isInstanceOf(clazz)
        if (reason != null) {
          assertThat(e.cause?.message).isEqualTo(reason)
        }
      } else {
        if (reason != null) {
          assertThat((e as RemoteProcessEngineException).message).isEqualTo(reason)
        }
      }
    }
  }

  fun exception_is_thrown(clazz: Class<out Throwable>? = null, reason: String? = null, callable: () -> Unit) {
    try {
      callable.invoke()
      fail { "Expecting exception caused by $clazz" }
    } catch (e: Exception) {
      assertThat(e).isInstanceOf(clazz)
      if (reason != null) {
        assertThat(e.message).isEqualTo(reason)
      }
    }
  }
}


@EnableJGiven
@EnableCamundaRestClient
@SpringBootApplication
class TestApplication {

  @Bean
  fun objectMapper(): ObjectMapper {
    return JacksonDataFormatConfigurator.configureObjectMapper(ObjectMapper())
  }

}
