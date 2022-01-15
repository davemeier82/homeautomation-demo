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

package com.github.davemeier82.homeautomation.demo;

import com.github.davemeier82.homeautomation.core.device.Device;
import com.github.davemeier82.homeautomation.core.device.DeviceId;
import com.github.davemeier82.homeautomation.core.device.property.Relay;
import com.github.davemeier82.homeautomation.core.event.DevicePropertyEvent;
import com.github.davemeier82.homeautomation.core.event.MotionDetectedEvent;
import com.github.davemeier82.homeautomation.shelly.device.Shelly1;
import com.github.davemeier82.homeautomation.spring.core.DeviceRegistry;
import com.github.davemeier82.homeautomation.zigbee2mqtt.device.Zigbee2MqttDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;

import static com.github.davemeier82.homeautomation.core.device.DeviceId.deviceIdFromDevice;

@Component
@ConditionalOnProperty(name = "light-controller.enabled", havingValue = "true")
public class LightController {
  private static final Logger log = LoggerFactory.getLogger(LightController.class);

  private final ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
  private final DeviceRegistry deviceRegistry;
  private final DeviceId wardrobeMotionSensorId;
  private final DeviceId wardrobeLightId;
  private final Duration turnOffDelay;

  private ScheduledFuture<?> turnOffLightTask = null;

  public LightController(DeviceRegistry deviceRegistry,
                         @Value("${light-controller.wardrobe-motion-sensor-id}") String wardrobeMotionSensorId,
                         @Value("${light-controller.wardrobe-light-id}") String wardrobeLightId,
                         @Value("${light-controller.turn-off-delay}") Duration turnOffDelay
  ) {
    this.deviceRegistry = deviceRegistry;
    this.wardrobeMotionSensorId = new DeviceId(wardrobeMotionSensorId, Zigbee2MqttDevice.TYPE);
    this.wardrobeLightId = new DeviceId(wardrobeLightId, Shelly1.TYPE);
    log.debug("wardrobeLightId: {} wardrobeMotionSensorId: {}", wardrobeLightId, wardrobeMotionSensorId);
    this.turnOffDelay = turnOffDelay;
    taskScheduler.initialize();
  }

  @EventListener
  public void handleEvent(DevicePropertyEvent event) {
    DeviceId deviceId = deviceIdFromDevice(event.getDeviceProperty().getDevice());
    if (wardrobeMotionSensorId.equals(deviceId) && event instanceof MotionDetectedEvent motionDetectedEvent) {
      scheduleWardrobeLightOff(motionDetectedEvent);
    }
  }

  private void scheduleWardrobeLightOff(MotionDetectedEvent motionDetectedEvent) {
    if (turnOffLightTask != null) {
      turnOffLightTask.cancel(false);
    }
    ZonedDateTime turnOffTime = motionDetectedEvent.getEventTime().plus(turnOffDelay);
    log.debug("schedule turn off for {} at {}", wardrobeLightId, turnOffTime);
    turnOffLightTask = taskScheduler.schedule(() -> {
      log.debug("turn off {}", wardrobeLightId);
      Optional<Device> light = deviceRegistry.getByDeviceId(wardrobeLightId);
      light.ifPresent(device -> {
        Shelly1 shelly1 = (Shelly1) device;
        shelly1.getDeviceProperties().stream()
            .filter(deviceProperty -> deviceProperty instanceof Relay)
            .map(deviceProperty -> (Relay) deviceProperty)
            .findFirst()
            .ifPresent(Relay::turnOff);
      });
    }, turnOffTime.toInstant());
  }
}
