package com.onestep.sdksample.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import co.onestep.android.core.OSTIdentificationState

class MainViewModel : ViewModel() {

    var sdkState by mutableStateOf<OSTIdentificationState>(OSTIdentificationState.Unidentified)

    var isConnecting by mutableStateOf(true)
}
