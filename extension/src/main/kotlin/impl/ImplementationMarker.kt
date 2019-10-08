package org.camunda.bpm.extension.feign.impl

import kotlin.reflect.KClass

/**
 * Marker to navigate to implementations.
 */
fun implementedBy(clazz: KClass<*>): Nothing = throw NotImplementedError("Operation implemented in ${clazz.simpleName}.")
