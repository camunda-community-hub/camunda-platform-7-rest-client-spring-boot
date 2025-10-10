package org.camunda.community.rest.variables;

import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.type.ValueType;
import org.camunda.community.rest.variables.serialization.JavaSerializationValueSerializerTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class ValueTypeRegistrationTest {

  static Stream<Arguments> determineValueType() {
    return Stream.of(
      arguments(null, ValueType.NULL),
      arguments(true, ValueType.BOOLEAN),
      arguments(Boolean.TRUE, ValueType.BOOLEAN),
      arguments(new Date(), ValueType.DATE),
      arguments(.1d, ValueType.DOUBLE),
      arguments(Double.valueOf("0.1"), ValueType.DOUBLE),
      arguments(1, ValueType.INTEGER),
      arguments(Integer.valueOf("1"), ValueType.INTEGER),
      arguments(1L, ValueType.LONG),
      arguments(Long.valueOf("1"), ValueType.LONG),
      arguments((short) 1, ValueType.SHORT),
      arguments(Short.valueOf("1"), ValueType.SHORT),
      arguments("string", ValueType.STRING),
      arguments("string".getBytes(), ValueType.BYTES),
      arguments(BigDecimal.valueOf(1L), ValueType.NUMBER),
      arguments(new ArrayList<String>(), ValueType.OBJECT),
      arguments(new JavaSerializationValueSerializerTest(), ValueType.OBJECT)
    );
  }

  @ParameterizedTest
  @MethodSource
  void determineValueType(final Object value, final ValueType expectedType) {
    ValueTypeRegistration valueTypeRegistration = new ValueTypeRegistration();
    assertThat(valueTypeRegistration.convertToTypedValue(value, false, Variables.SerializationDataFormats.JSON).getType()).isEqualTo(expectedType);
  }

}
