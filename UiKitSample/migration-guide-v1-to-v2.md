# OneStep SDK Migration Guide: v1 to v2

This guide walks you through migrating your Android app from OneStep SDK v1 to v2.

## Table of Contents

1. [Overview](#1-overview)
2. [Dependency Update](#2-dependency-update)
3. [Import Changes](#3-import-changes)
4. [Initialization Migration](#4-initialization-migration)
5. [Authentication Migration](#5-authentication-migration)
6. [State Management Migration](#6-state-management-migration)
7. [MotionLab Migration](#7-motionlab-migration)
8. [Monitoring Migration](#8-monitoring-migration)
9. [Insights Migration](#9-insights-migration)
10. [Configuration Migration](#10-configuration-migration)
11. [Notification Migration](#11-notification-migration)
12. [Logout Migration](#12-logout-migration)
13. [Removed APIs](#13-removed-apis)
14. [Quick Reference Table](#14-quick-reference-table)
15. [Migration Checklist](#15-migration-checklist)
16. [Troubleshooting Common Migration Errors](#16-troubleshooting-common-migration-errors)

---

## 1. Overview

### What Changed

OneStep SDK v2 represents a complete architectural redesign:

- **v1**: Monolithic `OneStep` class with Builder pattern in `co.onestep.android.core.external`
- **v2**: Clean singleton `OneStep` object in `co.onestep.android.core` with three distinct product surfaces

**The Three Product Surfaces:**
1. **MotionLab** - Clinical-grade gait analysis and activity recording
2. **Monitoring** - Continuous background activity tracking with GDPR compliance
3. **Insights** - Analytics and interpretation of motion data

**⚠️ Breaking Changes:** This is a major version upgrade with breaking changes across the entire API surface. You cannot drop-in replace v1 with v2.

### Why We Changed

- **Cleaner API**: Separation of concerns makes code more maintainable
- **Better State Management**: Reactive StateFlow/Flow instead of callbacks
- **Modern Kotlin**: Suspend functions for async operations
- **GDPR Compliance**: Built-in opt-in/opt-out mechanisms for monitoring
- **Improved Developer Experience**: Explicit initialization, better error handling

---

## 2. Dependency Update

The Maven coordinates remain the same, only the version changes.

```gradle
dependencies {
    // Update from v1.x.x to v2.x.x
    implementation("co.onestep.android:core:2.0.0")  // Check Maven Central for latest
}
```

**Check for Latest Version:**
- Maven Central: https://central.sonatype.com/artifact/co.onestep.android/core
- GitHub Releases: https://github.com/OneStepRND/onestep-sdk-android

---

## 3. Import Changes

### Package Structure

The core package changed from `co.onestep.android.core.external` to `co.onestep.android.core`:

```kotlin
// ❌ v1 imports
import co.onestep.android.core.external.OneStep
import co.onestep.android.core.external.models.*

// ✅ v2 imports
import co.onestep.android.core.OneStep
import co.onestep.android.core.OSTConfiguration
import co.onestep.android.core.OSTState
import co.onestep.android.core.OSTEvent
import co.onestep.android.core.OSTIdentifyResult
import co.onestep.android.core.OSTIdentifyError

// Product-specific imports
import co.onestep.android.core.monitoring.*
import co.onestep.android.core.motionLab.*
import co.onestep.android.core.insights.*

// v2 model imports (external package removed)
import co.onestep.android.core.common.models.measurement.*
import co.onestep.android.core.common.models.*
import co.onestep.android.core.common.models.recording.*
import co.onestep.android.core.platform.models.OSTUserAttributes
import co.onestep.android.core.monitoring.models.*  // notification configs
```

### Key Classes by Product

**MotionLab:**
- `OSTRecorderState`, `OSTAnalyserState`
- `OSTActivityType`, `OSTUserInputMetaData`

**Monitoring:**
- `OSTMonitoringConfig`, `OSTMonitoringConfig.EnrollmentPolicy`
- `OSTMonitoringRuntimeState`, `OSTMonitoringBlocker`, `OSTMonitoringPreference`
- `OSTMonitoringDailySummary`, `OSTDailySummariesQuery`

**Insights:**
- `OSTMotionDataService`
- `OSTResult`

---

## 4. Initialization Migration

### v1: Builder Pattern with Configuration Object

```kotlin
// ❌ v1
val config = OSTSdkConfiguration(
    enableMonitoringFeature = true,
    monitoringNotificationConfig = OSTDefaultNotificationConfig(
        title = { "Tracking Activity" },
        text = { "OneStep is monitoring your movement" },
        icon = R.drawable.ic_notification
    ),
    recorderNotificationConfig = OSTDefaultNotificationConfig(
        title = { "Recording" },
        text = { "Recording in progress" },
        icon = R.drawable.ic_recording
    ),
    collectPedometer = true,
    useImperialSystem = OSTMeasurementSystem.METRIC,
    mockIMU = false,
    analyticsHandler = MyAnalyticsHandler(),
    userAttributes = mapOf("age" to "35", "weight" to "70"),
    additionalConfigurations = mapOf("customKey" to "customValue")
)

OneStep.Builder(application, "your-app-id", "your-api-key")
    .setIdentityVerification(hmacSignature)
    .setConfiguration(config)
    .build()
```

### v2: Simple Function Call

```kotlin
// ✅ v2
OneStep.initialize(
    application = application,
    clientToken = "your-client-token",
    config = OSTConfiguration(
        appId = "your-app-id",  // @Deprecated — retained for backward compatibility only
        additionalConfig = mapOf("customKey" to "customValue")  // optional
    )
)
```

> **Note:** `OSTConfiguration.appId` is deprecated. The client token now serves as both the app identifier and API key. The `appId` field is retained only for backward compatibility and will be removed in a future release.

### Key Differences

| Aspect | v1 | v2 |
|--------|----|----|
| **Pattern** | Builder with complex config object | Simple function call |
| **Config** | `OSTSdkConfiguration` with 15+ fields | `OSTConfiguration` with 2 optional fields |
| **Monitoring** | Configured at init | Separate `monitoring.enable()` |
| **Recording** | Configured at init | MotionLab is ready immediately after identification |
| **Units** | Set in config | Set via `configuration.setMeasurementUnits()` |
| **User Attributes** | Set in config | Set via `updateUserAttributes()` |
| **Analytics** | Handler interface | Reactive `events` Flow |

---

## 5. Authentication Migration

### v1: Authentication in Builder

```kotlin
// ❌ v1
OneStep.Builder(application, "your-app-id", "your-api-key")
    .setIdentityVerification(hmacSignature)  // Optional but recommended
    .build()
```

### v2: Separate Identify Step

```kotlin
// ✅ v2 - Option 1: HMAC Authentication (Recommended)
OneStep.initialize(
    application = application,
    clientToken = "your-client-token"
)

val result = OneStep.identify(
    userId = "user-123",
    identityVerification = hmacSignature  // Optional but strongly recommended for production
)

when (result) {
    is OSTIdentifyResult.Success -> {
        Log.d("OneStep", "User identified successfully")
        // Proceed with SDK features
    }
    is OSTIdentifyResult.Failure -> {
        Log.e("OneStep", "Identify failed: ${result.error} - ${result.message}")
        // Handle error based on OSTIdentifyError type
    }
}
```

```kotlin
// ✅ v2 - Option 2: JWT Authentication (New in v2)
OneStep.initialize(application, clientToken)

val result = OneStep.connectAsUser(
    userId = "user-123",
    jwt = "eyJhbGciOiJIUzI1NiIs..."
)

when (result) {
    is OSTIdentifyResult.Success -> { /* ready */ }
    is OSTIdentifyResult.Failure -> { /* handle error */ }
}
```

```kotlin
// ✅ v2 - Option 3: Auto-Initialization (Convenience)
// If you haven't called initialize() yet, identify() will auto-initialize
val result = OneStep.identify(
    userId = "user-123"
)
```

### OSTIdentifyResult Types

```kotlin
sealed interface OSTIdentifyResult {
    data object Success : OSTIdentifyResult
    data class Failure(
        val error: OSTIdentifyError,
        val message: String
    ) : OSTIdentifyResult
}

enum class OSTIdentifyError {
    INVALID_CLIENT_TOKEN,      // Client token is invalid or expired
    VERIFICATION_FAILED,        // HMAC verification failed
    NETWORK_ERROR,             // Network request failed
    NOT_INITIALIZED,           // SDK not initialized (shouldn't happen with auto-init)
    UNKNOWN                    // Unexpected error
}
```

### Error Handling Example

```kotlin
val result = OneStep.identify(userId, hmac)

when (result) {
    is OSTIdentifyResult.Success -> {
        // Ready to use SDK features
        startMonitoring()
    }
    is OSTIdentifyResult.Failure -> {
        when (result.error) {
            OSTIdentifyError.INVALID_CLIENT_TOKEN -> {
                // Refresh client token from your backend
                showError("Authentication expired. Please log in again.")
            }
            OSTIdentifyError.VERIFICATION_FAILED -> {
                // HMAC signature invalid - possible tampering
                showError("Authentication failed. Please try again.")
            }
            OSTIdentifyError.NETWORK_ERROR -> {
                // Retry or show offline mode
                showError("Network error. Please check your connection.")
            }
            else -> {
                showError("Something went wrong: ${result.message}")
            }
        }
    }
}
```

---

## 6. State Management Migration

### v1: Callbacks or Polling

```kotlin
// ❌ v1
if (OneStep.isInitialized()) {
    // SDK is ready
}

// Limited state visibility
```

### v2: Reactive StateFlow + Flow

```kotlin
// ✅ v2 - Observe state changes
lifecycleScope.launch {
    OneStep.state.collect { state ->
        when (state) {
            is OSTState.Uninitialized -> {
                // SDK not yet initialized
                showInitButton()
            }
            is OSTState.Ready -> {
                // Initialized but no user identified
                showLoginButton()
            }
            is OSTState.Identified -> {
                // User authenticated and ready
                Log.d("OneStep", "User: ${state.userId}")
                enableFeatures()
            }
            is OSTState.Error -> {
                // Error occurred
                Log.e("OneStep", "Error ${state.code}: ${state.message}")
                showError(state.message)
            }
        }
    }
}
```

```kotlin
// ✅ v2 - Observe analytics events
lifecycleScope.launch {
    OneStep.events.collect { event ->
        Log.d("Analytics", "Event: ${event.name}")
        event.properties.forEach { (key, value) ->
            Log.d("Analytics", "  $key: $value")
        }

        // Forward to your analytics platform
        myAnalytics.track(event.name, event.properties)
    }
}
```

### State Type Definitions

```kotlin
sealed class OSTState {
    data object Uninitialized : OSTState()
    data object Ready : OSTState()
    data class Identified(val userId: String) : OSTState()
    data class Error(val code: Int, val message: String) : OSTState()
}

data class OSTEvent(
    val name: String,
    val properties: Map<String, Any> = emptyMap(),
    val timestamp: Long = System.currentTimeMillis()
)
```

### Migration Pattern

```kotlin
// ❌ v1: Callback-based
class MyAnalyticsHandler : AnalyticsHandler {
    override fun onEvent(name: String, properties: Map<String, Any>) {
        // Handle event
    }
}
config.analyticsHandler = MyAnalyticsHandler()

// ✅ v2: Flow-based
lifecycleScope.launch {
    OneStep.events.collect { event ->
        // Handle event
        myAnalytics.track(event.name, event.properties)
    }
}
```

---

## 7. MotionLab Migration

MotionLab is the product surface for clinical-grade gait analysis and activity recording.

### v1: Recorder via OneStep Object

```kotlin
// ❌ v1
val recorder = OneStep.getRecorder()

recorder.start(
    activityType = OSTActivityType.WALK,
    duration = 60_000,
    // ... other params
)

recorder.stop()

recorder.analyze()

// State observation
recorder.recorderState.collect { state -> ... }
```

### v2: Explicit MotionLab Interface

```kotlin
// ✅ v2

// 1. Optional: Pre-warm for faster start (NEW)
OneStep.motionLab.prepareForRecording(OSTActivityType.WALK)

// 2. Start recording
OneStep.motionLab.start(
    activityType = OSTActivityType.WALK,
    duration = 60_000,  // milliseconds
    sensorEnhancedMode = false,
    userInputMetadata = OSTUserInputMetaData(
        assistiveDevice = OSTAssistiveDevice.NONE,
        levelOfAssistance = OSTLevelOfAssistance.INDEPENDENT
    ),
    customMetadata = mapOf("location" to "clinic", "session" to "baseline")
)

// 3. Add markers during recording (NEW)
OneStep.motionLab.addMarker("turn_start")
OneStep.motionLab.addMarker("turn_end")

// 4. Stop recording
OneStep.motionLab.stop()

// 5. Analyze
val measurement = OneStep.motionLab.analyze(timeout = 120_000)  // milliseconds

// 6. Observe states
lifecycleScope.launch {
    OneStep.motionLab.recorderState.collect { state ->
        when (state) {
            OSTRecorderState.INITIALIZED -> { /* ready to record */ }
            OSTRecorderState.RECORDING -> { /* recording in progress */ }
            OSTRecorderState.FINALIZING -> { /* finalizing recording */ }
            OSTRecorderState.DONE -> { /* recording complete */ }
        }
    }
}

lifecycleScope.launch {
    OneStep.motionLab.stepsCount.collect { steps ->
        updateStepCounter(steps)
    }
}

lifecycleScope.launch {
    OneStep.motionLab.analyserState.collect { state ->
        when (state) {
            is OSTAnalyserState.Idle -> { /* not analyzing */ }
            is OSTAnalyserState.Uploading -> { /* uploading data */ }
            is OSTAnalyserState.Analyzing -> { /* analyzing */ }
            is OSTAnalyserState.Analyzed -> { /* analysis complete */ }
            is OSTAnalyserState.Failed -> {
                // Handle specific error types
                when (state.error) {
                    is OSTAnalyserError.TooShort -> {
                        // Recording was too short for meaningful analysis
                        showError("Recording too short. Please walk for at least 10 seconds.")
                    }
                    is OSTAnalyserError.Timeout -> {
                        // Analysis timed out
                        showError("Analysis timed out. Please try again.")
                    }
                    is OSTAnalyserError.NetworkError -> {
                        // Network issue during data upload
                        showError("Network error. Check your connection and retry.")
                    }
                    is OSTAnalyserError.ServerError -> {
                        // Server-side analysis error
                        showError("Server error during analysis. Please try again later.")
                    }
                    is OSTAnalyserError.General -> {
                        // Other unexpected error
                        showError("Analysis failed: ${state.error.error}")
                    }
                }
            }
        }
    }
}
```

### Measurement CRUD Operations (NEW in v2)

```kotlin
// ✅ v2 - Read measurements
val measurements = OneStep.motionLab.readMotionMeasurements(
    request = TimeRangedDataRequest(
        timeRangeFilter = OSTTimeRangeFilter.between(
            startTime = startDate.toEpochMilli(),
            endTime = endDate.toEpochMilli()
        )
    )
)

val singleMeasurement = OneStep.motionLab.readSingleMotionMeasurement(
    uuid = measurementUuid
)

// ✅ v2 - Update measurement metadata
OneStep.motionLab.updateMotionMeasurement(
    uuid = measurementUuid,
    userInputMetaData = OSTUserInputMetaData(
        assistiveDevice = OSTAssistiveDevice.WALKER,
        levelOfAssistance = OSTLevelOfAssistance.MINIMAL_ASSISTANCE
    )
)

// ✅ v2 - Update 6MWT course length
OneStep.motionLab.updateSixMinuteWalkCourseLength(
    uuid = measurementUuid,
    walkCourseLength = OSTWalkCourseLength(
        value = 30,
        unit = "meters"
    )
)

// ✅ v2 - Delete measurement
OneStep.motionLab.deleteMotionMeasurement(uuid = measurementUuid)
```

### Measurement Units (NEW in v2)

```kotlin
// ✅ v2 - Set measurement units
OneStep.motionLab.setMeasurementUnits(OSTMeasurementSystem.METRIC)
// or
OneStep.motionLab.setMeasurementUnits(OSTMeasurementSystem.IMPERIAL)
```

### Advanced: Direct Recorder Access

```kotlin
// ✅ v2 - For advanced use cases requiring direct recorder control
val recorder = OneStep.motionLab.getRecordingService()
// Use recorder methods directly (not recommended for most use cases)
```

### Reset MotionLab State

```kotlin
// ✅ v2 - Reset to clean state
OneStep.motionLab.reset()
```

### Key Changes Summary

| Feature | v1 | v2 |
|---------|----|----|
| **Initialization** | Automatic | Ready immediately after identification |
| **Pre-warming** | N/A | `prepareForRecording()` available |
| **Start recording** | `recorder.start()` | `motionLab.start()` |
| **Stop recording** | `recorder.stop()` | `motionLab.stop()` |
| **Analyze** | `recorder.analyze()` | `motionLab.analyze()` |
| **Markers** | N/A | `motionLab.addMarker()` |
| **Read measurements** | Via data service | `motionLab.readMotionMeasurements()` |
| **Update measurement** | Via data service | `motionLab.updateMotionMeasurement()` |
| **Delete measurement** | Via data service | `motionLab.deleteMotionMeasurement()` |
| **Units** | In config | `motionLab.setMeasurementUnits()` |
| **Reset** | N/A | `motionLab.reset()` |

---

## 8. Monitoring Migration

Monitoring is the product surface for continuous background activity tracking with built-in GDPR compliance.

### v1: Auto-Start with Configuration Flag

```kotlin
// ❌ v1
val config = OSTSdkConfiguration(
    enableMonitoringFeature = true,
    collectPedometer = true,
    monitoringIntensity = OSTMonitoringIntensity.Enhanced,
    monitoringNotificationConfig = OSTDefaultNotificationConfig(
        title = { "Activity Monitoring" },
        text = { "Tracking your daily movement" },
        icon = R.drawable.ic_monitoring
    )
)

OneStep.Builder(application, appId, apiKey)
    .setConfiguration(config)
    .build()

// Monitoring started automatically
```

### v2: Explicit Initialization with Enrollment Policy

```kotlin
// ✅ v2

// 1. Enable monitoring (after SDK init + identify)
OneStep.monitoring.enable(
    config = OSTMonitoringConfig(
        enrollmentPolicy = OSTMonitoringConfig.EnrollmentPolicy.AUTO_ENROLL_AFTER_AUTH,
        // or EXPLICIT_OPT_IN_REQUIRED for GDPR compliance
        debug = OSTMonitoringConfig.Debug(
            verboseLogging = false,       // Enable for debugging
            showDebugNotifications = false // Enable for diagnostic overlays
        )
    )
)

// 2. For EXPLICIT_OPT_IN_REQUIRED: Show consent dialog and opt-in
fun showConsentDialog() {
    MaterialAlertDialogBuilder(context)
        .setTitle("Activity Monitoring")
        .setMessage("Allow OneStep to track your daily activity?")
        .setPositiveButton("Allow") { _, _ ->
            lifecycleScope.launch {
                OneStep.monitoring.optIn()
            }
        }
        .setNegativeButton("Deny") { _, _ ->
            lifecycleScope.launch {
                OneStep.monitoring.optOut()
            }
        }
        .show()
}

// 3. Observe monitoring state
lifecycleScope.launch {
    OneStep.monitoring.state.collect { state ->
        when (state) {
            is OSTMonitoringRuntimeState.Inactive -> {
                // Not running
                showInactiveUI()
            }
            is OSTMonitoringRuntimeState.Blocked -> {
                // Blocked by one or more reasons
                state.reasons.forEach { blocker ->
                    when (blocker) {
                        OSTMonitoringBlocker.IDENTIFICATION_REQUIRED -> {
                            showLoginPrompt()
                        }
                        OSTMonitoringBlocker.PERMISSIONS_REQUIRED -> {
                            requestActivityRecognitionPermission()
                        }
                        OSTMonitoringBlocker.OPT_IN_REQUIRED -> {
                            showConsentDialog()
                        }
                        OSTMonitoringBlocker.OPTED_OUT -> {
                            showOptedOutUI()
                        }
                    }
                }
            }
            is OSTMonitoringRuntimeState.Active -> {
                // Monitoring is running
                showActiveUI()
            }
            is OSTMonitoringRuntimeState.Error -> {
                // Error occurred
                Log.e("Monitoring", "Error: ${state.throwable.message}")
                showErrorUI(state.throwable.message)
            }
        }
    }
}

// 4. Observe user preference
lifecycleScope.launch {
    OneStep.monitoring.preference.collect { pref ->
        when (pref) {
            OSTMonitoringPreference.NOT_SET -> { /* no decision yet */ }
            OSTMonitoringPreference.OPTED_IN -> { /* user allowed */ }
            OSTMonitoringPreference.OPTED_OUT -> { /* user denied */ }
        }
    }
}
```

### Daily Summaries (NEW in v2)

```kotlin
// ✅ v2 - Get daily summary
val summary = OneStep.monitoring.getDailySummary(LocalDate.now())
summary?.let {
    Log.d("Steps", "Today's steps: ${it.steps}")
    Log.d("Parameters", "Raw parameters: ${it.parameters}")
}

// ✅ v2 - Get multiple daily summaries
val summaries = OneStep.monitoring.getDailySummaries(
    query = OSTDailySummariesQuery(
        from = LocalDate.now().minusDays(7),
        to = LocalDate.now(),
        order = OSTSortOrder.DESC,
        maxDays = 31
    )
)

summaries.forEach { summary ->
    Log.d("Summary", "${summary.date}: ${summary.steps} steps")
}
```

### Step Bouts (NEW in v2)

```kotlin
// ✅ v2 - Initialize step bouts (if not using monitoring.enable)
OneStep.monitoring.stepBouts.initialize()

// Get daily steps aggregation
val dailySteps = OneStep.monitoring.stepBouts.getDailySteps(
    from = startDate,
    to = endDate
)

// Get hourly steps aggregation
val hourlySteps = OneStep.monitoring.stepBouts.getHourlySteps(
    from = startDateTime,
    to = endDateTime
)

// Get walking bouts (continuous walking periods)
val bouts = OneStep.monitoring.stepBouts.getWalkingBouts(
    from = startDateTime,
    to = endDateTime
)

bouts.forEach { bout ->
    Log.d("Bout", "Steps: ${bout.steps}, Duration: ${bout.durationMillis}ms")
}
```

### V1-Compatible Bridges

```kotlin
// ✅ v2 - Access walking bouts service directly (for v1 compatibility)
val walkingBoutsService = OneStep.monitoring.getWalkingBoutsService()

// ✅ v2 - Set custom notification (for v1 compatibility)
OneStep.monitoring.setCustomMonitoringNotification(
    OSTDefaultNotificationConfig(
        title = { "Activity Monitoring" },
        text = { "Tracking your daily movement" },
        icon = R.drawable.ic_monitoring
    )
)
```

### Enrollment Policies

| Policy | Behavior | Use Case |
|--------|----------|----------|
| `AUTO_ENROLL_AFTER_AUTH` | Monitoring starts automatically after user is identified | Low-friction apps where monitoring is core feature |
| `EXPLICIT_OPT_IN_REQUIRED` | Monitoring requires explicit `optIn()` call | GDPR-compliant apps, apps where monitoring is optional |

### Monitoring State Machine

```
Uninitialized
    ↓ enable()
Ready (if AUTO_ENROLL_AFTER_AUTH) or Blocked(OPT_IN_REQUIRED)
    ↓ identify() + optIn() (if needed)
Active (if all permissions granted) or Blocked(PERMISSIONS_REQUIRED)
    ↓ permission granted
Active
    ↓ optOut()
Blocked(OPTED_OUT)
```

### Key Changes Summary

| Feature | v1 | v2 |
|---------|----|----|
| **Enable monitoring** | Config flag | `monitoring.enable()` |
| **GDPR compliance** | Manual implementation | Built-in enrollment policies |
| **Opt-in/out** | Custom implementation | `optIn()` / `optOut()` |
| **State observation** | Limited | Rich state machine with blockers |
| **Daily summaries** | Via data service | `getDailySummary()` / `getDailySummaries()` |
| **Step bouts** | Limited | Full `stepBouts` API |
| **Permissions handling** | Manual | Automatic blocker detection |

---

## 9. Insights Migration

Insights is the product surface for analytics and interpretation of motion data.

### v1: Direct Data Service Access

```kotlin
// ❌ v1
// Access varied depending on implementation
```

### v2: Unified Insights Interface

```kotlin
// ✅ v2 - Get data service
val dataService = OneStep.insights.getMotionDataService()

// Get all parameter metadata
val allMetadata = dataService.getAllParametersMetadata()
allMetadata.forEach { (paramName, metadata) ->
    Log.d("Parameter", "$paramName: ${metadata.displayName}")
    Log.d("Parameter", "  Unit: ${metadata.units}")
    Log.d("Parameter", "  Category: ${metadata.category}")
}

// Get the primary parameter for a measurement
val mainParameter = dataService.mainParam(measurement)
mainParameter?.let { entry ->
    Log.d("Main", "Primary metric: ${entry.key} = ${entry.value}")
}

// Get specific parameter metadata
val velocityMeta = dataService.getParameterMetadata(OSTParamName.WALKING_VELOCITY)
velocityMeta?.let {
    Log.d("Velocity", "Display: ${it.displayName}")
    Log.d("Velocity", "Unit: ${it.unit}")
    Log.d("Velocity", "Description: ${it.description}")
}

// Get norm for a parameter
val cadenceNorm = dataService.getNormByName(OSTParamName.WALKING_CADENCE)
cadenceNorm?.let { norm ->
    Log.d("Norm", "Min: ${norm.min}, Max: ${norm.max}")
}

// Check if value is within norms
val velocityValue = 1.2f
val isNormal = dataService.isWithinNorms(
    paramName = OSTParamName.WALKING_VELOCITY,
    value = velocityValue
)
Log.d("Velocity", "1.2 m/s is ${if (isNormal) "within" else "outside"} normal range")

// Get discrete score (classification)
val velocityScore = dataService.discreteScore(
    paramName = OSTParamName.WALKING_VELOCITY,
    value = velocityValue
)
Log.d("Score", "Velocity score: $velocityScore")

// Get insights for a measurement
val result = dataService.getInsights(measurementUuid)
when (result) {
    is OSTResult.Success -> {
        result.data.insights.forEach { insight ->
            Log.d("Insight", "Type: ${insight.insightType}")
            Log.d("Insight", "Text: ${insight.textMarkdown}")
            Log.d("Insight", "Severity: ${insight.severity}")
        }
    }
    is OSTResult.Error -> {
        Log.e("Insights", "Failed to get insights: ${result.exception.message}")
    }
}
```

### OSTResult Pattern

```kotlin
sealed class OSTResult<out T> {
    data class Success<out T>(val data: T) : OSTResult<T>()
    data class Error(val exception: Throwable, val code: Int? = null) : OSTResult<Nothing>()
}

// Usage pattern
when (val result = dataService.getInsights(uuid)) {
    is OSTResult.Success -> {
        // Handle success
        val insights = result.data
    }
    is OSTResult.Error -> {
        // Handle error
        Log.e("Error", result.exception.message)
    }
}
```

### Key Changes Summary

| Feature | v1 | v2 |
|---------|----|----|
| **Access** | Via data service | `insights.getMotionDataService()` |
| **Parameter metadata** | Limited | Full metadata with display names, units, descriptions |
| **Norms** | Manual lookup | `getNormByName()`, `isWithinNorms()` |
| **Scoring** | Manual calculation | `discreteScore()` |
| **Insights** | Limited | Rich insights with markdown, severity, type |
| **Error handling** | Exceptions | `OSTResult<T>` sealed class |

---

## 10. Configuration Migration

v1's monolithic `OSTSdkConfiguration` has been split across multiple product-level configurations in v2.

### Configuration Mapping Table

| v1 Field | v2 Equivalent |
|----------|---------------|
| `enableMonitoringFeature` | `OneStep.monitoring.enable()` |
| `monitoringNotificationConfig` | `OneStep.monitoring.setCustomMonitoringNotification()` |
| `monitoringIntensity` | Managed internally by SDK |
| `collectPedometer` | `OneStep.monitoring.stepBouts.initialize()` |
| `recorderNotificationConfig` | Set via `OSTRecorder` service |
| `recorderCollectGeoLocation` | Managed via Android permissions |
| `useImperialSystem` | `OneStep.configuration.setMeasurementUnits()` or `OneStep.motionLab.setMeasurementUnits()` |
| `mockIMU` | `OneStep.configuration.setMockIMU()` |
| `userAttributes` | `OneStep.updateUserAttributes()` |
| `analyticsHandler` | `OneStep.events` Flow |
| `additionalConfigurations` | `OSTConfiguration(additionalConfig = ...)` |
| `syncConfigurations` | Managed internally by SDK |
| `remoteDataRetentionPeriodHours` | Managed internally by SDK |
| `incognitoUserProfile` | Managed internally by SDK |
| `sendAnalyticsData` | Managed internally by SDK |

### Migration Examples

#### Measurement Units

```kotlin
// ❌ v1
OSTSdkConfiguration(
    useImperialSystem = OSTMeasurementSystem.METRIC
)

// ✅ v2 - Global configuration
OneStep.configuration.setMeasurementUnits(OSTMeasurementSystem.METRIC)

// ✅ v2 - MotionLab-specific (overrides global)
OneStep.motionLab.setMeasurementUnits(OSTMeasurementSystem.IMPERIAL)
```

#### Mock IMU (for testing)

```kotlin
// ❌ v1
OSTSdkConfiguration(
    mockIMU = true
)

// ✅ v2
OneStep.configuration.setMockIMU(OSTMockIMU.SUCCESSFUL)  // simulate walk
// or
OneStep.configuration.setMockIMU(OSTMockIMU.TUG_SUCCESS)  // simulate TUG
// or
OneStep.configuration.setMockIMU(OSTMockIMU.NONE)  // use real sensors (default)
```

#### User Attributes

```kotlin
// ❌ v1
OSTSdkConfiguration(
    userAttributes = mapOf(
        "age" to "35",
        "weight" to "70",
        "height" to "175"
    )
)

// ✅ v2
OneStep.updateUserAttributes(
    OSTUserAttributes.Builder()
        .withAge(35)
        .withWeightKg(70)
        .withHeightCm(175)
        .build()
)
```

#### Additional Configurations

```kotlin
// ❌ v1
OSTSdkConfiguration(
    additionalConfigurations = mapOf(
        "customFeature" to "enabled",
        "debugMode" to "true"
    )
)

// ✅ v2
OneStep.initialize(
    application = application,
    clientToken = clientToken,
    config = OSTConfiguration(
        additionalConfig = mapOf(
            "customFeature" to "enabled",
            "debugMode" to "true"
        )
    )
)
```

---

## 11. Notification Migration

### v1: Notifications in Configuration

```kotlin
// ❌ v1
OSTSdkConfiguration(
    monitoringNotificationConfig = OSTDefaultNotificationConfig(
        title = { "Activity Monitoring" },
        text = { "Tracking your daily movement" },
        icon = R.drawable.ic_monitoring
    ),
    recorderNotificationConfig = OSTDefaultNotificationConfig(
        title = { "Recording" },
        text = { "Recording in progress" },
        icon = R.drawable.ic_recording
    )
)
```

### v2: Product-Level Notification Configuration

```kotlin
// ✅ v2 - Monitoring notification
OneStep.monitoring.setCustomMonitoringNotification(
    OSTDefaultNotificationConfig(
        title = { "Activity Monitoring" },
        text = { "Tracking your daily movement" },
        icon = R.drawable.ic_monitoring
    )
)

// ✅ v2 - Recording notification (via recorder service)
val recorder = OneStep.motionLab.getRecordingService()
// Configure recorder notification if needed
```

### Notification Config Types

All three notification config types from v1 remain available in v2:

#### 1. OSTDefaultNotificationConfig

```kotlin
OSTDefaultNotificationConfig(
    title = { "Title" },
    text = { "Description" },
    icon = R.drawable.ic_notification
)
```

#### 2. OSTCustomNotificationConfig

```kotlin
OSTCustomNotificationConfig(
    appName = "MyApp",
    stepsGoal = 10000,
    icon = R.drawable.ic_notification
)
```

#### 3. OSTNativeNotificationConfig

```kotlin
OSTNativeNotificationConfig(
    notification = NotificationCompat.Builder(context, channelId)
        .setContentTitle("Custom Title")
        .setContentText("Custom Text")
        .setSmallIcon(R.drawable.ic_notification)
        .build(),
    icon = R.drawable.ic_notification
)
```

---

## 12. Logout Migration

### v1: disconnect()

```kotlin
// ❌ v1
OneStep.disconnect()
```

### v2: logout()

```kotlin
// ✅ v2
OneStep.logout()

// What logout() does:
// 1. Stops monitoring if active
// 2. Clears user credentials and identification
// 3. Reverts SDK state to Ready (initialized but no user)
// 4. Preserves SDK initialization (no need to call initialize() again)
```

### State Transitions

```
Identified (userId = "user-123")
    ↓ logout()
Ready (no user)
    ↓ identify(newUserId)
Identified (userId = "newUserId")
```

### Example: Logout Flow

```kotlin
// ✅ v2 - Complete logout flow
fun logoutUser() {
    lifecycleScope.launch {
        // Stop monitoring if active
        if (OneStep.monitoring.preference.value == OSTMonitoringPreference.OPTED_IN) {
            OneStep.monitoring.optOut()
        }

        // Logout from SDK
        OneStep.logout()

        // Observe state change
        OneStep.state.collect { state ->
            when (state) {
                is OSTState.Ready -> {
                    // Successfully logged out
                    navigateToLogin()
                }
                is OSTState.Error -> {
                    showError("Logout failed: ${state.message}")
                }
                else -> { /* ignore */ }
            }
        }
    }
}
```

---

## 12.5. Permission Handling Migration

### v1: Manual Callback

In v1, you were required to notify the SDK after requesting permissions:

```kotlin
// ❌ v1
OneStep.onPermissionGranted(permission, granted)
```

This was called after requesting runtime permissions (e.g., `ACTIVITY_RECOGNITION`) to inform the SDK of the result.

### v2: Automatic Lifecycle Detection

In v2, the SDK detects permission changes automatically via Android lifecycle events. **No manual notification is needed** — just request the permission normally using standard Android APIs.

```kotlin
// ✅ v2 - Just request the permission normally
val permissionLauncher = registerForActivityResult(
    ActivityResultContracts.RequestPermission()
) { granted ->
    // No need to call OneStep.onPermissionGranted() — SDK detects this automatically
    if (granted) {
        Log.d("Permissions", "Permission granted — SDK will detect automatically")
    }
}

permissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
```

### What Changed

| Aspect | v1 | v2 |
|--------|----|----|
| **Permission notification** | Manual `onPermissionGranted()` call required | Automatic via lifecycle observers |
| **Developer responsibility** | Must call SDK after each permission result | Just request permission normally |
| **Detection** | Only when explicitly notified | Continuous lifecycle-based detection |

---

## 13. Removed APIs

These v1 classes and methods no longer exist in v2. Use the v2 replacements instead.

### Removed Classes

| v1 Class | Purpose | v2 Replacement |
|----------|---------|----------------|
| `OneStep.Builder` | SDK initialization builder | `OneStep.initialize()` + `OneStep.identify()` |
| `OSTSdkConfiguration` | Monolithic configuration object | `OSTConfiguration` (simplified) + product-level configs |
| `OSTMonitoringIntensity` | Monitoring intensity enum | Managed internally |
| `OSTBackgroundRecord` | Background monitoring data aggregation model | `OSTMonitoringDailySummary` via `monitoring.getDailySummary()` |
| `OSTBackgroundMetadata` | Metadata for background records | `OSTMonitoringDailySummary.parameters` |
| `StatelessMotionDataService` | Stateless insights implementation | `OSTMotionDataService` via `insights.getMotionDataService()` |
| `BgRepository` / `BgRepositoryImpl` | Background record storage | `monitoring.getDailySummaries()` |
| `AnalyzedBgSampleDao` | Room DAO for analyzed background samples | Internal — managed by SDK |
| `BackgroundWalkDto` / `BackgroundWalkDataResponse` | DTO models for background walks | Internal — managed by SDK |
| `DataAggregator` | Background data aggregation logic | Internal — managed by SDK |
| `NoRetentionDataRequest` | Stateless data request model | Removed — no longer applicable |
| `SDKMeasurement` | v1 measurement wrapper | `OSTMotionMeasurement` |
| `SdkDailyBackgroundMeasurement` | v1 daily summary model | `OSTMonitoringDailySummary` |
| `StatelessInsightsRequest` / `StatelessInsightsDto` | v1 insights request/response | `OSTMotionDataService.getInsights()` |
| `PatientData` | v1 patient data model | `OSTUserAttributes` with Builder |
| `OSTJsonSerialization` | v1 custom JSON handling | Internal — managed by SDK |

> **Note:** `OSTSdkConfiguration` class still exists in the codebase because the notification config types (`OSTNotificationConfig`, `OSTDefaultNotificationConfig`, `OSTCustomNotificationConfig`, `OSTNativeNotificationConfig`) are defined in the same file. However, the `OSTSdkConfiguration` configuration pattern itself is no longer used for SDK initialization.

### Removed Methods

| v1 Method | v2 Replacement |
|-----------|----------------|
| `OneStep.Builder(...).build()` | `OneStep.initialize()` |
| `OneStep.isInitialized()` | `OneStep.state` StateFlow (check `.value` or collect) |
| `OneStep.disconnect()` | `OneStep.logout()` |
| `OneStep.enableBackgroundMonitoring()` | `OneStep.monitoring.enable()` + `OneStep.monitoring.optIn()` |
| `OneStep.getRecorder()` | `OneStep.motionLab.getRecordingService()` |
| `OneStep.onPermissionGranted(perm, granted)` | Automatic — SDK detects permission changes via lifecycle |

### Removed Configuration Fields

| v1 Field | v2 Replacement |
|----------|----------------|
| `config.enableMonitoringFeature` | `OneStep.monitoring.enable()` |
| `config.collectPedometer` | `OneStep.monitoring.stepBouts.initialize()` |
| `config.monitoringIntensity` | Managed internally |
| `config.recorderCollectGeoLocation` | Managed via Android permissions |
| `config.syncConfigurations` | Managed internally |
| `config.remoteDataRetentionPeriodHours` | Managed internally |
| `config.incognitoUserProfile` | Managed internally |
| `config.sendAnalyticsData` | Managed internally |

---

## 14. Quick Reference Table

### Core SDK

| v1 API | v2 API |
|--------|--------|
| `OneStep.Builder(app, appId, apiKey).build()` | `OneStep.initialize(app, clientToken, config)` |
| `.setIdentityVerification(hmac)` | `OneStep.identify(userId, identityVerification)` |
| N/A | `OneStep.connectAsUser(userId, jwt)` |
| `OneStep.disconnect()` | `OneStep.logout()` |
| `OneStep.isInitialized()` | `OneStep.state` (StateFlow — observe or check `.value`) |
| N/A | `OneStep.sync()` |
| N/A | `OneStep.updateUserAttributes(attrs)` |
| N/A | `OneStep.updatePushToken(token)` |
| N/A | `OneStep.handleNotification(payload)` |
| `OneStep.onPermissionGranted(perm, granted)` | Removed — automatic detection via lifecycle |

### Configuration

| v1 API | v2 API |
|--------|--------|
| `OSTSdkConfiguration(...)` | `OSTConfiguration(appId?, additionalConfig)` |
| `config.enableMonitoringFeature = true` | `OneStep.monitoring.enable()` |
| `config.collectPedometer = true` | `OneStep.monitoring.stepBouts.initialize()` |
| `config.mockIMU = true` | `OneStep.configuration.setMockIMU(OSTMockIMU.SUCCESSFUL)` |
| `config.useImperialSystem = ...` | `OneStep.configuration.setMeasurementUnits(...)` |
| `config.userAttributes = attrs` | `OneStep.updateUserAttributes(attrs)` |
| `config.analyticsHandler = handler` | `OneStep.events.collect { ... }` |

### MotionLab

| v1 API | v2 API |
|--------|--------|
| `OneStep.getRecorder()` | `OneStep.motionLab.getRecordingService()` |
| `recorder.start(...)` | `OneStep.motionLab.start(...)` |
| `recorder.stop()` | `OneStep.motionLab.stop()` |
| `recorder.analyze()` | `OneStep.motionLab.analyze()` |
| N/A | `OneStep.motionLab.prepareForRecording(type)` |
| N/A | `OneStep.motionLab.addMarker(label)` |
| N/A | `OneStep.motionLab.readMotionMeasurements(...)` |
| N/A | `OneStep.motionLab.readSingleMotionMeasurement(uuid)` |
| N/A | `OneStep.motionLab.updateMotionMeasurement(uuid, data)` |
| N/A | `OneStep.motionLab.deleteMotionMeasurement(uuid)` |
| N/A | `OneStep.motionLab.setMeasurementUnits(system)` |
| N/A | `OneStep.motionLab.reset()` |

### Monitoring

| v1 API | v2 API |
|--------|--------|
| `config.enableMonitoringFeature = true` | `OneStep.monitoring.enable(config)` |
| N/A | `OneStep.monitoring.optIn()` / `optOut()` |
| N/A | `OneStep.monitoring.state` |
| N/A | `OneStep.monitoring.preference` |
| N/A | `OneStep.monitoring.getDailySummary(date)` |
| N/A | `OneStep.monitoring.getDailySummaries(query)` |
| N/A | `OneStep.monitoring.stepBouts.*` |
| N/A | `OneStep.monitoring.getWalkingBoutsService()` |
| `config.monitoringNotificationConfig` | `OneStep.monitoring.setCustomMonitoringNotification(...)` |

### Insights

| v1 API | v2 API |
|--------|--------|
| Data service access varied | `OneStep.insights.getMotionDataService()` |
| N/A | `dataService.mainParam(measurement)` |
| N/A | `dataService.getAllParametersMetadata()` |
| N/A | `dataService.getParameterMetadata(name)` |
| N/A | `dataService.getNormByName(name)` |
| N/A | `dataService.isWithinNorms(name, value)` |
| N/A | `dataService.discreteScore(name, value)` |
| N/A | `dataService.getInsights(uuid)` |

---

## 15. Migration Checklist

Use this checklist to ensure a complete migration:

### Setup

- [ ] Update SDK version in `build.gradle` to v2.x.x
- [ ] Sync Gradle and resolve any dependency conflicts
- [ ] Update imports from `co.onestep.android.core.external` to `co.onestep.android.core`

### Initialization

- [ ] Replace `OneStep.Builder(...)` with `OneStep.initialize(...)`
- [ ] Remove `OSTSdkConfiguration` object
- [ ] Replace with `OSTConfiguration` (appId and additionalConfig only)
- [ ] Move monitoring, recording, and other configs to product-level initialization

### Authentication

- [ ] Replace `.setIdentityVerification(hmac)` with separate `OneStep.identify(userId, identityVerification)` call
- [ ] Handle `OSTIdentifyResult` (Success/Failure) from `identify()`
- [ ] Add error handling for `OSTIdentifyError` cases
- [ ] Note: `appId` and `apiKey` are no longer passed to `identify()` — the client token handles authentication
- [ ] Consider using `connectAsUser()` if JWT authentication is available

### State Management

- [ ] Replace state callbacks with `OneStep.state.collect { ... }`
- [ ] Handle all `OSTState` cases: Uninitialized, Ready, Identified, Error
- [ ] Replace analytics handler with `OneStep.events.collect { ... }`
- [ ] Forward events to your analytics platform

### MotionLab

- [ ] Update recorder access: `OneStep.getRecorder()` → `OneStep.motionLab.getRecordingService()`
- [ ] Update recording methods: `recorder.start()` → `OneStep.motionLab.start()`
- [ ] Update stop: `recorder.stop()` → `OneStep.motionLab.stop()`
- [ ] Update analyze: `recorder.analyze()` → `OneStep.motionLab.analyze()`
- [ ] Consider using `prepareForRecording()` for faster start times
- [ ] Add markers during recording if needed: `motionLab.addMarker(label)`
- [ ] Update measurement CRUD operations to use `motionLab.*` methods
- [ ] Set measurement units: `motionLab.setMeasurementUnits(system)`

### Monitoring

- [ ] Remove `enableMonitoringFeature` from config
- [ ] Add `OneStep.monitoring.enable(OSTMonitoringConfig(...))` after identify
- [ ] Choose enrollment policy: AUTO_ENROLL_AFTER_AUTH or EXPLICIT_OPT_IN_REQUIRED
- [ ] Add `OneStep.monitoring.optIn()` if using EXPLICIT_OPT_IN_REQUIRED
- [ ] Add monitoring state observation: `monitoring.state.collect { ... }` (after `monitoring.enable()` call)
- [ ] Handle `OSTMonitoringBlocker` cases in state observation
- [ ] Add preference observation: `monitoring.preference.collect { ... }`
- [ ] Update notification config: `monitoring.setCustomMonitoringNotification(...)`
- [ ] Replace pedometer with step bouts: `monitoring.stepBouts.initialize()`

### Configuration

- [ ] Move mock IMU: `OneStep.configuration.setMockIMU(...)`
- [ ] Move measurement units: `OneStep.configuration.setMeasurementUnits(...)`
- [ ] Move user attributes: `OneStep.updateUserAttributes(...)`
- [ ] Keep only appId and additionalConfig in `OSTConfiguration`

### Logout

- [ ] Replace `OneStep.disconnect()` with `OneStep.logout()`
- [ ] Add monitoring opt-out before logout if needed
- [ ] Handle state transitions after logout

### Push Notifications (NEW)

- [ ] Add `OneStep.updatePushToken(token)` when FCM token refreshes
- [ ] Add `OneStep.handleNotification(payload)` in notification receiver

### Permissions

The SDK automatically detects permission changes via lifecycle events — no explicit callback needed.

### Testing

- [ ] Test initialization flow with `initialize()` + `identify()`
- [ ] Test error handling for `OSTIdentifyResult.Failure`
- [ ] Test all recording flows (Walk, 6MWT, TUG, etc.)
- [ ] Test monitoring opt-in/opt-out flow
- [ ] Test monitoring blockers (permissions, opt-in, etc.)
- [ ] Test step bouts and daily summaries
- [ ] Test state observation and transitions
- [ ] Test logout and re-login flow
- [ ] Test measurement CRUD operations
- [ ] Test insights retrieval

### Cleanup

- [ ] Remove all `OSTSdkConfiguration` code
- [ ] Remove any v1-specific workarounds
- [ ] Update documentation and comments
- [ ] Remove unused imports

---

## 16. Troubleshooting Common Migration Errors

### Compilation Errors

#### "Unresolved reference: Builder"

`OneStep.Builder` was removed. Use `OneStep.initialize()` instead:

```kotlin
// ❌ v1
OneStep.Builder(application, appId, apiKey).build()

// ✅ v2
OneStep.initialize(application, clientToken, OSTConfiguration())
val result = OneStep.identify(userId, identityVerification)
```

#### "Unresolved reference: OSTSdkConfiguration"

The monolithic configuration object was replaced with `OSTConfiguration` and product-level configs:

```kotlin
// ❌ v1
val config = OSTSdkConfiguration(enableMonitoringFeature = true, ...)

// ✅ v2
OneStep.initialize(application, clientToken, OSTConfiguration())
OneStep.monitoring.enable(OSTMonitoringConfig(...))
```

#### "Unresolved reference: disconnect"

`disconnect()` was renamed to `logout()`:

```kotlin
// ❌ v1
OneStep.disconnect()

// ✅ v2
OneStep.logout()
```

#### "Unresolved reference: enableBackgroundMonitoring"

Background monitoring is now initialized through the Monitoring product surface:

```kotlin
// ❌ v1
OneStep.enableBackgroundMonitoring()

// ✅ v2
OneStep.monitoring.enable(OSTMonitoringConfig(
    enrollmentPolicy = OSTMonitoringConfig.EnrollmentPolicy.AUTO_ENROLL_AFTER_AUTH
))
OneStep.monitoring.optIn()  // if using EXPLICIT_OPT_IN_REQUIRED
```

#### "Unresolved reference: getRecorder"

Direct recorder access moved to MotionLab:

```kotlin
// ❌ v1
val recorder = OneStep.getRecorder()

// ✅ v2
val recorder = OneStep.motionLab.getRecordingService()
// Or use the higher-level MotionLab API directly:
OneStep.motionLab.start(activityType = OSTActivityType.WALK, duration = 60_000)
```

#### "Type mismatch: OSTBackgroundRecord"

`OSTBackgroundRecord` was removed entirely. Use the new daily summary APIs:

```kotlin
// ❌ v1
val records: List<OSTBackgroundRecord> = ...

// ✅ v2
val summary = OneStep.monitoring.getDailySummary(LocalDate.now())
// or
val summaries = OneStep.monitoring.getDailySummaries(OSTDailySummariesQuery(
    from = LocalDate.now().minusDays(7),
    to = LocalDate.now()
))
```

### Runtime Issues

#### State is always Uninitialized

Ensure `initialize()` is called before `identify()`. If using auto-initialization, ensure the Application context is available:

```kotlin
// ✅ Correct order
OneStep.initialize(application, clientToken)  // 1. Initialize first
val result = OneStep.identify(userId)  // 2. Then identify

// ✅ Or use auto-initialization (identify will call initialize internally)
val result = OneStep.identify(userId)
```

#### Monitoring not starting

Check `monitoring.state` for `OSTMonitoringBlocker` reasons:

```kotlin
OneStep.monitoring.state.collect { state ->
    when (state) {
        is OSTMonitoringRuntimeState.Blocked -> {
            state.reasons.forEach { blocker ->
                Log.w("Monitoring", "Blocked by: $blocker")
                // Common blockers:
                // IDENTIFICATION_REQUIRED → call identify() first
                // PERMISSIONS_REQUIRED → request ACTIVITY_RECOGNITION permission
                // OPT_IN_REQUIRED → call monitoring.optIn()
                // OPTED_OUT → user has opted out
            }
        }
        else -> { /* ... */ }
    }
}
```

---

## Support

If you encounter issues during migration:

1. **Check the docs**: Review the [Getting Started Guide](getting-started.md) and [API Reference](INDEX.md)
2. **Review examples**: Check example apps in the SDK repository
3. **Contact support**: Reach out to OneStep support with specific error messages and code snippets

---

**Last Updated**: February 2026
**SDK Version**: v2.0.0
