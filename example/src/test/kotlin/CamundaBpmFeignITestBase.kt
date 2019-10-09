package org.camunda.bpm.extension.feign.itest

import com.tngtech.jgiven.Stage
import com.tngtech.jgiven.base.ScenarioTestBase
import com.tngtech.jgiven.integration.spring.EnableJGiven
import com.tngtech.jgiven.integration.spring.SpringScenarioTest
import feign.Logger
import org.camunda.bpm.extension.feign.EnableCamundaFeign
import org.junit.runner.RunWith
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Bean
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
abstract class CamundaBpmFeignITestBase<SERVICE : Any, ACTION : ActionStage<ACTION, SERVICE>, ASSERT : AssertStage<ASSERT, SERVICE>> : SpringScenarioTest<ACTION, ACTION, ASSERT>() {
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
@EnableCamundaFeign
@SpringBootApplication
class TestApplication
