package org.camunda.community.rest.variables.ext

import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.type.TypeFactory


fun Any.constructType(): JavaType = when (this) {
  is Collection<*> if this.javaClass.typeParameters.size == 1 && this.isNotEmpty() -> {
    TypeFactory.defaultInstance().constructCollectionType(this.javaClass, this.first()!!.javaClass)
  }

  is Array<*> if this.javaClass.typeParameters.size == 1 && this.isNotEmpty() -> {
    TypeFactory.defaultInstance().constructArrayType(this.first()!!.javaClass)
  }

  else -> {
    TypeFactory.defaultInstance().constructType(this.javaClass)
  }
}
