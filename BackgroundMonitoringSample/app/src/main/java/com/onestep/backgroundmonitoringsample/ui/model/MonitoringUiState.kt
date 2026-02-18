package com.onestep.backgroundmonitoringsample.ui.model

import co.onestep.android.core.monitoring.OSTMonitoringPreference
import co.onestep.android.core.monitoring.OSTMonitoringRuntimeState

data class MonitoringUiState(
    val isActive: Boolean = false,
    val preference: OSTMonitoringPreference = OSTMonitoringPreference.NOT_SET,
    val runtimeState: OSTMonitoringRuntimeState = OSTMonitoringRuntimeState.Inactive,
)
