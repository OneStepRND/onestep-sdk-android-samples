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
import co.onestep.android.core.external.models.measurement.OSTActivityType
import co.onestep.android.core.external.models.sdkOut.OSTInitResult
import co.onestep.android.uikit.features.carlog.presentation.OSTCarelogActivity
import co.onestep.android.uikit.features.permissions.OSTPermissionFlowActivity
import co.onestep.android.uikit.features.recordFlow.configurations.OSTPreRecordingQuestionData
import co.onestep.android.uikit.features.recordFlow.configurations.OSTPrepareDuration
import co.onestep.android.uikit.features.recordFlow.configurations.OSTRecordingConfiguration
import co.onestep.android.uikit.features.recordFlow.presentation.OSTRecordingFlowActivity
import co.onestep.android.uikit.features.recordFlow.screens.instructions.OSTMeasurementInstructionsData
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
        collectSDKConnectionState()
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
                        onStartDefaultRecording = { defaultRecordingFlow() },
                        onStartSixMinuteWalkTest = { sixMinuteWalkTestRecordingFlow() },
                        onStartPermissionsFlow = {
                            if (context.applicationContext.checkSelfPermission(Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED) {
                                scope.launch {
                                    snackbarHostState.showSnackbar(message = permissionsMessage)
                                }
                            } else {
                                /*
                                 * Start the OneStep permission collection flow.
                                 */
                                startActivity(OSTPermissionFlowActivity.buildIntent(this))
                            }
                        },
                        onStartCareLogActivity = {
                            /*
                             * Start the OneStep UIKit Carelog (Activity History).
                             */
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
    private fun defaultRecordingFlow() {
        startActivity(
            OSTRecordingFlowActivity.buildIntent(
                this,
                config = OSTRecordingConfiguration.default(),
                customMetadata = mapOf("app" to "uikit sample app"),
            ),
        )
    }

    /**
     * Start the OneStep UIKit Recording Flow for a 6 Minute Walk Test.
     * This sample demonstrates the most extensive recording configuration and customization.
     */
    private fun sixMinuteWalkTestRecordingFlow() {
        val config = OSTRecordingConfiguration(
            activityType = OSTActivityType.WALK,
            duration = 360,
            isCountingDown = true,
            showPhonePositionScreen = false,
            prepareScreenDuration = OSTPrepareDuration.TEN_SECONDS,
            playVoiceOver = true,
            instructions = OSTMeasurementInstructionsData(
                activityDisplayName = "6 Minute Walk Test",
                instructions = listOf(
                    "The object of this test is to walk as far as possible for 6 minutes",
                    "You will walk back and forth in this hallway.",
                    "You are permitted to slow down, to stop, and to rest as necessary",
                ),
                hints = listOf(
                    "You may lean against the wall while resting",
                ),
                gifUrl = "https://thoracicandsleep.com.au/wp-content/uploads/2022/11/IDEA-LG-COPD-Monitoring-Model-Offers-Potential-Alternative-to-6-Minute-Walk-Test-image-1.jpg",
            ),
            preRecordingQuestions= listOf(
                    OSTPreRecordingQuestionData(
                    title = "Demo Question",
                    tagsValues = listOf("tag1", "tag2"),
                    isMultiSelect = false,
                ),
            )
        )
        startActivity(
            OSTRecordingFlowActivity.buildIntent(
                this,
                config =config,
                customMetadata = mapOf(
                    "app" to "uikit sample app",
                    "custom_activity" to "6mwt",
                ),
            ),
        )
    }

    private fun collectSDKConnectionState() {
        val uiKitSampleApplication = application as UiKitSampleApplication
        viewModel.isConnecting = uiKitSampleApplication.isConnecting
        lifecycleScope.launch {
            uiKitSampleApplication.sdkConnectionState.collect {
                viewModel.sdkInitialized = it
                viewModel.isConnecting = false
            }
        }
    }


    private fun connect() {
        viewModel.isConnecting = true
        (application as UiKitSampleApplication).connect { result ->
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