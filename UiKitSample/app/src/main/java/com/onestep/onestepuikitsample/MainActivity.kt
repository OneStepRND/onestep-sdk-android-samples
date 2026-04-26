package com.onestep.onestepuikitsample

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.lifecycleScope
import co.onestep.android.core.OneStep
import co.onestep.android.uikit.features.carlog.presentation.OSTCarelogActivity
import co.onestep.android.uikit.features.permissions.OSTPermissionFlowActivity
import co.onestep.android.uikit.features.recordFlow.configurations.OSTRecordingConfiguration
import co.onestep.android.uikit.features.recordFlow.presentation.OSTRecordingFlowActivity
import com.onestep.onestepuikitsample.screens.App
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val viewModel: MainViewModel by viewModels()

    private val permissionsMessage = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        "Permission already granted, revoke permissions to test flow"
    } else {
        "Activity Recognition permission is not required On this Android version: (${Build.VERSION.SDK_INT})"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        enableEdgeToEdge()
        if (supportActionBar != null) {
            supportActionBar?.hide()
        }
        collectSDKState()

        setContent {
            val scope = rememberCoroutineScope()
            val snackbarHostState = remember { SnackbarHostState() }
            val context = LocalContext.current
            Scaffold(
                snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                content = { padding ->
                    App(
                        modifier = Modifier.padding(padding).background(color = MaterialTheme.colorScheme.surface),
                        viewModel,
                        connect = { connect() },
                        onStartDefaultRecording = {
                            startRecordingFlow(OSTRecordingConfiguration.defaultWalk(this).copy(readyForAnalysisUiAssist = true))
                                                  },
                        onStartTugTest = { startRecordingFlow(OSTRecordingConfiguration.tug(this)) },
                        onStartStsTest = { startRecordingFlow(OSTRecordingConfiguration.sts(this)) },
                        onStartDualTaskTest = { startRecordingFlow(OSTRecordingConfiguration.dualTaskSubtract(this, instructions = "This will be displayed in the preparation screen prior to the test, you can use this stub to read instructions to your subject")) },
                        onStartRomTest = { startRecordingFlow(OSTRecordingConfiguration.romExt(this)) },
                        onStartBalanceTest = { startRecordingFlow(OSTRecordingConfiguration.balanceTest(this)) },
                        onStartPermissionsFlow = {
                            if (context.applicationContext.checkSelfPermission(Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED) {
                                scope.launch {
                                    snackbarHostState.showSnackbar(message = permissionsMessage)
                                }
                            } else {
                                startActivity(OSTPermissionFlowActivity.buildIntent(this))
                            }
                        },
                        onStartCareLogActivity = {
                            startActivity(OSTCarelogActivity.buildIntent(this))
                        },
                    )
                }
            )
        }
    }

    /**
     * Start the OneStep UIKit Recording Flow.
     * Custom technical metadata can be passed to the flow.
     * Recording configuration can be customized as needed.
     */
    private fun startRecordingFlow(config: OSTRecordingConfiguration) {
        startActivity(
            OSTRecordingFlowActivity.buildIntent(
                this,
                config = config,
                customMetadata = mapOf("app" to "uikit sample app"),
            ),
        )
    }

    private fun collectSDKState() {
        lifecycleScope.launch {
            OneStep.state.collect { state ->
                viewModel.sdkState = state
                viewModel.isConnecting = false
            }
        }
    }

    private fun connect() {
        viewModel.isConnecting = true
        lifecycleScope.launch {
            (application as UiKitSampleApplication).initializeSdk()
        }
    }
}
