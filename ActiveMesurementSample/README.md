# OneStepSDK

## Versioning

![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)
![API](https://img.shields.io/badge/API-26%2B-brightgreen.svg)
![Alpha Version](https://img.shields.io/badge/beta-0.8.0-red.svg)

OneStepSDK is an Android library for collecting sensor data and analyzing it to produce a walk score, quantifying the nuances of walking patterns.

## Features

- Efficient sensor data collection
- Analytical walk score generation
- Easy integration and configuration
- Configurable settings for tailored data collection

## Getting Started

To include OneStepSDK in your project, reach out to `ziv@onestep.co` for more details and access to the SDK.

### Installation

```kotlin
OneStep.Builder(
        this.applicationContext,
        API_KEY,
        APP_ID,
        distinctId,
        IDENTITY_VERIFICATION_SECRET,
).build()
```

### Logout
```kotlin
OneStep.disconnect()
```

## Service Integration

To fully utilize the OneStepSDK, especially for features that require a foreground service, it's crucial to declare the `OneStepRecordingService` in your app's `AndroidManifest.xml`. This service is responsible for managing foreground processes and displaying ongoing notifications, which are essential for continuous data recording.

### Adding the Service to Your Manifest

Include the services declaration within the `<application>` tag of your `AndroidManifest.xml`:

```xml
<service
        android:name="co.onestep.android.core.external.services.OneStepRecordingService"
        android:foregroundServiceType="health" />
<service
        android:name="co.onestep.android.core.external.services.OneStepForegroundService"
        android:foregroundServiceType="health" />
```

Customize the foreground notification by adding a `NotificationConfig` object to the initilize method:
```
.setInAppNotificationConfig(
            NotificationConfig(
                title = "Recording your activity.",
                text = "Recording performed by SDK",
                icon = R.drawable.notification_icon,
            ),
        )
```

## pro-guard rule requirment
In order to facilitate the SDK to connect to the OneStep servers and retreive walk analysis,  
add the following rules to you `proguard-rules.pro` file:

`-keep class co.onestep.android.core.** { *; } `

## API Reference

Here's how you can use the main features of the SDK:

- **Initialize SDK**: initialize(appId: String, configuration: SdkConfiguration):
  Initializes the SDK with the specified application ID and configuration settings.

- **disconnect**: disconnect():
  clears all OneStep data, tokens and dependencies.

- **Retrieve measurements data**: getMotionMeasurements(request: TimedDataRequest? = null): List<MotionMeasurement>
  Query for Motion measurements captured by the SDK

- **Recording Service**: getRecordingService(): SimpleRecorder:
  Provides access to the recording service for custom data recording workflows.

## Support

For support, additional information, or to report issues, contact `ziv@onestep.co`.