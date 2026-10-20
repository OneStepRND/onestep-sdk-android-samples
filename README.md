# OneStepSDK

## Versioning

![API](https://img.shields.io/badge/API-26%2B-brightgreen.svg)
![Commercial License](https://img.shields.io/badge/license-Commercial-green.svg)
![Core](https://img.shields.io/badge/core-2.3.0-red.svg)
![UiKit](https://img.shields.io/badge/uikit-2.3.0-blue.svg)

This repository contains sample Android applications demonstrating how to integrate and use the OneStep SDK for motion analysis. The apps showcase how to record motion data, analyze it, and display the results within your own application.

📖 **Full documentation:** [OneStep Collect for Android](https://glorious-caboc-cd3.notion.site/onestep-collect-for-android)

## Samples in this repo

Each sample is a standalone Gradle project with its own README (setup, keys, and what it demonstrates):

| Sample | Demonstrates |
|---|---|
| [`ActiveMesurementSample`](ActiveMesurementSample/README.md) | On-demand ("active") measurement — record a walk and get a walk score. |
| [`BackgroundMonitoringSample`](BackgroundMonitoringSample/README.md) | Background activity monitoring, daily summaries, and foreground-service notification styles. |
| [`UiKitSample`](UiKitSample/README.md) | Drop-in OneStep **UIKit** screens (recording flow, summary, carelog, permission flow). |

All three follow the same setup pattern: credentials live in a gitignored `local.properties` and are exposed via `BuildConfig` (see each sample's README), so no keys are committed.

## Features

- Real-time motion recording and analysis
- Data enrichment with metadata, norms, and insights
- Background monitoring
- Customizable UI components (UIKit)

## Getting Started

The OneStep SDK is currently available exclusively to our customers. We provide API keys and technical documentation through our developer portals. If you're a OneStep customer, please reach out to your account representative or access your developer portal for the necessary resources and detailed instructions.

### Requirements

| Component | Version |
|---|---|
| Android API (min) | 26 |
| Android API (target / compile) | 36 |
| Android Gradle Plugin | 9.0.0 (Gradle 9.1.0, JDK 17+) |
| Kotlin | 2.2.10 |
| Compose BOM | 2026.03.00 |
| OneStep SDK (core / uikit) | 2.3.0 |

All three samples use the same toolchain. Each sample has its own `gradle/libs.versions.toml`, so
bump versions in all three together. The samples use AGP 9 defaults: built-in Kotlin (no
`org.jetbrains.kotlin.android` plugin, only the Compose compiler plugin) and the new DSL, with no
compatibility opt-outs in `gradle.properties`. You can copy the build files into an AGP 9 project
as-is.

### Installation

```kotlin
// app/build.gradle.kts — pick one. UIKit includes core.
implementation("co.onestep.android:core:2.3.0")
implementation("co.onestep.android:uikit:2.3.0")
```

`OneStep.initialize` returns an `OSTResult<OneStep>` — the success value is the SDK instance you use for everything else. Hold on to it (e.g. on your `Application`) and bind a user via `setPatient`, which is suspend and returns an `OSTResult`.

```kotlin
class MyApplication : Application() {

    lateinit var oneStepSdk: OneStep
        private set

    override fun onCreate() {
        super.onCreate()
        OneStep.initialize(
            application = this,
            onAuthLost = { error ->
                // Called when the session expires (401/403)
                Log.w("OneStep", "Auth lost: ${error.message}")
            },
        ).onSuccess { oneStep ->
            oneStepSdk = oneStep
        }.onError { error ->
            Log.e("OneStep", "init failed: ${error.cause.message}")
        }
    }

    suspend fun connectUser() {
        oneStepSdk.setPatient(
            apiKey = "<YOUR-CLIENT-TOKEN>",
            customerPatientId = "<YOUR-USER-DISTINCT-ID>",
            // NOT the secret — the hex HMAC_SHA256(secret, customerPatientId) digest, computed
            // server-side. Pass null to skip identity verification. (In the samples this comes
            // from local.properties via BuildConfig.)
            identityVerification = null,
            userAttributes = {
                // Optional — set atomically with identification
                withSex(OSTUserAttributes.Sex.MALE)
            },
        ).onError { error ->
            // error.cause.type is OSTError.Type (InvalidClientToken, NetworkError, …)
            Log.e("OneStep", "setPatient failed: ${error.cause.type} - ${error.cause.message}")
        }
    }
}
```

### Observe SDK state

`identificationState` is the single source of truth for whether a patient is bound.

```kotlin
lifecycleScope.launch {
    oneStepSdk.identificationState.collect { state ->
        when (state) {
            OSTIdentificationState.Unidentified -> { /* SDK is initialized, no user bound */ }
            is OSTIdentificationState.Identified -> { /* state.patientId is identified */ }
            is OSTIdentificationState.Lost -> { /* state.cause describes the loss (401/403, expired, …) */ }
        }
    }
}
```

### Logout

```kotlin
oneStepSdk.clearPatient()
```

`clearPatient()` clears credentials, stops monitoring, and transitions `identificationState` back to `Unidentified`. SDK initialization is preserved — you don't need to call `initialize()` again before the next `setPatient()`.

## Upgrading from 2.0.x to 2.3.0

Typical integrations (initialize, `setPatient`, MotionLab, Monitoring, UIKit flows) compile
against 2.3.0 without edits. A few narrow APIs did change: see the breaking notes for 2.0.1 (Static
Balance metadata keys, `analyticsName` values), 2.1.2 (`DataDogEvents` removed) and 2.1.3
(`recordingSavedScreen` callbacks are `suspend`) in the SDK changelog. Then check these points:

1. **Check the result of `motionLab.start()`.** It now returns an `OSTResult` error instead of
   throwing for a missing `ACTIVITY_RECOGNITION` permission (`MissingPermission`) or a duration
   over `RECORDER_LIMIT_MILLIS` (`InvalidArgument`). A `try`/`catch` around `start()` no longer sees
   these. If you ignore the result, your UI shows a recording that never began. `addMarker()`
   returns `InvalidState` when the recording already ended.
2. **Remove workarounds that refresh the monitoring notification.** `setCustomMonitoringNotification`
   now repaints the live notification itself.
3. **Rebuild prebuilt artifacts and update test fakes.** Several constructors gained parameters
   (`OSTRecordingConfiguration`, `OSTCustomNotificationConfig`, `OSTUserInputMetaData`), so a
   binary built against 2.0.x must be rebuilt. A fake that implements `OSTMotionLab` must add
   `currentRecordingWindow`, `rescheduleAutoStop`, `uploadWithoutAnalysis`,
   `setCustomRecordingNotification` and the `balanceScore` parameter of
   `selfReportMotionMeasurement`. A fake `OSTMonitoring` must add
   `setMonitoringNotificationDismissible` and `isMonitoringNotificationDismissible`.
4. **Plan for the UIKit deprecations.** `preRecordingQuestions`, `postTaggingData`,
   `showPreRecordingAssistiveDeviceSelection`, `showPreRecordingFootwearSelection` and `balance` on
   `OSTRecordingConfiguration` are replaced by `tagFields` and will be removed in the next release.

New APIs since 2.0, and where this repo shows them:

| API | Since | Sample |
|---|---|---|
| `motionLab.currentRecordingWindow` — the SDK's recording clock | 2.1.0 | ActiveMesurementSample |
| `motionLab.setCustomRecordingNotification { … }` | 2.2.0 | ActiveMesurementSample |
| `monitoring.setMonitoringNotificationDismissible(…)` | 2.2.0 | BackgroundMonitoringSample |
| `custom(title = …, text = …)` monitoring notification copy | 2.2.2 | BackgroundMonitoringSample |
| `OSTActivityType.GENERIC_RECORDING`, `motionLab.uploadWithoutAnalysis()` | 2.1.4 | UiKitSample (`genericRecording()`) |
| `motionLab.rescheduleAutoStop(…)` | 2.1.2 | — |
| `OSTRecordingConfiguration.onInstructionsRequested`, `tagFields` | 2.2.2 / 2.3.0 | — (see UiKitSample README) |

UiKitSample now also launches the 6-minute walk, 2-minute walk and Stairs flows, which exist since
2.0. For the full list, see the SDK changelog and the
[documentation](https://glorious-caboc-cd3.notion.site/onestep-collect-for-android).

## Support

For SDK access, credentials, support, or to report an issue, contact `shahar@onestep.co`.

## License

The OneStep SDK and these sample apps are commercial software, licensed to OneStep customers under
their agreement with OneStep.
