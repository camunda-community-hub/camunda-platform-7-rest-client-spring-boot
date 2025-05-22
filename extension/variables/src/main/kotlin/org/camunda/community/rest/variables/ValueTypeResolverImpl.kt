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

import org.camunda.bpm.engine.variable.type.ValueType
import org.camunda.bpm.engine.variable.type.ValueTypeResolver
import org.springframework.stereotype.Component

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
