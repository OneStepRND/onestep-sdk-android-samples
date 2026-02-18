package com.onestep.backgroundmonitoringsample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import co.onestep.android.core.OSTState
import co.onestep.android.core.OneStep
import com.onestep.backgroundmonitoringsample.screens.MainScreen
import com.onestep.backgroundmonitoringsample.ui.model.ScreenState
import com.onestep.backgroundmonitoringsample.viewmodels.MainViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        collectSDKState()
        setContent {
            MainScreen(viewModel)
        }
    }

    private fun collectSDKState() {
        viewModel.setState(ScreenState.Loading)
        lifecycleScope.launch {
            OneStep.state.collect { state ->
                when (state) {
                    is OSTState.Identified -> {
                        viewModel.collectMonitoringState()
                        viewModel.setState(ScreenState.Initialized)
                    }
                    is OSTState.Error -> {
                        viewModel.setState(ScreenState.Error("SDK Error: ${state.message}"))
                    }
                    is OSTState.Ready -> {
                        // SDK initialized but not yet identified, keep loading
                    }
                    is OSTState.Uninitialized -> {
                        // Still loading
                    }
                }
            }
        }
    }
}
