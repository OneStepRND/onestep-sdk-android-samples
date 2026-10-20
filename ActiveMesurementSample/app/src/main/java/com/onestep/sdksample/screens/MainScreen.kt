package com.onestep.sdksample.screens

import android.Manifest
import android.os.Build
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

/** Owns the permission state, which needs an Activity, and renders [MainScreenContent]. */
@Composable
@OptIn(ExperimentalPermissionsApi::class)
fun MainScreen(
    isConnecting: Boolean,
    isIdentified: Boolean,
    sessionLostMessage: String?,
    connect: () -> Unit,
    disconnect: () -> Unit
) {
    val activityRecognitionPermissionState = rememberPermissionState(
        permission = Manifest.permission.ACTIVITY_RECOGNITION
    )

    MainScreenContent(
        isConnecting = isConnecting,
        isIdentified = isIdentified,
        sessionLostMessage = sessionLostMessage,
        hasActivityRecognitionPermission = activityRecognitionPermissionState.status.isGranted,
        onRequestPermission = activityRecognitionPermissionState::launchPermissionRequest,
        connect = connect,
        disconnect = disconnect,
    )
}

@Composable
private fun MainScreenContent(
    isConnecting: Boolean,
    isIdentified: Boolean,
    sessionLostMessage: String?,
    hasActivityRecognitionPermission: Boolean,
    onRequestPermission: () -> Unit,
    connect: () -> Unit,
    disconnect: () -> Unit,
) {
    when {
        isConnecting -> {
            Box(Modifier.fillMaxSize()) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            }
        }

        // Unidentified or Lost
        !isIdentified -> {
            SDKnotInitialized(sessionLostMessage) { connect() }
        }

        // OneStep recorder requires Activity Recognition permission since Android 14.0
        !hasActivityRecognitionPermission && isOverAndroid34 -> {
            NoActivityRecognitionPermission { onRequestPermission() }
        }

        else -> WalkRecordScreen { disconnect() }
    }
}

val isOverAndroid34 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE

@Preview(showBackground = true)
@Composable
private fun MainScreenNotIdentifiedPreview() {
    MainScreenContent(
        isConnecting = false,
        isIdentified = false,
        sessionLostMessage = null,
        hasActivityRecognitionPermission = true,
        onRequestPermission = {},
        connect = {},
        disconnect = {},
    )
}

@Preview(showBackground = true)
@Composable
private fun MainScreenConnectingPreview() {
    MainScreenContent(
        isConnecting = true,
        isIdentified = false,
        sessionLostMessage = null,
        hasActivityRecognitionPermission = true,
        onRequestPermission = {},
        connect = {},
        disconnect = {},
    )
}
