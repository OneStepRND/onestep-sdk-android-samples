package com.onestep.backgroundmonitoringsample.viewmodels

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.onestep.android.core.OSTState
import co.onestep.android.core.OneStep
import com.onestep.backgroundmonitoringsample.ui.model.MonitoringUiState
import com.onestep.backgroundmonitoringsample.ui.model.ScreenState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    var screenState by mutableStateOf<ScreenState>(ScreenState.Loading)

    private val _monitoringUiState = MutableStateFlow(MonitoringUiState())
    val monitoringUiState: StateFlow<MonitoringUiState> = _monitoringUiState

    fun setState(state: ScreenState) {
        screenState = state
    }

    fun start() {
        viewModelScope.launch {
            OneStep.state.collect { state ->
                when (state) {
                    is OSTState.Identified -> {
                        collectMonitoringState()
                        screenState = ScreenState.Initialized
                    }
                    is OSTState.Error -> screenState = ScreenState.Error("SDK Error: ${state.message}")
                    is OSTState.Ready,
                    is OSTState.Uninitialized -> screenState = ScreenState.Loading
                }
            }
        }
    }

    fun optInToMonitoring() {
        Log.d("Zivi", "optInToMonitoring")
        viewModelScope.launch {
            Log.d("Zivi", "OneStep.state.value: ${OneStep.state.value}")
            if (OneStep.state.value is OSTState.Identified) {
                OneStep.monitoring.optIn()
            }
        }
    }

    fun optOutOfMonitoring() {
        Log.d("Zivi", "optOutOfMonitoring")

        viewModelScope.launch {
            if (OneStep.state.value is OSTState.Identified) {
                OneStep.monitoring.optOut()
            }
        }
    }

    private fun collectMonitoringState() {
        viewModelScope.launch {
            combine(
                OneStep.monitoring.state,
                OneStep.monitoring.preference,
            ) { runtimeState, preference ->
                MonitoringUiState(
                    preference = preference,
                    runtimeState = runtimeState,
                )
            }.collect {
                Log.d("Zivi", "collectMonitoringState: $it")
                _monitoringUiState.value = it
            }
        }
    }
}
