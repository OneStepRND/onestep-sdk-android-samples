package com.onestep.onestepuikitsample.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import co.onestep.android.core.external.OneStep
import co.onestep.android.core.external.models.sdkOut.OSTInitResult
import com.onestep.onestepuikitsample.MainViewModel
import com.onestep.onestepuikitsample.ui.componenets.Loading

@Composable
fun App(
    modifier: Modifier,
    viewModel: MainViewModel,
    connect: () -> Unit,
    onStartDefaultRecording: () -> Unit,
    onStartTugTest: () -> Unit,
    onStartStsTest: () -> Unit,
    onStartDualTaskTest: () -> Unit,
    onStartRomTest: () -> Unit,
    onStartBalanceTest: () -> Unit,
    onStartPermissionsFlow: () -> Unit,
    onStartCareLogActivity: () -> Unit,
) {
    LaunchedEffect(Unit) {
        // Collect monitoring stats from the SDK as a Kotlin Flow
        if (OneStep.isInitialized()) {
            OneStep.monitoringStatsFlow().collect {
                viewModel.collectionData.value = it
            }
        }
    }

    when {
        viewModel.isConnecting -> Loading()
        viewModel.sdkInitialized == null
                || viewModel.sdkInitialized is OSTInitResult.Error -> SDKnotInitialized(viewModel) { connect() }
        else -> MainScreen(
            modifier,
            onStartDefaultRecording,
            onStartTugTest,
            onStartStsTest,
            onStartDualTaskTest,
            onStartRomTest,
            onStartBalanceTest,
            onStartPermissionsFlow,
            onStartCareLogActivity,
            viewModel
        )
    }
}






