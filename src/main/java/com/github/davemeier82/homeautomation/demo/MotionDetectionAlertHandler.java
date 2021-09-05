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

import com.github.davemeier82.homeautomation.core.PushNotificationService;
import com.github.davemeier82.homeautomation.core.device.DeviceId;
import com.github.davemeier82.homeautomation.spring.core.event.MotionDetectedSpringEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.concurrent.ConcurrentHashMap;

import static com.github.davemeier82.homeautomation.core.device.DeviceId.deviceIdFromDevice;

@Component
public class MotionDetectionAlertHandler {
  private static final Logger log = LoggerFactory.getLogger(MotionDetectionAlertHandler.class);
  private final PushNotificationService pushNotificationService;
  private final ConcurrentHashMap<DeviceId, ZonedDateTime> lastNotificationSent = new ConcurrentHashMap<>();
  private final Duration debounceTime;

  public MotionDetectionAlertHandler(PushNotificationService pushNotificationService,
                                     @Value("${motion-detection.alert.debounce-time:120s}") Duration debounceTime
  ) {
    this.pushNotificationService = pushNotificationService;
    this.debounceTime = debounceTime;
  }

  @EventListener
  public void handleEvent(MotionDetectedSpringEvent event) {
    DeviceId deviceId = deviceIdFromDevice(event.getSensor().getDevice());
    ZonedDateTime lastNotification = lastNotificationSent.get(deviceId);
    if (lastNotification == null || Duration.between(lastNotification, ZonedDateTime.now()).compareTo(debounceTime) > 0) {
      String displayName = event.getSensor().getDevice().getDisplayName();
      log.debug("Device '{}' detected motion", displayName);
      pushNotificationService.sendTextMessage("Motion detected", "Motion detected by device: " + displayName);
      lastNotificationSent.put(deviceId, ZonedDateTime.now());
    }
  }
}
