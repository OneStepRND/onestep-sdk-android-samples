package com.onestep.sdksample.screens

import android.Manifest
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import co.onestep.android.core.external.models.InitResult
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.onestep.backgroundmonitoringsample.screens.WalkRecordScreen
import com.onestep.sdksample.viewmodels.MainViewModel

@Composable
@OptIn(ExperimentalPermissionsApi::class)
fun MainScreen(viewModel: MainViewModel, connect: (MainViewModel) -> Unit) {
    val sdkInitialized = viewModel.sdkInitialized
    val isConnecting = viewModel.isConnecting

    val activityRecognitionPermissionState = rememberPermissionState(
        permission = Manifest.permission.ACTIVITY_RECOGNITION
    )

    when {
        isConnecting -> {
            Box(Modifier.fillMaxSize()) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            }
        }

        sdkInitialized == null || sdkInitialized is InitResult.Error -> {
            SDKnotInitialized(viewModel) {
                connect(viewModel)
            }
        }

        !activityRecognitionPermissionState.status.isGranted -> {
            NoActivityRecognitionPermission {
                activityRecognitionPermissionState.launchPermissionRequest()
            }
        }

        else -> {
            WalkRecordScreen()
        }
    }
}

