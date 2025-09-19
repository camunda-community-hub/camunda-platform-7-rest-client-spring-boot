package org.camunda.community.rest.variables.serialization

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.camunda.bpm.engine.variable.Variables.SerializationDataFormats
import org.camunda.bpm.engine.variable.type.SerializableValueType
import org.camunda.community.rest.client.model.VariableValueDto
import org.camunda.community.rest.variables.ValueMapper
import org.camunda.community.rest.variables.ValueTypeRegistration
import org.camunda.community.rest.variables.ValueTypeResolverImpl
import org.junit.jupiter.api.Test

class JsonValueSerializerTest {
  private val objectMapper = jacksonObjectMapper().apply { findAndRegisterModules() }
  private val valueTypeResolver = ValueTypeResolverImpl()
  private val valueTypeRegistration = ValueTypeRegistration()
  private val jsonValueSerializer = JsonValueSerializer(objectMapper)

  private val valueMapper = ValueMapper(
    objectMapper = objectMapper,
    valueTypeResolver = valueTypeResolver,
    valueTypeRegistration = valueTypeRegistration,
    serializationFormat = SerializationDataFormats.JSON,
    valueSerializers = listOf(jsonValueSerializer),
    customValueSerializers = listOf()
  )

  data class Foo(val name: String, val age: Int)

  val orig = mapOf<String, Any?>(
    "bar" to 1L,
    "foo" to Foo("Foo", 1),
    "baz" to listOf(1L, 2L, 3L),
  )

  @Test
  fun `can serialize complex map`() {
    val dtos = valueMapper.mapValues(orig)
    assertThat(dtos).hasSize(3)

    val foo: VariableValueDto = dtos["foo"]!!
    val bar: VariableValueDto = dtos["bar"]!!
    val baz: VariableValueDto = dtos["baz"]!!

    assertThat(bar.type).isEqualTo("Long")
    assertThat(foo.type).isEqualTo("Object")
    assertThat(baz.type).isEqualTo("Object")

    assertThat<Any?>(foo.valueInfo[SerializableValueType.VALUE_INFO_SERIALIZATION_DATA_FORMAT])
      .isEqualTo(SerializationDataFormats.JSON.getName())
    assertThat<Any?>(foo.value)
      .isNotNull()
      .isInstanceOf(String::class.java)

  }

  @Test
  fun `can deserialize complex map`() {
    val dtos = valueMapper.mapValues(orig)

    val deserialized = valueMapper.mapDtos(dtos)

    assertThat(deserialized).hasSize(3)
    assertThat(deserialized["foo"]).isInstanceOf(Foo::class.java)
      .extracting("name", "age")
      .containsExactly("Foo", 1)
  }
}
