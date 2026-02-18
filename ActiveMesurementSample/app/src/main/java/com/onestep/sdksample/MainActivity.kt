package com.onestep.sdksample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.lifecycleScope
import co.onestep.android.core.OneStep
import co.onestep.android.core.OSTState
import com.onestep.sdksample.screens.MainScreen
import com.onestep.sdksample.viewmodels.MainViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        collectSDKState()
        setContent {
            val coroutineScope = rememberCoroutineScope()
            MainScreen(
                viewModel,
                connect = {
                    coroutineScope.launch {
                        viewModel.isConnecting = true
                        (application as SDKSampleApplication).initializeSdk()
                    }
                },
                disconnect = {
                    // equivalent to "logout"
                    // cleaning: JWT token, preferences, cached data, workers, monitoring...
                    OneStep.logout()
                    viewModel.sdkState = OSTState.Uninitialized
                }
            )
        }
    }

    private fun collectSDKState() {
        lifecycleScope.launch {
            OneStep.state.collect { state ->
                viewModel.sdkState = state
                if (state !is OSTState.Uninitialized) {
                    viewModel.isConnecting = false
                }
            }
        }
    }
}
