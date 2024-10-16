package com.onestep.sdksample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import co.onestep.android.core.external.OneStep
import co.onestep.android.core.external.models.sdkOut.OSTInitResult
import com.onestep.sdksample.screens.MainScreen
import com.onestep.sdksample.viewmodels.MainViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        collectSDKConnectionState()
        setContent {
            MainScreen(
                viewModel,
                connect = {
                    connect(viewModel)
                },
                disconnect = {
                    // equivalent to "logout"
                    // cleaning: JWT token, preferences, cached data, workers, monitoring...
                    OneStep.disconnect()
                    viewModel.sdkInitialized = null
                }
            )
        }
    }

    private fun collectSDKConnectionState() {
        lifecycleScope.launch {
            (application as SDKSampleApplication).sdkConnectionState.collect {
                viewModel.sdkInitialized = it
                viewModel.isConnecting = false
            }
        }
    }


    private fun connect(viewModel: MainViewModel) {
        (application as SDKSampleApplication).connect { result ->
            viewModel.sdkInitialized = when (result) {
                is OSTInitResult.Success -> {
                    viewModel.isConnecting = false
                    result
                }

                is OSTInitResult.Error -> {
                    viewModel.isConnecting = false
                    result
                }
            }
        }
    }
}
