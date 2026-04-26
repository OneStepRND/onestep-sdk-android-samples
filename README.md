# OneStepSDK

## Versioning

![API](https://img.shields.io/badge/API-26%2B-brightgreen.svg)
![Core](https://img.shields.io/badge/core-2.0.0-red.svg)
![UiKit](https://img.shields.io/badge/uikit-2.0.0-blue.svg)

This repository contains sample Android applications demonstrating how to integrate and use the OneStep SDK for motion analysis. The apps showcase how to record motion data, analyze it, and display the results within your own application.

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

Initialize the SDK once (typically in your `Application.onCreate`), then identify the current user. `identify` is a suspend function and returns an `OSTResult<Unit>` you should branch on.

```kotlin
OneStep.initialize(
    application = this,
    clientToken = "<YOUR-CLIENT-TOKEN>",
)

val result = OneStep.identify(
    userId = "<YOUR-USER-DISTINCT-ID>",
    identityVerification = "<YOUR-IDENTITY-VERIFICATION-SECRET>", // or null in development
)

when (result) {
    is OSTResult.Success -> {
        // SDK is ready. Optionally set user attributes:
        OneStep.updateUserAttributes(
            OSTUserAttributes.Builder()
                .withSex(OSTUserAttributes.Sex.MALE)
                .build()
        )
    }
    is OSTResult.Error -> {
        // result.error is OSTResult.Code (INVALID_CLIENT_TOKEN, NETWORK_ERROR, …)
        Log.e("OneStep", "identify failed: ${result.error} - ${result.message}")
    }
}
```

### Observe SDK state

```kotlin
lifecycleScope.launch {
    OneStep.state.collect { state ->
        when (state) {
            is OSTState.Uninitialized -> { /* not yet initialized */ }
            is OSTState.Ready -> { /* initialized, no user identified */ }
            is OSTState.Identified -> { /* state.userId is identified */ }
            is OSTState.Error -> { /* state.code, state.message */ }
        }
    }
}
```

### Logout

```kotlin
OneStep.logout()
```

`logout()` clears credentials, stops monitoring, and reverts the SDK to `OSTState.Ready`. SDK initialization is preserved — you don't need to call `initialize()` again before the next `identify()`.
