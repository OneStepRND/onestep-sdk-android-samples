package com.onestep.sdksample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import co.onestep.android.core.OSTIdentificationState
import co.onestep.android.core.OneStep
import com.onestep.sdksample.screens.MISSING_CREDENTIALS_MESSAGE
import com.onestep.sdksample.screens.MainScreen
import com.onestep.sdksample.screens.SDK_INIT_FAILED_MESSAGE
import com.onestep.sdksample.screens.SetupError
import com.onestep.sdksample.viewmodels.MainViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // targetSdk 35+ draws edge to edge; safeDrawingPadding keeps content clear of system bars.
        enableEdgeToEdge()
        val app = application as SDKSampleApplication
        val oneStep = app.oneStepSdk
        // Fail fast: without an initialized SDK or credentials, nothing below can work.
        if (oneStep == null || app.missingCredentials.isNotEmpty()) {
            val message = if (oneStep == null) SDK_INIT_FAILED_MESSAGE else MISSING_CREDENTIALS_MESSAGE
            setContent {
                Box(Modifier.safeDrawingPadding()) { SetupError(message = message) }
            }
            return
        }
        collectSDKState(oneStep)
        setContent {
            val coroutineScope = rememberCoroutineScope()
            Box(Modifier.safeDrawingPadding()) {
                val sdkState = viewModel.sdkState
                MainScreen(
                    isConnecting = viewModel.isConnecting,
                    isIdentified = sdkState is OSTIdentificationState.Identified,
                    sessionLostMessage = (sdkState as? OSTIdentificationState.Lost)
                        ?.let { "Session lost: ${it.cause.message}" },
                    connect = {
                        coroutineScope.launch {
                            viewModel.isConnecting = true
                            // On success the identificationState collector clears the spinner once
                            // the SDK reports Identified. Clear it here only on failure, so the
                            // "Identify user" screen never flashes between the two.
                            if (!app.connectUser()) viewModel.isConnecting = false
                        }
                    },
                    disconnect = {
                        // equivalent to "logout" — clears the bound patient,
                        // tokens, preferences, cached data, workers, monitoring…
                        oneStep.clearPatient()
                        viewModel.sdkState = OSTIdentificationState.Unidentified
                    }
                )
            }
        }
    }

    private fun collectSDKState(oneStep: OneStep) {
        lifecycleScope.launch {
            oneStep.identificationState.collect { state ->
                viewModel.sdkState = state
                if (state !is OSTIdentificationState.Unidentified) {
                    viewModel.isConnecting = false
                }
            }
        }
    }
}
