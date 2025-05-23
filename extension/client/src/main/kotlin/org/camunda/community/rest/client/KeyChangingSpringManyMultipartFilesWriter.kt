/*-
 * #%L
 * camunda-platform-7-rest-client-spring-boot
 * %%
 * Copyright (C) 2019 Camunda Services GmbH
 * %%
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 *  under one or more contributor license agreements. See the NOTICE file
 *  distributed with this work for additional information regarding copyright
 *  ownership. Camunda licenses this file to you under the Apache License,
 *  Version 2.0; you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * #L%
 */
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
