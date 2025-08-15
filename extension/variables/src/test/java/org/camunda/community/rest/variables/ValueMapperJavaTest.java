package org.camunda.community.rest.variables;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.type.SerializableValueType;
import org.camunda.community.rest.variables.format.JavaSerializedObjectFormatValueMapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class ValueMapperJavaTest {

  @Nested
  class JavaSerializedObjectFormatMapperTest {

    private final JavaSerializedObjectFormatValueMapper mapper = new JavaSerializedObjectFormatValueMapper();

    private final ValueMapper valueMapper = new ValueMapper(
      new ObjectMapper(),
      new ValueTypeResolverImpl(),
      List.of(mapper)
    );

    @Test
    void serializeAndDeserializesMapWithPrimitivesAndObjects() {
      var orig = Map.of("foo", 1L, "bar", List.of(2L, 3L));

      var mapped = valueMapper.mapValues(orig);

      assertThat(mapped).hasSize(2);

      var foo = mapped.get("foo");
      var bar = mapped.get("bar");

      assertThat(foo.getType()).isEqualTo("Long");
      assertThat(bar.getType()).isEqualTo("Object");
      assertThat(bar.getValueInfo().get(SerializableValueType.VALUE_INFO_SERIALIZATION_DATA_FORMAT))
        .isEqualTo(Variables.SerializationDataFormats.JAVA.getName());
      assertThat(bar.getValue())
        .isNotNull()
        .isInstanceOf(String.class)
      ;

      var deserialized = valueMapper.mapDtos(mapped);

      assertThat(deserialized).hasSize(2);
      assertThat(deserialized.get("foo")).isEqualTo(1L);
      assertThat(deserialized.get("bar")).isEqualTo(List.of(2L, 3L));
    }
  }
}
