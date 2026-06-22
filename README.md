# OneStepSDK

## Versioning

![API](https://img.shields.io/badge/API-26%2B-brightgreen.svg)
![Core](https://img.shields.io/badge/core-2.0.0-red.svg)
![UiKit](https://img.shields.io/badge/uikit-2.0.0-blue.svg)

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
| Android API (target / compile) | 35 |
| Android Gradle Plugin | 9.0.0 |
| Kotlin | 2.2.10 |
| Compose BOM | 2024.09.02 |
| OneStep SDK (core / uikit) | 2.0.0 |

Toolchain versions mirror `UiKitSample/gradle/libs.versions.toml`, the canonical reference for sample apps in this repo. Bump versions there first, then propagate.

### Installation

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
