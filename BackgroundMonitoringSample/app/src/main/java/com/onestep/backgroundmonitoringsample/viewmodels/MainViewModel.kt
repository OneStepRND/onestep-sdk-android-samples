package com.onestep.backgroundmonitoringsample.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.onestep.backgroundmonitoringsample.ui.model.AggregateType
import com.onestep.backgroundmonitoringsample.ui.model.ScreenState

class MainViewModel: ViewModel() {

    private var permissionState by mutableStateOf(false)

    private var isInitialized by mutableStateOf(false)

    var screenState by mutableStateOf<ScreenState>(ScreenState.NotInitialized(""))

    fun setState(state: ScreenState) {
        if (state is ScreenState.Initialized) {
            screenState = if (permissionState) {
                isInitialized = true
                ScreenState.Initialized
            } else {
                ScreenState.NoPermission
            }
        } else {
            screenState = state
        }
    }

    fun setPermissionGranted(isGranted: Boolean) {
        permissionState = isGranted
        screenState = if (isGranted) {
            if (isInitialized) {
                ScreenState.Initialized
            } else {
                ScreenState.NotInitialized("")
            }
        } else {
            ScreenState.NoPermission
        }
    }

    fun showRecords(it: AggregateType) {
        screenState = ScreenState.AggregatedRecords(it)
    }

    companion object {
        val TAG: String = MainViewModel::class.simpleName ?: "MainViewModel"
    }
}
