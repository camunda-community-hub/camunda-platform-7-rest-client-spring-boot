/*-
 * #%L
 * camunda-platform-7-rest-client-spring-boot
 * %%
 * Copyright (C) 2019 Camunda Services GmbH
 * %%
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 *  under one or more contributor license agreements. See the NOTICE file
 *  distributed with this work for additional information regarding copyright
 *  ownership. Camunda licenses this file to you under the Apache License,
 *  Version 2.0; you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * #L%
 */

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
  @Deprecated("Use fine grained check methods instead.")
  fun canHandle(value: Any?): Boolean {
    return canMapValue(value) ||
      canSerializeValue(value as TypedValue) ||
      canDeserializeValue(value as SerializableValue)
  }

  /**
   * @param value - the (nullable) value to check
   * @return `true` if the #mapValue method should be called.
   */
  fun canMapValue(value: Any?): Boolean

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


  /**
   * Maps the value into a typed value.
   * @return typed representation.
   */
  fun mapValue(value: Any?): TypedValue

  /**
   * Serializes the value (still returning the serializable value type).
   * @return serialized representation.
   */
  fun serializeValue(value: TypedValue): SerializableValue

  /**
   * De-serializes the value.
   * @return typed value.
   */
  fun deserializeValue(value: SerializableValue): TypedValue

}
