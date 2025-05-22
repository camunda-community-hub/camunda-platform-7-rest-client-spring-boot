package org.camunda.community.rest.itest

import org.assertj.core.api.Assertions.assertThat
import org.camunda.bpm.engine.RuntimeService
import org.camunda.community.rest.client.api.TaskApiClient
import org.camunda.community.rest.itest.SpringBootConfigurationITest.CustomValueMapperConfiguration.Companion.VALUE_MAPPER
import org.camunda.community.rest.itest.stages.TestApplication
import org.camunda.community.rest.variables.ValueMapper
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.test.context.ActiveProfiles

internal class SpringBootConfigurationITest {

  @Configuration
  class CustomValueMapperConfiguration {
    companion object {
      val VALUE_MAPPER: ValueMapper = ValueMapper()
    }

    @Bean
    fun myValueMapper(): ValueMapper {
      return VALUE_MAPPER
    }
  }


  @Nested
  @SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = [TestApplication::class],
  )
  @ActiveProfiles("itest")
  inner class ExtensionEnabledByDefaultTest {

    @Autowired(required = false)
    @Qualifier("remote")
    lateinit var runtimeService: RuntimeService

    @Autowired(required = false)
    lateinit var valueMapper: ValueMapper

    @Autowired(required = false)
    lateinit var taskApiClient: TaskApiClient

    @Test
    fun `components are initialized`() {
      assertThat(this::runtimeService.isInitialized).isTrue()
      assertThat(this::valueMapper.isInitialized).isTrue()
      assertThat(this::taskApiClient.isInitialized).isTrue()
    }
  }

  @Nested
  @SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = [CustomValueMapperConfiguration::class, TestApplication::class],
  )
  @ActiveProfiles("itest")
  inner class ExtensionEnabledWithOverriddenValueMapperTest {

    @Autowired(required = false)
    @Qualifier("remote")
    lateinit var runtimeService: RuntimeService

    @Autowired(required = false)
    lateinit var valueMapper: ValueMapper

    @Autowired(required = false)
    lateinit var taskApiClient: TaskApiClient

    @Test
    fun `components are initialized using overridden valueMapper`() {
      assertThat(this::runtimeService.isInitialized).isTrue()
      assertThat(this::valueMapper.isInitialized).isTrue()
      assertThat(this::taskApiClient.isInitialized).isTrue()
      assertThat(this.valueMapper).isEqualTo(VALUE_MAPPER)
    }
  }


  @Nested
  @SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = [TestApplication::class],
    properties = ["camunda.rest.client.enabled=false"]
  )
  @ActiveProfiles("itest")
  inner class ExtensionDisabledPerPropertyTest {

    @Autowired(required = false)
    @Qualifier("remote")
    lateinit var runtimeService: RuntimeService

    @Autowired(required = false)
    lateinit var valueMapper: ValueMapper

    @Autowired(required = false)
    lateinit var taskApiClient: TaskApiClient

    @Test
    fun `no components are initialized`() {
      assertThat(this::runtimeService.isInitialized).isFalse()
      assertThat(this::valueMapper.isInitialized).isFalse()
      assertThat(this::taskApiClient.isInitialized).isFalse()
    }

  }
}
