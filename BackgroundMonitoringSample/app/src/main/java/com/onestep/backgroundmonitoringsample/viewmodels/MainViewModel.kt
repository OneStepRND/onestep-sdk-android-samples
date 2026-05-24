package com.onestep.backgroundmonitoringsample.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.onestep.android.core.OSTIdentificationState
import co.onestep.android.core.OneStep
import co.onestep.android.core.getOr
import co.onestep.android.core.monitoring.getMonitoring
import com.onestep.backgroundmonitoringsample.ui.model.MonitoringUiState
import com.onestep.backgroundmonitoringsample.ui.model.ScreenState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    // The SDK is initialised in BgMonitoringSampleApplication.onCreate(), so by the
    // time this ViewModel is constructed the process-singleton handle is available.
    private val oneStep: OneStep = OneStep.getInstance().getOr(null)
        ?: error("OneStep SDK not initialized")

    var screenState by mutableStateOf<ScreenState>(ScreenState.Loading)

    private val _monitoringUiState = MutableStateFlow(MonitoringUiState())
    val monitoringUiState: StateFlow<MonitoringUiState> = _monitoringUiState

    fun setState(state: ScreenState) {
        screenState = state
    }

    fun start() {
        viewModelScope.launch {
            oneStep.identificationState.collect { state ->
                when (state) {
                    is OSTIdentificationState.Identified -> {
                        collectMonitoringState()
                        screenState = ScreenState.Initialized
                    }
                    is OSTIdentificationState.Lost ->
                        screenState = ScreenState.Error("SDK session lost: ${state.cause.message}")
                    OSTIdentificationState.Unidentified ->
                        screenState = ScreenState.Loading
                }
            }
        }
    }

    fun optInToMonitoring() {
        viewModelScope.launch {
            if (oneStep.identificationState.value is OSTIdentificationState.Identified) {
                oneStep.getMonitoring().getOr(null)?.optIn()
            }
        }
    }

    fun optOutOfMonitoring() {
        viewModelScope.launch {
            if (oneStep.identificationState.value is OSTIdentificationState.Identified) {
                oneStep.getMonitoring().getOr(null)?.optOut()
            }
        }
    }

    private fun collectMonitoringState() {
        viewModelScope.launch {
            val monitoring = oneStep.getMonitoring().getOr(null) ?: return@launch
            combine(
                monitoring.state,
                monitoring.preference,
            ) { runtimeState, preference ->
                MonitoringUiState(
                    preference = preference,
                    runtimeState = runtimeState,
                )
            }.collect {
                _monitoringUiState.value = it
            }
        }
    }
}
