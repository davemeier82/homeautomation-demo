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

package io.github.davemeier82.homeautomation.demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.davemeier82.homeautomation.core.device.mqtt.MqttDeviceFactory;
import io.github.davemeier82.homeautomation.core.event.EventPublisher;
import io.github.davemeier82.homeautomation.core.event.factory.EventFactory;
import io.github.davemeier82.homeautomation.core.mqtt.MqttClient;
import io.github.davemeier82.homeautomation.hivemq.HiveMqMqttClient;
import io.github.davemeier82.homeautomation.instar.InstarMqttDeviceFactory;
import io.github.davemeier82.homeautomation.shelly.ShellyMqttDeviceFactory;
import io.github.davemeier82.homeautomation.spring.core.DeviceRegistry;
import io.github.davemeier82.homeautomation.spring.core.OnFirstEventMqttDeviceLoader;
import io.github.davemeier82.homeautomation.zigbee2mqtt.Zigbee2MqttDeviceFactory;
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
  MqttDeviceFactory zigbee2MqttDeviceFactory(EventPublisher eventPublisher,
                                             EventFactory eventFactory,
                                             MqttClient mqttClient,
                                             ObjectMapper objectMapper
  ) {
    return new Zigbee2MqttDeviceFactory(eventPublisher, eventFactory, mqttClient, objectMapper);
  }

  @Bean
  MqttDeviceFactory instarMqttDeviceFactory(EventPublisher eventPublisher,
                                            EventFactory eventFactory,
                                            MqttClient mqttClient,
                                            ObjectMapper objectMapper
  ) {
    return new InstarMqttDeviceFactory(eventPublisher, eventFactory, mqttClient, objectMapper);
  }

  @Bean
  OnFirstEventMqttDeviceLoader onFirstEventMqttDeviceLoader(List<MqttDeviceFactory> mqttDeviceFactories,
                                                            DeviceRegistry deviceRegistry,
                                                            EventPublisher eventPublisher,
                                                            EventFactory eventFactory
  ) {
    return new OnFirstEventMqttDeviceLoader(mqttDeviceFactories, deviceRegistry, eventPublisher, eventFactory);
  }

  @Bean
  MqttClient mqttClient(EventFactory eventFactory,
                        EventPublisher eventPublisher,
                        @Value("${hivemq.server.host}") String serverHost,
                        @Value("${hivemq.server.port:1883}") int serverPort,
                        @Value("${hivemq.server.username:#{null}}") String username,
                        @Value("${hivemq.server.password:#{null}}") String password
  ) {
    return new HiveMqMqttClient(eventFactory, eventPublisher, serverHost, serverPort, username, password);
  }

}
