package org.camunda.community.rest.variables.serialization

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.type.TypeFactory
import org.camunda.bpm.engine.variable.Variables
import org.camunda.bpm.engine.variable.Variables.SerializationDataFormats
import org.camunda.bpm.engine.variable.impl.value.ObjectValueImpl
import org.camunda.bpm.engine.variable.value.SerializableValue
import org.camunda.bpm.engine.variable.value.TypedValue
import org.camunda.community.rest.variables.ext.constructType

/**
 * Serializer for serializing and deserializing values in JSON format.
 * This serializer uses Jackson to serialize and deserialize objects to/from JSON format.
 */
class JsonValueSerializer(
  private val objectMapper: ObjectMapper,
) : ValueSerializer {

  override fun serializeValue(value: TypedValue): SerializableValue {
    require(value is ObjectValueImpl) { "Variable value must be a ObjectValueImpl to be serialized, but was: ${value.type}" }
    if (value.valueSerialized == null) {
      try {
        val serializedValue = objectMapper.writeValueAsString(value.value)
        return Variables.serializedObjectValue(serializedValue)
          .serializationDataFormat(serializationDataFormat)
          .objectTypeName(value.value.constructType().toCanonical())
          .create()
      } catch (e: JsonProcessingException) {
        throw IllegalArgumentException("Object value could not be serialized into '${serializationDataFormat}'", e)
      }
    }
    return value
  }

  override fun deserializeValue(value: SerializableValue): TypedValue = if (!value.isDeserialized) {
    require(value is ObjectValueImpl) { "Variable value must be ObjectValueImpl to be serialized, but was: ${value.type}" }
    val deserializedValue: Any = try {
      val clazz = TypeFactory.defaultInstance().constructFromCanonical(value.objectTypeName)
      objectMapper.readValue(value.valueSerialized, clazz) as Any
    } catch (e: Exception) {
      throw IllegalStateException("Error deserializing value $value", e)
    }
    ObjectValueImpl(deserializedValue, value.valueSerialized, value.serializationDataFormat, value.objectTypeName, true)
  } else {
    value
  }

  override val serializationDataFormat: SerializationDataFormats = SerializationDataFormats.JSON

}
