package com.onestep.onestepuikitsample

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import co.onestep.android.core.external.models.InitResult

class MainViewModel: ViewModel() {

    var sdkInitialized by mutableStateOf<InitResult?>(null)

    var isConnecting by mutableStateOf(false)

    companion object {
        val TAG: String = MainViewModel::class.simpleName ?: "MainViewModel"
    }
}
