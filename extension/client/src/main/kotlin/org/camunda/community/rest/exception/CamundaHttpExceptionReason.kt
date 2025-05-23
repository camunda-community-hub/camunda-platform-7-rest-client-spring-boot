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
package org.camunda.community.rest.exception

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}
/**
 * Exception reason.
 * @constructor constructs the reason.
 * @param clazz class name for exception reason.
 * @param message text.
 */
internal data class CamundaHttpExceptionReason(
  @JsonProperty("type")
  val clazz: String,
  @JsonProperty("message")
  val message: String,
  @JsonProperty("code")
  val code: String?
) {
  companion object {
    private val FULL_QUALIFIED_CLASS_NAME = "(([a-zA-Z_\$][a-zA-Z\\d_\$]*\\.)*[a-zA-Z_\$][a-zA-Z\\d_\$]*): (.*)".toRegex()

    /**
     * Factory method to construct a reason from string response of the server.
     * @param message response.
     * @return instance or <code>null</code>.
     */
    fun fromMessage(message: String): CamundaHttpExceptionReason? {
      val match = FULL_QUALIFIED_CLASS_NAME.find(message)
      return if (match != null) {
        val (clazz, _, remaining) = match.destructured
          CamundaHttpExceptionReason(clazz = clazz, message = remaining, code = null)
      } else {
        logger.debug { "REST-CLIENT-003 Could not parse Camunda exception from server response: \n$message" }
        null
      }
    }
  }
}
