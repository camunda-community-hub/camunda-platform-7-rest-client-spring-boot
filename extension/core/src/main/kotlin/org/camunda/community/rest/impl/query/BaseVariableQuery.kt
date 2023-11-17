package org.camunda.community.rest.impl.query

import org.camunda.bpm.engine.query.Query
import org.camunda.bpm.engine.variable.value.TypedValue

@Suppress("FINITE_BOUNDS_VIOLATION_IN_JAVA")
abstract class BaseVariableQuery<T : Query<*, *>, U>(
  val queryVariableValues: MutableList<QueryVariableValue> = mutableListOf(),
  var variableNamesIgnoreCase: Boolean? = null,
  var variableValuesIgnoreCase: Boolean? = null

) : BaseQuery<T, U>() {

  fun variableValueEquals(name: String, value: Any?) = this.apply {
    queryVariableValues.add(createQueryVariableValue(name, value, QueryOperator.EQUALS, true))
  } as T

  fun variableValueNotEquals(name: String, value: Any?) = this.apply {
    queryVariableValues.add(createQueryVariableValue(name, value, QueryOperator.NOT_EQUALS, true))
  } as T

  fun variableValueGreaterThan(name: String, value: Any?) = this.apply {
    queryVariableValues.add(createQueryVariableValue(name, value, QueryOperator.GREATER_THAN, true))
  } as T

  fun variableValueGreaterThanOrEqual(name: String, value: Any?) = this.apply {
    queryVariableValues.add(createQueryVariableValue(name, value, QueryOperator.GREATER_THAN_OR_EQUAL, true))
  } as T

  fun variableValueLessThan(name: String, value: Any?) = this.apply {
    queryVariableValues.add(createQueryVariableValue(name, value, QueryOperator.LESS_THAN, true))
  } as T

  fun variableValueLessThanOrEqual(name: String, value: Any?) = this.apply {
    queryVariableValues.add(createQueryVariableValue(name, value, QueryOperator.LESS_THAN_OR_EQUAL, true))
  } as T

  fun variableValueLike(name: String, value: String?) = this.apply {
    queryVariableValues.add(createQueryVariableValue(name, value, QueryOperator.LIKE, true))
  } as T

  fun variableValueNotLike(name: String, value: String?) = this.apply {
    queryVariableValues.add(createQueryVariableValue(name, value, QueryOperator.NOT_LIKE, true))
  } as T


  fun matchVariableNamesIgnoreCase() = this.apply {
    variableNamesIgnoreCase = true
    queryVariableValues.forEach { it.variableNameIgnoreCase = true }
  } as T

  fun matchVariableValuesIgnoreCase() = this.apply {
    variableValuesIgnoreCase = true
    queryVariableValues.forEach { it.variableValueIgnoreCase = true }
  } as T

  protected fun createQueryVariableValue(
    name: String,
    value: Any?,
    operator: QueryOperator,
    processInstanceScope: Boolean
  ): QueryVariableValue {
    val shouldMatchVariableValuesIgnoreCase = variableValuesIgnoreCase == true && value != null && String::class.java.isAssignableFrom(value.javaClass)
    val shouldMatchVariableNamesIgnoreCase = variableNamesIgnoreCase == true
    return QueryVariableValue(
      name,
      value,
      operator,
      processInstanceScope,
      shouldMatchVariableNamesIgnoreCase,
      shouldMatchVariableValuesIgnoreCase
    )
  }

}

data class QueryVariableValue(val name: String, val value: Any?, val operator: QueryOperator,
                              val local: Boolean = false, var variableNameIgnoreCase: Boolean = false,
                              var variableValueIgnoreCase: Boolean = false)

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
