package org.camunda.community.rest.variables

import jakarta.annotation.PostConstruct
import org.camunda.bpm.engine.variable.type.ValueTypeResolver
import org.camunda.bpm.engine.variable.value.SerializableValue
import org.camunda.bpm.engine.variable.value.TypedValue
import org.camunda.spin.Spin.JSON
import org.camunda.spin.json.SpinJsonNode
import org.camunda.spin.plugin.variable.SpinValues.jsonValue
import org.camunda.spin.plugin.variable.type.impl.JsonValueTypeImpl
import org.camunda.spin.plugin.variable.type.impl.XmlValueTypeImpl
import org.camunda.spin.plugin.variable.value.JsonValue
import org.camunda.spin.plugin.variable.value.SpinValue
import org.camunda.spin.plugin.variable.value.impl.JsonValueImpl
import org.camunda.spin.plugin.variable.value.impl.SpinValueImpl
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.stereotype.Component

/**
 * Custom value mapper to map SPIN values.
 * Will only be used, if the SPIN dependencies are on the classpath.
 */
@Component
@ConditionalOnClass(SpinValue::class)
class SpinValueMapper(
  private val valueTypeResolver: ValueTypeResolver
) : CustomValueMapper {

  /**
   * Adds the SPIN value types to the list of known types by the [ValueTypeResolver].
   */
  @PostConstruct
  fun addValueTypes() {
    valueTypeResolver.addType(JsonValueTypeImpl())
    valueTypeResolver.addType(XmlValueTypeImpl())
  }

  override fun mapValue(variableValue: Any): TypedValue =
    if (variableValue is SpinJsonNode) {
      jsonValue(variableValue).create()
    } else if (variableValue is SpinValue) {
      variableValue
    } else {
      throw IllegalStateException("Variable value $variableValue not supported")
    }

  override fun canHandle(variableValue: Any) = variableValue is SpinValue || variableValue is SpinJsonNode

  override fun serializeValue(variableValue: SerializableValue): SerializableValue =
    if (variableValue is SpinValueImpl) {
      variableValue.apply { valueSerialized = variableValue.value.toString() }
    } else {
      variableValue
    }

  override fun deserializeValue(variableValue: SerializableValue): SerializableValue =
    if (variableValue is JsonValue) {
      jsonValue(JSON(variableValue.valueSerialized)).create().apply { (this as JsonValueImpl).valueSerialized = variableValue.valueSerialized }
    } else {
      variableValue
    }

}
