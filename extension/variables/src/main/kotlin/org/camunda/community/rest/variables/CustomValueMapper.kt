package org.camunda.community.rest.variables

import org.camunda.bpm.engine.variable.value.SerializableValue
import org.camunda.bpm.engine.variable.value.TypedValue

/**
 * Custom value mapper.
 */
interface CustomValueMapper {

  /**
   * Check method.
   * @param variableValue value.
   * @return `true`of the mapper is responsible.
   */
  fun canHandle(variableValue: Any): Boolean

  /**
   * Maps the value into a typed value.
   * @param variableValue value.
   * @return typed representation.
   */
  fun mapValue(variableValue: Any): TypedValue

  /**
   * Serializes the value (still returning the serializable value type).
   * @param variableValue value.
   * @return serialized representation.
   */
  fun serializeValue(variableValue: SerializableValue): SerializableValue

  /**
   * De-serializes the value.
   * @param variableValue serialized value.
   * @return typed value.
   */
  fun deserializeValue(variableValue: SerializableValue): TypedValue

}
