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

import io.github.davemeier82.homeautomation.core.device.DeviceId;
import io.github.davemeier82.homeautomation.core.device.property.DevicePropertyId;
import io.github.davemeier82.homeautomation.core.event.DevicePropertyEvent;
import io.github.davemeier82.homeautomation.core.event.MotionUpdatedEvent;
import io.github.davemeier82.homeautomation.core.event.RelayStateChangedEvent;
import io.github.davemeier82.homeautomation.shelly.device.ShellyDeviceType;
import io.github.davemeier82.homeautomation.shelly.device.property.controller.ShellyRelayDevicePropertyController;
import io.github.davemeier82.homeautomation.zigbee2mqtt.device.Zigbee2MqttDeviceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.concurrent.ScheduledFuture;

@Component
@ConditionalOnProperty(name = "light-controller.enabled", havingValue = "true")
public class LightController {
  private static final Logger log = LoggerFactory.getLogger(LightController.class);

  private final ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
  private final DeviceId wardrobeMotionSensorId;
  private final DeviceId wardrobeLightId;
  private final Duration turnOffDelay;
  private final ShellyRelayDevicePropertyController shellyRelayDevicePropertyController;
  private ScheduledFuture<?> turnOffWardrobeLightTask = null;

  public LightController(@Value("${light-controller.wardrobe-motion-sensor-id}") String wardrobeMotionSensorId,
                         @Value("${light-controller.wardrobe-light-id}") String wardrobeLightId,
                         @Value("${light-controller.turn-off-delay}") Duration turnOffDelay,
                         @Lazy ShellyRelayDevicePropertyController shellyRelayDevicePropertyController
  ) {
    this.wardrobeMotionSensorId = new DeviceId(wardrobeMotionSensorId, Zigbee2MqttDeviceType.ZIGBEE_2_MQTT);
    this.wardrobeLightId = new DeviceId(wardrobeLightId, ShellyDeviceType.SHELLY_1);
    this.shellyRelayDevicePropertyController = shellyRelayDevicePropertyController;
    log.debug("wardrobeLightId: {} wardrobeMotionSensorId: {}", wardrobeLightId, wardrobeMotionSensorId);
    this.turnOffDelay = turnOffDelay;
    taskScheduler.initialize();
  }

  @EventListener
  public void handleEvent(DevicePropertyEvent<?> event) {
    DeviceId deviceId = event.getDevicePropertyId().deviceId();
    switch (event) {
      case MotionUpdatedEvent motionUpdatedEvent when wardrobeMotionSensorId.equals(deviceId) -> {
        if (motionUpdatedEvent.motionDetected()) {
          scheduleWardrobeLightOff(motionUpdatedEvent.getNewTimestamp().plus(turnOffDelay));
        }
      }
      case RelayStateChangedEvent relayStateChangedEvent when wardrobeLightId.equals(deviceId) -> {
        if (relayStateChangedEvent.isOn()) {
          scheduleWardrobeLightOff(relayStateChangedEvent.getNewTimestamp().plus(turnOffDelay));
        }
      }
      default -> {
      }
    }
  }

  private void scheduleWardrobeLightOff(OffsetDateTime turnOffTime) {
    if (turnOffWardrobeLightTask != null) {
      turnOffWardrobeLightTask.cancel(false);
    }
    log.debug("schedule turn off for {} at {}", wardrobeLightId, turnOffTime);
    turnOffWardrobeLightTask = taskScheduler.schedule(() -> {
      log.debug("turn off {}", wardrobeLightId);
      shellyRelayDevicePropertyController.turnOff(new DevicePropertyId(wardrobeLightId, "0"));
    }, turnOffTime.toInstant());
  }
}
