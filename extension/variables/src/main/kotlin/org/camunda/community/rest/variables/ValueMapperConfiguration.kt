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
import org.camunda.spin.plugin.variable.value.SpinValue
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ValueMapperConfiguration {
  @Bean
  @ConditionalOnMissingBean(ValueTypeResolver::class)
  fun defaultValueTypeResolver(): ValueTypeResolver {
    return ValueTypeResolverImpl()
  }

  @Bean
  @ConditionalOnMissingBean(SpinValueMapper::class)
  @ConditionalOnClass(SpinValue::class)
  fun defaultSpinValueMapper(valueTypeResolver: ValueTypeResolver): SpinValueMapper {
    return SpinValueMapper(valueTypeResolver = valueTypeResolver)
  }

  @Bean
  @ConditionalOnMissingBean(ValueMapper::class)
  fun defaultValueMapper(
    objectMapper: ObjectMapper,
    valueTypeResolver: ValueTypeResolver,
    customValueMappers: List<CustomValueMapper>
  ): ValueMapper {
    return ValueMapper(
      objectMapper = objectMapper,
      valueTypeResolver = valueTypeResolver,
      customValueMapper = customValueMappers
    )
  }
}
