package com.onestep.backgroundmonitoringsample.ui.model

sealed class ScreenState {
    data object Initialized : ScreenState()
    data class Error(val message: String) : ScreenState()
    data object Loading : ScreenState()
    data class Records(val screenType: ScreenType) : ScreenState()
}