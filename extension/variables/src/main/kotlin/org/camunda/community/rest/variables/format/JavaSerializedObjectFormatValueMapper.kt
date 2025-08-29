package org.camunda.community.rest.variables.format

import org.camunda.bpm.engine.variable.Variables
import org.camunda.bpm.engine.variable.Variables.SerializationDataFormats
import org.camunda.bpm.engine.variable.type.ValueType
import org.camunda.bpm.engine.variable.value.ObjectValue
import org.camunda.bpm.engine.variable.value.SerializableValue
import org.camunda.bpm.engine.variable.value.TypedValue
import org.camunda.community.rest.variables.ext.format
import org.camunda.community.rest.variables.ext.hasSerializationDataFormat
import org.camunda.community.rest.variables.ext.resolveValueType
import java.io.*
import kotlin.io.encoding.Base64

/**
 * Custom value mapper for Java serialized objects.
 */
open class JavaSerializedObjectFormatValueMapper : FormatValueMapper {

  companion object {
    fun <T : Serializable> encodeBase64(value: T): String = with(ByteArrayOutputStream()) {
      ObjectOutputStream(this).use { it.writeObject(value) }
      return Base64.encode(toByteArray())
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Serializable> decodeBase64(base64: String): T = with(Base64.decode(base64)) {
      ObjectInputStream(ByteArrayInputStream(this)).use { it.readObject() as T }
    }
  }

  override fun canMapValue(value: Any?) = ValueType.OBJECT == resolveValueType(value)

  override fun canSerializeValue(value: TypedValue) = value is ObjectValue
    && value.hasSerializationDataFormat(serializationDataFormat)
    && value.value is Serializable

  override fun canDeserializeValue(value: SerializableValue) = value is ObjectValue
  && value.hasSerializationDataFormat(serializationDataFormat)
  && value.valueSerialized != null

  override fun mapValue(value: Any?): TypedValue = Variables
    .objectValue(requireNotNull(value) { "Value can not be null, filtered in canMapValue()" })
    .serializationDataFormat(serializationDataFormat)
    .create()

  override fun serializeValue(value: TypedValue): SerializableValue {
    require(value is ObjectValue) { "Expected ObjectValue, got: $value" }
    val obj = requireNotNull(value.value) { "ObjectValue.value must not be null" }
    require(obj is Serializable) { "Value must implement Serializable for JAVA format" }

    return Variables.serializedObjectValue(encodeBase64(obj))
      .serializationDataFormat(serializationDataFormat)
      .objectTypeName(obj.javaClass.name)
      .create()
  }

  override fun deserializeValue(value: SerializableValue): TypedValue {
    require(value is ObjectValue) { "Expected ObjectValue, got: $value" }
    val serialized = value.valueSerialized
    val deserialized: Any = if (serialized != null) {
      decodeBase64(serialized)
    } else {
      value.value
    }
    return Variables.objectValue(deserialized)
      .serializationDataFormat(serializationDataFormat.format)
      .create()
  }

  override val serializationDataFormat: SerializationDataFormats = SerializationDataFormats.JAVA
}
