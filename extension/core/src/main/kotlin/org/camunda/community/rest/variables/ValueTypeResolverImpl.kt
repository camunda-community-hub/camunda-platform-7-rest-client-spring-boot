package org.camunda.community.rest.variables

import org.camunda.bpm.engine.variable.type.ValueType
import org.camunda.bpm.engine.variable.type.ValueTypeResolver
import org.springframework.stereotype.Component
import java.util.*

/**
 * Implementation of the [ValueTypeResolver] that is taken from the Camunda engine.
 */
@Component
class ValueTypeResolverImpl : ValueTypeResolver {

  private val knownTypes: MutableMap<String, ValueType> = setOf(
    ValueType.BOOLEAN,
    ValueType.BYTES,
    ValueType.DATE,
    ValueType.DOUBLE,
    ValueType.INTEGER,
    ValueType.LONG,
    ValueType.NULL,
    ValueType.SHORT,
    ValueType.STRING,
    ValueType.OBJECT,
    ValueType.NUMBER,
    ValueType.FILE,
  ).associateBy { it.name }.toMutableMap()

  override fun addType(type: ValueType) {
    knownTypes[type.name] = type
  }

  override fun typeForName(name: String): ValueType? = knownTypes[name]

  override fun getSubTypes(type: ValueType): List<ValueType> {
    val types: MutableList<ValueType> = mutableListOf()

    val validParents: MutableSet<ValueType> = mutableSetOf(type)

    for (knownType in knownTypes.values) {
      if (validParents.contains(knownType.parent)) {
        validParents.add(knownType)
        if (!knownType.isAbstract) {
          types.add(knownType)
        }
      }
    }

    return types
  }


}

/**
 * Tries to guess the type from the passed value.
 */
@Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
fun Any?.resolveValueType(): ValueType = when (this) {
  null -> ValueType.NULL
  is Boolean -> ValueType.BOOLEAN
  is Date -> ValueType.DATE
  is Double -> ValueType.DOUBLE
  is Integer -> ValueType.INTEGER
  is Long -> ValueType.LONG
  is Short -> ValueType.SHORT
  is String -> ValueType.STRING
  is ByteArray -> ValueType.BYTES
  is Number -> ValueType.NUMBER
  else -> ValueType.OBJECT
}
