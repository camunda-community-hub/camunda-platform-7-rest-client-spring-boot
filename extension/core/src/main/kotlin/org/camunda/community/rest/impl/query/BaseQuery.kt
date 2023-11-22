package org.camunda.community.rest.impl.query

import mu.KLogging
import org.camunda.bpm.engine.ProcessEngineException
import org.camunda.bpm.engine.query.Query
import org.camunda.bpm.engine.variable.type.ValueType
import kotlin.reflect.KProperty1
import kotlin.reflect.KType
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible


@Suppress("FINITE_BOUNDS_VIOLATION_IN_JAVA")
abstract class BaseQuery<T : Query<*, *>, U>(
  val orderingProperties: MutableList<QueryOrderingProperty> = mutableListOf(),
  var tenantIds: Array<out String>? = null,
  var tenantIdsSet: Boolean = false,
) : Query<T, U> {

  companion object : KLogging()

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
    if (tenantIdsSet && this.tenantIds == null) {
      throw ProcessEngineException("Invalid query usage: cannot set both tenantIdIn and withoutTenantId filters.")
    }
    this.tenantIds = tenantIds
    this.tenantIdsSet = true
  } as T

  fun withoutTenantId() = this.apply {
    if (!tenantIds.isNullOrEmpty()) {
      throw ProcessEngineException("Invalid query usage: cannot set both tenantIdIn and withoutTenantId filters.")
    }
    this.tenantIds = null
    this.tenantIdsSet = true
  } as T

  fun orderBy(property: String) {
    orderingProperties.add(QueryOrderingProperty(property = property))
  }

  override fun asc(): T = this.apply { direction(SortDirection.ASC) } as T

  override fun desc():T = this.apply { direction(SortDirection.DESC) } as T

  fun direction(direction: SortDirection) {
    if (orderingProperties.last().direction != null) {
      throw IllegalStateException("sort direction cannot be set twice for same property")
    }
    orderingProperties.last().direction = direction
  }

  fun orderByTenantId() = this.apply { orderBy("tenantId") } as T

  open fun validate() {
    if (orderingProperties.any { it.direction == null }) throw IllegalStateException("sort direction has to be set for each ordering property")
  }

  fun <Q : BaseQuery<T, U>> valueForProperty(name: String, query: Q, expectedType: KType): Any? {
    val propertiesByName = query::class.memberProperties.associateBy { it.name }
    val property = propertiesByName[name]
    if (property == null) {
      throw IllegalArgumentException("no property found for $name")
    } else if (!property.returnType.isSubtypeOf(expectedType)) {
      throw IllegalArgumentException("${property.returnType} is not assignable to $expectedType for $name")
    } else {
      property.isAccessible = true
      val propValue = (property as KProperty1<Q, *>).get(query)
      return if (propValue is Collection<*>) propValue.joinToString(",") else propValue
    }

  }

  fun sortProperty(): QueryOrderingProperty? {
    if (this.orderingProperties.size > 1) BaseQuery.logger.warn { "sorting with more than one property not supported, ignoring all but first" }
    return this.orderingProperties.firstOrNull()
  }

}

enum class SortDirection {
  ASC,
  DESC
}

enum class Relation {
  PROCESS_INSTANCE,
  EXECUTION,
  TASK,
  CASE_INSTANCE,
  CASE_EXECUTION
}

data class QueryOrderingProperty(val property: String, var direction: SortDirection? = null, val type: ValueType? = null, val relation: Relation? = null)

enum class SuspensionState {
  ACTIVE,
  SUSPENDED
}
