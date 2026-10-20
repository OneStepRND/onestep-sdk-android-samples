package com.onestep.backgroundmonitoringsample.screens

import android.Manifest
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.onestep.backgroundmonitoringsample.ui.model.ActivityItem
import com.onestep.backgroundmonitoringsample.ui.model.MonitoringUiState
import com.onestep.backgroundmonitoringsample.ui.model.NotificationStyle
import com.onestep.backgroundmonitoringsample.ui.model.ScreenState


@Composable
@OptIn(ExperimentalPermissionsApi::class)
fun MainScreen(
    screenState: ScreenState,
    monitoringState: MonitoringUiState,
    notificationStyle: NotificationStyle,
    notificationDismissible: Boolean,
    records: List<ActivityItem>,
    onScreenStateChange: (ScreenState) -> Unit,
    onOptIn: () -> Unit,
    onOptOut: () -> Unit,
    onSelectNotificationStyle: (NotificationStyle) -> Unit,
    onNotificationDismissibleChange: (Boolean) -> Unit,
    onLoadRecords: () -> Unit,
) {
    // Permission for activity recognition is required to use the SDK
    val activityRecognitionPermissionState = rememberMultiplePermissionsState(
        permissions = listOf(Manifest.permission.ACTIVITY_RECOGNITION, Manifest.permission.POST_NOTIFICATIONS)
    )

    LaunchedEffect(Unit) {
        // Ask permissions if not granted
        if (!activityRecognitionPermissionState.permissions.first { it.permission == Manifest.permission.ACTIVITY_RECOGNITION }.status.isGranted) {
            activityRecognitionPermissionState.launchMultiplePermissionRequest()
        }
    }

    LaunchedEffect(activityRecognitionPermissionState.permissions.first { it.permission == Manifest.permission.ACTIVITY_RECOGNITION }.status) {
        // Opt in to monitoring once permissions are granted
        if (activityRecognitionPermissionState.permissions.first { it.permission == Manifest.permission.ACTIVITY_RECOGNITION }.status.isGranted) {
            onOptIn()
        }
    }

    BackHandler {
        if (screenState is ScreenState.Records) {
            onScreenStateChange(ScreenState.Initialized)
        }
    }

    AnimatedContent(
        targetState = screenState, label = "screen_state"
    ) { state ->
        when (state) {
            is ScreenState.Loading -> {
                Box(Modifier.fillMaxSize()) {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }
            }

            is ScreenState.Error -> SetupError(message = state.message)

            is ScreenState.Initialized -> {
                MonitoringScreen(
                    monitoringState = monitoringState,
                    notificationStyle = notificationStyle,
                    onOptIn = onOptIn,
                    onOptOut = onOptOut,
                    onSelectNotificationStyle = onSelectNotificationStyle,
                    notificationDismissible = notificationDismissible,
                    onNotificationDismissibleChange = onNotificationDismissibleChange,
                    onShowRecords = {
                        onScreenStateChange(ScreenState.Records)
                    },
                )
            }

            is ScreenState.Records -> {
                LaunchedEffect(Unit) { onLoadRecords() }
                RecordsListScreen(items = records)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MainScreenPreview() {
    MainScreen(
        screenState = ScreenState.Initialized,
        monitoringState = MonitoringUiState(),
        notificationStyle = NotificationStyle.DEFAULT,
        notificationDismissible = false,
        records = emptyList(),
        onScreenStateChange = {},
        onOptIn = {},
        onOptOut = {},
        onSelectNotificationStyle = {},
        onNotificationDismissibleChange = {},
        onLoadRecords = {},
    )
}

@Preview(showBackground = true)
@Composable
private fun MainScreenErrorPreview() {
    MainScreen(
        screenState = ScreenState.Error("SDK session lost: sample error"),
        monitoringState = MonitoringUiState(),
        notificationStyle = NotificationStyle.DEFAULT,
        notificationDismissible = false,
        records = emptyList(),
        onScreenStateChange = {},
        onOptIn = {},
        onOptOut = {},
        onSelectNotificationStyle = {},
        onNotificationDismissibleChange = {},
        onLoadRecords = {},
    )
}
