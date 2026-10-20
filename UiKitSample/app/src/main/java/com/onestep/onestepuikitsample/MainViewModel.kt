package com.onestep.onestepuikitsample

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import co.onestep.android.core.OSTIdentificationState
import co.onestep.android.uikit.OSTTheme

class MainViewModel : ViewModel() {
    var sdkState by mutableStateOf<OSTIdentificationState>(OSTIdentificationState.Unidentified)
    var isConnecting by mutableStateOf(false)

    var colorsDropdownExpanded by mutableStateOf(false)
    var fontsDropdownExpanded by mutableStateOf(false)
    var currentFont by mutableStateOf(OSTTheme.font)
}
