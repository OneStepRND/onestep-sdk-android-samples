package com.onestep.onestepuikitsample

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import co.onestep.android.core.external.models.sdkOut.OSTBackgroundMonitoringStats
import co.onestep.android.core.external.models.sdkOut.OSTInitResult
import co.onestep.android.uikit.OSTTheme

class MainViewModel: ViewModel() {
    var sdkInitialized by mutableStateOf<OSTInitResult?>(null)
    var isConnecting by mutableStateOf(false)
    var collectionData = mutableStateOf(OSTBackgroundMonitoringStats.empty())

    var colorsDropdownExpanded = mutableStateOf(false)
    var fontsDropdownExpanded = mutableStateOf(false)
    var currentFont = mutableStateOf(OSTTheme.font)
}
