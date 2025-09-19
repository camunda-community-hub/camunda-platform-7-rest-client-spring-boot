package org.camunda.community.rest.variables

import org.camunda.bpm.engine.variable.type.ValueType
import java.util.Date
import kotlin.reflect.KClass
import kotlin.reflect.full.superclasses

class ValueTypeRegistration {

  private val knownClasses: MutableMap<KClass<*>, ValueType> = mutableMapOf(
    Pair(Boolean::class, ValueType.BOOLEAN),
    Pair(Date::class, ValueType.DATE),
    Pair(Double::class, ValueType.DOUBLE),
    Pair(Integer::class, ValueType.INTEGER),
    Pair(Long::class, ValueType.LONG),
    Pair(Short::class, ValueType.SHORT),
    Pair(String::class, ValueType.STRING),
    Pair(ByteArray::class, ValueType.BYTES),
    Pair(Number::class, ValueType.NUMBER),
    Pair(Object::class, ValueType.OBJECT),
  )

  fun registerTypeForClass(type: ValueType, clazz: KClass<*>) {
    knownClasses[clazz] = type
  }

  /**
   * Tries to guess the type from the passed value.
   */
  @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
  fun getRegisteredValueType(value: Any?): ValueType =
    if (value == null) ValueType.NULL else knownClasses[value::class]
      ?: value::class.superclasses.map { knownClasses[it] }.firstOrNull() ?: ValueType.OBJECT

}
