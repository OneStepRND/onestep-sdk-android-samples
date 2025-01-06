# [OneStep](https://www.onestep.co/) UIKit for Android

[![Platform](https://img.shields.io/badge/platform-Android-green.svg)](https://github.com/OneStepRND/onestep-sdk-android-samples/tree/main/UiKitSample)
[![Languages](https://img.shields.io/badge/language-Kotlin-orange.svg)](https://github.com/OneStepRND/onestep-sdk-android-samples/tree/main/UiKitSample)
![Commercial License](https://img.shields.io/badge/license-Commercial-green.svg)
![UiKit Version](https://img.shields.io/badge/version-1.1.2-blue.svg)

## Introduction

**OneStep UIKit** for Android is a development kit providing pre-built, customizable UI components to seamlessly integrate OneStep motion intelligence features into new or existing Android applications. Designed to work with the OneStep SDK, the UIKit offers ready-to-use user interfaces for recording, displaying, and summarizing motion data. 

## Main Features

### Recording Flow

The Recording Flow provides a seamless and customizable experience for users to record motion data using the OneStep SDK. It guides users through the necessary steps to perform a motion recording with ease.

![ThemeLight](https://glorious-caboc-cd3.notion.site/image/https%3A%2F%2Fprod-files-secure.s3.us-west-2.amazonaws.com%2F6bffdf7a-2c2e-49a1-9e0f-293395486e36%2F594d95f6-77ce-40d7-8aff-66c548dbeb43%2FScreenshot_2024-09-19_at_10.49.23.png?table=block&id=fff96ca2-482b-81bd-b8ef-e8c0add5ebab&spaceId=6bffdf7a-2c2e-49a1-9e0f-293395486e36&width=2000&userId=&cache=v2)

### Summary Screen

The Summary Screen displays key insights and analytics derived from the recorded motion data. Users can review their performance, receive immediate feedback, and insert additional metadata.

![ThemeLight](https://glorious-caboc-cd3.notion.site/image/https%3A%2F%2Fprod-files-secure.s3.us-west-2.amazonaws.com%2F6bffdf7a-2c2e-49a1-9e0f-293395486e36%2F47fd8337-0fe8-444a-89e9-04a264ece860%2FScreenshot_2024-09-19_at_10.51.24.png?table=block&id=fff96ca2-482b-816d-82c3-d173a5667ec4&spaceId=6bffdf7a-2c2e-49a1-9e0f-293395486e36&width=2000&userId=&cache=v2)

### Carelog

The Carelog screen offers a comprehensive history of the user's collected motion data. It allows users to track their progress over time by viewing past recordings and their associated metrics.

### Permission Flow

The Permission Flow ensures that the app has all the necessary permissions to function properly. It guides users through granting access to motion data and other required permissions in a clear and user-friendly manner.

### Brand Customization

OneStep UIKit provides customization options, enabling you to tailor the UI components to match your brand identity.

## Before getting started

### Requirements

The minimum requirements for OneStep UIKit for Android are:

- API version 26+

### API Keys

Before integrating OneStep UIKit, you need to obtain your API credentials:

- App ID: Your application's unique identifier provided by OneStep.
- API Key: The API key associated with your OneStep account.

You can retrieve these credentials from the OneStep back-office under Developers > Settings.

## Getting started

### Try the sample app

Our sample app has all the core features of OneStep UIKit for Android. Download the app from our GitHub repository to get an idea of what you can build with the actual UIKit before building your own project.

### Use your API key
To start using the OneStep SDK, initialize it in your `Application` class or equivalent entry point of your app:

```kotlin
 OneStep.Builder(
    this.applicationContext,
    apiKey = API_KEY,//"<YOUR-API-KEY-HERE>",
    appId = APP_ID,//"<YOUR-APP-ID-HERE>",
    distinctId = //"<YOUR-USER-DISTINCT-ID>",
    identityVerification = //"<YOUR-IDENTITY-VERIFICATION-SECRET-HERE>", // or null if in development
)
```

### Installation
UIKit for Android can be installed through Maven Central:

Via version catalog:
```bash
oneStep-uikit = { group = "co.onestep.android", name = "uikit", version.ref = "oneStepUiKit" }
```
Via Gradle
```Bash
implementation "co.onestep.android:uikit:$oneStepUiKit"

```
Note: it depends on the [OneStep SDK Core](https://central.sonatype.com/artifact/co.onestep.android/core).

### Permissions
Physical activity permission is required for motion recording.

## UIKit at a glance

UIKit Components are as follows:

| Component                     |Description|
|-------------------------------|---|
| OSTCarelogActivity            |Displays a history of recorded motion data.|
| OSTPermissionFlowActivity     |Manages the permission request flow for motion and location access.|
| OSTMeasurementSummaryActivity |Displays detailed summaries of recorded measurements.|
| OSTRecordingFlowActivity      |Manages the flow for recording motion data.|
| OSTRecordingConfiguration     |Configures the settings for a motion recording session.|
| OSTMeasurementInstructionsData|Provides instructions for users during a motion recording.|
| OSTTheme                      |Customizes the appearance of the UI components.|

Take a look at the [technical documentation](https://glorious-caboc-cd3.notion.site/OneStep-SDK-Core-Android-bbca1bea365f44e7bd12a105f772df93).

## Support

For support, additional information, or to report issues, please contact `shahar@onestep.co`.


