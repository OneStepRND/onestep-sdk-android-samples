# Background recording Sample app

## Versioning

![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)
![API](https://img.shields.io/badge/API-26%2B-brightgreen.svg)
![SDK](https://img.shields.io/badge/sdk-2.0.0-red.svg)

BackgroundMonitoringSample is an app demonstrating the OneStep core SDK collecting background sensor data and syncing it to produce background recording records.
## Features

- Detecting movement.
- Recording motion data as a background service.
- Storing and analysing motion data.
- Presenting the motion records by days, hours...

## Main functionality

- Monitoring screen demonstrates how to register and unregister the background recording as well as how to sync the data from the server.
> **Note:** syncing will happen by default in the background in time frames corresponding to the developers initialization configurations.

- Records screen demonstrates how to fetch the records from the server and present them in a list.

![Alt text](https://github.com/OneStepRND/onestep-sdk-android-samples/blob/main/BackgroundMonitoringSample/backgroundTutorial.gif)

### Requirements

| Component | Version |
|---|---|
| Android API (min) | 26 |
| Android API (target / compile) | 35 |
| Android Gradle Plugin | 9.0.0 |
| Kotlin | 2.2.10 |
| Compose BOM | 2024.09.02 |
| OneStep SDK (core) | 2.0.0 |

Toolchain versions mirror `UiKitSample/gradle/libs.versions.toml`, the canonical reference for sample apps in this repo.

### Installation

Clone this repo or download the code, run the app in Android studio or your choice of IDE
Make sure you run this sample app on an actual device to use the motion sensors and get actual background record and analysis.

### Keys

To initilize this app you need to obtain specific API-KEY, reach out to `shahar@onestep.co` for more details and access to the SDK.

## Support

For support, additional information, or to report issues, contact `ziv@onestep.co`.
