package org.camunda.community.rest.impl.query

import org.camunda.bpm.engine.query.Query
import org.camunda.bpm.engine.variable.type.ValueType
import org.camunda.bpm.engine.variable.value.TypedValue

@Suppress("FINITE_BOUNDS_VIOLATION_IN_JAVA")
abstract class BaseVariableQuery<T : Query<*, *>, U>(
  val queryVariableValues: MutableList<QueryVariableValue> = mutableListOf(),
  var variableNamesIgnoreCase: Boolean? = null,
  var variableValuesIgnoreCase: Boolean? = null

) : BaseQuery<T, U>() {

  fun variableValueEquals(name: String, value: Any?) = this.apply {
    queryVariableValues.add(QueryVariableValue(name = name, value = value, operator = QueryOperator.EQUALS))
  } as T

  fun variableValueNotEquals(name: String, value: Any?) = this.apply {
    queryVariableValues.add(QueryVariableValue(name = name, value = value, operator = QueryOperator.NOT_EQUALS))
  } as T

  fun variableValueGreaterThan(name: String, value: Any?) = this.apply {
    queryVariableValues.add(QueryVariableValue(name = name, value = value, operator = QueryOperator.GREATER_THAN))
  } as T

  fun variableValueGreaterThanOrEqual(name: String, value: Any?) = this.apply {
    queryVariableValues.add(QueryVariableValue(name = name, value = value, operator = QueryOperator.GREATER_THAN_OR_EQUAL))
  } as T

  fun variableValueLessThan(name: String, value: Any?) = this.apply {
    queryVariableValues.add(QueryVariableValue(name = name, value = value, operator = QueryOperator.LESS_THAN))
  } as T

  fun variableValueLessThanOrEqual(name: String, value: Any?) = this.apply {
    queryVariableValues.add(QueryVariableValue(name = name, value = value, operator = QueryOperator.LESS_THAN_OR_EQUAL))
  } as T

  fun variableValueLike(name: String, value: String?) = this.apply {
    queryVariableValues.add(QueryVariableValue(name = name, value = value, operator = QueryOperator.LIKE))
  } as T

  fun variableValueNotLike(name: String, value: String?) = this.apply {
    queryVariableValues.add(QueryVariableValue(name = name, value = value, operator = QueryOperator.NOT_LIKE))
  } as T


  fun matchVariableNamesIgnoreCase() = this.apply {
    variableNamesIgnoreCase = true
  } as T

  fun matchVariableValuesIgnoreCase() = this.apply {
    variableValuesIgnoreCase = true
  } as T

}

data class QueryVariableValue(val name: String, val value: Any?, val operator: QueryOperator,
                              val processVariable: Boolean = false, val taskVariable: Boolean = false)

enum class QueryOperator {
  EQUALS,
  NOT_EQUALS,
  GREATER_THAN,
  GREATER_THAN_OR_EQUAL,
  LESS_THAN,
  LESS_THAN_OR_EQUAL,
  LIKE,
  NOT_LIKE
}
