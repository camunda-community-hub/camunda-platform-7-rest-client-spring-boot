package org.camunda.community.rest.client

import feign.form.ContentType
import feign.form.MultipartFormContentProcessor
import feign.form.multipart.Output
import feign.form.spring.SpringFormEncoder
import feign.form.spring.SpringManyMultipartFilesWriter
import feign.form.spring.SpringSingleMultipartFileWriter

/**
 * Camunda needs a different key for each file sent over the API -> enhance key with index.
 */
class KeyChangingSpringManyMultipartFilesWriter(
  private val fileWriter: SpringSingleMultipartFileWriter
) : SpringManyMultipartFilesWriter() {

  companion object {
    @JvmStatic
    fun camundaMultipartFormEncoder(): SpringFormEncoder {
      return object : SpringFormEncoder() {
        init {
          (getContentProcessor(ContentType.MULTIPART) as MultipartFormContentProcessor).addFirstWriter(
            KeyChangingSpringManyMultipartFilesWriter(
              SpringSingleMultipartFileWriter()
            )
          )
        }
      }
    }
  }

  override fun write(output: Output?, boundary: String?, key: String?, value: Any?) {
    // Camunda needs a different key for each file sent over the API -> enhance key with index
    when (value) {
      is Array<*> -> value.forEachIndexed { index, file -> fileWriter.write(output, boundary, "$key$index", file) }
      is Iterable<*> -> value.forEachIndexed { index, file -> fileWriter.write(output, boundary, "$key$index", file) }
      else -> {
        super.write(output, boundary, key, value)
      }
    }
  }
}
