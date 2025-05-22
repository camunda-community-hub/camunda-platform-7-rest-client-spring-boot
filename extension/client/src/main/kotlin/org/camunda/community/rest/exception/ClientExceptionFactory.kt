package org.camunda.community.rest.exception

/**
 * Factory responsible for creation exception of type <T>.
 * @param T type of exception to create.
 */
@FunctionalInterface
interface ClientExceptionFactory<T: Exception> {
  /**
   * Creates an exception.
   * @param message message of the exception.
   * @param cause optional reason.
   * @return exception instance.
   */
  fun create(message: String, cause: Throwable? = null): T
}
