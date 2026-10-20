package com.onestep.quickstart

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.onestep.android.core.OSTActivityType
import co.onestep.android.core.OSTIdentificationState
import co.onestep.android.core.OSTParamName
import co.onestep.android.core.OSTResult
import co.onestep.android.core.OneStep
import co.onestep.android.core.getOr
import co.onestep.android.core.motionLab.OSTMotionMeasurement
import co.onestep.android.core.motionLab.OSTRecorderState
import co.onestep.android.core.motionLab.OSTResultState
import co.onestep.android.core.motionLab.getMotionLab
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

/** Everything the screen can show. Each state maps to one layout in [QuickstartScreen]. */
@Immutable
sealed interface QuickstartUiState {
    /** The sample cannot run: the SDK did not initialize, or local.properties is incomplete. */
    data class Blocked(val reason: String) : QuickstartUiState

    /** No patient is bound. [error] says why, if a connect failed or the session was lost. */
    data class Disconnected(val error: String? = null) : QuickstartUiState

    /** An SDK call is in flight. [label] says which one. */
    data class Busy(val label: String) : QuickstartUiState

    /** A patient is bound, so recording is possible. [message] is the last result or error. */
    data class Connected(val message: String? = null) : QuickstartUiState
}

class QuickstartViewModel(private val oneStep: OneStep?) : ViewModel() {

    private val _state = MutableStateFlow<QuickstartUiState>(QuickstartUiState.Disconnected())
    val state: StateFlow<QuickstartUiState> = _state.asStateFlow()

    private var recordJob: Job? = null

    init {
        // Name only the missing keys, never their values.
        val missingKeys = listOf(
            "onestep.clientToken" to BuildConfig.CLIENT_TOKEN,
            "onestep.customerPatientId" to BuildConfig.CUSTOMER_PATIENT_ID,
        ).filter { (_, value) -> value.isBlank() }.map { (key, _) -> key }
        when {
            oneStep == null -> _state.value = QuickstartUiState.Blocked(
                "The OneStep SDK failed to initialize on this device. See Logcat for the error type.",
            )
            missingKeys.isNotEmpty() -> _state.value = QuickstartUiState.Blocked(
                "Set ${missingKeys.joinToString()} in local.properties, then rebuild.",
            )
            else -> viewModelScope.launch {
                // identificationState is the source of truth for "is a patient bound". It starts as
                // Unidentified and changes to Identified once the SDK restores a patient from a
                // previous run, so observe it instead of reading it once.
                oneStep.identificationState.collect { identification ->
                    _state.update { current ->
                        when {
                            // Never interrupt a running call. It reads identificationState when it ends.
                            current is QuickstartUiState.Busy -> current
                            // Keep the last result or error message when nothing really changed.
                            identification is OSTIdentificationState.Identified &&
                                current is QuickstartUiState.Connected -> current
                            identification is OSTIdentificationState.Unidentified &&
                                current is QuickstartUiState.Disconnected -> current
                            else -> stateFor(identification)
                        }
                    }
                }
            }
        }
    }

    fun connect() {
        val sdk = oneStep ?: return
        if (_state.value is QuickstartUiState.Busy) return
        _state.value = QuickstartUiState.Busy("Connecting…")
        viewModelScope.launch {
            // Bind this app user to OneStep. Measurements are stored under this patient.
            // Credentials come from local.properties via BuildConfig, never from source.
            sdk.setPatient(
                apiKey = BuildConfig.CLIENT_TOKEN,
                customerPatientId = BuildConfig.CUSTOMER_PATIENT_ID,
                identityVerification = BuildConfig.IDENTITY_VERIFICATION.ifBlank { null },
            ).getOr { error ->
                _state.value = QuickstartUiState.Disconnected("Could not connect (${error.typeName}).")
                return@launch
            }
            // setPatient() succeeded, so the patient is bound. identificationState can still hold
            // its old value for a moment, so do not read it here. The collector applies later changes.
            _state.value = QuickstartUiState.Connected()
        }
    }

    /** Records a 30-second walk and shows its walk score. */
    fun recordWalk(permissionGranted: Boolean) {
        val sdk = oneStep ?: return
        // A second start() while recording is refused, and its reset() would end the first recording.
        if (recordJob?.isActive == true) return
        if (!permissionGranted) {
            _state.value = QuickstartUiState.Connected("Allow Physical activity in Settings to record.")
            return
        }
        // Set Busy now, before the coroutine starts, so the Record button goes away at once.
        _state.value = QuickstartUiState.Busy("Preparing…")
        recordJob = viewModelScope.launch {
            val message = recordAndAnalyze(sdk)
            // The session can be lost during the recording, and that change was not applied while
            // Busy. Read the identification state now, so the screen never shows a stale Connected.
            _state.value = stateFor(sdk.identificationState.value, message)
        }
    }

    /** Maps the SDK's identification state to the screen. [message] shows only while still identified. */
    private fun stateFor(identification: OSTIdentificationState, message: String? = null): QuickstartUiState =
        when (identification) {
            is OSTIdentificationState.Identified -> QuickstartUiState.Connected(message)
            is OSTIdentificationState.Lost -> QuickstartUiState.Disconnected(
                "Session lost (${identification.cause.type.name}). Connect again.",
            )
            else -> QuickstartUiState.Disconnected()
        }

    private suspend fun recordAndAnalyze(sdk: OneStep): String {
        // MotionLab is the recording API. It is available only after setPatient() succeeds.
        val motionLab = sdk.getMotionLab().getOr { return "Recording unavailable (${it.typeName})." }

        // Clear the previous recording, so recorderState starts again from INITIALIZED.
        motionLab.reset()

        // Start the recording foreground service while the app is in the foreground (Android 14+
        // forbids starting it from the background). Success(false) means the app was already in
        // the background, so no service started.
        motionLab.prepareForRecording(OSTActivityType.WALK)

        _state.value = QuickstartUiState.Busy("Recording a 30 s walk. Walk now.")
        // start() returns an error instead of throwing (SDK 2.3.0+). A refused start records nothing.
        motionLab.start(OSTActivityType.WALK, RECORDING_MILLIS).getOr { error ->
            // Release the service prepareForRecording() started, so its notification goes away.
            motionLab.reset()
            return "Could not start the recording (${error.typeName})."
        }

        // The recorder stops by itself after RECORDING_MILLIS and then reports DONE.
        withTimeoutOrNull(RECORDING_MILLIS + FINISH_GRACE_MILLIS) {
            motionLab.recorderState.first { it == OSTRecorderState.DONE }
        } ?: run {
            // The recorder may still be running. stop() ends it, then reset() releases the
            // session and its foreground service.
            motionLab.stop()
            motionLab.reset()
            return "The recording did not finish in time."
        }

        _state.value = QuickstartUiState.Busy("Analyzing…")
        // Upload the recording and wait for the server-side analysis.
        val measurement = motionLab.analyze(timeout = ANALYZE_TIMEOUT_MILLIS)
            .getOr { return "Analysis failed (${it.typeName})." }
        return when (measurement.resultState) {
            OSTResultState.FULL_ANALYSIS -> measurement.summary()
            OSTResultState.PARTIAL_ANALYSIS -> measurement.summary() + "\n(partial analysis)"
            OSTResultState.EMPTY_ANALYSIS -> "Empty analysis: the walk had too few steps to score."
            // The result did not arrive in time. The measurement is saved, and the SDK syncs it later.
            null -> "Analysis not available yet (timeout or network). The SDK syncs it later."
        }
    }

    // params maps each gait parameter to its value. The walk score is one of them.
    private fun OSTMotionMeasurement.summary(): String =
        "Walk score: ${params[OSTParamName.WALKING_WALK_SCORE] ?: "N/A"}\nSteps: ${metadata.steps ?: "N/A"}"

    // Show only the error type. cause.message can contain the server's response body, so never
    // show or log it.
    private val OSTResult.Error.typeName: String get() = cause.type.name

    private companion object {
        const val RECORDING_MILLIS = 30_000L
        const val FINISH_GRACE_MILLIS = 30_000L
        const val ANALYZE_TIMEOUT_MILLIS = 60_000L
    }
}
