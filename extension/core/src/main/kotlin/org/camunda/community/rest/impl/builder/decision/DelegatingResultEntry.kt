package org.camunda.community.rest.impl.builder.decision

import org.camunda.bpm.dmn.engine.DmnDecisionResultEntries
import org.camunda.bpm.dmn.engine.DmnDecisionRuleResult
import org.camunda.bpm.engine.variable.value.TypedValue

class DelegatingDmnDecisionRuleResult(resultMap: Map<String, TypedValue>) : DelegatingResultEntry(resultMap), DmnDecisionRuleResult

class DelegatingDmnDecisionResultEntries(resultMap: Map<String, TypedValue>) : DelegatingResultEntry(resultMap), DmnDecisionResultEntries

open class DelegatingResultEntry(
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

  fun <T : Any> getFirstEntry(): T? = values.firstOrNull() as T?

  fun <T : TypedValue> getFirstEntryTyped(): T? = resultMap.values.firstOrNull() as T?

  fun <T : Any> getSingleEntry(): T? = if (isEmpty()) null else values.single() as T

  fun <T : TypedValue> getSingleEntryTyped(): T? = if (isEmpty()) null else resultMap.values.firstOrNull() as T?

  fun <T : Any> getEntry(key: String): T? = resultMap[key]?.value as T?

  fun <T : TypedValue> getEntryTyped(key: String): T? = resultMap[key] as T?

  fun getEntryMap(): MutableMap<String, Any> = resultMap.mapValues { it.value }.toMutableMap()

  fun getEntryMapTyped(): MutableMap<String, TypedValue> = resultMap.toMutableMap()

  override val entries: MutableSet<MutableMap.MutableEntry<String, Any>> = getEntryMap().entries
  override val keys = resultMap.keys.toMutableSet()
  override val values: MutableCollection<Any> = resultMap.values.map { it.value }.toMutableList()
  override val size = resultMap.size

}

