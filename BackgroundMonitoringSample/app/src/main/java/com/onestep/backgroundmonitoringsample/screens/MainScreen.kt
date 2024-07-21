package com.onestep.backgroundmonitoringsample.screens

import android.Manifest
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
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
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.onestep.backgroundmonitoringsample.ui.model.ScreenState
import com.onestep.backgroundmonitoringsample.viewmodels.MainViewModel
import kotlinx.coroutines.launch

@Composable
@OptIn(ExperimentalPermissionsApi::class)
fun MainScreen(
    viewModel: MainViewModel,
    connect: () -> Unit
) {
    val screenState = viewModel.screenState
    val scope = rememberCoroutineScope()

    // Permission for activity recognition is required to use the SDK
    val activityRecognitionPermissionState = rememberPermissionState(
        permission = Manifest.permission.ACTIVITY_RECOGNITION
    )

    LaunchedEffect(activityRecognitionPermissionState.status) {
        viewModel.setPermissionGranted(activityRecognitionPermissionState.status.isGranted)
    }

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

    BackHandler {
        if (screenState is ScreenState.AggregatedRecords) {
            viewModel.setState(ScreenState.Initialized)
        }
    }

    AnimatedContent(
        targetState = screenState, label = "screen_state"
    ) { state ->
        when (state) {
            ScreenState.Loading -> {
                Box(Modifier.fillMaxSize()) {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }
            }
            ScreenState.NoPermission -> NoActivityRecognitionPermission {
                    activityRecognitionPermissionState.launchPermissionRequest()
                }

            is ScreenState.NotInitialized -> SDKnotInitialized(viewModel) { connect() }

            ScreenState.Initialized -> {
                MonitoringScreen(
                    collectionData = collectionData,
                    onShowRecords = {
                        viewModel.showRecords(it)
                    },
                ) {
                    scope.launch {
                        if (OneStep.isInitialized()) {
                            OneStep.monitoringStatsFlow().collect {
                                collectionData = it
                            }
                        }
                    }
                }
            }

            is ScreenState.AggregatedRecords -> AggregateRecordsListScreen(aggregateType = state.aggregateType)
        }
    }
}