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
import co.onestep.android.core.OSTIdentificationState
import co.onestep.android.core.OneStep
import co.onestep.android.uikit.features.carlog.presentation.OSTCarelogActivity
import co.onestep.android.uikit.features.permissions.OSTPermissionFlowActivity
import co.onestep.android.uikit.features.recordFlow.configurations.OSTRecordingConfiguration
import co.onestep.android.uikit.features.recordFlow.presentation.OSTRecordingFlowActivity
import com.onestep.onestepuikitsample.screens.App
import com.onestep.onestepuikitsample.screens.MISSING_CREDENTIALS_MESSAGE
import com.onestep.onestepuikitsample.screens.RecordingFlow
import com.onestep.onestepuikitsample.screens.SDK_INIT_FAILED_MESSAGE
import com.onestep.onestepuikitsample.screens.SetupError
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
        val app = application as UiKitSampleApplication
        val oneStep = app.oneStepSdk
        // Fail fast: without an initialized SDK or credentials, nothing below can work.
        if (oneStep == null || app.missingCredentials.isNotEmpty()) {
            val message = if (oneStep == null) SDK_INIT_FAILED_MESSAGE else MISSING_CREDENTIALS_MESSAGE
            setContent {
                Scaffold { padding -> SetupError(message = message, modifier = Modifier.padding(padding)) }
            }
            return
        }
        collectSDKState(oneStep)

        setContent {
            val scope = rememberCoroutineScope()
            val snackbarHostState = remember { SnackbarHostState() }
            val context = LocalContext.current
            val sdkState = viewModel.sdkState
            Scaffold(
                snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                content = { padding ->
                    App(
                        modifier = Modifier.padding(padding).background(color = MaterialTheme.colorScheme.surface),
                        isConnecting = viewModel.isConnecting,
                        isIdentified = sdkState is OSTIdentificationState.Identified,
                        sessionLostMessage = (sdkState as? OSTIdentificationState.Lost)
                            ?.let { "Session lost: ${it.cause.message}" },
                        currentFont = viewModel.currentFont,
                        onFontChange = { viewModel.currentFont = it },
                        colorsDropdownExpanded = viewModel.colorsDropdownExpanded,
                        onColorsDropdownExpandedChange = { viewModel.colorsDropdownExpanded = it },
                        fontsDropdownExpanded = viewModel.fontsDropdownExpanded,
                        onFontsDropdownExpandedChange = { viewModel.fontsDropdownExpanded = it },
                        connect = { connect() },
                        onStartRecording = { flow -> startRecordingFlow(flow.toConfiguration()) },
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

    /** Maps each sample flow to its UIKit configuration factory. */
    private fun RecordingFlow.toConfiguration(): OSTRecordingConfiguration = when (this) {
        RecordingFlow.WALK ->
            OSTRecordingConfiguration.defaultWalk(this@MainActivity).copy(readyForAnalysisUiAssist = true)
        RecordingFlow.TUG -> OSTRecordingConfiguration.tug(this@MainActivity)
        RecordingFlow.STS -> OSTRecordingConfiguration.sts(this@MainActivity)
        RecordingFlow.KNEE_ROM -> OSTRecordingConfiguration.romExt(this@MainActivity)
        RecordingFlow.BALANCE -> OSTRecordingConfiguration.balanceTest(this@MainActivity)
        RecordingFlow.DUAL_TASK -> OSTRecordingConfiguration.dualTaskSubtract(
            this@MainActivity,
            instructions = "This will be displayed in the preparation screen prior to the test, " +
                "you can use this stub to read instructions to your subject",
        )
        RecordingFlow.SIX_MINUTE_WALK -> OSTRecordingConfiguration.sixMinuteWalk(this@MainActivity)
        RecordingFlow.TWO_MINUTE_WALK -> OSTRecordingConfiguration.twoMinuteWalk(this@MainActivity)
        RecordingFlow.STAIRS -> OSTRecordingConfiguration.stairs(this@MainActivity)
        // Open-ended recording with no known protocol (SDK 2.1.4+): uploaded but never analyzed.
        // `title` lets the clinician name it, since OneStep does not know what was recorded.
        RecordingFlow.GENERIC -> OSTRecordingConfiguration.genericRecording().copy(title = "Sample session")
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

    private fun collectSDKState(oneStep: OneStep) {
        lifecycleScope.launch {
            oneStep.identificationState.collect { state ->
                viewModel.sdkState = state
                viewModel.isConnecting = false
            }
        }
    }

    private fun connect() {
        viewModel.isConnecting = true
        lifecycleScope.launch {
            (application as UiKitSampleApplication).connectUser()
        }
    }
}
