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

Connect a user, record, and analyze
([`QuickstartViewModel.kt`](app/src/main/java/com/onestep/quickstart/QuickstartViewModel.kt)):

```kotlin
// 1. Bind the user. Credentials come from local.properties via BuildConfig.
oneStep.setPatient(
    apiKey = BuildConfig.CLIENT_TOKEN,
    customerPatientId = BuildConfig.CUSTOMER_PATIENT_ID,
    identityVerification = BuildConfig.IDENTITY_VERIFICATION.ifBlank { null },
)

// 2. Record. Request ACTIVITY_RECOGNITION first (see MainActivity.kt).
val motionLab = oneStep.getMotionLab().getOr { return it.describe() }
motionLab.reset()
motionLab.prepareForRecording(OSTActivityType.WALK) // while the app is in the foreground
motionLab.start(OSTActivityType.WALK, 30_000).getOr { error ->
    motionLab.reset() // a refused start records nothing; release the service
    return "Could not start: ${error.describe()}"
}
withTimeoutOrNull(60_000) { motionLab.recorderState.first { it == OSTRecorderState.DONE } }

// 3. Analyze and read the result.
val measurement = motionLab.analyze(timeout = 60_000).getOr { return it.describe() }
val walkScore = measurement.params[OSTParamName.WALKING_WALK_SCORE]
val steps = measurement.metadata.steps
```

Every result is an `OSTResult`: check it. Since SDK 2.3.0, `start()` returns an error (for example
`MissingPermission`) instead of throwing.

## Next steps

The other samples in this repository show more:

- [`ActiveMesurementSample`](../ActiveMesurementSample/README.md): live timer and step count,
  user tags, a branded recording notification, history, norms and insights.
- [`BackgroundMonitoringSample`](../BackgroundMonitoringSample/README.md): background activity
  monitoring and daily summaries.
- [`UiKitSample`](../UiKitSample/README.md): drop-in OneStep UIKit screens.

Full documentation:
[OneStep Collect for Android](https://glorious-caboc-cd3.notion.site/onestep-collect-for-android).

## Support

For SDK access, credentials, support, or to report an issue, contact `shahar@onestep.co`.
