/*
 * Copyright 2021-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.davemeier82.homeautomation.demo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

  private final String[] allowedOrigins;
  private final String[] allowedHeaders;
  private final String[] allowedMethods;
  private final String[] exposedHeaders;

  public WebMvcConfig(
      @Value("${allowedOrigins:null}") String[] allowedOrigins,
      @Value("${allowedHeaders:*}") String[] allowedHeaders,
      @Value("${allowedMethods:*}") String[] allowedMethods,
      @Value("${exposedHeaders:*}") String[] exposedHeaders
  ) {
    this.allowedOrigins = allowedOrigins;
    this.allowedHeaders = allowedHeaders;
    this.allowedMethods = allowedMethods;
    this.exposedHeaders = exposedHeaders;
  }

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/**")
            .allowedOriginPatterns(allowedOrigins)
        .allowedMethods(allowedMethods)
        .allowedHeaders(allowedHeaders)
        .exposedHeaders(exposedHeaders);
  }
}