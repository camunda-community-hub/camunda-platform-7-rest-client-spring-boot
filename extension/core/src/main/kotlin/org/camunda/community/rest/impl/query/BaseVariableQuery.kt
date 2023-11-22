package org.camunda.community.rest.impl.query

import org.camunda.bpm.engine.query.Query
import org.camunda.bpm.engine.variable.type.ValueType
import org.camunda.bpm.engine.variable.value.TypedValue

/**
 * Base class for queries which allow querying by variable values.
 */
@Suppress("FINITE_BOUNDS_VIOLATION_IN_JAVA")
abstract class BaseVariableQuery<T : Query<*, *>, U>(
  val queryVariableValues: MutableList<QueryVariableValue> = mutableListOf(),
  var variableNamesIgnoreCase: Boolean? = null,
  var variableValuesIgnoreCase: Boolean? = null

) : BaseQuery<T, U>() {

  /**
   * Adds a variable query for the equals operator.
   */
  fun variableValueEquals(name: String, value: Any?) = this.apply {
    queryVariableValues.add(QueryVariableValue(name = name, value = value, operator = QueryOperator.EQUALS))
  } as T

  /**
   * Adds a variable query for the not equals operator.
   */
  fun variableValueNotEquals(name: String, value: Any?) = this.apply {
    queryVariableValues.add(QueryVariableValue(name = name, value = value, operator = QueryOperator.NOT_EQUALS))
  } as T

  /**
   * Adds a variable query for the greater than operator.
   */
  fun variableValueGreaterThan(name: String, value: Any?) = this.apply {
    queryVariableValues.add(QueryVariableValue(name = name, value = value, operator = QueryOperator.GREATER_THAN))
  } as T

  /**
   * Adds a variable query for the greater than or equal operator.
   */
  fun variableValueGreaterThanOrEqual(name: String, value: Any?) = this.apply {
    queryVariableValues.add(QueryVariableValue(name = name, value = value, operator = QueryOperator.GREATER_THAN_OR_EQUAL))
  } as T

  /**
   * Adds a variable query for the less than operator.
   */
  fun variableValueLessThan(name: String, value: Any?) = this.apply {
    queryVariableValues.add(QueryVariableValue(name = name, value = value, operator = QueryOperator.LESS_THAN))
  } as T

  /**
   * Adds a variable query for the less than or equal operator.
   */
  fun variableValueLessThanOrEqual(name: String, value: Any?) = this.apply {
    queryVariableValues.add(QueryVariableValue(name = name, value = value, operator = QueryOperator.LESS_THAN_OR_EQUAL))
  } as T

  /**
   * Adds a variable query for the like operator.
   */
  fun variableValueLike(name: String, value: String?) = this.apply {
    queryVariableValues.add(QueryVariableValue(name = name, value = value, operator = QueryOperator.LIKE))
  } as T

  /**
   * Adds a variable query for the not like operator.
   */
  fun variableValueNotLike(name: String, value: String?) = this.apply {
    queryVariableValues.add(QueryVariableValue(name = name, value = value, operator = QueryOperator.NOT_LIKE))
  } as T

  /**
   * Specifies that the case of the variable name should be ignored in the query.
   */
  fun matchVariableNamesIgnoreCase() = this.apply {
    variableNamesIgnoreCase = true
  } as T

  /**
   * Specifies that the case of the variable values should be ignored in the query.
   */
  fun matchVariableValuesIgnoreCase() = this.apply {
    variableValuesIgnoreCase = true
  } as T

}

/**
 * Data class for a variable value to query for.
 */
data class QueryVariableValue(val name: String, val value: Any?, val operator: QueryOperator,
                              val processVariable: Boolean = false, val taskVariable: Boolean = false)

/**
 * Enumeration of possible query operators.
 */
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
