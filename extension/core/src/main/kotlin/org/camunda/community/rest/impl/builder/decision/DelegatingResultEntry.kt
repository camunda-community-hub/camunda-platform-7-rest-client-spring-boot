package org.camunda.community.rest.impl.builder.decision

import org.camunda.bpm.dmn.engine.DmnDecisionResultEntries
import org.camunda.bpm.dmn.engine.DmnDecisionRuleResult
import org.camunda.bpm.engine.variable.value.TypedValue

/**
 * Implementation of the [DelegatingResultEntry] that can be used as a [DmnDecisionRuleResult].
 */
class DelegatingDmnDecisionRuleResult(resultMap: Map<String, TypedValue>) : DelegatingResultEntry(resultMap), DmnDecisionRuleResult

/**
 * Implementation of the [DelegatingResultEntry] that can be used as a [DmnDecisionResultEntries].
 */
class DelegatingDmnDecisionResultEntries(resultMap: Map<String, TypedValue>) : DelegatingResultEntry(resultMap), DmnDecisionResultEntries

/**
 * Abstract class for result entries from a decision evaluation.
 */
sealed class DelegatingResultEntry(
  private val resultMap: Map<String, TypedValue>
) : MutableMap<String, Any> {
  override fun clear() { throw UnsupportedOperationException("decision output is immutable") }

  override fun put(key: String, value: Any): Any? { throw UnsupportedOperationException("decision output is immutable") }

  override fun putAll(from: Map<out String, Any>) { throw UnsupportedOperationException("decision output is immutable") }

  override fun remove(key: String): Any? { throw UnsupportedOperationException("decision output is immutable") }

  override fun containsKey(key: String) = resultMap.containsKey(key)

  override fun containsValue(value: Any) = values.contains(value)

  override fun get(key: String) = resultMap[key]?.value

  override fun isEmpty() = resultMap.isEmpty()

  /**
   * Get the first entry of null if empty.
   */
  fun <T : Any> getFirstEntry(): T? = values.firstOrNull() as T?

  /**
   * Get the first entry as a [TypedValue] or null if empty.
   */
  fun <T : TypedValue> getFirstEntryTyped(): T? = resultMap.values.firstOrNull() as T?

  /**
   * Gets a single entry or null if empty. Throws an exception if more than on entry.
   */
  fun <T : Any> getSingleEntry(): T? = if (isEmpty()) null else values.single() as T

  /**
   * Gets a single entry as a [TypedValue] or null if empty. Throws an exception if more than on entry.
   */
  fun <T : TypedValue> getSingleEntryTyped(): T? = if (isEmpty()) null else resultMap.values.firstOrNull() as T?

  /**
   * Gets the entry for the specified name of null if none found.
   */
  fun <T : Any> getEntry(key: String): T? = resultMap[key]?.value as T?

  /**
   * Gets the entry for the specified name as a [TypedValue] of null if none found.
   */
  fun <T : TypedValue> getEntryTyped(key: String): T? = resultMap[key] as T?

  /**
   * Retrieves the map of entries.
   */
  fun getEntryMap(): MutableMap<String, Any> = resultMap.mapValues { it.value }.toMutableMap()

  /**
   * Retrieves the map of entries with the values as [TypedValue].
   */
  fun getEntryMapTyped(): MutableMap<String, TypedValue> = resultMap.toMutableMap()

  override val entries: MutableSet<MutableMap.MutableEntry<String, Any>> = getEntryMap().entries
  override val keys = resultMap.keys.toMutableSet()
  override val values: MutableCollection<Any> = resultMap.values.map { it.value }.toMutableList()
  override val size = resultMap.size

}

