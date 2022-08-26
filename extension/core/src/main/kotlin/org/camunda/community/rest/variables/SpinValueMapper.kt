package org.camunda.community.rest.variables

import org.camunda.bpm.engine.variable.Variables.untypedValue
import org.camunda.bpm.engine.variable.value.SerializableValue
import org.camunda.bpm.engine.variable.value.TypedValue
import org.camunda.spin.Spin.JSON
import org.camunda.spin.json.SpinJsonNode
import org.camunda.spin.plugin.variable.SpinValues.jsonValue
import org.camunda.spin.plugin.variable.value.JsonValue
import org.camunda.spin.plugin.variable.value.SpinValue
import org.camunda.spin.plugin.variable.value.impl.JsonValueImpl
import org.camunda.spin.plugin.variable.value.impl.SpinValueImpl
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.stereotype.Component

@Component
@ConditionalOnClass(SpinValue::class)
class SpinValueMapper : CustomValueMapper {

  override fun mapValue(variableValue: Any): TypedValue =
    if (variableValue is SpinJsonNode) {
      jsonValue(variableValue).create()
    } else {
      untypedValue(variableValue)
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
