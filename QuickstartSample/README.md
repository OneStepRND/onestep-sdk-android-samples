# OneStep SDK Quickstart

![Commercial License](https://img.shields.io/badge/license-Commercial-green.svg)
![API](https://img.shields.io/badge/API-26%2B-brightgreen.svg)
![SDK](https://img.shields.io/badge/sdk-2.3.0-red.svg)

QuickstartSample is the smallest complete integration of the OneStep core SDK
(`co.onestep.android:core:2.3.0`). One screen connects a test user, records a 30-second walk, and
shows the walk score and step count. It has three Kotlin files: the `Application` that initializes
the SDK, a `ViewModel` with every SDK call, and a Compose `Activity`. Copy it as the start of your
own integration.

## Run it

1. Copy the credentials template:
   ```sh
   cp local.properties.example local.properties
   ```
2. In `local.properties`, set `onestep.clientToken` and `onestep.customerPatientId`.
   `onestep.identityVerification` is optional. Use a test user id, never a real patient's name or
   record number. `local.properties` is gitignored: do not commit it.
3. In Android Studio, open the `QuickstartSample` folder (not the repository root).
4. Run the `app` configuration on a physical device. Emulators have no real motion sensors.
5. Tap **Connect**, then **Record 30 s walk**, allow the permission, and walk with the phone.

If a key is missing, the app names it and stops. If the SDK cannot initialize (for example, the
device has no gyroscope), the app says so.

## The whole integration

Add the SDK:

```kotlin
// app/build.gradle.kts
implementation("co.onestep.android:core:2.3.0")
```

Initialize once, in `Application.onCreate()`
([`QuickstartApplication.kt`](app/src/main/java/com/onestep/quickstart/QuickstartApplication.kt)):

```kotlin
oneStep = OneStep.initialize(
    application = this,
    onAuthLost = { Log.w(TAG, "OneStep auth lost; call setPatient() again") },
).getOr(null) // null when initialization fails, for example on a device without a gyroscope
```

Connect a user, then record and analyze. These are excerpts of `connect()` and
`recordAndAnalyze()` in
[`QuickstartViewModel.kt`](app/src/main/java/com/onestep/quickstart/QuickstartViewModel.kt), with
the constants inlined and the Busy state updates left out. The file wraps `error.cause.type.name` in a small `typeName` helper.

```kotlin
// 1. Bind the user. Credentials come from local.properties via BuildConfig.
sdk.setPatient(
    apiKey = BuildConfig.CLIENT_TOKEN,
    customerPatientId = BuildConfig.CUSTOMER_PATIENT_ID,
    identityVerification = BuildConfig.IDENTITY_VERIFICATION.ifBlank { null },
).getOr { error ->
    _state.value = QuickstartUiState.Disconnected("Could not connect (${error.cause.type.name}).")
    return@launch
}
```

```kotlin
// 2. Record. Request ACTIVITY_RECOGNITION first (see MainActivity.kt).
val motionLab = sdk.getMotionLab().getOr { return "Recording unavailable (${it.cause.type.name})." }
motionLab.reset() // clear the previous recording
// Starts the recording service. Call it while the app is in the foreground: it returns
// Success(false), and starts no service, when the app is already in the background.
motionLab.prepareForRecording(OSTActivityType.WALK)
motionLab.start(OSTActivityType.WALK, 30_000L).getOr { error ->
    motionLab.reset() // a refused start records nothing; release the service
    return "Could not start the recording (${error.cause.type.name})."
}
withTimeoutOrNull(60_000L) {
    motionLab.recorderState.first { it == OSTRecorderState.DONE }
} ?: run {
    motionLab.stop() // the recorder may still be running
    motionLab.reset()
    return "The recording did not finish in time."
}
```

```kotlin
// 3. Analyze and read the result.
val measurement = motionLab.analyze(timeout = 60_000L)
    .getOr { return "Analysis failed (${it.cause.type.name})." }
return when (measurement.resultState) {
    OSTResultState.FULL_ANALYSIS -> measurement.summary()
    OSTResultState.PARTIAL_ANALYSIS -> measurement.summary() + "\n(partial analysis)"
    OSTResultState.EMPTY_ANALYSIS -> "Empty analysis: the walk had too few steps to score."
    null -> "Analysis not available yet (timeout or network). The SDK syncs it later."
}

// summary() reads the walk score and the step count:
//   params[OSTParamName.WALKING_WALK_SCORE] and metadata.steps
```

Every call returns an `OSTResult`: check it. Show only `error.cause.type.name`. Never show
`error.cause.message` or the `error.cause.type` object itself (for example `"${error.cause.type}"`):
both can contain the server's response body.

Permissions:

- `ACTIVITY_RECOGNITION`: without it, `start()` fails with `MissingPermission` on Android 14+.
  On Android 10-13 the recording starts but counts no steps.
- `POST_NOTIFICATIONS`: on Android 13+ the recording notification is visible only if the app has
  this permission. This sample does not request it; a real app should.

## Next steps

The other samples in this repository show more:

- [`ActiveMeasurementSample`](../ActiveMeasurementSample/README.md): live timer and step count,
  user tags, a branded recording notification, history, norms and insights.
- [`BackgroundMonitoringSample`](../BackgroundMonitoringSample/README.md): background activity
  monitoring and daily summaries.
- [`UiKitSample`](../UiKitSample/README.md): drop-in OneStep UIKit screens.

Full documentation:
[OneStep Collect for Android](https://glorious-caboc-cd3.notion.site/onestep-collect-for-android).

## Support

For SDK access, credentials, support, or to report an issue, contact `shahar@onestep.co`.
