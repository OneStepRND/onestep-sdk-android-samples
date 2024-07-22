package com.onestep.onestepuikitsample.screens

import android.Manifest
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import co.onestep.android.core.external.OneStep
import co.onestep.android.core.external.models.BackgroundMonitoringStats
import co.onestep.android.core.external.models.InitResult
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.onestep.backgroundmonitoringsample.screens.NoActivityRecognitionPermission
import com.onestep.backgroundmonitoringsample.screens.SDKnotInitialized
import com.onestep.onestepuikitsample.MainViewModel
import kotlinx.coroutines.launch

@Composable
@OptIn(ExperimentalPermissionsApi::class)
fun MainScreen(
    viewModel: MainViewModel,
    connect: () -> Unit,
    onsStartUikit: () -> Unit
) {
    val sdkInitialized = viewModel.sdkInitialized
    val isConnecting = viewModel.isConnecting
    val scope = rememberCoroutineScope()

    // Permission for activity recognition is required to use the SDK
    val activityRecognitionPermissionState = rememberPermissionState(
        permission = Manifest.permission.ACTIVITY_RECOGNITION
    )

    // BackgroundMonitoringStats is a data class that holds the monitoring stats
    var collectionData by remember { mutableStateOf(BackgroundMonitoringStats.empty()) }

    LaunchedEffect(Unit) {
        // Collect monitoring stats from the SDK as a Kotlin Flow
        if (OneStep.isInitialized()) {
            OneStep.monitoringStatsFlow().collect {
                collectionData = it
            }
        }
    }

    when {
        isConnecting -> {
            Box(Modifier.fillMaxSize()) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            }
        }

        sdkInitialized == null || sdkInitialized is InitResult.Error -> {
            SDKnotInitialized(viewModel) {
                connect()
            }
        }

        !activityRecognitionPermissionState.status.isGranted -> {
            NoActivityRecognitionPermission {
                activityRecognitionPermissionState.launchPermissionRequest()
            }
        }

        else -> {
            Box {
                Button(onClick = { onsStartUikit() }) {
                    Text("START RECORDING FLOW")
                }
            }
        }
    }
}

