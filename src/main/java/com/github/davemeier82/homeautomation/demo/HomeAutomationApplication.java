/*
 * Copyright 2021-2021 the original author or authors.
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

package com.github.davemeier82.homeautomation.demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.davemeier82.homeautomation.core.device.mqtt.MqttDeviceFactory;
import com.github.davemeier82.homeautomation.core.event.EventFactory;
import com.github.davemeier82.homeautomation.core.event.EventPublisher;
import com.github.davemeier82.homeautomation.core.mqtt.MqttClient;
import com.github.davemeier82.homeautomation.hivemq.HiveMqMqttClient;
import com.github.davemeier82.homeautomation.shelly.ShellyMqttDeviceFactory;
import com.github.davemeier82.homeautomation.spring.core.DeviceRegistry;
import com.github.davemeier82.homeautomation.spring.core.OnFirstEventMqttDeviceLoader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;

@SpringBootApplication
public class HomeAutomationApplication {

  public static void main(String[] args) {
    SpringApplication.run(HomeAutomationApplication.class, args);
  }

  @Bean
  MqttDeviceFactory shellyMqttDeviceFactory(EventPublisher eventPublisher,
                                            EventFactory eventFactory,
                                            MqttClient mqttClient,
                                            ObjectMapper objectMapper
  ) {
    return new ShellyMqttDeviceFactory(eventPublisher, eventFactory, mqttClient, objectMapper);
  }

  @Bean
  OnFirstEventMqttDeviceLoader onFirstEventMqttDeviceLoader(List<MqttDeviceFactory> mqttDeviceFactories,
                                                            MqttClient mqttClient,
                                                            DeviceRegistry deviceRegistry
  ) {
    return new OnFirstEventMqttDeviceLoader(mqttDeviceFactories, mqttClient, deviceRegistry);
  }

  @Bean
  MqttClient mqttClient(EventFactory eventFactory,
                        EventPublisher eventPublisher,
                        @Value("${hivemq.server.host}") String serverHost,
                        @Value("${hivemq.server.port:1883}") int serverPort
  ) {
    return new HiveMqMqttClient(eventFactory, eventPublisher, serverHost, serverPort);
  }
}
