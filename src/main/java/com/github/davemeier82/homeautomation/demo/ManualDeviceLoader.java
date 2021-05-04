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

import com.github.davemeier82.homeautomation.core.device.Device;
import com.github.davemeier82.homeautomation.core.device.mqtt.MqttSubscriber;
import com.github.davemeier82.homeautomation.core.event.EventFactory;
import com.github.davemeier82.homeautomation.core.event.EventPublisher;
import com.github.davemeier82.homeautomation.instar.device.InstarMqttCamera;
import com.github.davemeier82.homeautomation.spring.core.event.MqttClientConnectedSpringEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ManualDeviceLoader {

  private final EventPublisher eventPublisher;
  private final EventFactory eventFactory;

  public ManualDeviceLoader(EventPublisher eventPublisher, EventFactory eventFactory) {
    this.eventPublisher = eventPublisher;
    this.eventFactory = eventFactory;
  }

  @EventListener
  public void loadDevices(MqttClientConnectedSpringEvent event) {
    List<Device> devices = new ArrayList<>();
    devices.add(new InstarMqttCamera("10D1DC21757F", "Kamera Carport", eventPublisher, eventFactory));
    devices.stream()
        .filter(device -> device instanceof MqttSubscriber)
        .map(device -> (MqttSubscriber) device)
        .forEach(device -> event.getClient().subscribe(device.getTopic(), device::processMessage));
  }
}
