package com.onestep.sdksample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.lifecycleScope
import co.onestep.android.core.OSTIdentificationState
import com.onestep.sdksample.screens.MainScreen
import com.onestep.sdksample.viewmodels.MainViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as SDKSampleApplication
        collectSDKState(app)
        setContent {
            val coroutineScope = rememberCoroutineScope()
            MainScreen(
                viewModel,
                connect = {
                    coroutineScope.launch {
                        viewModel.isConnecting = true
                        app.connectUser()
                    }
                },
                disconnect = {
                    // equivalent to "logout" — clears the bound patient,
                    // tokens, preferences, cached data, workers, monitoring…
                    app.oneStepSdk.clearPatient()
                    viewModel.sdkState = OSTIdentificationState.Unidentified
                }
            )
        }
    }

    private fun collectSDKState(app: SDKSampleApplication) {
        lifecycleScope.launch {
            app.oneStepSdk.identificationState.collect { state ->
                viewModel.sdkState = state
                if (state !is OSTIdentificationState.Unidentified) {
                    viewModel.isConnecting = false
                }
            }
        }
    }
}
