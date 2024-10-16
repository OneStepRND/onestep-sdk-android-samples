# OneStepSDK

## Versioning

![API](https://img.shields.io/badge/API-26%2B-brightgreen.svg)
![Core Version](https://img.shields.io/badge/core-1.0.10-red.svg)
![UiKit Version](https://img.shields.io/badge/uikit-1.0.1-blue.svg)

This repository contains sample Android applications demonstrating how to integrate and use the OneStep SDK for motion analysis. The apps showcase how to record motion data, analyze it, and display the results within your own application.

## Features

- Real-time motion recording and analysis
- Data enrichment with metadata, norms, and insights
- Background monitoring
- Customizable UI components (UIKit)

## Getting Started

The OneStep SDK is currently available exclusively to our customers. We provide API keys and technical documentation through our developer portals. If you're a OneStep customer, please reach out to your account representative or access your developer portal for the necessary resources and detailed instructions.

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
        android:name="co.onestep.android.core.external.services.OSTRecordingService"
        android:foregroundServiceType="health" />
<service
        android:name="co.onestep.android.core.external.services.OSTForegroundService"
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

## Support

For support, additional information, or to report issues, contact `support@onestep.co`.
