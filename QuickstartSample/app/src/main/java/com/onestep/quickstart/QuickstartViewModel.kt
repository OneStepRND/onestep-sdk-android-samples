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
import co.onestep.android.core.motionLab.OSTRecorderState
import co.onestep.android.core.motionLab.OSTResultState
import co.onestep.android.core.motionLab.getMotionLab
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

/** Everything the screen can show. Each state maps to one layout in [QuickstartScreen]. */
@Immutable
sealed interface QuickstartUiState {
    /** The sample cannot run: the SDK did not initialize, or local.properties is incomplete. */
    data class Blocked(val reason: String) : QuickstartUiState

    /** No patient is bound yet. [error] is the last connect failure, if any. */
    data class Disconnected(val error: String? = null) : QuickstartUiState

    /** An SDK call is in flight. [label] says which one. */
    data class Busy(val label: String) : QuickstartUiState

    /** A patient is bound, so recording is possible. [message] is the last result or error. */
    data class Connected(val message: String? = null) : QuickstartUiState
}

class QuickstartViewModel(private val oneStep: OneStep?) : ViewModel() {

    private val _state = MutableStateFlow(initialState())
    val state: StateFlow<QuickstartUiState> = _state.asStateFlow()

    private fun initialState(): QuickstartUiState {
        // Name only the missing keys, never their values.
        val missingKeys = listOf(
            "onestep.clientToken" to BuildConfig.CLIENT_TOKEN,
            "onestep.customerPatientId" to BuildConfig.CUSTOMER_PATIENT_ID,
        ).filter { (_, value) -> value.isBlank() }.map { (key, _) -> key }
        return when {
            oneStep == null -> QuickstartUiState.Blocked(
                "The OneStep SDK failed to initialize on this device. See Logcat for the error type.",
            )
            missingKeys.isNotEmpty() -> QuickstartUiState.Blocked(
                "Set ${missingKeys.joinToString()} in local.properties, then rebuild.",
            )
            // The SDK keeps the patient across app restarts, so skip Connect if one is bound.
            oneStep.identificationState.value is OSTIdentificationState.Identified ->
                QuickstartUiState.Connected()
            else -> QuickstartUiState.Disconnected()
        }
    }

    fun connect() {
        val sdk = oneStep ?: return
        _state.value = QuickstartUiState.Busy("Connecting…")
        viewModelScope.launch {
            // Bind this app user to OneStep. Measurements are stored under this patient.
            // Credentials come from local.properties via BuildConfig, never from source.
            val result = sdk.setPatient(
                apiKey = BuildConfig.CLIENT_TOKEN,
                customerPatientId = BuildConfig.CUSTOMER_PATIENT_ID,
                identityVerification = BuildConfig.IDENTITY_VERIFICATION.ifBlank { null },
            )
            result.getOr { error ->
                _state.value = QuickstartUiState.Disconnected(error.describe())
                return@launch
            }
            _state.value = QuickstartUiState.Connected()
        }
    }

    /** Records a 30-second walk and shows its walk score. */
    fun recordWalk(permissionGranted: Boolean) {
        val sdk = oneStep ?: return
        if (!permissionGranted) {
            _state.value = QuickstartUiState.Connected("Allow Physical activity in Settings to record.")
            return
        }
        viewModelScope.launch {
            _state.value = QuickstartUiState.Connected(recordAndAnalyze(sdk))
        }
    }

    private suspend fun recordAndAnalyze(sdk: OneStep): String {
        // MotionLab is the recording API. It is available only after setPatient() succeeds.
        val motionLab = sdk.getMotionLab().getOr { return it.describe() }

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
            return "Could not start: ${error.describe()}"
        }

        // The recorder stops by itself after RECORDING_MILLIS and then reports DONE.
        withTimeoutOrNull(RECORDING_MILLIS + FINISH_GRACE_MILLIS) {
            motionLab.recorderState.first { it == OSTRecorderState.DONE }
        } ?: run {
            motionLab.reset()
            return "The recording did not finish in time."
        }

        _state.value = QuickstartUiState.Busy("Analyzing…")
        // Upload the recording and wait for the server-side analysis.
        val measurement = motionLab.analyze(timeout = ANALYZE_TIMEOUT_MILLIS)
            .getOr { return "Analysis failed: ${it.describe()}" }
        if (measurement.resultState == OSTResultState.EMPTY_ANALYSIS || measurement.resultState == null) {
            return "No result: ${measurement.error?.message ?: "too few steps?"}"
        }
        // params maps each gait parameter to its value. The walk score is one of them.
        val walkScore = measurement.params[OSTParamName.WALKING_WALK_SCORE] ?: "N/A"
        return "Walk score: $walkScore\nSteps: ${measurement.metadata.steps ?: "N/A"}"
    }

    // Show the error type and message. SDK error messages do not contain credentials.
    private fun OSTResult.Error.describe(): String =
        "${cause.type.name}: ${cause.message}"

    private companion object {
        const val RECORDING_MILLIS = 30_000L
        const val FINISH_GRACE_MILLIS = 30_000L
        const val ANALYZE_TIMEOUT_MILLIS = 60_000L
    }
}
