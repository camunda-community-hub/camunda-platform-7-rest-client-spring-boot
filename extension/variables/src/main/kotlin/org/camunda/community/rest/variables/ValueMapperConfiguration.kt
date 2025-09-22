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

import com.fasterxml.jackson.databind.ObjectMapper
import org.camunda.bpm.engine.variable.type.ValueTypeResolver
import org.camunda.community.rest.variables.serialization.CustomValueSerializer
import org.camunda.community.rest.variables.serialization.JavaSerializationValueSerializer
import org.camunda.community.rest.variables.serialization.JsonValueSerializer
import org.camunda.community.rest.variables.serialization.SpinJsonValueSerializer
import org.camunda.community.rest.variables.serialization.SpinXmlValueSerializer
import org.camunda.spin.plugin.variable.value.SpinValue
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean

@AutoConfiguration
@EnableConfigurationProperties(value = [CamundaRestClientVariablesProperties::class])
class ValueMapperConfiguration {

  @Bean
  @ConditionalOnMissingBean(ValueTypeResolver::class)
  fun defaultValueTypeResolver(): ValueTypeResolver {
    return ValueTypeResolverImpl()
  }

  @Bean
  fun valueTypeRegistration() = ValueTypeRegistration()

  @Bean
  @ConditionalOnMissingBean(SpinXmlValueSerializer::class)
  @ConditionalOnClass(SpinValue::class)
  fun spinXmlValueSerializer(valueTypeResolver: ValueTypeResolver, valueTypeRegistration: ValueTypeRegistration): SpinXmlValueSerializer {
    return SpinXmlValueSerializer(valueTypeResolver = valueTypeResolver, valueTypeRegistration = valueTypeRegistration)
  }

  @Bean
  @ConditionalOnMissingBean(SpinJsonValueSerializer::class)
  @ConditionalOnClass(SpinValue::class)
  fun spinJsonValueSerializer(valueTypeResolver: ValueTypeResolver, valueTypeRegistration: ValueTypeRegistration): SpinJsonValueSerializer {
    return SpinJsonValueSerializer(valueTypeResolver = valueTypeResolver, valueTypeRegistration = valueTypeRegistration)
  }

  @Bean
  @ConditionalOnMissingBean(ValueMapper::class)
  fun defaultValueMapper(
    objectMapper: ObjectMapper,
    valueTypeResolver: ValueTypeResolver,
    valueTypeRegistration: ValueTypeRegistration,
    customValueSerializers: List<CustomValueSerializer>,
    customValueMappers: List<CustomValueMapper>,
    properties: CamundaRestClientVariablesProperties
  ): ValueMapper {
    val wrapperCustomValueMappers = customValueMappers.map {
      CustomValueMapperAdapter(it)
    }
    return ValueMapper(
      objectMapper = objectMapper,
      valueTypeResolver = valueTypeResolver,
      valueTypeRegistration = valueTypeRegistration,
      valueSerializers = listOf(JavaSerializationValueSerializer(), JsonValueSerializer(objectMapper)),
      customValueSerializers = customValueSerializers + wrapperCustomValueMappers,
      serializationFormat = properties.defaultSerializationFormat
    )
  }
}
