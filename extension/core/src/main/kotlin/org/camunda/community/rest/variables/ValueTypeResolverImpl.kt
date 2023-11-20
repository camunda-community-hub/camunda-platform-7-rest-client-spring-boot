package org.camunda.community.rest.variables

import org.camunda.bpm.engine.variable.type.ValueType
import org.camunda.bpm.engine.variable.type.ValueTypeResolver
import org.springframework.stereotype.Component

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
