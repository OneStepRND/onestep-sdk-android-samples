# In app measurement Sample app

## Versioning

![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)
![API](https://img.shields.io/badge/API-26%2B-brightgreen.svg)
![SDK](https://img.shields.io/badge/sdk-2.0.0-red.svg)

ActiveMesurementSample is an app demonstrarting the OneStep core SDK collecting sensor data and analyzing it to produce a walk score, quantifying the nuances of walking patterns.

## Features

- Recording motion data
- Storing and analysing motion data
- Presenting walk score, steps and errors

![Alt text](https://github.com/OneStepRND/onestep-sdk-android-samples/blob/main/ActiveMesurementSample/activeMeasurementTutorial.gif)

### Requirements

| Component | Version |
|---|---|
| Android API (min) | 26 |
| Android API (target / compile) | 35 |
| Android Gradle Plugin | 9.0.0 |
| Kotlin | 2.2.10 |
| Compose BOM | 2024.09.02 |
| OneStep SDK (core) | 2.0.0 |

Toolchain versions mirror `UiKitSample/gradle/libs.versions.toml`. If the live build at `app/build.gradle.kts` is temporarily ahead (e.g. `compileSdk=36` after an AGP bump), the canonical reference still wins for new sample apps.

### Installation

Clone this repo or download the code, run the app in Android studio or your choice of IDE
Make sure you run this sample app on an actual device to use the motion sensors and get an actual walk score.

### Keys

To initilize this app you need to obtain specific API-KEY, reach out to `shahar@onestep.co` for more details and access to the SDK.

## Support

For support, additional information, or to report issues, contact `ziv@onestep.co`.
