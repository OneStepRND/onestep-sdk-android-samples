# Background recording Sample app

## Versioning

![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)
![API](https://img.shields.io/badge/API-26%2B-brightgreen.svg)
![SDK](https://img.shields.io/badge/sdk-2.3.0-red.svg)

BackgroundMonitoringSample is an app demonstrating the OneStep core SDK collecting background sensor data and syncing it to produce background recording records.

## Documentation

📖 **[OneStep Collect for Android — full documentation](https://glorious-caboc-cd3.notion.site/onestep-collect-for-android)**

That guide covers SDK setup, identity verification, the monitoring lifecycle (`enable` vs
`optIn`/`optOut`), notification configuration, daily summaries, and the events stream. This sample
links back to it from the relevant code.

> `migration-guide-ios-developer.md` in this folder is a v1→v2 API reference aimed at **iOS**
> developers porting from the Android SDK. Android consumers can ignore it.

## Features

- Detecting movement.
- Recording motion data as a background service.
- Storing and analysing motion data.
- Presenting the recorded data as daily step summaries.

## Main functionality

- Monitoring screen demonstrates how to register and unregister the background recording as well as how to sync the data from the server.
> **Note:** syncing will happen by default in the background in time frames corresponding to the developers initialization configurations.

- Records screen demonstrates how to fetch the records from the server and present them in a list.

- Notification style picker on the Monitoring screen demonstrates the three ways to render the
  foreground-service notification (see below). Picking a style updates the live notification
  immediately and is remembered across app restarts.

- "Allow dismissing the notification" switch demonstrates `setMonitoringNotificationDismissible`
  (see below).

## Where to look

| What | File |
|---|---|
| Initialize, identify, `enable()` monitoring | [`BgMonitoringSampleApplication.kt`](app/src/main/java/com/onestep/backgroundmonitoringsample/BgMonitoringSampleApplication.kt) |
| `optIn()` / `optOut()`, monitoring state, daily summaries | [`MainViewModel.kt`](app/src/main/java/com/onestep/backgroundmonitoringsample/viewmodels/MainViewModel.kt) |
| The three notification styles | [`MonitoringNotifications.kt`](app/src/main/java/com/onestep/backgroundmonitoringsample/notifications/MonitoringNotifications.kt) |
| Runtime permission requests | [`MainScreen.kt`](app/src/main/java/com/onestep/backgroundmonitoringsample/screens/MainScreen.kt) |

![Alt text](https://github.com/OneStepRND/onestep-sdk-android-samples/blob/main/BackgroundMonitoringSample/backgroundTutorial.gif)

## Monitoring lifecycle: `enable` vs opt-in

These are two different things and a common point of confusion:

- `monitoring.enable()` — wires up the monitoring subsystem for the session. Called once after
  identification (see `BgMonitoringSampleApplication`). It does **not** start collecting by itself.
- `monitoring.optIn()` / `monitoring.optOut()` — the user's consent toggle. `optIn()` actually
  starts background collection (this sample calls it from `MainViewModel` once the runtime
  permissions are granted); `optOut()` stops it. Observe `monitoring.preference` and
  `monitoring.state` (a `OSTMonitoringRuntimeState` of `Active` / `Inactive` / `Blocked` / `Error`)
  to drive your UI.

## Permissions

The host app requests two **runtime** permissions (see `MainScreen`): `ACTIVITY_RECOGNITION` and,
on Android 13+, `POST_NOTIFICATIONS`. The SDK contributes the rest via manifest merging, so you do
not declare them — but several are privacy-sensitive and **must be reflected in your Play Store Data
safety form**, notably `RECORD_AUDIO` + `FOREGROUND_SERVICE_MICROPHONE` (audio-based motion sensing)
and `ACCESS_FINE/COARSE_LOCATION`. See the commented list in `AndroidManifest.xml`, or open
Android Studio → *Merged Manifest* for the authoritative set.

## Customizing the monitoring notification

Background monitoring runs in a foreground service, so Android requires an ongoing notification
while it is active. You control its appearance through a single SDK entry point:

```kotlin
monitoring.setCustomMonitoringNotification {
    // call exactly one of: default(...) / custom(...) / native(...)
}
```

`setCustomMonitoringNotification` persists the config and repaints the live notification
immediately when monitoring is active. The SDK restores the config whenever the service restarts,
including after a reboot or an app update.

- Call it from `Application.onCreate()`, right after `initialize()` succeeds. It does not need an
  identified patient. `onCreate()` runs in every process, so a `native(...)` notification also
  survives a headless restart of the service. This sample re-applies the user's last choice there
  (`BgMonitoringSampleApplication`).
- To change the style at runtime, call it again. You do not need to restart monitoring.

The selected style is persisted in `SharedPreferences`, so the picker and the live notification stay
in sync across app restarts.

There are three styles, each backed by an `OSTNotificationConfig`. This sample builds all three in
[`MonitoringNotifications`](app/src/main/java/com/onestep/backgroundmonitoringsample/notifications/MonitoringNotifications.kt)
and lets you switch between them from the Monitoring screen.

**1. Default** — the SDK renders a simple title/text notification. `title`/`text` are lambdas so
the SDK can re-evaluate them (e.g. for localization) whenever it rebuilds the notification.

```kotlin
monitoring.setCustomMonitoringNotification {
    default(
        icon = R.drawable.ic_launcher_foreground,
        title = { "Background Monitoring Sample is active" },
        text = { "OneStep is tracking your movement in the background" },
    )
}
```

**2. Custom** — the SDK renders a richer notification with live progress toward a daily step goal.
The SDK fills in the current step count for you. `title` replaces the `appName` header and `text`
adds one line under it (SDK 2.2.2+). They are plain strings, not lambdas, because the SDK persists
them to rebuild the notification after a restart.

```kotlin
monitoring.setCustomMonitoringNotification {
    custom(
        appName = "Background Monitoring Sample",
        stepsGoal = 8_000,
        icon = R.drawable.ic_launcher_foreground,
        title = "Your daily activity",
        text = "Steps count toward your daily goal",
    )
}
```

**3. Native** — you build the entire `android.app.Notification` and hand it to the SDK. Use this
when you need full control (custom layouts, actions, styles). You own the notification channel, so
create it before building. The `icon` argument is the small icon the foreground service falls back
to.

```kotlin
monitoring.setCustomMonitoringNotification {
    native(
        notification = NotificationCompat.Builder(context, YOUR_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("OneStep is recording")
            .setOngoing(true)
            .build(),
        icon = R.drawable.ic_launcher_foreground,
    )
}
```

### Letting users dismiss the notification

By default, a swipe on the monitoring notification is undone at once. From SDK 2.2.0 you can let the
user dismiss it:

```kotlin
monitoring.setMonitoringNotificationDismissible(true)
val allowed = monitoring.isMonitoringNotificationDismissible // read it back for a settings UI
```

Monitoring keeps running while the notification is hidden. Android shows it again when the service
restarts (reboot, app update, process restart). If you expose this as a setting, label it as
permission to dismiss ("Allow dismissing the notification"), not as "Hide the notification".

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

1. Clone this repo (or download the code) and open this module in Android Studio.
2. **Add your credentials to `local.properties` first** (see [Keys](#keys)) — without them the SDK
   fails to connect and the app shows "SDK session lost".
3. Run on a **physical device** — the emulator has no motion sensors, so you won't get real
   background records or analysis.

### Keys

To initialize this app you need a OneStep client token (and optionally an identity-verification
secret). Reach out to `shahar@onestep.co` for access to the SDK and credentials.

Credentials are **never stored in source**. They are read from `local.properties` (which is
gitignored) and exposed to the app through `BuildConfig` at build time. Add these lines to
`local.properties`:

```properties
onestep.clientToken=<YOUR-CLIENT-TOKEN>
onestep.customerPatientId=<A-STABLE-ID-FOR-THE-CURRENT-USER>
# Precomputed identity-verification digest (leave blank to skip identity verification):
onestep.identityVerification=<HMAC-DIGEST>
```

**Identity verification.** `onestep.identityVerification` is **not** the secret — it is the
hex-encoded `HMAC_SHA256(key = <identity-verification secret>, message = <customerPatientId>)`.
The secret must stay on your server; production apps should fetch the precomputed digest from a
backend and never embed the secret. For local testing you can compute the digest with:

```bash
printf '%s' "<customerPatientId>" | openssl dgst -sha256 -hmac "<SECRET>" -hex
```

Recompute the digest whenever you change `onestep.customerPatientId`, since the digest is bound to
that id. If the digest is wrong, the SDK fails to connect with HTTP 403 `invalid hmac_digest`.

## Support

For support, additional information, or to report issues, contact `ziv@onestep.co`.
