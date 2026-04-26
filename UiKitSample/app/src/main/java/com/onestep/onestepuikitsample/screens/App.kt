package com.onestep.onestepuikitsample.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import co.onestep.android.core.OSTState
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
    when {
        viewModel.isConnecting -> Loading()
        viewModel.sdkState is OSTState.Uninitialized
                || viewModel.sdkState is OSTState.Error -> SDKnotInitialized(viewModel) { connect() }
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
