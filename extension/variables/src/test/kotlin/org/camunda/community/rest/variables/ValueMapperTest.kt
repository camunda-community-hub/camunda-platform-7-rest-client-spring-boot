package org.camunda.community.rest.variables

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.camunda.bpm.engine.variable.Variables
import org.camunda.community.rest.client.model.VariableValueDto
import org.camunda.community.rest.variables.serialization.JavaSerializationValueSerializer
import org.camunda.community.rest.variables.serialization.JsonValueSerializer
import org.camunda.community.rest.variables.serialization.SpinValueSerializer
import org.camunda.community.rest.variables.serialization.ValueSerializer
import org.junit.jupiter.api.Test
import org.mockito.internal.util.collections.Sets
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.*

class ValueMapperTest {
  private val objectMapper = jacksonObjectMapper().apply { findAndRegisterModules() }
  private val valueTypeResolver = ValueTypeResolverImpl()
  private val valueTypeRegistration = ValueTypeRegistration()

  private val valueMapper = ValueMapper(
    objectMapper = objectMapper,
    valueTypeResolver = valueTypeResolver,
    valueTypeRegistration = valueTypeRegistration,
    serializationFormat = Variables.SerializationDataFormats.JSON,
    valueSerializers = listOf(
      JavaSerializationValueSerializer(),
      JsonValueSerializer(objectMapper)
    ),
    customValueSerializers = listOf(
      SpinValueSerializer(valueTypeResolver, valueTypeRegistration),
    )
  )

  @Test
  fun `can map null values`() {

    val mapWithNulls = mapOf<String, Any?>(
      "key" to "value", "keyForNull" to null
    )

    val dtos = valueMapper.mapValues(mapWithNulls)
    assertThat(dtos).containsOnlyKeys(mapWithNulls.keys)
    assertThat(dtos["key"]).isEqualTo(VariableValueDto().apply {
      type = "String"
      value = "value"
    })
    assertThat(dtos["keyForNull"]).isEqualTo(
      VariableValueDto().apply {
        type = "Null"
        value = null
      })
  }

  @Test
  fun `can map primitive types`() {

    val now = Instant.now()
    val map = mapOf<String, Any?>(
      "keyString" to "string",
      "keyInt" to 17,
      "keyLong" to 18L,
      "keyDouble" to 19.0,
      "keyBoolean" to true,
      "keyDate" to Date.from(now)
    )

    val dtos = valueMapper.mapValues(map)
    assertThat(dtos).containsOnlyKeys(map.keys)
    assertThat(dtos["keyString"]).isEqualTo(VariableValueDto().apply { type = "String"; value = "string" })
    assertThat(dtos["keyInt"]).isEqualTo(VariableValueDto().apply { type = "Integer"; value = 17 })
    assertThat(dtos["keyLong"]).isEqualTo(VariableValueDto().apply { type = "Long"; value = 18L })
    assertThat(dtos["keyDouble"]).isEqualTo(VariableValueDto().apply { type = "Double"; value = 19.0 })
    assertThat(dtos["keyBoolean"]).isEqualTo(VariableValueDto().apply { type = "Boolean"; value = true })
    assertThat(dtos["keyDate"]).isEqualTo(VariableValueDto().apply { type = "Date"; value = Date.from(now) })
  }

  @Test
  fun `can map complex types supported by jackson`() {
    val now = Instant.now()
    val map = mapOf<String, Any?>(
      "keyLocalDateTime" to LocalDateTime.ofInstant(now, ZoneOffset.UTC),
      "keyZonedDateTime" to ZonedDateTime.ofInstant(now, ZoneOffset.UTC)
    )
    val dtos = valueMapper.mapValues(map)
    assertThat(dtos).containsOnlyKeys(map.keys)

    assertThat(dtos["keyLocalDateTime"]).isEqualTo(VariableValueDto().apply {
      type = "Object"
      value = objectMapper.writeValueAsString(LocalDateTime.ofInstant(now, ZoneOffset.UTC))
      valueInfo = mapOf(
        "objectTypeName" to "java.time.LocalDateTime",
        "serializationDataFormat" to "application/json"
      )
    })
    assertThat(dtos["keyZonedDateTime"]).isEqualTo(VariableValueDto().apply {
      type = "Object"
      value = objectMapper.writeValueAsString(ZonedDateTime.ofInstant(now, ZoneOffset.UTC))
      valueInfo = mapOf(
        "objectTypeName" to "java.time.ZonedDateTime",
        "serializationDataFormat" to "application/json"
      )
    })
  }

  @Test
  fun `can map collections`() {
    val map = mapOf<String, Any?>(
      "keyArray" to arrayOf("a", "b", "c"),
      "keyArray2" to listOf("a", "b", "c").toTypedArray(),
      "keySet" to setOf("a", "b", "c"),
      "keySet2" to Sets.newSet("a", "b", "c"),
      "keyList" to listOf("a", "b", "c"),
      "keyMapString" to mapOf("a" to "bar", "b" to 7, "c" to true),
      "keyMap" to mapOf(1 to "bar", true to 7, "c" to null),
    )
    val dtos = valueMapper.mapValues(map)
    assertThat(dtos).containsOnlyKeys(map.keys)

    assertThat(dtos["keyArray"]).isEqualTo(VariableValueDto().apply {
      type = "Object"
      value = objectMapper.writeValueAsString(arrayOf("a", "b", "c"))
      valueInfo = mapOf(
        "objectTypeName" to "[Ljava.lang.String;",
        "serializationDataFormat" to "application/json"
      )
    })
    assertThat(dtos["keyArray2"]).isEqualTo(VariableValueDto().apply {
      type = "Object"
      value = objectMapper.writeValueAsString(arrayOf("a", "b", "c"))
      valueInfo = mapOf(
        "objectTypeName" to "[Ljava.lang.String;",
        "serializationDataFormat" to "application/json"
      )
    })
    assertThat(dtos["keySet"]).isEqualTo(VariableValueDto().apply {
      type = "Object"
      value = objectMapper.writeValueAsString(setOf("a", "b", "c"))
      valueInfo = mapOf(
        "objectTypeName" to "java.util.LinkedHashSet<java.lang.String>",
        "serializationDataFormat" to "application/json"
      )
    })

    assertThat(dtos["keySet2"]).isEqualTo(VariableValueDto().apply {
      type = "Object"
      value = objectMapper.writeValueAsString(setOf("a", "b", "c"))
      valueInfo = mapOf(
        "objectTypeName" to "java.util.LinkedHashSet<java.lang.String>",
        "serializationDataFormat" to "application/json"
      )
    })

    assertThat(dtos["keyList"]).isEqualTo(VariableValueDto().apply {
      type = "Object"
      value = objectMapper.writeValueAsString(listOf("a", "b", "c"))
      valueInfo = mapOf(
        "objectTypeName" to "java.util.Arrays\$ArrayList<java.lang.String>",
        "serializationDataFormat" to "application/json"
      )
    })

    assertThat(dtos["keyMapString"]).isEqualTo(VariableValueDto().apply {
      type = "Object"
      value = objectMapper.writeValueAsString(mapOf<String, Any>("a" to "bar", "b" to 7, "c" to true))
      valueInfo = mapOf(
        "objectTypeName" to "java.util.LinkedHashMap<java.lang.Object,java.lang.Object>",
        "serializationDataFormat" to "application/json"
      )
    })

    assertThat(dtos["keyMap"]).isEqualTo(VariableValueDto().apply {
      type = "Object"
      value = objectMapper.writeValueAsString(mapOf<Any, Any?>(1 to "bar", true to 7, "c" to null))
      valueInfo = mapOf(
        "objectTypeName" to "java.util.LinkedHashMap<java.lang.Object,java.lang.Object>",
        "serializationDataFormat" to "application/json"
      )
    })

  }

}
