package org.camunda.community.rest.variables

import com.fasterxml.jackson.databind.ObjectMapper
import org.camunda.bpm.engine.variable.type.ValueTypeResolver
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ValueMapperConfiguration {
  @Bean
  @ConditionalOnMissingBean(ValueMapper::class)
  fun defaultValueTypeResolver(): ValueTypeResolver {
    return ValueTypeResolverImpl()
  }

  @Bean
  @ConditionalOnMissingBean(ValueMapper::class)
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
