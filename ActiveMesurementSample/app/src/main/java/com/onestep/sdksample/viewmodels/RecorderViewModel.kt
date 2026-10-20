package com.onestep.sdksample.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.onestep.android.core.OSTActivityType
import co.onestep.android.core.OneStep
import co.onestep.android.core.getOr
import co.onestep.android.core.motionLab.OSTAnalyserState
import co.onestep.android.core.motionLab.OSTAssistiveDevice
import co.onestep.android.core.motionLab.OSTLevelOfAssistance
import co.onestep.android.core.motionLab.OSTMotionMeasurement
import co.onestep.android.core.motionLab.OSTRecorderState
import co.onestep.android.core.motionLab.OSTResultState
import co.onestep.android.core.motionLab.getMotionLab
import co.onestep.android.core.onError
import co.onestep.android.core.onSuccess
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * UI state for the recording flow. Collapses the two SDK state machines
 * (recorder + analyser) and the analysis result into a single sealed type
 * so the screen can do one `when` over it.
 *
 * Lifecycle: Idle → Recording → Finalizing → Uploading → Analyzing → Analyzed | Failed
 */
sealed interface RecorderUiState {
    data object Idle : RecorderUiState
    data object Recording : RecorderUiState
    data object Finalizing : RecorderUiState
    data object Uploading : RecorderUiState
    data object Analyzing : RecorderUiState
    data class Analyzed(val measurement: OSTMotionMeasurement) : RecorderUiState
    data class Failed(val reason: String) : RecorderUiState
}

/** Live progress of the recording in flight, for the timer and step counter. */
data class RecordingProgress(
    val elapsedSeconds: Long,
    val totalSeconds: Long,
    val steps: Int,
)

class RecorderViewModel : ViewModel() {

    // The SDK is initialised in SDKSampleApplication.onCreate() so by the time this
    // ViewModel is constructed (WalkRecordScreen is only shown after identify) we can
    // safely fetch the process-singleton handle.
    private val oneStep: OneStep = OneStep.getInstance().getOr(null)
        ?: error("OneStep SDK not initialized")
    private val motionLab = oneStep.getMotionLab().getOr(null)
        ?: error("MotionLab unavailable")

    // State driven by the SDK's recorder and analyser state machines.
    private val _state = MutableStateFlow<RecorderUiState>(RecorderUiState.Idle)

    // A refused start() is kept apart from the SDK-driven state: the reset() that releases the
    // recording service emits INITIALIZED, which would otherwise overwrite the error with Idle.
    private val startError = MutableStateFlow<String?>(null)

    val state: StateFlow<RecorderUiState> = combine(_state, startError) { sdkState, error ->
        error?.let { RecorderUiState.Failed(it) } ?: sdkState
    }.stateIn(viewModelScope, SharingStarted.Eagerly, RecorderUiState.Idle)

    // The SDK owns the recording clock: currentRecordingWindow publishes when capture started and
    // when it will auto-stop. Re-read it on every tick instead of counting seconds in the UI, so
    // the timer never drifts from the recorder (screen-off, backgrounding, dropped frames). The
    // ticker runs only while recording; the window is not cleared when a recording stops.
    @OptIn(ExperimentalCoroutinesApi::class)
    val progress: StateFlow<RecordingProgress?> = motionLab.recorderState
        .flatMapLatest { recorderState ->
            if (recorderState != OSTRecorderState.RECORDING) return@flatMapLatest flowOf(null)
            combine(motionLab.currentRecordingWindow, motionLab.stepsCount, ticker()) { window, steps, _ ->
                window?.let {
                    RecordingProgress(
                        elapsedSeconds = it.elapsedMillis().coerceAtMost(it.totalMillis) / 1000,
                        totalSeconds = it.totalMillis / 1000,
                        steps = steps,
                    )
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private fun ticker() = flow {
        while (true) {
            emit(Unit)
            delay(TICK_MS)
        }
    }

    init {
        // Recorder state machine: INITIALIZED → RECORDING → FINALIZING → DONE
        viewModelScope.launch {
            motionLab.recorderState.collect { recorderState ->
                Log.d(TAG, "RecorderState: $recorderState")
                when (recorderState) {
                    OSTRecorderState.INITIALIZED -> _state.value = RecorderUiState.Idle
                    OSTRecorderState.RECORDING -> _state.value = RecorderUiState.Recording
                    OSTRecorderState.FINALIZING -> _state.value = RecorderUiState.Finalizing
                    OSTRecorderState.DONE -> {
                        // The measurement is saved on-device. We auto-trigger analyze() here for
                        // the demo's "tap Stop, see the result" UX.
                        //
                        // This is a deliberate demo choice, NOT a requirement. In production you
                        // can move the analyse() call to a separate "Analyze" button, or skip the
                        // immediate analyze entirely — the SDK's background worker will sync and
                        // analyze on its own. The choice is purely about *when* your code wants
                        // the result back.
                        viewModelScope.launch { analyse() }
                    }
                }
            }
        }

        // Analyser state machine: Idle → Uploading → Analyzing → Analyzed | Failed
        // The terminal state (Analyzed) plus the actual measurement payload is set
        // inside analyse() below so the UI gets the OSTMotionMeasurement directly.
        viewModelScope.launch {
            motionLab.analyserState.collect { analyserState ->
                Log.d(TAG, "AnalyserState: $analyserState")
                when (analyserState) {
                    OSTAnalyserState.Idle,
                    OSTAnalyserState.Analyzed -> Unit // handled by analyse() result
                    OSTAnalyserState.Uploading -> _state.value = RecorderUiState.Uploading
                    OSTAnalyserState.Analyzing -> _state.value = RecorderUiState.Analyzing
                    is OSTAnalyserState.Failed -> {
                        // OSTAnalyserError subclasses describe the structured cause
                        // (network, timeout, server, too-short, …) for finer recovery.
                        _state.value = RecorderUiState.Failed("Analysis failed: ${analyserState.error}")
                    }
                }
            }
        }
    }

    /**
     * Start a 60-second timed walk recording.
     *
     * Caller responsibilities:
     *  - don't call start() while RECORDING (the demo's button gating prevents this).
     *  - request ACTIVITY_RECOGNITION permission on Android 14+ (handled in MainScreen).
     */
    fun startRecording() {
        startError.value = null
        viewModelScope.launch {
            // reset() clears any previous session so a fresh one can begin.
            motionLab.reset()

            // Start the recording foreground service while the app is still in the foreground.
            // On Android 14+ a foreground service cannot start from the background, so call this
            // before start() (in a real app, during your countdown or "get ready" screen).
            // It returns Success(false), not an error, when the app is already in the background.
            motionLab.prepareForRecording(OSTActivityType.WALK)
                .onSuccess { started ->
                    if (!started) Log.w(TAG, "prepareForRecording: app not in foreground")
                }

            motionLab.start(
                activityType = OSTActivityType.WALK,
                durationMillis = RECORDING_DURATION_MS,
                userInputMetadata = {
                    // Optional user tagging: free-text note, tags, and structured fields like
                    // assistive device and level of assistance.
                    note = "Sample recording"
                    tags = listOf("demo")
                    assistiveDevice = OSTAssistiveDevice.NONE
                    levelOfAssistance = OSTLevelOfAssistance.INDEPENDENT
                    // Optional caller-supplied metadata, propagated to the measurement record.
                    customMetadata(
                        "app" to "DemoApp",
                        "is_demo" to true,
                        "version" to 1.1,
                    )
                },
            ).onError { error ->
                // Since SDK 2.3.0, start() returns an error instead of throwing. A refused start
                // records nothing, so always check the result. Typical error.cause.type values:
                // MissingPermission (ACTIVITY_RECOGNITION on Android 14+) and InvalidArgument
                // (duration over RECORDER_LIMIT_MILLIS).
                startError.value = "Could not start: ${error.cause.type.name}"
                // Release the service prepareForRecording() started. Otherwise its notification
                // stays up for up to 60 seconds although nothing is recording.
                motionLab.reset()
            }
        }
    }

    fun stopRecording() {
        viewModelScope.launch { motionLab.stop() }
    }

    /**
     * Pull the analysis result.
     *
     * analyze() is a multi-step async transaction:
     *   1. upload raw motion data to OneStep servers
     *   2. server-side pipeline processes the data
     *   3. SDK polls for the result
     *
     * For a short walk this usually takes 5–15 seconds. An Error result means the
     * transaction failed (network, timeout, server) — the structured reason flows
     * through analyserState as OSTAnalyserState.Failed.
     */
    private suspend fun analyse() {
        val measurement = motionLab.analyze(timeout = ANALYZE_TIMEOUT_MS).getOr(null) ?: run {
            Log.w(TAG, "analyze() failed — see analyserState for the reason")
            return
        }

        when (measurement.resultState) {
            OSTResultState.FULL_ANALYSIS,
            OSTResultState.PARTIAL_ANALYSIS -> {
                Log.d(TAG, "Analyzed: id=${measurement.id} steps=${measurement.metadata.steps}")
                _state.value = RecorderUiState.Analyzed(measurement)
            }
            OSTResultState.EMPTY_ANALYSIS -> {
                _state.value = RecorderUiState.Failed(
                    "Empty analysis: ${measurement.error?.message ?: "unknown"}"
                )
            }
            null -> {
                _state.value = RecorderUiState.Failed("Analysis result not available")
            }
        }
    }

    private companion object {
        private val TAG = RecorderViewModel::class.simpleName ?: "RecorderViewModel"
        private const val RECORDING_DURATION_MS = 60_000L
        private const val ANALYZE_TIMEOUT_MS = 60_000L
        private const val TICK_MS = 500L
    }
}
