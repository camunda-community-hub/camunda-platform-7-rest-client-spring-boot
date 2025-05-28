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

  override fun mapValue(variableValue: Any?): TypedValue =
    when (variableValue) {
      is SpinJsonNode -> jsonValue(variableValue).create()
      is SpinValue -> variableValue
      else -> throw IllegalStateException("Variable value $variableValue not supported")
    }

  override fun canHandle(variableValue: Any?) = variableValue is SpinValue || variableValue is SpinJsonNode

  override fun serializeValue(variableValue: SerializableValue): SerializableValue =
    if (variableValue is SpinValueImpl) {
      variableValue.apply { valueSerialized = variableValue.value.toString() }
    } else {
      variableValue
    }

  override fun deserializeValue(variableValue: SerializableValue): SerializableValue =
    if (variableValue is JsonValue) {
      jsonValue(JSON(variableValue.valueSerialized))
        .create()
        .apply { (this as JsonValueImpl).valueSerialized = variableValue.valueSerialized }
    } else {
      variableValue
    }

}
