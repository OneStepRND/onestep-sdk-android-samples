package com.onestep.sdksample.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.onestep.android.core.OSTActivityType
import co.onestep.android.core.OneStep
import co.onestep.android.core.motionLab.OSTAnalyserState
import co.onestep.android.core.motionLab.OSTAssistiveDevice
import co.onestep.android.core.motionLab.OSTLevelOfAssistance
import co.onestep.android.core.motionLab.OSTMotionMeasurement
import co.onestep.android.core.motionLab.OSTRecorderState
import co.onestep.android.core.motionLab.OSTResultState
import co.onestep.android.core.motionLab.OSTUserInputMetaData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

class RecorderViewModel : ViewModel() {

    private val _state = MutableStateFlow<RecorderUiState>(RecorderUiState.Idle)
    val state: StateFlow<RecorderUiState> = _state.asStateFlow()

    init {
        // Recorder state machine: INITIALIZED → RECORDING → FINALIZING → DONE
        viewModelScope.launch {
            OneStep.motionLab.recorderState.collect { recorderState ->
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
            OneStep.motionLab.analyserState.collect { analyserState ->
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
        // reset() clears any previous session so a fresh one can begin.
        OneStep.motionLab.reset()

        viewModelScope.launch {
            // Optional caller-supplied metadata, propagated to the measurement record.
            val customMetadata = mapOf(
                "app" to "DemoApp",
                "is_demo" to true,
                "version" to 1.1,
            )

            // Optional user tagging: free-text note, tags, and structured fields like
            // assistive device and level of assistance.
            val userInput = OSTUserInputMetaData(
                note = "Sample recording",
                tags = listOf("demo"),
                assistiveDevice = OSTAssistiveDevice.NONE,
                levelOfAssistance = OSTLevelOfAssistance.INDEPENDENT,
            )

            OneStep.motionLab.start(
                activityType = OSTActivityType.WALK,
                durationMillis = RECORDING_DURATION_MS,
                customMetadata = customMetadata,
                userInputMetadata = userInput,
            )
        }
    }

    fun stopRecording() {
        viewModelScope.launch { OneStep.motionLab.stop() }
    }

    /**
     * Pull the analysis result.
     *
     * analyze() is a multi-step async transaction:
     *   1. upload raw motion data to OneStep servers
     *   2. server-side pipeline processes the data
     *   3. SDK polls for the result
     *
     * For a short walk this usually takes 5–15 seconds. A null return means the
     * transaction failed (network, timeout, server) — the structured reason flows
     * through analyserState as OSTAnalyserState.Failed.
     */
    private suspend fun analyse() {
        val measurement = OneStep.motionLab.analyze(timeout = ANALYZE_TIMEOUT_MS)
        if (measurement == null) {
            Log.w(TAG, "analyze() returned null — see analyserState for the reason")
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
            else -> {
                _state.value = RecorderUiState.Failed("Analysis result not available")
            }
        }
    }

    private companion object {
        private val TAG = RecorderViewModel::class.simpleName ?: "RecorderViewModel"
        private const val RECORDING_DURATION_MS = 60_000L
        private const val ANALYZE_TIMEOUT_MS = 60_000L
    }
}
