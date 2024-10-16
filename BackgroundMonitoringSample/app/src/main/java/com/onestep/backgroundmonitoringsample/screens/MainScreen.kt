package com.onestep.backgroundmonitoringsample.screens

import android.Manifest
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.onestep.backgroundmonitoringsample.ui.model.ScreenState
import com.onestep.backgroundmonitoringsample.viewmodels.MainViewModel


@Composable
@OptIn(ExperimentalPermissionsApi::class)
fun MainScreen(viewModel: MainViewModel) {
    // BackgroundMonitoringStats is a data class that holds the monitoring stats
    val collectionData = viewModel.backgroundStats.collectAsState()// by remember { mutableStateOf(BackgroundMonitoringStats.empty()) }
    val screenState = viewModel.screenState

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
        // Register background monitoring once permissions are granted
        if (activityRecognitionPermissionState.permissions.first { it.permission == Manifest.permission.ACTIVITY_RECOGNITION }.status.isGranted) {
            viewModel.registerForBackgroundMonitoring()
        }
    }

    BackHandler {
        if (screenState is ScreenState.AggregatedRecords) {
            viewModel.setState(ScreenState.Initialized)
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

            is ScreenState.Error -> {
                Box(Modifier.fillMaxSize()) {
                    Text(
                        modifier = Modifier.align(Alignment.Center),
                        text = state.message,
                        color = androidx.compose.ui.graphics.Color.Red,
                        fontSize = 20.sp
                    )
                }
            }

            is ScreenState.Initialized -> {
                MonitoringScreen(
                    collectionData = collectionData.value,
                    onShowRecords = {
                        viewModel.showRecords(it)
                    },
                )
            }

            is ScreenState.AggregatedRecords -> AggregateRecordsListScreen(aggregateType = state.aggregateType)
        }
    }
}