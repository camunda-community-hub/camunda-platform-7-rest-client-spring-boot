/*-
 * #%L
 * camunda-rest-client-spring-boot
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
package org.camunda.bpm.extension.rest.config

import feign.QueryMapEncoder
import feign.codec.EncodeException
import org.camunda.bpm.engine.impl.util.ReflectUtil
import org.camunda.bpm.engine.rest.dto.CamundaQueryParam
import org.camunda.bpm.engine.rest.dto.VariableQueryParameterDto
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.beans.IntrospectionException
import java.beans.Introspector
import java.beans.PropertyDescriptor
import java.lang.reflect.InvocationTargetException

/**
 * Configuration of query map encoder to be able to map QueryDTOs to a list of query params.
 */
@Configuration
class CamundaQueryMapEncoderConfiguration {
  /**
   * Provide own encoder.
   */
  @Bean
  fun camundaQueryMapEncoder(): QueryMapEncoder = CamundaQueryMapEncoder()
}

/**
 * Query map encoder for Camunda DTO classes using [@CamundaQueryParam] annotation.
 */
class CamundaQueryMapEncoder : QueryMapEncoder {

  private val classToMetadata: MutableMap<Class<*>, ObjectParamMetadata> = HashMap()

  override fun encode(value: Any?): MutableMap<String, Any> {
    return try {
      val propertyNameToValue: MutableMap<String, Any> = HashMap()
      if (value != null) {
        val metadata: ObjectParamMetadata = getMetadata(value.javaClass)
        for (pd in metadata.objectProperties) {

          val result = if (pd.readMethod != null) {
            pd.readMethod.invoke(value)
          } else {
            ReflectUtil.getField(pd.name, value.javaClass)?.apply { this.trySetAccessible() }?.get(value)
          }
          if (result != null && result !== value) { // avoid nulls and recursive return types
            val alias = pd.writeMethod.getAnnotation(CamundaQueryParam::class.java)
            val name = alias?.value ?: pd.name // get the name property from the property name or from camunda annotation
            if (result is Collection<*>) {
              propertyNameToValue[name] = result.map { encodeCollectionElement(it) }.joinToString(";")
            } else {
              propertyNameToValue[name] = result
            }
          }
        }
      }
      propertyNameToValue
    } catch (e: IllegalAccessException) {
      throw EncodeException("Failure encoding object into query map", e)
    } catch (e: IntrospectionException) {
      throw EncodeException("Failure encoding object into query map", e)
    } catch (e: InvocationTargetException) {
      throw EncodeException("Failure encoding object into query map", e)
    }
  }

  private fun encodeCollectionElement(value: Any?): Any? =
    when (value) {
      is VariableQueryParameterDto -> "${value.name}_${value.operator}_${value.value}"
      else -> value
    }


  private fun getMetadata(objectType: Class<*>): ObjectParamMetadata {
    return classToMetadata.getOrPut(objectType) { ObjectParamMetadata.parseObjectType(objectType) }
  }

  /**
   * Object metadata folding properties information.
   */
  data class ObjectParamMetadata(val objectProperties: List<PropertyDescriptor>) {

    companion object {
      /**
       * Factory method.
       */
      fun parseObjectType(type: Class<*>): ObjectParamMetadata {
        val properties: MutableList<PropertyDescriptor> = mutableListOf()
        for (pd in Introspector.getBeanInfo(type).propertyDescriptors) {
          val hasSetterMethod = pd.writeMethod != null
          if (hasSetterMethod) {
            properties.add(pd)
          }
        }
        return ObjectParamMetadata(properties)
      }
    }
  }

}
