package org.camunda.community.rest.example.standalone.client;

import java.util.Objects;

public class MyDataStructure {

  private final String string;
  private final Integer integer;

  public MyDataStructure(String string, Integer integer) {
    this.integer = integer;
    this.string = string;
  }

  public String getString() {
    return string;
  }

  public Integer getInteger() {
    return integer;
  }

  @Override
  public String toString() {
    return "MyDataStructure{" +
      "string='" + string + '\'' +
      ", integer=" + integer +
      '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    MyDataStructure that = (MyDataStructure) o;
    if (!Objects.equals(string, that.string)) {
      return false;
    }
    return Objects.equals(integer, that.integer);
  }

  @Override
  public int hashCode() {
    int result = string != null ? string.hashCode() : 0;
    result = 31 * result + (integer != null ? integer.hashCode() : 0);
    return result;
  }
}
