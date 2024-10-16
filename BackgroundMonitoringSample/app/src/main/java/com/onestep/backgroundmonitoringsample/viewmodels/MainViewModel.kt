package com.onestep.backgroundmonitoringsample.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.onestep.android.core.external.OneStep
import co.onestep.android.core.external.models.sdkOut.OSTBackgroundMonitoringStats
import com.onestep.backgroundmonitoringsample.ui.model.AggregateType
import com.onestep.backgroundmonitoringsample.ui.model.ScreenState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel: ViewModel() {

    var screenState by mutableStateOf<ScreenState>(ScreenState.Loading)

    private var _backgroundStats: MutableStateFlow<OSTBackgroundMonitoringStats> = MutableStateFlow(
        OSTBackgroundMonitoringStats.empty())

    val backgroundStats: StateFlow<OSTBackgroundMonitoringStats> =_backgroundStats

    fun setState(state: ScreenState) {
        screenState = state
    }

    fun showRecords(it: AggregateType) {
        screenState = ScreenState.AggregatedRecords(it)
    }

    fun registerForBackgroundMonitoring() {
        if (OneStep.isInitialized()) {
            OneStep.registerBackgroundMonitoring()
        }
    }

    fun collectMonitoringStats() {
        viewModelScope.launch {
            if (OneStep.isInitialized()) {
                OneStep.monitoringStatsFlow().collect {
                    _backgroundStats.value = it
                }
            }
        }
    }

    companion object {
        val TAG: String = MainViewModel::class.simpleName ?: "MainViewModel"
    }
}