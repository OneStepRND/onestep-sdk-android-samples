package com.onestep.onestepuikitsample.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import co.onestep.android.core.external.OneStep
import co.onestep.android.core.external.models.BackgroundMonitoringStats
import co.onestep.android.core.external.models.InitResult
import com.onestep.backgroundmonitoringsample.screens.SDKnotInitialized
import com.onestep.onestepuikitsample.MainViewModel

@Composable
fun MainScreen(
    viewModel: MainViewModel,
    connect: () -> Unit,
    onsStartUikit: () -> Unit,
    onStartPermissionsFlow: () -> Unit
) {
    val sdkInitialized = viewModel.sdkInitialized
    val isConnecting = viewModel.isConnecting

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

        else -> {
            Column(Modifier.padding(32.dp)) {
                Button(onClick = { onsStartUikit() }) {
                    Text("START RECORDING FLOW")
                }
                Button(onClick = { onStartPermissionsFlow() }) {
                    Text("START PERMISSIONS FLOW")
                }
            }
        }
    }
}
