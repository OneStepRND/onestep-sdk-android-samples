package com.onestep.backgroundmonitoringsample.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.onestep.android.core.OSTState
import co.onestep.android.core.OneStep
import co.onestep.android.core.monitoring.OSTMonitoringRuntimeState
import com.onestep.backgroundmonitoringsample.ui.model.ScreenType
import com.onestep.backgroundmonitoringsample.ui.model.MonitoringUiState
import com.onestep.backgroundmonitoringsample.ui.model.ScreenState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    var screenState by mutableStateOf<ScreenState>(ScreenState.Loading)

    private var _monitoringUiState: MutableStateFlow<MonitoringUiState> = MutableStateFlow(
        MonitoringUiState()
    )
    val monitoringUiState: StateFlow<MonitoringUiState> = _monitoringUiState

    fun setState(state: ScreenState) {
        screenState = state
    }

    fun showRecords(it: ScreenType) {
        screenState = ScreenState.Records(it)
    }

    fun optInToMonitoring() {
        viewModelScope.launch {
            if (OneStep.state.value is OSTState.Identified) {
                OneStep.monitoring.optIn()
            }
        }
    }

    fun optOutOfMonitoring() {
        viewModelScope.launch {
            if (OneStep.state.value is OSTState.Identified) {
                OneStep.monitoring.optOut()
            }
        }
    }

    fun collectMonitoringState() {
        viewModelScope.launch {
            combine(
                OneStep.monitoring.state,
                OneStep.monitoring.preference,
            ) { runtimeState, preference ->
                MonitoringUiState(
                    isActive = runtimeState is OSTMonitoringRuntimeState.Active,
                    preference = preference,
                    runtimeState = runtimeState,
                )
            }.collect {
                _monitoringUiState.value = it
            }
        }
    }

    companion object {
        val TAG: String = MainViewModel::class.simpleName ?: "MainViewModel"
    }
}
