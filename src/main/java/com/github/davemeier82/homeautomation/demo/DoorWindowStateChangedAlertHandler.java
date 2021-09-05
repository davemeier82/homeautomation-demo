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
import com.github.davemeier82.homeautomation.spring.core.event.WindowStateChangedSpringEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class DoorWindowStateChangedAlertHandler {

  private static final Logger log = LoggerFactory.getLogger(DoorWindowStateChangedAlertHandler.class);
  private final PushNotificationService pushNotificationService;

  public DoorWindowStateChangedAlertHandler(PushNotificationService pushNotificationService) {
    this.pushNotificationService = pushNotificationService;
  }

  @EventListener
  public void handleEvent(WindowStateChangedSpringEvent event) {
    String displayName = event.getWindow().getDevice().getDisplayName();
    if (event.isOpen().getValue()) {
      log.debug("'{}' was opened", displayName);
      pushNotificationService.sendTextMessage(displayName, displayName + " was opened");
    } else {
      log.debug("'{}' was closed", displayName);
      pushNotificationService.sendTextMessage(displayName, displayName + " was closed");
    }
  }

}
