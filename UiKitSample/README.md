# [OneStep](https://www.onestep.co/) UIKit for Android

[![Platform](https://img.shields.io/badge/platform-Android-green.svg)](https://github.com/OneStepRND/onestep-sdk-android-samples/tree/main/UiKitSample)
[![Languages](https://img.shields.io/badge/language-Kotlin-orange.svg)](https://github.com/OneStepRND/onestep-sdk-android-samples/tree/main/UiKitSample)
![Commercial License](https://img.shields.io/badge/license-Commercial-green.svg)
![UiKit](https://img.shields.io/badge/uikit-2.3.0-blue.svg)

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

| Component | Version |
|---|---|
| Android API (min) | 26 |
| Android API (target / compile) | 36 |
| Android Gradle Plugin | 9.0.0 (Gradle 9.1.0, JDK 17+) |
| Kotlin | 2.2.10 |
| Compose BOM | 2026.03.00 |
| OneStep UIKit | 2.3.0 |

All three samples in this repo use the same toolchain. Bump versions in every sample's `gradle/libs.versions.toml` together.

### API Keys

Before integrating OneStep UIKit, obtain your API credentials from the OneStep back-office under
Developers > Settings (reach out to `shahar@onestep.co` for access).

This sample keeps credentials **out of source** — they are read from `local.properties` (gitignored)
and exposed via `BuildConfig`. Add these lines to `local.properties`:

```properties
onestep.clientToken=<YOUR-CLIENT-TOKEN>
onestep.customerPatientId=<YOUR-USER-DISTINCT-ID>
# Precomputed identity-verification digest (leave blank to skip identity verification):
onestep.identityVerification=<HMAC-DIGEST>
```

`onestep.identityVerification` is **not** the secret — it is the hex-encoded
`HMAC_SHA256(key = <identity-verification secret>, message = <customerPatientId>)`, computed on your
server (the secret must never ship in the app). For local testing:

```bash
printf '%s' "<customerPatientId>" | openssl dgst -sha256 -hmac "<SECRET>" -hex
```

## Getting started

### Try the sample app

Our sample app has all the core features of OneStep UIKit for Android. Download the app from our GitHub repository to get an idea of what you can build with the actual UIKit before building your own project.

### Use your client token

Initialize the SDK once in `Application.onCreate`, then identify the current user. `setPatient` is a
suspend function returning an `OSTResult<Unit>` you should branch on (see `UiKitSampleApplication`).

```kotlin
// 1. Initialize once in Application.onCreate()
OneStep.initialize(
    application = this,
    onAuthLost = { error -> Log.w("OneStep", "Auth lost: ${error.message}") },
).onSuccess { oneStep ->
    // 2. Identify the user (call from a coroutine — setPatient is suspend).
    //    Credentials come from BuildConfig, sourced from local.properties.
    oneStep.setPatient(
        apiKey = BuildConfig.CLIENT_TOKEN,
        customerPatientId = BuildConfig.CUSTOMER_PATIENT_ID,
        identityVerification = BuildConfig.IDENTITY_VERIFICATION.ifBlank { null },
    ).onError { error -> Log.e("OneStep", "identify failed: ${error.cause.message}") }
}
```

### Installation
UIKit for Android can be installed through Maven Central:

Via version catalog (`gradle/libs.versions.toml`):
```toml
[versions]
oneStepUiKit = "2.3.0"

[libraries]
oneStep-uikit = { group = "co.onestep.android", name = "uikit", version.ref = "oneStepUiKit" }
```
Then in `app/build.gradle.kts`:
```kotlin
implementation(libs.oneStep.uikit)
// or, without a version catalog:
implementation("co.onestep.android:uikit:2.3.0")
```
Note: it depends on the [OneStep SDK Core](https://central.sonatype.com/artifact/co.onestep.android/core).

### Permissions
Physical activity permission (`ACTIVITY_RECOGNITION`) is required for motion recording. Launch
`OSTPermissionFlowActivity` to let the UIKit request it with its own screens.

## Recording flows in this sample

Each button on the main screen launches `OSTRecordingFlowActivity` with one
`OSTRecordingConfiguration` factory. The mapping lives in
[`MainActivity.kt`](app/src/main/java/com/onestep/onestepuikitsample/MainActivity.kt) →
`toConfiguration()`, and the list in
[`RecordingFlow.kt`](app/src/main/java/com/onestep/onestepuikitsample/screens/RecordingFlow.kt).

| Button | Factory |
|---|---|
| Walk recording | `defaultWalk(context)` |
| Timed up and go | `tug(context)` |
| Sit to stand | `sts(context)` |
| Knee extension test | `romExt(context)` |
| Balance test | `balanceTest(context)` |
| Dual task | `dualTaskSubtract(context, instructions)` |
| 6-minute walk test | `sixMinuteWalk(context)` |
| 2-minute walk test | `twoMinuteWalk(context)` |
| Stairs | `stairs(context)` |
| Generic recording | `genericRecording()` — uploaded, never analyzed (SDK 2.1.4+) |

```kotlin
startActivity(
    OSTRecordingFlowActivity.buildIntent(
        context,
        config = OSTRecordingConfiguration.sixMinuteWalk(context),
        customMetadata = mapOf("app" to "uikit sample app"),
    ),
)
```

### Configuration options added since 2.0

- `onInstructionsRequested: (OSTActivityType) -> Unit` (2.2.2) — show your own instructions UI when
  the user taps "View instructions". Capture no `Activity` or `Context` in it.
- `title` (2.1.4) — names a Generic Recording. Longer than 30 characters is truncated.
- `tagFields` (2.3.0) — renders your backend tag catalog as pre- and post-recording questions.
  It replaces `preRecordingQuestions`, `postTaggingData`, `showPreRecordingAssistiveDeviceSelection`,
  `showPreRecordingFootwearSelection` and `balance`, which are deprecated and will be removed in
  the next release.

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


