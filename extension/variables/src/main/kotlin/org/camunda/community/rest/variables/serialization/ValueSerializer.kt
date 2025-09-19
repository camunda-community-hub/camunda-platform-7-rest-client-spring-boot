package org.camunda.community.rest.variables.serialization

import org.camunda.bpm.engine.variable.value.SerializableValue
import org.camunda.bpm.engine.variable.value.SerializationDataFormat
import org.camunda.bpm.engine.variable.value.TypedValue

interface ValueSerializer {

  val serializationDataFormat: SerializationDataFormat

  fun serializeValue(value: TypedValue): SerializableValue

  fun deserializeValue(value: SerializableValue): TypedValue

}
