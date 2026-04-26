# OneStep SDK v2 - iOS Developer API Reference

Complete API mapping from Android Kotlin SDK to iOS Swift implementation.

## 1. Overview

### Architecture
The OneStep SDK v2 uses a **singleton pattern** with three distinct product surfaces:

```
OneStep (Singleton)
├── MotionLab    - Motion capture, analysis, clinical assessments
├── Monitoring   - Background activity tracking, daily summaries
└── Insights     - AI-driven analytics, parameter interpretations
```

### Design Principles

- **Clean Interface**: Single entry point (`OneStep`) with clear product separation
- **State Management**: Reactive state using StateFlow (Kotlin Coroutines) → iOS equivalent: `@Published`, Combine, or AsyncSequence
- **Async Operations**: Suspend functions for async work → iOS: `async throws`
- **Type Safety**: Sealed classes for exhaustive result handling → iOS: enums with associated values
- **SDK Lifecycle**: Clear state machine: `Uninitialized → Ready → Identified → Error`

### Package Structure
Android package: `co.onestep.android.core`
Models: `co.onestep.android.core.common.models.*` (the `external` package was removed in v2)

For iOS, recommended: `OneStepSDK` module/framework

---

## 2. SDK Lifecycle

### Entry Point

**Android (Kotlin):**
```kotlin
object OneStep
```

**iOS (Swift):**
```swift
class OneStep {
    static let shared = OneStep()
    private init() { }
}
```

### Initialization Methods

#### `initialize()`
**Android:**
```kotlin
fun initialize(
    application: Application,
    clientToken: String,
    config: OSTConfiguration = OSTConfiguration()
)
```

**iOS:**
```swift
func initialize(
    clientToken: String,
    config: OSTConfiguration = OSTConfiguration()
) async throws
```

**Usage:** Call once at app launch (Android: `Application.onCreate()`, iOS: `application(_:didFinishLaunchingWithOptions:)`)

**Note:** iOS does not have an `Application` object equivalent. The SDK should register for lifecycle notifications internally.

---

#### `identify()` - HMAC Authentication
**Android:**
```kotlin
suspend fun identify(
    userId: String,
    appId: String,
    apiKey: String,
    identityVerification: String? = null
): OSTIdentifyResult
```

**iOS:**
```swift
func identify(
    userId: String,
    appId: String,
    apiKey: String,
    identityVerification: String? = nil
) async throws -> OSTIdentifyResult
```

**Authentication:** Server generates `identityVerification = HMAC-SHA256(userId, oneStepSecret)`

**Auto-initialization:** If SDK not initialized, this method will auto-call `initialize()` first.

---

#### `connectAsUser()` - JWT Authentication
**Android:**
```kotlin
suspend fun connectAsUser(
    userId: String,
    jwt: String
): OSTIdentifyResult
```

**iOS:**
```swift
func connectAsUser(
    userId: String,
    jwt: String
) async throws -> OSTIdentifyResult
```

**Authentication:** Use existing JWT token for authentication.

**Auto-initialization:** If SDK not initialized, this method will auto-call `initialize()` first.

---

#### `logout()`
**Android:**
```kotlin
fun logout()
```

**iOS:**
```swift
func logout()
```

**Behavior:** Clears identity, stops monitoring, reverts to `Ready` state.

---

#### `sync()`
**Android:**
```kotlin
fun sync()
```

**iOS:**
```swift
func sync()
```

**Behavior:** Force immediate data synchronization with backend.

---

#### Checking Initialization State
**Android:**
```kotlin
// No isInitialized() method — use the state StateFlow instead:
val isReady = OneStep.state.value !is OSTState.Uninitialized
```

**iOS:**
```swift
// Use the state property:
let isReady = OneStep.shared.state.value != .uninitialized
```

**Behavior:** Check SDK initialization state via the reactive `state` property.

---

#### `updateUserAttributes()`
**Android:**
```kotlin
fun updateUserAttributes(userAttributes: OSTUserAttributes)
```

**iOS:**
```swift
func updateUserAttributes(_ userAttributes: OSTUserAttributes)
```

**Behavior:** Update user profile (demographics, surgery info, etc.)

---

### State Machine

```
Uninitialized → initialize() → Ready → identify()/connectAsUser() → Identified
                                                                    ↓
                                                                  Error
                                                                    ↓
                                                            logout() → Ready
```

---

### SDK Properties

**Android:**
```kotlin
val state: StateFlow<OSTState>
val events: Flow<OSTEvent>
val configuration: OSTRuntimeConfiguration
val motionLab: MotionLab
val monitoring: Monitoring
val insights: Insights
```

**iOS:**
```swift
// Option 1: Combine
@Published var state: OSTState
var events: AnyPublisher<OSTEvent, Never> { get }

// Option 2: AsyncSequence
var state: CurrentValueSubject<OSTState, Never>
var events: AsyncStream<OSTEvent> { get }

var configuration: OSTRuntimeConfiguration { get }
var motionLab: MotionLab { get }
var monitoring: Monitoring { get }
var insights: Insights { get }
```

---

## 3. Authentication

### Authentication Paths

#### 1. HMAC Authentication
```swift
// Server-side generates:
// identityVerification = HMAC-SHA256(userId, oneStepSecret)

let result = await OneStep.shared.identify(
    userId: "user123",
    appId: "app456",
    apiKey: "api_key_789",
    identityVerification: "server_generated_hmac"
)
```

#### 2. JWT Authentication
```swift
// Use existing JWT token
let result = await OneStep.shared.connectAsUser(
    userId: "user123",
    jwt: "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
)
```

---

### OSTIdentifyResult

**Android:**
```kotlin
sealed interface OSTIdentifyResult {
    data object Success : OSTIdentifyResult
    data class Failure(
        val error: OSTIdentifyError,
        val message: String
    ) : OSTIdentifyResult
}

enum class OSTIdentifyError {
    INVALID_CLIENT_TOKEN,
    VERIFICATION_FAILED,
    NETWORK_ERROR,
    NOT_INITIALIZED,
    UNKNOWN
}
```

**iOS:**
```swift
enum OSTIdentifyResult {
    case success
    case failure(error: OSTIdentifyError, message: String)
}

enum OSTIdentifyError {
    case invalidClientToken
    case verificationFailed
    case networkError
    case notInitialized
    case unknown
}
```

**Usage:**
```swift
let result = await OneStep.shared.identify(...)

switch result {
case .success:
    print("Authentication successful")
case .failure(let error, let message):
    print("Auth failed: \(error) - \(message)")
}
```

---

## 4. State & Events

### OSTState

**Android:**
```kotlin
sealed class OSTState {
    data object Uninitialized : OSTState()
    data object Ready : OSTState()
    data class Identified(val userId: String) : OSTState()
    data class Error(val code: Int, val message: String) : OSTState()
}
```

**iOS:**
```swift
enum OSTState {
    case uninitialized
    case ready
    case identified(userId: String)
    case error(code: Int, message: String)
}
```

**State Observation:**
```swift
// Combine approach
OneStep.shared.$state
    .sink { state in
        switch state {
        case .uninitialized:
            print("SDK not initialized")
        case .ready:
            print("SDK ready, waiting for authentication")
        case .identified(let userId):
            print("User identified: \(userId)")
        case .error(let code, let message):
            print("Error \(code): \(message)")
        }
    }
    .store(in: &cancellables)

// AsyncSequence approach
Task {
    for await state in OneStep.shared.stateSequence {
        // Handle state changes
    }
}
```

---

### OSTEvent

**Android:**
```kotlin
data class OSTEvent(
    val name: String,
    val properties: Map<String, Any>,
    val timestamp: Long
)
```

**iOS:**
```swift
struct OSTEvent {
    let name: String
    let properties: [String: Any]
    let timestamp: TimeInterval  // Note: Convert from millis to seconds
}
```

**Event Names:**
- `recording_started`
- `recording_completed`
- `recording_cancelled`
- `measurement_analyzed`
- `monitoring_started`
- `monitoring_stopped`
- `daily_summary_ready`
- `user_identified`
- `user_logged_out`
- `error_occurred`

**Event Observation:**
```swift
// Combine
OneStep.shared.events
    .sink { event in
        print("Event: \(event.name)")
    }
    .store(in: &cancellables)

// AsyncSequence
Task {
    for await event in OneStep.shared.events {
        print("Event: \(event.name)")
    }
}
```

---

## 5. MotionLab Product

### MotionLab Interface

**Android:**
```kotlin
interface MotionLab
```

**iOS:**
```swift
protocol MotionLab: AnyObject {
    // State properties
    var recorderState: CurrentValueSubject<OSTRecorderState, Never> { get }
    var stepsCount: CurrentValueSubject<Int, Never> { get }
    var analyserState: CurrentValueSubject<OSTAnalyserState, Never> { get }

    // Methods
    func initialize(config: OSTMotionLabConfig) async throws
    func prepareForRecording(activityType: OSTActivityType) async throws -> Bool
    func start(
        activityType: OSTActivityType,
        duration: TimeInterval?,
        sensorEnhancedMode: Bool,
        userInputMetadata: OSTUserInputMetaData?,
        customMetadata: [String: Any]?
    ) async throws
    func stop() async throws
    func reset()
    func analyze(timeout: TimeInterval) async throws -> OSTMotionMeasurement?
    func currentRecordingLimit() -> TimeInterval
    func addMarker(_ marker: String)
    func readMotionMeasurements(request: TimeRangedDataRequest?) async throws -> [OSTMotionMeasurement]
    func readSingleMotionMeasurement(uuid: String) async throws -> OSTMotionMeasurement?
    func updateMotionMeasurement(uuid: String, userInputMetaData: OSTUserInputMetaData) async throws
    func updateSixMinuteWalkCourseLength(uuid: String, requestBody: OSTWalkCourseLength) async throws
    func deleteMotionMeasurement(uuid: String)
    func getRecordingService() -> OSTRecorder
    func setMeasurementUnits(_ measurementSystem: OSTMeasurementSystem)
}
```

---

### State Properties

#### `recorderState`
**Android:**
```kotlin
val recorderState: StateFlow<OSTRecorderState>

enum class OSTRecorderState {
    INITIALIZED,
    RECORDING,
    FINALIZING,
    DONE
}
```

**iOS:**
```swift
var recorderState: CurrentValueSubject<OSTRecorderState, Never> { get }

enum OSTRecorderState {
    case initialized
    case recording
    case finalizing
    case done
}
```

---

#### `stepsCount`
**Android:**
```kotlin
val stepsCount: StateFlow<Int>
```

**iOS:**
```swift
var stepsCount: CurrentValueSubject<Int, Never> { get }
```

**Behavior:** Real-time step count during recording.

---

#### `analyserState`
**Android:**
```kotlin
val analyserState: StateFlow<OSTAnalyserState>

sealed class OSTAnalyserState {
    data object Idle : OSTAnalyserState()
    data object Uploading : OSTAnalyserState()
    data object Analyzing : OSTAnalyserState()
    data object Analyzed : OSTAnalyserState()
    data class Failed(
        val throwable: Throwable?,
        val error: OSTAnalyserError
    ) : OSTAnalyserState()
}
```

**iOS:**
```swift
var analyserState: CurrentValueSubject<OSTAnalyserState, Never> { get }

enum OSTAnalyserState {
    case idle
    case uploading
    case analyzing
    case analyzed
    case failed(throwable: Error?, error: OSTAnalyserError)
}
```

---

### Methods

#### `initialize()`
**Android:**
```kotlin
suspend fun initialize(config: OSTMotionLabConfig = OSTMotionLabConfig.defaults())
```

**iOS:**
```swift
func initialize(config: OSTMotionLabConfig = .defaults()) async throws
```

---

#### `prepareForRecording()`
**Android:**
```kotlin
suspend fun prepareForRecording(activityType: OSTActivityType): Boolean
```

**iOS:**
```swift
func prepareForRecording(activityType: OSTActivityType) async throws -> Bool
```

**Returns:** `true` if ready to record.

---

#### `start()`
**Android:**
```kotlin
suspend fun start(
    activityType: OSTActivityType,
    duration: Long? = null,  // milliseconds
    sensorEnhancedMode: Boolean = false,
    userInputMetadata: OSTUserInputMetaData? = OSTUserInputMetaData(),
    customMetadata: Map<String, Any>? = emptyMap()
)
```

**iOS:**
```swift
func start(
    activityType: OSTActivityType,
    duration: TimeInterval? = nil,  // seconds - NOTE: conversion needed
    sensorEnhancedMode: Bool = false,
    userInputMetadata: OSTUserInputMetaData? = OSTUserInputMetaData(),
    customMetadata: [String: Any]? = [:]
) async throws
```

**Important:** Duration is in **milliseconds** (Android) vs **seconds** (iOS standard). Convert: `seconds = millis / 1000.0`

---

#### `stop()`
**Android:**
```kotlin
suspend fun stop()
```

**iOS:**
```swift
func stop() async throws
```

---

#### `reset()`
**Android:**
```kotlin
fun reset()
```

**iOS:**
```swift
func reset()
```

**Behavior:** Resets recorder state without stopping.

---

#### `analyze()`
**Android:**
```kotlin
suspend fun analyze(timeout: Long = 60000): OSTMotionMeasurement?
```

**iOS:**
```swift
func analyze(timeout: TimeInterval = 60.0) async throws -> OSTMotionMeasurement?
```

**Returns:** Analyzed measurement or `nil` if analysis fails/times out.

---

#### `currentRecordingLimit()`
**Android:**
```kotlin
fun currentRecordingLimit(): Long  // milliseconds
```

**iOS:**
```swift
func currentRecordingLimit() -> TimeInterval  // seconds
```

---

#### `addMarker()`
**Android:**
```kotlin
fun addMarker(marker: String)
```

**iOS:**
```swift
func addMarker(_ marker: String)
```

**Behavior:** Timestamp event during recording for later analysis.

---

#### `readMotionMeasurements()`
**Android:**
```kotlin
suspend fun readMotionMeasurements(
    request: TimeRangedDataRequest? = null
): List<OSTMotionMeasurement>
```

**iOS:**
```swift
func readMotionMeasurements(
    request: TimeRangedDataRequest? = nil
) async throws -> [OSTMotionMeasurement]
```

---

#### `readSingleMotionMeasurement()`
**Android:**
```kotlin
suspend fun readSingleMotionMeasurement(uuid: String): OSTMotionMeasurement?
```

**iOS:**
```swift
func readSingleMotionMeasurement(uuid: String) async throws -> OSTMotionMeasurement?
```

---

#### `updateMotionMeasurement()`
**Android:**
```kotlin
suspend fun updateMotionMeasurement(
    uuid: String,
    userInputMetaData: OSTUserInputMetaData
)
```

**iOS:**
```swift
func updateMotionMeasurement(
    uuid: String,
    userInputMetaData: OSTUserInputMetaData
) async throws
```

---

#### `updateSixMinuteWalkCourseLength()`
**Android:**
```kotlin
suspend fun updateSixMinuteWalkCourseLength(
    uuid: String,
    requestBody: OSTWalkCourseLength
)
```

**iOS:**
```swift
func updateSixMinuteWalkCourseLength(
    uuid: String,
    requestBody: OSTWalkCourseLength
) async throws
```

---

#### `deleteMotionMeasurement()`
**Android:**
```kotlin
fun deleteMotionMeasurement(uuid: String)
```

**iOS:**
```swift
func deleteMotionMeasurement(uuid: String)
```

---

#### `getRecordingService()`
**Android:**
```kotlin
fun getRecordingService(): OSTRecorder
```

**iOS:**
```swift
func getRecordingService() -> OSTRecorder
```

---

#### `setMeasurementUnits()`
**Android:**
```kotlin
fun setMeasurementUnits(measurementSystem: OSTMeasurementSystem)
```

**iOS:**
```swift
func setMeasurementUnits(_ measurementSystem: OSTMeasurementSystem)
```

---

### OSTActivityType

**Android:**
```kotlin
enum class OSTActivityType(val serializedName: String, @StringRes val displayName: Int) {
    WALK("walk", R.string.walk),
    STS("sts", R.string.sit_to_stand),
    TUG("tug", R.string.timed_up_and_go),
    ROM_KNEE_FLEX("rom_knee_flex", R.string.knee_flexion),
    ROM_KNEE_EXT("rom_knee_ext", R.string.knee_extension),
    BALANCE_TEST("balance_test", R.string.balance_test),
    DUAL_TASK_WALK_SUBTRACT("dual_task_walk_subtract", R.string.dual_task_walk),
    SIX_MINUTE_WALK("walk_6_min_test", R.string.six_minute_walk),
    TWO_MINUTE_WALK("walk_2_min_test", R.string.two_minute_walk),
    STAIRS("stairs", R.string.stairs)
}
```

**iOS:**
```swift
enum OSTActivityType: String {
    case walk = "walk"
    case sts = "sts"
    case tug = "tug"
    case romKneeFlex = "rom_knee_flex"
    case romKneeExt = "rom_knee_ext"
    case balanceTest = "balance_test"
    case dualTaskWalkSubtract = "dual_task_walk_subtract"
    case sixMinuteWalk = "walk_6_min_test"
    case twoMinuteWalk = "walk_2_min_test"
    case stairs = "stairs"
}
```

---

### OSTAnalyserError

**Android:**
```kotlin
sealed interface OSTAnalyserError {
    data class TooShort(val error: String) : OSTAnalyserError
    data class General(
        val throwable: Throwable?,
        val error: String
    ) : OSTAnalyserError
    data class Timeout(
        val throwable: Throwable?,
        val error: String
    ) : OSTAnalyserError
    data class ServerError(
        val throwable: Throwable?,
        val error: String
    ) : OSTAnalyserError
    data class NetworkError(
        val throwable: Throwable?,
        val error: String
    ) : OSTAnalyserError
}
```

**iOS:**
```swift
enum OSTAnalyserError {
    case tooShort(error: String)
    case general(throwable: Error?, error: String)
    case timeout(throwable: Error?, error: String)
    case serverError(throwable: Error?, error: String)
    case networkError(throwable: Error?, error: String)
}
```

---

### OSTMotionLabConfig

**Android:**
```kotlin
data class OSTMotionLabConfig(
    val debug: Debug = Debug()
) {
    data class Debug(
        val mockImu: Boolean = false
    )

    companion object {
        fun defaults(): OSTMotionLabConfig = OSTMotionLabConfig()
    }
}
```

**iOS:**
```swift
struct OSTMotionLabConfig {
    var debug: Debug = Debug()

    struct Debug {
        var mockImu: Bool = false
    }

    static func defaults() -> OSTMotionLabConfig {
        return OSTMotionLabConfig()
    }
}
```

---

## 6. Monitoring Product

### Monitoring Interface

**Android:**
```kotlin
interface Monitoring
```

**iOS:**
```swift
protocol Monitoring: AnyObject {
    // Properties
    var preference: CurrentValueSubject<OSTMonitoringPreference, Never> { get }
    var state: CurrentValueSubject<OSTMonitoringRuntimeState, Never> { get }
    var stepBouts: OSTStepBouts { get }

    // Methods
    func initialize(config: OSTMonitoringConfig)
    func optIn() async throws
    func optOut() async throws
    func getDailySummary(date: Date) async throws -> OSTMonitoringDailySummary?
    func getDailySummaries(query: OSTDailySummariesQuery) async throws -> [OSTDailyBackgroundMeasurement]
    func getWalkingBoutsService() -> WalkingBoutsRepository  // v1 bridge
    func setCustomMonitoringNotification(notificationConfig: OSTNotificationConfig?)  // v1 bridge
}
```

---

### Properties

#### `preference`
**Android:**
```kotlin
val preference: StateFlow<OSTMonitoringPreference>

enum class OSTMonitoringPreference {
    NOT_SET,
    OPTED_IN,
    OPTED_OUT
}
```

**iOS:**
```swift
var preference: CurrentValueSubject<OSTMonitoringPreference, Never> { get }

enum OSTMonitoringPreference {
    case notSet
    case optedIn
    case optedOut
}
```

---

#### `state`
**Android:**
```kotlin
val state: StateFlow<OSTMonitoringRuntimeState>

sealed class OSTMonitoringRuntimeState {
    data object Inactive : OSTMonitoringRuntimeState()
    data class Blocked(val reasons: Set<OSTMonitoringBlocker>) : OSTMonitoringRuntimeState()
    data object Active : OSTMonitoringRuntimeState()
    data class Error(val throwable: Throwable) : OSTMonitoringRuntimeState()
}

enum class OSTMonitoringBlocker {
    IDENTIFICATION_REQUIRED,
    PERMISSIONS_REQUIRED,
    OPT_IN_REQUIRED,
    OPTED_OUT
}
```

**iOS:**
```swift
var state: CurrentValueSubject<OSTMonitoringRuntimeState, Never> { get }

enum OSTMonitoringRuntimeState {
    case inactive
    case blocked(reasons: Set<OSTMonitoringBlocker>)
    case active
    case error(throwable: Error)
}

enum OSTMonitoringBlocker: Hashable {
    case identificationRequired
    case permissionsRequired
    case optInRequired
    case optedOut
}
```

---

#### `stepBouts`
**Android:**
```kotlin
val stepBouts: OSTStepBouts
```

**iOS:**
```swift
var stepBouts: OSTStepBouts { get }
```

---

### Methods

#### `initialize()`
**Android:**
```kotlin
fun initialize(config: OSTMonitoringConfig = OSTMonitoringConfig.defaults())
```

**iOS:**
```swift
func initialize(config: OSTMonitoringConfig = .defaults())
```

---

#### `optIn()`
**Android:**
```kotlin
suspend fun optIn()
```

**iOS:**
```swift
func optIn() async throws
```

**Behavior:** User opts into background monitoring.

---

#### `optOut()`
**Android:**
```kotlin
suspend fun optOut()
```

**iOS:**
```swift
func optOut() async throws
```

**Behavior:** User opts out of background monitoring.

---

#### `getDailySummary()`
**Android:**
```kotlin
suspend fun getDailySummary(date: LocalDate): OSTMonitoringDailySummary?
```

**iOS:**
```swift
func getDailySummary(date: Date) async throws -> OSTMonitoringDailySummary?
```

**Note:** Convert iOS `Date` to calendar day for comparison.

---

#### `getDailySummaries()`
**Android:**
```kotlin
suspend fun getDailySummaries(
    query: OSTDailySummariesQuery = OSTDailySummariesQuery()
): List<OSTDailyBackgroundMeasurement>
```

**iOS:**
```swift
func getDailySummaries(
    query: OSTDailySummariesQuery = OSTDailySummariesQuery()
) async throws -> [OSTDailyBackgroundMeasurement]
```

---

#### `getWalkingBoutsService()`
**Android:**
```kotlin
fun getWalkingBoutsService(): WalkingBoutsRepository
```

**iOS:**
```swift
func getWalkingBoutsService() -> WalkingBoutsRepository
```

**Note:** v1 compatibility bridge.

---

#### `setCustomMonitoringNotification()`
**Android:**
```kotlin
fun setCustomMonitoringNotification(notificationConfig: OSTNotificationConfig?)
```

**iOS:**
```swift
func setCustomMonitoringNotification(notificationConfig: OSTNotificationConfig?)
```

**Note:** v1 compatibility bridge.

---

### OSTMonitoringConfig

**Android:**
```kotlin
data class OSTMonitoringConfig(
    val enrollmentPolicy: EnrollmentPolicy = EnrollmentPolicy.AUTO_ENROLL_AFTER_AUTH,
    val debug: Debug = Debug()
) {
    data class Debug(
        val verboseLogging: Boolean = false,
        val showDebugNotifications: Boolean = false
    )
}

enum class EnrollmentPolicy {
    EXPLICIT_OPT_IN_REQUIRED,
    AUTO_ENROLL_AFTER_AUTH
}
```

**iOS:**
```swift
struct OSTMonitoringConfig {
    var enrollmentPolicy: EnrollmentPolicy = .autoEnrollAfterAuth
    var debug: Debug = Debug()

    struct Debug {
        var verboseLogging: Bool = false
        var showDebugNotifications: Bool = false
    }

    static func defaults() -> OSTMonitoringConfig {
        return OSTMonitoringConfig()
    }
}

enum EnrollmentPolicy {
    case explicitOptInRequired
    case autoEnrollAfterAuth
}
```

---

### OSTDailySummariesQuery

**Android:**
```kotlin
data class OSTDailySummariesQuery(
    val from: LocalDate? = null,
    val to: LocalDate? = null,
    val order: OSTSortOrder = OSTSortOrder.DESC,
    val maxDays: Int = 31
)

enum class OSTSortOrder {
    ASC,
    DESC
}
```

**iOS:**
```swift
struct OSTDailySummariesQuery {
    var from: Date?
    var to: Date?
    var order: OSTSortOrder = .desc
    var maxDays: Int = 31
}

enum OSTSortOrder {
    case asc
    case desc
}
```

---

### OSTMonitoringDailySummary

**Android:**
```kotlin
data class OSTMonitoringDailySummary(
    val date: LocalDate,
    val steps: Int?,
    val parameters: Map<String, Float>
)
```

**iOS:**
```swift
struct OSTMonitoringDailySummary {
    let date: Date
    let steps: Int?
    let parameters: [String: Float]
}
```

---

### OSTStepBouts Interface

**Android:**
```kotlin
interface OSTStepBouts {
    fun initialize(onResult: ((Boolean) -> Unit)? = null)
    suspend fun getDailySteps(from: LocalDate?, to: LocalDate?): List<OSTStepDailySummary>
    suspend fun getHourlySteps(from: LocalDate?, to: LocalDate?): List<OSTStepHourlySummary>
    suspend fun getWalkingBouts(from: LocalDate?, to: LocalDate?): List<OSTWalkingBout>
    fun clear()
}
```

**iOS:**
```swift
protocol OSTStepBouts: AnyObject {
    func initialize(onResult: ((Bool) -> Void)?)
    func getDailySteps(from: Date?, to: Date?) async throws -> [OSTStepDailySummary]
    func getHourlySteps(from: Date?, to: Date?) async throws -> [OSTStepHourlySummary]
    func getWalkingBouts(from: Date?, to: Date?) async throws -> [OSTWalkingBout]
    func clear()
}
```

---

### Step Models

**Android:**
```kotlin
data class OSTStepDailySummary(
    val date: LocalDate,
    val totalSteps: Int
)

data class OSTStepHourlySummary(
    val hour: String,  // Format: "2024-01-15T14:00:00"
    val totalSteps: Int
)

data class OSTWalkingBout(
    val steps: Int,
    val startTimeMillis: Long,
    val durationMillis: Long
)
```

**iOS:**
```swift
struct OSTStepDailySummary {
    let date: Date
    let totalSteps: Int
}

struct OSTStepHourlySummary {
    let hour: String  // ISO 8601 format
    let totalSteps: Int
}

struct OSTWalkingBout {
    let steps: Int
    let startTime: TimeInterval  // Unix timestamp in seconds
    let duration: TimeInterval   // Duration in seconds
}
```

**Important:** Convert milliseconds to seconds for iOS `TimeInterval`.

---

## 7. Insights Product

### Insights Interface

**Android:**
```kotlin
interface Insights {
    suspend fun getMotionDataService(): OSTMotionDataService
}
```

**iOS:**
```swift
protocol Insights: AnyObject {
    func getMotionDataService() async throws -> OSTMotionDataService
}
```

---

### OSTMotionDataService Interface

**Android:**
```kotlin
interface OSTMotionDataService {
    fun mainParam(motionMeasurement: OSTMotionMeasurement): Map.Entry<OSTParamName, Float>?
    fun getAllParametersMetadata(): Map<OSTParamName, OSTParameterMetadata>
    fun getNormByName(name: OSTParamName?): OSTNorm?
    fun getParameterMetadata(paramName: OSTParamName): OSTParameterMetadata
    fun isWithinNorms(param: OSTParamName, value: Float): Boolean?
    fun discreteScore(motionMeasurement: OSTMotionMeasurement, value: Float): DiscreteColor?
    fun discreteScore(param: OSTParamName, value: Float): DiscreteColor?
    suspend fun getInsights(uuid: String): OSTResult<OSTInsights>
}
```

**iOS:**
```swift
protocol OSTMotionDataService: AnyObject {
    func mainParam(_ motionMeasurement: OSTMotionMeasurement) -> (key: OSTParamName, value: Float)?
    func getAllParametersMetadata() -> [OSTParamName: OSTParameterMetadata]
    func getNormByName(_ name: OSTParamName?) -> OSTNorm?
    func getParameterMetadata(_ paramName: OSTParamName) -> OSTParameterMetadata
    func isWithinNorms(param: OSTParamName, value: Float) -> Bool?
    func discreteScore(motionMeasurement: OSTMotionMeasurement, value: Float) -> DiscreteColor?
    func discreteScore(param: OSTParamName, value: Float) -> DiscreteColor?
    func getInsights(uuid: String) async throws -> OSTResult<OSTInsights>
}
```

---

### OSTInsights

**Android:**
```kotlin
data class OSTInsights(
    val uuid: String,
    val insights: List<OSTInsight>
)

data class OSTInsight(
    val paramName: OSTParamName?,
    val textMarkdown: String,
    val intent: OSTIntent?,
    val insightType: OSTInsightType?,
    val rank: Float
)

enum class OSTInsightType {
    TREND,
    COMPARISON,
    PARAMETER,
    FALL_RISK,
    EDUCATION,
    INFO
}

enum class OSTIntent {
    GOOD,
    NEUTRAL,
    BAD
}
```

**iOS:**
```swift
struct OSTInsights {
    let uuid: String
    let insights: [OSTInsight]
}

struct OSTInsight {
    let paramName: OSTParamName?
    let textMarkdown: String
    let intent: OSTIntent?
    let insightType: OSTInsightType?
    let rank: Float
}

enum OSTInsightType {
    case trend
    case comparison
    case parameter
    case fallRisk
    case education
    case info
}

enum OSTIntent {
    case good
    case neutral
    case bad
}
```

---

## 8. Data Models

### OSTMotionMeasurement

**Android:**
```kotlin
data class OSTMotionMeasurement(
    val id: String,
    val timestamp: Date,
    val type: OSTActivityType,
    val customMetadata: Map<String, Any>,
    val metadata: OSTMeasurementMetadata,
    val params: Map<OSTParamName, Float>,
    val parameterArrays: Map<String, List<Float>>,
    val status: MotionMeasurementStatus,
    val error: OSTError?,
    val resultState: OSTResultState?
)

enum class MotionMeasurementStatus {
    NOT_SYNCED,
    SYNCED,
    ANALYZED
}
```

**iOS:**
```swift
struct OSTMotionMeasurement {
    let id: String
    let timestamp: Date
    let type: OSTActivityType
    let customMetadata: [String: Any]
    let metadata: OSTMeasurementMetadata
    let params: [OSTParamName: Float]
    let parameterArrays: [String: [Float]]
    let status: MotionMeasurementStatus
    let error: OSTError?
    let resultState: OSTResultState?
}

enum MotionMeasurementStatus {
    case notSynced
    case synced
    case analyzed
}
```

---

### OSTMeasurementMetadata

**Android:**
```kotlin
data class OSTMeasurementMetadata(
    val locale: String?,
    val seconds: Int?,
    val steps: Int?,
    val lastModified: String?,
    val note: String?,
    val tags: List<String>,
    val assistiveDevice: Int?,
    val levelOfAssistance: Int?,
    val walkCourseLength: OSTWalkCourseLength?,
    val geoLat: Double?,
    val geoLng: Double?,
    val dataPath: String?,
    val audioDataPath: String?
)
```

**iOS:**
```swift
struct OSTMeasurementMetadata {
    let locale: String?
    let seconds: Int?
    let steps: Int?
    let lastModified: String?
    let note: String?
    let tags: [String]
    let assistiveDevice: Int?
    let levelOfAssistance: Int?
    let walkCourseLength: OSTWalkCourseLength?
    let geoLat: Double?
    let geoLng: Double?
    let dataPath: String?
    let audioDataPath: String?
}
```

---

### OSTResultState

**Android:**
```kotlin
enum class OSTResultState(val value: Int) {
    FULL_ANALYSIS(2),
    PARTIAL_ANALYSIS(1),
    EMPTY_ANALYSIS(0)
}
```

**iOS:**
```swift
enum OSTResultState: Int {
    case fullAnalysis = 2
    case partialAnalysis = 1
    case emptyAnalysis = 0
}
```

---

### OSTParamName (Complete Listing)

**Android:**
```kotlin
enum class OSTParamName(val columnName: String) {
    // Walking Parameters
    WALKING_STEPS("steps"),
    WALKING_CADENCE("cadence"),
    WALKING_VELOCITY("velocity"),
    WALKING_DOUBLE_SUPPORT("double_support"),
    WALKING_STANCE("stance"),
    WALKING_STANCE_ASYMMETRY("stance_asymmetry"),
    WALKING_STRIDE_LENGTH("stride_length"),
    WALKING_STEP_LENGTH("step_length"),
    WALKING_STEP_LENGTH_LEFT("step_length_left"),
    WALKING_STEP_LENGTH_RIGHT("step_length_right"),
    WALKING_STEP_LENGTH_DIFF("step_length_diff"),
    WALKING_STEP_LENGTH_ASYMMETRY("step_length_asymmetry"),
    WALKING_CONSISTENCY("consistency"),
    WALKING_HIP_RANGE("hip_range"),
    WALKING_BASE_WIDTH("base_width"),
    WALKING_DOUBLE_SUPPORT_ASYMMETRY("double_support_asymmetry"),
    WALKING_SINGLE_SUPPORT_RIGHT("single_support_right"),
    WALKING_SINGLE_SUPPORT_LEFT("single_support_left"),
    WALKING_STANCE_RIGHT("stance_right"),
    WALKING_STANCE_LEFT("stance_left"),
    WALKING_WALK_SCORE("walk_score"),
    WALKING_DISTANCE("distance"),
    WALKING_CADENCE_VARIABILITY("cadence_variability"),
    WALKING_VELOCITY_VARIABILITY("velocity_variability"),

    // Balance Parameters
    BALANCE_SENSORY_COMPOSITE_SCORE("balance_sensory_composite_score"),
    BALANCE_SENSORY_SINGLE_EC_SCORE("balance_sensory_single_ec_score"),
    BALANCE_SENSORY_SINGLE_EO_SCORE("balance_sensory_single_eo_score"),
    BALANCE_SENSORY_STABLE_EC_SCORE("balance_sensory_stable_ec_score"),
    BALANCE_SENSORY_STABLE_EO_SCORE("balance_sensory_stable_eo_score"),

    // TUG Parameters
    TUG_DURATION_SECONDS("tug_duration_seconds"),
    TUG_FORWARD_SECONDS("tug_forward_seconds"),
    TUG_BACKWARD_SECONDS("tug_backward_seconds"),
    TUG_SITTING_SECONDS("tug_sitting_seconds"),
    TUG_STANDING_SECONDS("tug_standing_seconds"),
    TUG_TURNING_SECONDS("tug_turning_seconds"),
    TUG_TURNING_TO_CHAIR_SECONDS("tug_turning_to_chair_seconds"),
    TUG_DISTANCE_METERS("tug_distance_meters"),

    // Sit-to-Stand Parameters
    STS_REPETITION_COUNT("sts_repetition_count"),
    STS_REPETITION_TIME("sts_repetition_time"),
    STS_REPETITION_VAR("sts_repetition_var"),
    STS_FATIGUE("sts_fatigue"),
    STS_ANGLE("sts_angle"),

    // Range of Motion Parameters
    RANGE_OF_MOTION_ANGLE("range_of_motion_angle"),
    HIP_EXT_RANGE_OF_MOTION_ANGLE("hip_ext_range_of_motion_angle"),
    HIP_FLEX_RANGE_OF_MOTION_ANGLE("hip_flex_range_of_motion_angle"),
    HIP_ABD_RANGE_OF_MOTION_ANGLE("hip_abd_range_of_motion_angle"),
    HIP_ADD_RANGE_OF_MOTION_ANGLE("hip_add_range_of_motion_angle"),
    KNEE_FLEX_RANGE_OF_MOTION_ANGLE("knee_flex_range_of_motion_angle"),
    KNEE_EXT_RANGE_OF_MOTION_ANGLE("knee_ext_range_of_motion_angle"),
    KNEE_FLEX_PASSIVE_RANGE_OF_MOTION_ANGLE("knee_flex_passive_range_of_motion_angle"),

    // 6-Minute and 2-Minute Walk Test Parameters
    SIX_MINUTE_WALK_DISTANCE_METERS("six_minute_walk_distance_meters"),
    SIX_MINUTE_WALK_LAPS("six_minute_walk_laps"),
    TWO_MINUTE_WALK_DISTANCE_METERS("two_minute_walk_distance_meters")
}
```

**iOS:**
```swift
enum OSTParamName: String {
    // Walking Parameters
    case walkingSteps = "steps"
    case walkingCadence = "cadence"
    case walkingVelocity = "velocity"
    case walkingDoubleSupport = "double_support"
    case walkingStance = "stance"
    case walkingStanceAsymmetry = "stance_asymmetry"
    case walkingStrideLength = "stride_length"
    case walkingStepLength = "step_length"
    case walkingStepLengthLeft = "step_length_left"
    case walkingStepLengthRight = "step_length_right"
    case walkingStepLengthDiff = "step_length_diff"
    case walkingStepLengthAsymmetry = "step_length_asymmetry"
    case walkingConsistency = "consistency"
    case walkingHipRange = "hip_range"
    case walkingBaseWidth = "base_width"
    case walkingDoubleSupportAsymmetry = "double_support_asymmetry"
    case walkingSingleSupportRight = "single_support_right"
    case walkingSingleSupportLeft = "single_support_left"
    case walkingStanceRight = "stance_right"
    case walkingStanceLeft = "stance_left"
    case walkingWalkScore = "walk_score"
    case walkingDistance = "distance"
    case walkingCadenceVariability = "cadence_variability"
    case walkingVelocityVariability = "velocity_variability"

    // Balance Parameters
    case balanceSensoryCompositeScore = "balance_sensory_composite_score"
    case balanceSensorySingleEcScore = "balance_sensory_single_ec_score"
    case balanceSensorySingleEoScore = "balance_sensory_single_eo_score"
    case balanceSensoryStableEcScore = "balance_sensory_stable_ec_score"
    case balanceSensoryStableEoScore = "balance_sensory_stable_eo_score"

    // TUG Parameters
    case tugDurationSeconds = "tug_duration_seconds"
    case tugForwardSeconds = "tug_forward_seconds"
    case tugBackwardSeconds = "tug_backward_seconds"
    case tugSittingSeconds = "tug_sitting_seconds"
    case tugStandingSeconds = "tug_standing_seconds"
    case tugTurningSeconds = "tug_turning_seconds"
    case tugTurningToChairSeconds = "tug_turning_to_chair_seconds"
    case tugDistanceMeters = "tug_distance_meters"

    // Sit-to-Stand Parameters
    case stsRepetitionCount = "sts_repetition_count"
    case stsRepetitionTime = "sts_repetition_time"
    case stsRepetitionVar = "sts_repetition_var"
    case stsFatigue = "sts_fatigue"
    case stsAngle = "sts_angle"

    // Range of Motion Parameters
    case rangeOfMotionAngle = "range_of_motion_angle"
    case hipExtRangeOfMotionAngle = "hip_ext_range_of_motion_angle"
    case hipFlexRangeOfMotionAngle = "hip_flex_range_of_motion_angle"
    case hipAbdRangeOfMotionAngle = "hip_abd_range_of_motion_angle"
    case hipAddRangeOfMotionAngle = "hip_add_range_of_motion_angle"
    case kneeFlexRangeOfMotionAngle = "knee_flex_range_of_motion_angle"
    case kneeExtRangeOfMotionAngle = "knee_ext_range_of_motion_angle"
    case kneeFlexPassiveRangeOfMotionAngle = "knee_flex_passive_range_of_motion_angle"

    // 6-Minute and 2-Minute Walk Test Parameters
    case sixMinuteWalkDistanceMeters = "six_minute_walk_distance_meters"
    case sixMinuteWalkLaps = "six_minute_walk_laps"
    case twoMinuteWalkDistanceMeters = "two_minute_walk_distance_meters"
}
```

---

### OSTUserInputMetaData

**Android:**
```kotlin
data class OSTUserInputMetaData(
    val note: String? = null,
    val tags: List<String>? = null,
    val assistiveDevice: OSTAssistiveDevice? = null,
    val levelOfAssistance: OSTLevelOfAssistance? = null,
    val walkCourseLength: OSTWalkCourseLength? = null
)
```

**iOS:**
```swift
struct OSTUserInputMetaData {
    var note: String?
    var tags: [String]?
    var assistiveDevice: OSTAssistiveDevice?
    var levelOfAssistance: OSTLevelOfAssistance?
    var walkCourseLength: OSTWalkCourseLength?
}
```

---

### OSTAssistiveDevice

**Android:**
```kotlin
enum class OSTAssistiveDevice(val value: Int) {
    NONE(0),
    WALKER(5),
    ROLLATOR(6),
    CANE(2),
    CRUTCH_DOUBLE(4),
    CRUTCH_SINGLE(3)
}
```

**iOS:**
```swift
enum OSTAssistiveDevice: Int {
    case none = 0
    case walker = 5
    case rollator = 6
    case cane = 2
    case crutchDouble = 4
    case crutchSingle = 3
}
```

---

### OSTLevelOfAssistance

**Android:**
```kotlin
enum class OSTLevelOfAssistance(val value: Int) {
    INDEPENDENT(1),
    MODIFIED_INDEPENDENT(2),
    STANDBY_ASSISTANCE(3),
    MIN_ASSISTANCE(4),
    MODERATE_ASSISTANCE(5),
    MAX_ASSISTANCE(6),
    TOTAL_ASSISTANCE(7),
    UNABLE_TO_PERFORM(8)
}
```

**iOS:**
```swift
enum OSTLevelOfAssistance: Int {
    case independent = 1
    case modifiedIndependent = 2
    case standbyAssistance = 3
    case minAssistance = 4
    case moderateAssistance = 5
    case maxAssistance = 6
    case totalAssistance = 7
    case unableToPerform = 8
}
```

---

### OSTWalkCourseLength

**Android:**
```kotlin
data class OSTWalkCourseLength(
    val value: Int,
    val unit: String  // "feet" or "meters"
) {
    companion object {
        const val FEET_UNIT = "feet"
        const val METERS_UNIT = "meters"
    }
}
```

**iOS:**
```swift
struct OSTWalkCourseLength {
    let value: Int
    let unit: String  // "feet" or "meters"

    static let feetUnit = "feet"
    static let metersUnit = "meters"
}
```

---

### OSTUserAttributes (Builder Pattern)

**Android:**
```kotlin
class OSTUserAttributes(
    val attributes: HashMap<String, Any?>,
    val customAttributes: HashMap<String, Any>
) {
    val firstName: String?
    val lastName: String?
    val email: String?
    val phone: String?
    val profileImageUrl: String?
    val emrId: String?
    val dateOfBirth: Date?          // java.util.Date, not String
    val sex: String?                // stored as description string
    val heightCm: Int?              // Int, not Float
    val weightKg: Int?              // Int, not Float
    val age: Int?
    val mainSurgeryDate: Date?      // java.util.Date, not String
    val mainSurgeryType: OSTSurgeryType?
    val mainSurgerySide: OSTSurgerySide?

    enum class Sex(val description: String) {
        MALE("male"),
        FEMALE("female")
    }

    class Builder {
        fun withFirstName(name: String): Builder
        fun withLastName(name: String): Builder
        fun withEmail(email: String): Builder
        fun withPhone(phone: String): Builder
        fun withProfileImage(url: String): Builder
        fun withEmrId(emrId: String): Builder
        fun withDateOfBirth(dateOfBirth: Date): Builder    // Date, not String
        fun withAge(age: Int): Builder
        fun withMainSurgery(date: Date, type: OSTSurgeryType, side: OSTSurgerySide): Builder  // Date, not String
        fun withSex(sex: Sex): Builder
        fun withHeightCm(heightCm: Int): Builder           // Int, not Float
        fun withWeightKg(weightKg: Int): Builder           // Int, not Float
        fun withCustomAttribute(key: String, value: Any): Builder
        fun build(): OSTUserAttributes
    }
}
```

**iOS:**
```swift
class OSTUserAttributes {
    var firstName: String?
    var lastName: String?
    var email: String?
    var phone: String?
    var profileImageUrl: String?
    var emrId: String?
    var dateOfBirth: Date?          // Date, not String
    var sex: String?                // stored as description string
    var heightCm: Int?              // Int, not Float
    var weightKg: Int?              // Int, not Float
    var age: Int?
    var mainSurgeryDate: Date?      // Date, not String
    var mainSurgeryType: OSTSurgeryType?
    var mainSurgerySide: OSTSurgerySide?
    var customAttributes: [String: Any] = [:]

    enum Sex: String {
        case male = "male"
        case female = "female"
    }

    // Builder pattern (Swift approach)
    @discardableResult
    func withFirstName(_ firstName: String) -> OSTUserAttributes {
        self.firstName = firstName
        return self
    }

    @discardableResult
    func withLastName(_ lastName: String) -> OSTUserAttributes {
        self.lastName = lastName
        return self
    }

    @discardableResult
    func withEmail(_ email: String) -> OSTUserAttributes {
        self.email = email
        return self
    }

    @discardableResult
    func withPhone(_ phone: String) -> OSTUserAttributes {
        self.phone = phone
        return self
    }

    @discardableResult
    func withProfileImage(_ url: String) -> OSTUserAttributes {
        self.profileImageUrl = url
        return self
    }

    @discardableResult
    func withEmrId(_ emrId: String) -> OSTUserAttributes {
        self.emrId = emrId
        return self
    }

    @discardableResult
    func withDateOfBirth(_ dateOfBirth: Date) -> OSTUserAttributes {  // Date, not String
        self.dateOfBirth = dateOfBirth
        return self
    }

    @discardableResult
    func withAge(_ age: Int) -> OSTUserAttributes {
        self.age = age
        return self
    }

    @discardableResult
    func withMainSurgery(date: Date, type: OSTSurgeryType, side: OSTSurgerySide) -> OSTUserAttributes {  // Date, not String
        self.mainSurgeryDate = date
        self.mainSurgeryType = type
        self.mainSurgerySide = side
        return self
    }

    @discardableResult
    func withSex(_ sex: Sex) -> OSTUserAttributes {
        self.sex = sex
        return self
    }

    @discardableResult
    func withHeightCm(_ heightCm: Int) -> OSTUserAttributes {  // Int, not Float
        self.heightCm = heightCm
        return self
    }

    @discardableResult
    func withWeightKg(_ weightKg: Int) -> OSTUserAttributes {  // Int, not Float
        self.weightKg = weightKg
        return self
    }

    @discardableResult
    func withCustomAttribute(key: String, value: Any) -> OSTUserAttributes {
        self.customAttributes[key] = value
        return self
    }
}

// Usage:
let attributes = OSTUserAttributes()
    .withFirstName("John")
    .withLastName("Doe")
    .withEmail("john@example.com")
    .withAge(45)
```

---

### OSTSurgeryType

**Android:**
```kotlin
enum class OSTSurgeryType(val description: String) {
    THR("thr"),
    TKR("tkr"),
    UNKNOWN("unknown")
}
```

**iOS:**
```swift
enum OSTSurgeryType: String {
    case thr = "thr"
    case tkr = "tkr"
    case unknown = "unknown"
}
```

---

### OSTSurgerySide

**Android:**
```kotlin
enum class OSTSurgerySide(val description: String) {
    LEFT("left"),
    RIGHT("right"),
    UNKNOWN("unknown")
}
```

**iOS:**
```swift
enum OSTSurgerySide: String {
    case left = "left"
    case right = "right"
    case unknown = "unknown"
}
```

---

### OSTParameterMetadata

**Android:**
```kotlin
data class OSTParameterMetadata(
    val activity: OSTActivityType,
    val displayName: String,
    val units: String?,
    val imperialUnits: String? = null,
    val category: String,
    val lowRange: Float?,
    val sortKey: Float? = null,
    val isMainParam: Boolean? = null,
    val highRange: Float?,
    val roundDigits: Float?,
    val imperialRoundDigits: Float? = null
)
```

**iOS:**
```swift
struct OSTParameterMetadata {
    let activity: OSTActivityType
    let displayName: String
    let units: String?
    let imperialUnits: String?
    let category: String
    let lowRange: Float?
    let sortKey: Float?
    let isMainParam: Bool?
    let highRange: Float?
    let roundDigits: Float?
    let imperialRoundDigits: Float?
}
```

---

### OSTNorm

**Android:**
```kotlin
data class OSTNorm(
    var units: String? = null,
    val parts: List<NormPart>? = null
)

data class NormPart(
    var start: Float,
    var end: Float,
    val color: String,
    val includeStart: Boolean,
    val includeEnd: Boolean
)
```

**iOS:**
```swift
struct OSTNorm {
    var units: String?
    let parts: [NormPart]?
}

struct NormPart {
    let start: Float
    let end: Float
    let color: String
    let includeStart: Bool
    let includeEnd: Bool
}
```

---

### OSTResult

**Android:**
```kotlin
sealed class OSTResult<out T> {
    data class Success<T>(val data: T) : OSTResult<T>()
    data class Error(val exception: Throwable, val code: Int?) : OSTResult<Nothing>()
}
```

**iOS:**
```swift
enum OSTResult<T> {
    case success(data: T)
    case error(exception: Error, code: Int?)
}

// Usage:
let result: OSTResult<OSTInsights> = await service.getInsights(uuid: "123")
switch result {
case .success(let data):
    print("Insights: \(data)")
case .error(let exception, let code):
    print("Error \(code ?? -1): \(exception)")
}
```

---

### OSTMockIMU

**Android:**
```kotlin
enum class OSTMockIMU {
    NONE,
    SUCCESSFUL,
    ERROR_POSITION,
    ERROR_OTHER,
    ERROR_NO_CYCLE,
    ERROR_SHORT,
    ERROR_CURVY,
    ERROR_STATIC,
    BALANCE_TEST,
    STS_SUCCESS,
    TUG_SUCCESS,
    DUAL_TASK_SUCCESS,
    ROM_SUCCESS,
    SIX_MIN_WALK_SUCCESS
}
```

**iOS:**
```swift
enum OSTMockIMU {
    case none
    case successful
    case errorPosition
    case errorOther
    case errorNoCycle
    case errorShort
    case errorCurvy
    case errorStatic
    case balanceTest
    case stsSuccess
    case tugSuccess
    case dualTaskSuccess
    case romSuccess
    case sixMinWalkSuccess
}
```

---

### OSTMeasurementSystem

**Android:**
```kotlin
enum class OSTMeasurementSystem {
    METRIC,
    IMPERIAL,
    DEFAULT
}
```

**iOS:**
```swift
enum OSTMeasurementSystem {
    case metric
    case imperial
    case `default`  // backticks for reserved keyword
}
```

---

## 9. Configuration

### OSTConfiguration

**Android:**
```kotlin
data class OSTConfiguration(
    val appId: String? = null,
    val additionalConfig: Map<String, Any> = emptyMap()
)
```

**iOS:**
```swift
struct OSTConfiguration {
    var appId: String?
    var additionalConfig: [String: Any] = [:]
}
```

---

### OSTRuntimeConfiguration

**Android:**
```kotlin
interface OSTRuntimeConfiguration {
    fun setMockIMU(imu: OSTMockIMU)
    fun setMeasurementUnits(units: OSTMeasurementSystem)
}
```

**iOS:**
```swift
protocol OSTRuntimeConfiguration: AnyObject {
    func setMockIMU(_ imu: OSTMockIMU)
    func setMeasurementUnits(_ units: OSTMeasurementSystem)
}
```

---

## 10. Permissions

### Required Permissions

| Platform | Permission | When | Purpose |
|----------|------------|------|---------|
| Android | `ACTIVITY_RECOGNITION` | Runtime (Android 10+) | MotionLab recording, Monitoring |
| iOS | `NSMotionUsageDescription` | Info.plist | Core Motion access |
| iOS | `NSLocationWhenInUseUsageDescription` | Info.plist (optional) | Geo-tagging measurements |

### Optional Permissions

**Android:**
- `ACCESS_FINE_LOCATION` (geo-tagging)
- `ACCESS_COARSE_LOCATION` (geo-tagging)

**iOS:**
- `CoreLocation` when-in-use authorization (geo-tagging)

### Permission Change Detection

**Android v2:** The SDK detects permission changes automatically via Android lifecycle events. The v1 method `onPermissionGranted()` has been removed — no manual notification is needed after requesting permissions.

**iOS:** Implement equivalent automatic detection using notification observers (e.g., `UIApplication.didBecomeActiveNotification`) to re-check permission status when the app returns to foreground.

---

### Hardware Requirements

**Android:**
```xml
<uses-feature
    android:name="android.hardware.sensor.gyroscope"
    android:required="true" />  <!-- Required for MotionLab -->
```

**iOS:**
- Gyroscope required for MotionLab
- Accelerometer required (all iOS devices have this)
- Check `CMMotionManager.isGyroAvailable()` at runtime

---

## 11. Push Notifications

### Notification Management

**Android:**
```kotlin
fun updatePushToken(token: String)
fun handleNotification(payload: Map<String, String>): Boolean
```

**iOS:**
```swift
func updatePushToken(_ token: String)
func handleNotification(payload: [String: String]) -> Bool
```

**Returns:** `true` if notification was handled by SDK, `false` if app should handle.

---

### OSTNotificationConfig

**Android:**
```kotlin
sealed interface OSTNotificationConfig {
    val icon: Int
}

data class OSTDefaultNotificationConfig(
    val title: (() -> String)?,
    val text: (() -> String)?,
    override val icon: Int
) : OSTNotificationConfig

data class OSTCustomNotificationConfig(
    val appName: String,
    val stepsGoal: Int = 8000,
    override val icon: Int
) : OSTNotificationConfig

data class OSTNativeNotificationConfig(
    val notification: Notification,
    override val icon: Int
) : OSTNotificationConfig
```

**iOS:**
```swift
protocol OSTNotificationConfig {
    var icon: String? { get }  // iOS: SF Symbol name or asset name
}

struct OSTDefaultNotificationConfig: OSTNotificationConfig {
    let title: (() -> String)?
    let text: (() -> String)?
    let icon: String?
}

struct OSTCustomNotificationConfig: OSTNotificationConfig {
    let appName: String
    let stepsGoal: Int = 8000
    let icon: String?
}

struct OSTNativeNotificationConfig: OSTNotificationConfig {
    let content: UNMutableNotificationContent
    let icon: String?
}
```

---

## 12. Platform Considerations

### Kotlin to Swift Mapping Table

| Kotlin Construct | Swift Equivalent | Notes |
|------------------|------------------|-------|
| `object OneStep` | `class OneStep { static let shared }` | Singleton pattern |
| `suspend fun` | `func name() async throws` | Coroutines → Swift Concurrency |
| `StateFlow<T>` | `@Published var` / `CurrentValueSubject<T, Never>` | State observation |
| `Flow<T>` | `AsyncStream<T>` / `AnyPublisher<T, Never>` | Event streams |
| `sealed class` | `enum` with associated values | Exhaustive switching |
| `sealed interface` | `enum` with associated values | Exhaustive switching |
| `data class` | `struct` | Value types |
| `enum class` | `enum` | Enumerations |
| `Map<K, V>` | `[K: V]` | Dictionaries |
| `List<T>` | `[T]` | Arrays |
| `Long` (time) | `TimeInterval` | **Milliseconds → Seconds** |
| `LocalDate` | `Date` + `Calendar` | Date handling |
| `Application` | *No equivalent* | App lifecycle |
| Coroutines | `Task`, `TaskGroup`, `async/await` | Concurrency |
| `?.let { }` | `if let` / `guard let` | Optional handling |
| `companion object` | `static` members | Static scope |

---

### Critical Time Conversion

**⚠️ IMPORTANT: Time Unit Differences**

| Android (Kotlin) | iOS (Swift) | Conversion |
|------------------|-------------|------------|
| `Long` milliseconds | `TimeInterval` seconds | `seconds = millis / 1000.0` |
| `System.currentTimeMillis()` | `Date().timeIntervalSince1970` | Divide by 1000 |
| Duration: `Long` | Duration: `TimeInterval` | Always convert |

**Example:**
```swift
// Android: duration = 60000 (60 seconds in millis)
// iOS:     duration = 60.0 (60 seconds)

let androidDuration: Int64 = 60000
let iosDuration: TimeInterval = TimeInterval(androidDuration) / 1000.0
```

---

### StateFlow vs. Combine/AsyncSequence

**Android StateFlow:**
```kotlin
val state: StateFlow<OSTState>

// Collect
viewModelScope.launch {
    oneStep.state.collect { state ->
        // Handle state
    }
}
```

**iOS Option 1: Combine**
```swift
@Published var state: OSTState

// Subscribe
OneStep.shared.$state
    .sink { state in
        // Handle state
    }
    .store(in: &cancellables)
```

**iOS Option 2: AsyncSequence**
```swift
var state: AsyncStream<OSTState> { get }

// Iterate
Task {
    for await state in OneStep.shared.state {
        // Handle state
    }
}
```

---

### Application Lifecycle

**Android:**
```kotlin
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        OneStep.initialize(
            application = this,
            clientToken = "token",
            config = OSTConfiguration()
        )
    }
}
```

**iOS:**
```swift
@main
struct MyApp: App {
    init() {
        Task {
            do {
                try await OneStep.shared.initialize(
                    clientToken: "token",
                    config: OSTConfiguration()
                )
            } catch {
                print("SDK init failed: \(error)")
            }
        }
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
```

**Note:** iOS has no `Application` object. The SDK must:
1. Register for `UIApplication` lifecycle notifications internally
2. Handle background/foreground transitions
3. Manage Core Motion session lifecycle

---

### Error Handling

**Android:**
```kotlin
sealed class OSTResult<out T> {
    data class Success<T>(val data: T) : OSTResult<T>()
    data class Error(val exception: Throwable, val code: Int?) : OSTResult<Nothing>()
}

// Usage
when (val result = service.getInsights(uuid)) {
    is OSTResult.Success -> handleData(result.data)
    is OSTResult.Error -> handleError(result.exception, result.code)
}
```

**iOS:**
```swift
enum OSTResult<T> {
    case success(data: T)
    case error(exception: Error, code: Int?)
}

// Usage
let result = await service.getInsights(uuid: uuid)
switch result {
case .success(let data):
    handleData(data)
case .error(let exception, let code):
    handleError(exception, code)
}
```

---

## Complete Implementation Checklist

When building the iOS SDK, ensure:

### Core SDK
- [ ] Singleton `OneStep` class with static `shared` instance
- [ ] State management using Combine or AsyncSequence
- [ ] `initialize()`, `identify()`, `connectAsUser()`, `logout()`, `sync()`
- [ ] State machine: Uninitialized → Ready → Identified → Error
- [ ] Event stream for SDK events
- [ ] Permission management and state updates

### MotionLab Product
- [ ] Recording state management (`OSTRecorderState`)
- [ ] Real-time step counting (`stepsCount` publisher)
- [ ] Analyzer state tracking (`OSTAnalyserState`)
- [ ] All activity types supported (`OSTActivityType`)
- [ ] Recording control: `start()`, `stop()`, `reset()`
- [ ] Analysis with timeout: `analyze()`
- [ ] Measurement CRUD operations
- [ ] Marker support during recording
- [ ] Measurement units (metric/imperial)

### Monitoring Product
- [ ] Opt-in/opt-out management
- [ ] Monitoring preference state (`OSTMonitoringPreference`)
- [ ] Runtime state with blockers (`OSTMonitoringRuntimeState`)
- [ ] Daily summaries API
- [ ] Step bouts interface: daily, hourly, walking bouts
- [ ] Background monitoring service
- [ ] Auto-enrollment policy support

### Insights Product
- [ ] Motion data service interface
- [ ] Parameter metadata retrieval
- [ ] Norms and scoring system
- [ ] AI insights API
- [ ] Discrete color scoring

### Data Models
- [ ] All enums mapped with raw values
- [ ] All structs/classes for measurements
- [ ] User attributes with builder pattern
- [ ] Result types with exhaustive switching
- [ ] Time conversion (millis → seconds) everywhere

### Platform Integration
- [ ] Core Motion integration
- [ ] Background processing
- [ ] Push notification handling
- [ ] Permission request flows
- [ ] App lifecycle management (no `Application` object)

### Testing
- [ ] Mock IMU configurations
- [ ] Debug mode support
- [ ] Error simulation
- [ ] Unit tests for all public APIs

---

## Additional Resources

For implementation details, refer to:
- Android source: `co.onestep.android.core`
- Getting started guide: `core/docs/getting-started.md`
- Monitoring guide: `core/docs/monitoring-guide.md`
- MotionLab guide: `core/docs/motionlab-guide.md`

---

**Document Version:** 1.0
**Android SDK Version:** v2.0
**Last Updated:** 2026-02-16
