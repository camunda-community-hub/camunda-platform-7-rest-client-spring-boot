package org.camunda.community.rest.impl.builder.decision

import org.camunda.bpm.dmn.engine.DmnDecisionResult
import org.camunda.bpm.dmn.engine.DmnDecisionResultEntries
import org.camunda.bpm.dmn.engine.DmnDecisionRuleResult
import org.camunda.bpm.dmn.engine.DmnDecisionTableResult
import org.camunda.bpm.engine.variable.value.TypedValue
import java.util.*

class DelegatingDmnDecisionTableResult(resultList: List<DelegatingDmnDecisionRuleResult>) :
  DelegatingResultList<DmnDecisionRuleResult>(resultList), DmnDecisionTableResult

class DelegatingDmnDecisionResult(resultList: List<DelegatingDmnDecisionResultEntries>) :
  DelegatingResultList<DmnDecisionResultEntries>(resultList), DmnDecisionResult

open class DelegatingResultList<T>(
  private val resultList: List<T>
) : MutableList<T> {
  override fun add(element: T): Boolean {
    throw UnsupportedOperationException("decision result is immutable")
  }

  override fun add(index: Int, element: T) {
    throw UnsupportedOperationException("decision result is immutable")
  }

  override fun addAll(index: Int, elements: Collection<T>): Boolean {
    throw UnsupportedOperationException("decision result is immutable")
  }

  override fun addAll(elements: Collection<T>): Boolean {
    throw UnsupportedOperationException("decision result is immutable")
  }

  override fun clear() {
    throw UnsupportedOperationException("decision result is immutable")
  }

  override fun listIterator(): MutableListIterator<T> = asUnmodifiableList(resultList.toMutableList()).listIterator()
  override fun listIterator(index: Int): MutableListIterator<T> = asUnmodifiableList(resultList.toMutableList()).listIterator()
  override fun remove(element: T): Boolean {
    throw UnsupportedOperationException("decision result is immutable")
  }

  override fun removeAll(elements: Collection<T>): Boolean {
    throw UnsupportedOperationException("decision result is immutable")
  }

  override fun removeAt(index: Int): T {
    throw UnsupportedOperationException("decision result is immutable")
  }

  override fun retainAll(elements: Collection<T>): Boolean {
    throw UnsupportedOperationException("decision result is immutable")
  }

  override fun set(index: Int, element: T): T {
    throw UnsupportedOperationException("decision result is immutable")
  }

  override fun subList(fromIndex: Int, toIndex: Int): MutableList<T> = asUnmodifiableList(
    resultList.subList(fromIndex, toIndex).toMutableList()
  )

  override fun contains(element: T) = resultList.contains(element)
  override fun containsAll(elements: Collection<T>) = resultList.containsAll(elements)
  override fun get(index: Int) = resultList[index]
  override fun indexOf(element: T) = resultList.indexOf(element)
  override fun isEmpty() = resultList.isEmpty()
  override fun iterator() = asUnmodifiableList(resultList.toMutableList()).iterator()
  override fun lastIndexOf(element: T) = resultList.lastIndexOf(element)
  fun getFirstResult() = resultList.firstOrNull()
  fun getSingleResult() = if (isEmpty()) null else resultList.single()
  fun <T> collectEntries(outputName: String) = resultList
    .map { it as DelegatingResultEntry }
    .filter { it.containsKey(outputName) }
    .map { it[outputName] as T }

  fun getResultList() = resultList.map { (it as DelegatingResultEntry).getEntryMap() }
  fun <T> getSingleEntry(): T? = (getSingleResult() as DelegatingResultEntry?)?.getSingleEntry()
  fun <T : TypedValue> getSingleEntryTyped(): T? = (getSingleResult() as DelegatingResultEntry?)?.getSingleEntryTyped()

  override val size = resultList.size

}

fun <T>asUnmodifiableList(list: MutableList<T>) = Collections.unmodifiableList(list)
