package com.onestep.sdksample.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import co.onestep.android.core.OSTIdentificationState

class MainViewModel : ViewModel() {

    var sdkState by mutableStateOf<OSTIdentificationState>(OSTIdentificationState.Unidentified)

    // Starts false so the initial (Unidentified) state shows the Connect screen. It is set true
    // only while a connect attempt is in flight (see MainActivity), and cleared once the SDK
    // reports a non-Unidentified state. Defaulting to true here deadlocks the first run: the SDK
    // stays Unidentified until the user connects, but the Connect button is hidden by the spinner.
    var isConnecting by mutableStateOf(false)
}
