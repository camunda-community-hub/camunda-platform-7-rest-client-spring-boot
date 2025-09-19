package org.camunda.community.rest.variables.serialization

import org.camunda.bpm.engine.variable.value.SerializableValue
import org.camunda.bpm.engine.variable.value.TypedValue

interface CustomValueSerializer : ValueSerializer {

  /**
   * @param value - the typedValue that should be serialized
   * @return `true` if the #serializeValue method should be called.
   */
  fun canSerializeValue(value: TypedValue): Boolean

  /**
   * @param value - the serializableValue that should be de-serialized
   * @return `true` if the #deserializeValue method should be called.
   */
  fun canDeserializeValue(value: SerializableValue): Boolean

}
