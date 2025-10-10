package org.camunda.community.rest.variables

import org.camunda.bpm.engine.variable.type.SerializableValueType
import org.camunda.bpm.engine.variable.type.ValueType
import org.camunda.bpm.engine.variable.value.SerializationDataFormat
import org.camunda.bpm.engine.variable.value.TypedValue
import java.util.Date
import kotlin.reflect.KClass
import kotlin.reflect.full.superclasses

class ValueTypeRegistration {

  private val knownClasses: MutableMap<KClass<*>, (value: Any, isTransient: Boolean, serializationDataFormat: SerializationDataFormat) -> TypedValue> = mutableMapOf(
    Pair(Boolean::class, ValueType.BOOLEAN::toTypedValue),
    Pair(Date::class, ValueType.DATE::toTypedValue),
    Pair(Double::class, ValueType.DOUBLE::toTypedValue),
    Pair(Integer::class, ValueType.INTEGER::toTypedValue),
    Pair(Long::class, ValueType.LONG::toTypedValue),
    Pair(Short::class, ValueType.SHORT::toTypedValue),
    Pair(String::class, ValueType.STRING::toTypedValue),
    Pair(ByteArray::class, ValueType.BYTES::toTypedValue),
    Pair(Number::class, ValueType.NUMBER::toTypedValue),
    Pair(Object::class, ValueType.OBJECT::toTypedValue),
  )

  fun registerTypeForClass(clazz: KClass<*>, conversion: (value: Any, isTransient: Boolean, serializationDataFormat: SerializationDataFormat) -> TypedValue) {
    knownClasses[clazz] = conversion
  }

  fun convertToTypedValue(variableValue: Any?, isTransient: Boolean, serializationFormat: SerializationDataFormat): TypedValue {
    if (variableValue is TypedValue) {
      return variableValue
    }
    if (variableValue == null) {
      return ValueType.NULL.createValue(null, mapOf(ValueType.VALUE_INFO_TRANSIENT to isTransient))
    }
    val conversionFunction = knownClasses[variableValue::class]
      ?: variableValue::class.superclasses.map { knownClasses[it] }.firstOrNull() ?: ValueType.OBJECT::toTypedValue
    return conversionFunction(variableValue, isTransient, serializationFormat)
  }

}

fun ValueType.toTypedValue(value: Any, isTransient: Boolean, serializationDataFormat: SerializationDataFormat): TypedValue {
  return if (this is SerializableValueType) {
    this.createValue(value, mapOf(ValueType.VALUE_INFO_TRANSIENT to isTransient,
      SerializableValueType.VALUE_INFO_SERIALIZATION_DATA_FORMAT to serializationDataFormat.name))
  } else {
    this.createValue(value, mapOf(ValueType.VALUE_INFO_TRANSIENT to isTransient))
  }
}

