package com.onestep.sdksample.screens

import android.Manifest
import android.os.Build
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import co.onestep.android.core.external.models.sdkOut.OSTInitResult
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.onestep.sdksample.viewmodels.MainViewModel

@Composable
@OptIn(ExperimentalPermissionsApi::class)
fun MainScreen(
    viewModel: MainViewModel,
    connect: () -> Unit,
    disconnect: () -> Unit
) {
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

        sdkInitialized == null || sdkInitialized is OSTInitResult.Error -> {
            SDKnotInitialized(viewModel) { connect() }
        }

        // OneStep recorder requires Activity Recognition permission since Android 14.0
        !activityRecognitionPermissionState.status.isGranted && isOverAndroid34 -> {
            NoActivityRecognitionPermission {
                activityRecognitionPermissionState.launchPermissionRequest()
            }
        }

        else -> WalkRecordScreen { disconnect() }
    }
}

val isOverAndroid34 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE


