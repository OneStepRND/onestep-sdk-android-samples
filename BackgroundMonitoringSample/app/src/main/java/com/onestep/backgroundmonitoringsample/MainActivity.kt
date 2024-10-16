package com.onestep.backgroundmonitoringsample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import co.onestep.android.core.external.models.sdkOut.OSTInitResult
import com.onestep.backgroundmonitoringsample.screens.MainScreen
import com.onestep.backgroundmonitoringsample.ui.model.ScreenState
import com.onestep.backgroundmonitoringsample.viewmodels.MainViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        collectSDKConnectionState()
        setContent {
            MainScreen(viewModel)
        }
    }

    private fun collectSDKConnectionState() {
        viewModel.setState(ScreenState.Loading)
        lifecycleScope.launch {
            (application as BgMonitoringSampleApplication).sdkConnectionState.collect {
                if ((application as BgMonitoringSampleApplication).enableBackgroundMonitoring) {
                    when (it) {
                        is OSTInitResult.Success -> {
                            viewModel.collectMonitoringStats()
                            viewModel.setState(ScreenState.Initialized)
                        }

                        is OSTInitResult.Error -> viewModel.setState(ScreenState.Error(it.message))
                    }
                } else {
                    viewModel.setState(ScreenState.Error("Background monitoring is disabled"))
                }
            }
        }
    }
}