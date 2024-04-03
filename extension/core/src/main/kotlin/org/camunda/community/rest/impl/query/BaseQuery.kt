package org.camunda.community.rest.impl.query

import mu.KLogging
import org.camunda.bpm.engine.ProcessEngineException
import org.camunda.bpm.engine.query.Query
import org.camunda.bpm.engine.variable.type.ValueType
import org.camunda.community.rest.impl.toOffsetDateTime
import java.time.OffsetDateTime
import java.util.*
import kotlin.reflect.KProperty1
import kotlin.reflect.KType
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.starProjectedType
import kotlin.reflect.full.withNullability
import kotlin.reflect.jvm.isAccessible

/**
 * Base clas for queries delegating to remote calls.
 */
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

  /**
   * Only select those results where the tenant id is in the list of supplied ids.
   * @param tenantIds ids to check for
   */
  fun tenantIdIn(vararg tenantIds: String) = this.apply {
    if (tenantIdsSet && this.tenantIds == null) {
      throw ProcessEngineException("Invalid query usage: cannot set both tenantIdIn and withoutTenantId filters.")
    }
    this.tenantIds = tenantIds
    this.tenantIdsSet = true
  } as T

  /**
   * Only select those results that have no tenant set.
   */
  fun withoutTenantId() = this.apply {
    if (!tenantIds.isNullOrEmpty()) {
      throw ProcessEngineException("Invalid query usage: cannot set both tenantIdIn and withoutTenantId filters.")
    }
    this.tenantIds = null
    this.tenantIdsSet = true
  } as T

  /**
   * Order the results by the supplied property.
   * Call [asc] or [desc] afterward, to set the sort direction for the property.
   * @param property name of the property to sort by
   */
  fun orderBy(property: String) {
    orderingProperties.add(QueryOrderingProperty(property = property))
  }

  /**
   * Sets the sorting direction to ascending for the current ordering property.
   * [orderBy] has to be called before calling this method.
   */
  override fun asc(): T = this.apply { direction(SortDirection.ASC) } as T

  /**
   * Sets the sorting direction to descending for the current ordering property.
   * [orderBy] has to be called before calling this method.
   */
  override fun desc():T = this.apply { direction(SortDirection.DESC) } as T

  /**
   * Set the sort direction for the current ordering property.
   * @param direction either ascending or descending
   */
  fun direction(direction: SortDirection) {
    if (orderingProperties.last().direction != null) {
      throw IllegalStateException("sort direction cannot be set twice for same property")
    }
    orderingProperties.last().direction = direction
  }

  /**
   * Order the results by tenant id.
   */
  fun orderByTenantId() = this.apply { orderBy("tenantId") } as T

  /**
   * Validate the parameers of the query.
   * Can be overwritten by subclasses to perform custom validation.
   */
  open fun validate() {
    if (orderingProperties.any { it.direction == null }) throw IllegalStateException("sort direction has to be set for each ordering property")
  }

  /**
   * Retrieves the value for the given property via reflection.
   * Expects a member property of the same name and compatible type in the concrete query class.
   * @param name name of the property
   * @param query concrete query instance
   * @param expectedType expected type of the property value
   * @return value of the property in the query instance
   */
  fun <Q : BaseQuery<T, U>> valueForProperty(name: String, query: Q, expectedType: KType): Any? {
    val propertiesByName = query::class.memberProperties.associateBy { it.name }
    val property = propertiesByName[name]
    if (property == null) {
      throw IllegalArgumentException("no property found for $name")
    } else if (property.returnType.isSubtypeOf(Date::class.starProjectedType.withNullability(true))
        && expectedType.isSubtypeOf(OffsetDateTime::class.starProjectedType)) {
      property.isAccessible = true
      val propValue = (property as KProperty1<Q, *>).get(query) as Date?
      return propValue?.toOffsetDateTime()
    } else if (!property.returnType.isSubtypeOf(expectedType)) {
      throw IllegalArgumentException("${property.returnType} is not assignable to $expectedType for $name")
    } else {
      property.isAccessible = true
      val propValue = (property as KProperty1<Q, *>).get(query)
      return if (propValue is Collection<*>) propValue.joinToString(",") else propValue
    }

  }

  /**
   * Retrieves the sort property from the list of ordering properties.
   * Throws an error if there is more than one ordering property, as that is currently not supported.
   * @return the sorting property
   */
  fun sortProperty(): QueryOrderingProperty? {
    if (this.orderingProperties.size > 1) BaseQuery.logger.warn { "sorting with more than one property not supported, ignoring all but first" }
    return this.orderingProperties.firstOrNull()
  }

}

/**
 * Enumeration of possible sort directions (ascending and descending).
 */
enum class SortDirection {
  ASC,
  DESC
}

/**
 * Enumeration of possible relations for variable queries (scope of the variable).
 */
enum class Relation {
  PROCESS_INSTANCE,
  EXECUTION,
  TASK,
  CASE_INSTANCE,
  CASE_EXECUTION
}

/**
 * Data class representing a ordering property used for sorting the results.
 */
data class QueryOrderingProperty(val property: String, var direction: SortDirection? = null, val type: ValueType? = null, val relation: Relation? = null)

/**
 * Enumeration of suspension states, that can be used in queries (active or suspended).
 */
enum class SuspensionState {
  ACTIVE,
  SUSPENDED
}
