package org.camunda.community.rest.itest

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.variable.Variables
import org.camunda.community.rest.client.api.TaskApiClient
import org.camunda.community.rest.itest.SpringBootConfigurationITest.CustomValueMapperConfiguration.Companion.VALUE_MAPPER
import org.camunda.community.rest.itest.stages.TestApplication
import org.camunda.community.rest.variables.SpinValueMapper
import org.camunda.community.rest.variables.ValueMapper
import org.camunda.community.rest.variables.ValueTypeResolverImpl
import org.camunda.spin.plugin.variable.value.SpinValue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.NoSuchBeanDefinitionException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.FilteredClassLoader
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.runner.WebApplicationContextRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.test.context.ActiveProfiles


internal class SpringBootConfigurationITest {

  @Configuration
  class CustomValueMapperConfiguration {
    companion object {
      val VALUE_MAPPER: ValueMapper =
        ValueMapper(objectMapper = jacksonObjectMapper(), valueTypeResolver = ValueTypeResolverImpl(),
          valueMappers = emptyList(),
          serializationFormat = Variables.SerializationDataFormats.JSON)
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
    lateinit var spinValueMapper: SpinValueMapper

    @Autowired(required = false)
    lateinit var taskApiClient: TaskApiClient

    @Test
    fun `components are initialized`() {
      assertThat(this::runtimeService.isInitialized).isTrue()
      assertThat(this::taskApiClient.isInitialized).isTrue()
      assertThat(this::valueMapper.isInitialized).isTrue()
      assertThat(this::spinValueMapper.isInitialized).isTrue()
      assertThat(this.valueMapper).isNotEqualTo(VALUE_MAPPER)
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

  @Nested
  inner class ExtensionEnabledByDefaultNoSpinTest {

    @Test
    fun `components are initialized without spin`() {
      WebApplicationContextRunner()
        .withInitializer { it.environment.setActiveProfiles("itest") }
        .withPropertyValues("camunda.bpm.generic-properties.properties.historyTimeToLive=P1D")
        .withUserConfiguration(TestApplication::class.java)
        .withClassLoader(FilteredClassLoader(SpinValue::class.java))
        .run {
          assertThatThrownBy { it.getBean(SpinValueMapper::class.java) }.isInstanceOf(NoSuchBeanDefinitionException::class.java)
          assertThat(it.getBean(ValueMapper::class.java)).isNotNull
        }

    }
  }

}
