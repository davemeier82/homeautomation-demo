# homeautomation-demo

## Introduction

This is an application to show the usage of the homeautomation framework with Spring Boot.

Features:

* Integration of MqTT Device (Shelly, Zigbee2mqtt, Weewx, Instar and custom devices)
* Push notification (Pushover, PushBullet or custom)
* All events can get stored in an Influx 2 database to visualize with Grafana
* Events are getting published to MqTT broker for Frontend or third party software
* Act upon InfluxDB data (e.g. send push notification if washing maschine starts)
* REST interface for configuration and triggering of events (e.g. toggle light)

### Dependency management

Add the following bom to the dependency management:

```xml

<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>io.github.davemeier82.homeautomation</groupId>
            <artifactId>homeautomation-bom</artifactId>
            <version>${homeautomation-bom.version}</version>
            <scope>import</scope>
            <type>pom</type>
        </dependency>
    </dependencies>
</dependencyManagement>
```

## Extensions

The framework provides different extensions that are explained in the corresponding README.ms files.

| Extension   | Description                                                         | README.md                                                                                      |
|-------------|---------------------------------------------------------------------|------------------------------------------------------------------------------------------------|
| spring-core | This is the main extension used for the Spring Boot integration     | [Readme](https://github.com/davemeier82/homeautomation-spring-core/blob/main/README.md)        |
| spring-rest | This extension adds REST endpoints for management and configuration | [Readme](https://github.com/davemeier82/homeautomation-spring-rest/blob/main/README.md)        |
| hivemq      | hivemq mqtt client extension                                        | [Readme](https://github.com/davemeier82/homeautomation-spring-hivemq/blob/main/README.md)      |
| shelly      | Extension for Shelly.cloud devices                                  | [Readme](https://github.com/davemeier82/homeautomation-spring-shelly/blob/main/README.md)      |
| zigbee2mqtt | Extension for Zigbee2mqtt devices                                   | [Readme](https://github.com/davemeier82/homeautomation-spring-zigbee2mqtt/blob/main/README.md) |
| instar      | Extension for Instar cameras                                        | [Readme](https://github.com/davemeier82/homeautomation-spring-instar/blob/main/README.md)      |
| weewx       | Extension for Weewex weather stations                               | [Readme](https://github.com/davemeier82/homeautomation-spring-weewx/blob/main/README.md)       |
| influxdb2   | Extension for InfluxDB 2 database integration                       | [Readme](https://github.com/davemeier82/homeautomation-spring-influxdb2/blob/main/README.md)   |

## Events

Supported events see https://github.com/davemeier82/homeautomation-spring-core/blob/main/README.md

### Examples

Send push notification when a lightning has struck in less than 15km distance (max every hour).

```java

@Component
public class LightningController {

    private final PushNotificationService pushNotificationServices;
    private OffsetDateTime lastLightning = OffsetDateTime.MIN;

    public LightningController(Set<PushNotificationService> pushNotificationServices) {
        this.pushNotificationServices = pushNotificationServices.stream().filter(s -> s.getServiceIds().contains("david")).findAny().orElseThrow();
    }

    @EventListener
    public void handleEvent(LightningDistanceChangedEvent event) {
        if (event.getKm() != null && event.getKm() < 15 && Duration.between(lastLightning, event.getNewTimestamp()).toHours() > 1) {
            pushNotificationServices.sendTextMessageToServiceWithId("myID", "Lightning stroke", "Lightning has struck " + event.getKm() + "km away.");
            lastLightning = event.getNewTimestamp();
        }
    }
}
```

Turn on light when Motion got detected

```java

import io.github.davemeier82.homeautomation.spring.core.persistence.entity.DevicePropertyId;

@Component
public class LightningController {

    private final RelayDevicePropertyController relayDevicePropertyController;
    private final DevicePropertyId MY_MOTION_SENSOR_ID;
    private final DevicePropertyId MY_LIGHT_ID;

    // Constructor

    @EventListener
    public void turnOnLightOnMotion(MotionChangedEvent event) {
        if (event.getDevicePropertyId().equals(MY_MOTION_SENSOR_ID) && event.motionDetected()) {
            relayDevicePropertyController.turnOn(MY_LIGHT_ID);
        }
    }
}
```