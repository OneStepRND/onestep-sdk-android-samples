package com.onestep.onestepuikitsample

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import co.onestep.android.core.external.OneStep
import co.onestep.android.core.external.models.InitResult
import co.onestep.android.uikit.features.inaapWalkFlow.RecordWalkFlowActivity
import co.onestep.android.uikit.features.inaapWalkFlow.configurations.WalkRecordConfiguration
import co.onestep.android.uikit.features.permissions.PermissionWizardActivity
import com.onestep.onestepuikitsample.screens.MainScreen
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()


    private val permissionsMessage = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        "Permission already granted, revoke permissions to test flow"
    } else {
        "Activity Recognition permission is not required On this Android version: (${Build.VERSION.SDK_INT})"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val scope = rememberCoroutineScope()
            val snackbarHostState = remember { SnackbarHostState() }

            Scaffold(
                snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                content = { padding ->
                    MainScreen(
                        viewModel,
                        connect = { connect() },
                        onsStartUikit = {
                            startActivity(
                                RecordWalkFlowActivity.buildIntent(
                                    this,
                                    WalkRecordConfiguration.default(),
                                    customMetadata = mapOf("app" to "sample app", "is_demo" to true, "version" to 1.1),
                                ),
                            )
                        },
                        onStartPermissionsFlow = {
                            if (OneStep.hasActivityRecognitionPermission()) {
                                scope.launch {
                                    snackbarHostState.showSnackbar(message = permissionsMessage)
                                }
                            } else {
                                startActivity(PermissionWizardActivity.buildIntent(this))
                            }
                        },
                    )
                }
            )
        }
    }


    private fun connect() {
        viewModel.isConnecting = true
        (application as UiKitSampleApplication).connect { result ->
            viewModel.sdkInitialized = when (result) {
                is InitResult.Success -> {
                    viewModel.isConnecting = false
                    result
                }

                is InitResult.Error -> {
                    viewModel.isConnecting = false
                    result
                }
            }
        }
    }
}