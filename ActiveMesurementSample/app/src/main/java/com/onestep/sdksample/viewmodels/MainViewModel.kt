package com.onestep.sdksample.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import co.onestep.android.core.external.models.sdkOut.OSTInitResult

class MainViewModel: ViewModel() {

    var sdkInitialized by mutableStateOf<OSTInitResult?>(null)

    var isConnecting by mutableStateOf(true)

}
