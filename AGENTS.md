# AGENTS.md

Guidance for AI coding assistants (Claude Code, Cursor, Copilot, and others) working in this repo,
or helping a developer integrate the OneStep Android SDK by following it.

## What this repo is

Public sample apps for the OneStep Android SDK (`co.onestep.android:core` and
`co.onestep.android:uikit` on Maven Central). Each folder is a **standalone Gradle project**: open
or build it from its own folder. There is no root Gradle build.

| Folder | Shows |
|---|---|
| `QuickstartSample/` | Start here. Core SDK, minimal: initialize, `setPatient`, a 30-second walk, its walk score |
| `ActiveMesurementSample/` | Core SDK: initialize, `setPatient`, record a walk, analyze it |
| `BackgroundMonitoringSample/` | Core SDK: background monitoring, daily summaries, notifications |
| `UiKitSample/` | UIKit: prebuilt recording flows, summary, carelog, permissions, theming |

Each sample's README maps its features to the files that implement them.

## Rules

1. **Never write credentials anywhere in the repo.** That covers client tokens, API keys,
   identity-verification secrets or digests, and patient ids, in code, Gradle files, docs, tests,
   commit messages and PR text. Credentials live only in each sample's gitignored
   `local.properties` (template: `local.properties.example`) and reach code as
   `BuildConfig.CLIENT_TOKEN`, `BuildConfig.CUSTOMER_PATIENT_ID` and
   `BuildConfig.IDENTITY_VERIFICATION`. Never create, read out or print `local.properties`.
   CI runs gitleaks with `.gitleaks.toml` and fails on a leak.
2. **No patient data (PHI).** Use obviously synthetic users and values in examples and previews.
   Never log credential values or patient data.
3. **Use the real SDK API.** It is not `identify()`, `logout()` or `OneStep.state`. The real API:
   - `OneStep.initialize(application, onAuthLost = { … })` returns `OSTResult<OneStep>`.
   - `oneStep.setPatient(apiKey, customerPatientId, identityVerification, userAttributes = { … })`
     is suspend and returns an `OSTResult`.
   - `oneStep.clearPatient()` logs out.
   - Observe `oneStep.identificationState` (`Unidentified` / `Identified` / `Lost`).
   - Get products with `oneStep.getMotionLab()`, `oneStep.getMonitoring()` and
     `oneStep.getInsights()`. Each returns an `OSTResult`.
   - Handle results with `.onSuccess {}`, `.onError { it.cause.type }` and `.getOr(default)`.
   - Since 2.3.0, `motionLab.start()` returns an error instead of throwing. Always check it.
4. **Keep the four samples in step.** They share one toolchain; a version bump touches every
   `gradle/libs.versions.toml`. Renovate groups these bumps.
5. **Compose:** pass state and lambdas to composables, not ViewModels. Give every composable a
   `@Preview`.

## Build and verify

From a sample folder, with JDK 17 or newer:

```bash
./gradlew :app:assembleDebug :app:assembleRelease :app:testDebugUnitTest :app:lintDebug
```

The build works with no `local.properties`: credential fields are then empty strings. Each sample
then shows a setup screen at launch that names the missing keys (and logs only the key names),
instead of failing later. A real recording needs a physical device; the emulator has no motion
sensors.

## Git

Branch from `main` with `feat/`, `fix/`, `chore/` or `docs/`. Commit subjects use `Feat:`, `Fix:`,
`Chore:` or `Docs:`. Split a PR that touches more than about 10 files by risk.
