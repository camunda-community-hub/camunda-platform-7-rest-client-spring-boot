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
package org.camunda.community.rest

import org.camunda.community.rest.client.FeignClientConfiguration
import org.camunda.community.rest.config.CamundaRestClientProperties
import org.camunda.community.rest.config.FeignErrorDecoderConfiguration
import org.camunda.community.rest.impl.*
import org.camunda.community.rest.variables.SpinValueMapper
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import


/**
 * Basic configuration of the extension.
 */
@Configuration
@EnableConfigurationProperties(CamundaRestClientProperties::class)
@Import(
  RemoteExternalTaskService::class,
  RemoteHistoryService::class,
  RemoteRepositoryService::class,
  RemoteRuntimeService::class,
  RemoteTaskService::class,
  RemoteDecisionService::class,
  FeignClientConfiguration::class,
  FeignErrorDecoderConfiguration::class,
  SpinValueMapper::class
)
class CamundaRestClientSpringBootExtension


/**
 * Enables the registration of REST client beans.
 */
@Import(CamundaRestClientSpringBootExtension::class)
annotation class EnableCamundaRestClient
