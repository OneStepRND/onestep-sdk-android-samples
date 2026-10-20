# In app measurement Sample app

## Versioning

![Commercial License](https://img.shields.io/badge/license-Commercial-green.svg)
![API](https://img.shields.io/badge/API-26%2B-brightgreen.svg)
![SDK](https://img.shields.io/badge/sdk-2.3.0-red.svg)

ActiveMesurementSample is an app demonstrating the OneStep core SDK collecting sensor data and analyzing it to produce a walk score, quantifying the nuances of walking patterns.

## Documentation

📖 **[OneStep Collect for Android — full documentation](https://glorious-caboc-cd3.notion.site/onestep-collect-for-android)** — SDK setup, identity verification, measurements, and more.

## Features

- Recording motion data
- Storing and analysing motion data
- Presenting walk score, steps and errors
- A live timer and step count during the recording
- A branded recording notification

## Where to look

| What | File |
|---|---|
| Initialize the SDK, brand the recording notification after `setPatient` | [`SDKSampleApplication.kt`](app/src/main/java/com/onestep/sdksample/SDKSampleApplication.kt) |
| Identify the user (`setPatient`) | [`SDKSampleApplication.kt`](app/src/main/java/com/onestep/sdksample/SDKSampleApplication.kt) → `connectUser()` |
| Observe `identificationState` | [`MainActivity.kt`](app/src/main/java/com/onestep/sdksample/MainActivity.kt) |
| Prepare, start, stop, analyze a recording | [`RecorderViewModel.kt`](app/src/main/java/com/onestep/sdksample/viewmodels/RecorderViewModel.kt) |
| Live timer from `currentRecordingWindow` | [`RecorderViewModel.kt`](app/src/main/java/com/onestep/sdksample/viewmodels/RecorderViewModel.kt) → `progress` |
| Insights, norms, history queries (reference only) | [`AdvancedMotionLabExamples.kt`](app/src/main/java/com/onestep/sdksample/examples/AdvancedMotionLabExamples.kt) |

## Recording: the calls in order

```kotlin
motionLab.prepareForRecording(OSTActivityType.WALK) // while the app is in the foreground
    .onSuccess { started -> if (!started) { /* app was in the background: no service */ } }
motionLab.start(OSTActivityType.WALK, durationMillis = 60_000)
    .onError { error ->
        // MissingPermission, InvalidArgument, …
        motionLab.reset() // releases the service prepareForRecording() started
    }
// ... the user walks; collect motionLab.currentRecordingWindow and motionLab.stepsCount ...
motionLab.stop()
val measurement = motionLab.analyze(timeout = 60_000).getOr(null)
```

- **Check the result of `start()`.** Since SDK 2.3.0, `start()` returns an error instead of
  throwing. A refused start records nothing, so a `try`/`catch` alone is not enough.
- **Call `prepareForRecording()` first.** On Android 14+, a foreground service cannot start from the
  background. Calling it while your app is visible keeps a later `start()` safe. It returns
  `Success(false)`, not an error, when the app is already in the background.
- **Call `reset()` after a refused start.** Otherwise the service that `prepareForRecording()`
  started keeps its notification up for up to 60 seconds.
- **Draw timers from `currentRecordingWindow`.** The SDK owns the recording clock. Re-read
  `remainingMillis()` or `elapsedMillis()` on each tick so the UI never drifts from the recorder.

![Alt text](https://github.com/OneStepRND/onestep-sdk-android-samples/blob/main/ActiveMesurementSample/activeMeasurementTutorial.gif)

### Requirements

| Component | Version |
|---|---|
| Android API (min) | 26 |
| Android API (target / compile) | 36 |
| Android Gradle Plugin | 9.0.0 (Gradle 9.1.0, JDK 17+) |
| Kotlin | 2.2.10 |
| Compose BOM | 2026.03.00 |
| OneStep SDK (core) | 2.3.0 |

These values come from this module's `gradle/libs.versions.toml` and `app/build.gradle.kts` — the
source of truth for what builds. Update them together if you bump versions.

### Installation

1. Clone this repo (or download the code) and open `ActiveMesurementSample` in Android Studio.
2. Add your credentials to `local.properties` (see [Keys](#keys)).
3. Run on a **physical device**. The emulator has no motion sensors, so you get no walk score.

### Keys

To initialize this app you need a OneStep client token (and optionally an identity-verification
secret). Reach out to `shahar@onestep.co` for access to the SDK and credentials.

Credentials are **never stored in source** — they are read from `local.properties` (gitignored) and
exposed via `BuildConfig` at build time. Add these lines to `local.properties`:

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

## Support

For support, additional information, or to report issues, contact `shahar@onestep.co`.
