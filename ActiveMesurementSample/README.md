# In app measurement Sample app

## Versioning

![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)
![API](https://img.shields.io/badge/API-26%2B-brightgreen.svg)
![SDK](https://img.shields.io/badge/sdk-2.0.0-red.svg)

ActiveMesurementSample is an app demonstrarting the OneStep core SDK collecting sensor data and analyzing it to produce a walk score, quantifying the nuances of walking patterns.

## Documentation

📖 **[OneStep Collect for Android — full documentation](https://glorious-caboc-cd3.notion.site/onestep-collect-for-android)** — SDK setup, identity verification, measurements, and more.

## Features

- Recording motion data
- Storing and analysing motion data
- Presenting walk score, steps and errors

![Alt text](https://github.com/OneStepRND/onestep-sdk-android-samples/blob/main/ActiveMesurementSample/activeMeasurementTutorial.gif)

### Requirements

| Component | Version |
|---|---|
| Android API (min) | 26 |
| Android API (target / compile) | 34 / 36 |
| Android Gradle Plugin | 8.9.2 |
| Kotlin | 2.0.21 |
| Compose BOM | 2025.04.01 |
| OneStep SDK (core) | 2.0.0 |

These values come from this module's `gradle/libs.versions.toml` and `app/build.gradle.kts` — the
source of truth for what builds. Update them together if you bump versions.

### Installation

Clone this repo or download the code, run the app in Android studio or your choice of IDE
Make sure you run this sample app on an actual device to use the motion sensors and get an actual walk score.

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

For support, additional information, or to report issues, contact `ziv@onestep.co`.
