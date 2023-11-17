package org.camunda.community.rest.impl.query

import org.camunda.bpm.engine.ProcessEngineException
import org.camunda.bpm.engine.query.Query


@Suppress("FINITE_BOUNDS_VIOLATION_IN_JAVA")
abstract class BaseQuery<T : Query<*, *>, U>(
  val orderingProperties: MutableList<QueryOrderingProperty> = mutableListOf(),
  var tenantIds: Array<out String>? = null,
  var tenantIdsSet: Boolean = false,
) : Query<T, U> {

  override fun unlimitedList(): List<U> {
    // FIXME: best approximation so far.
    return list()
  }

  override fun list(): List<U> = listPage(0, Int.MAX_VALUE)

  override fun singleResult(): U? {
    val results = list()
    return when {
      results.size == 1 -> results[0]
      results.size > 1 -> throw ProcessEngineException("Query return " + results.size.toString() + " results instead of expected maximum 1")
      else -> null
    }
  }

  fun tenantIdIn(vararg tenantIds: String) = this.apply {
    this.tenantIds = tenantIds
    this.tenantIdsSet = true
  } as T

  fun withoutTenantId() = this.apply {
    this.tenantIds = null
    this.tenantIdsSet = true
  } as T

  fun orderBy(property: String) {
    orderingProperties.add(QueryOrderingProperty(property = property))
  }

  override fun asc(): T = this.apply { direction(SortDirection.ASC) } as T

  override fun desc():T = this.apply { direction(SortDirection.ASC) } as T

  fun direction(direction: SortDirection) {
    if (orderingProperties.last().direction != null) {
      throw IllegalStateException("sort direction cannot be set twice for same property")
    }
    orderingProperties.last().direction = direction
  }

  fun orderByTenantId() = this.apply { orderBy("tenantId") } as T

  open fun checkQueryOk() {
    if (orderingProperties.any { it.direction == null }) throw IllegalStateException("sort direction has to be set for each ordering property")
  }

}

enum class SortDirection {
  ASC,
  DESC
}

data class QueryOrderingProperty(val property: String, var direction: SortDirection? = null)
