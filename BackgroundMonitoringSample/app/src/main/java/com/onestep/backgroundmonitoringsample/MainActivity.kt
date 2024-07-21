package com.onestep.backgroundmonitoringsample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import co.onestep.android.core.external.OneStep
import co.onestep.android.core.external.models.BackgroundMonitoringStats
import co.onestep.android.core.external.models.InitResult
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.onestep.backgroundmonitoringsample.ui.theme.BackgroundMonitoringSampleTheme
import com.onestep.sdksample.screens.MainScreen
import com.onestep.sdksample.viewmodels.MainViewModel
import kotlinx.coroutines.flow.Flow

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainScreen(
                viewModel,
                connect = { connect(it) }
            )
        }
    }


    private fun connect(viewModel: MainViewModel) {
        (application as BgMonitoringSampleApplication).connect { result ->
            viewModel.sdkInitialized = when (result) {
                is InitResult.Success -> {
                    viewModel.isConnecting = false
                    result
                }

                is InitResult.Error -> {
                    viewModel.isConnecting = false
                    result
                }
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionAwareScreen(
    onInitializeSDK: () -> Unit = {}
) {
    val activityRecognitionPermissionState = rememberPermissionState(
        permission = android.Manifest.permission.ACTIVITY_RECOGNITION
    )

    when {
        !OneStep.isInitialized() -> SDKnotInitialized { onInitializeSDK() }

        !activityRecognitionPermissionState.status.isGranted -> {
            NoActivityRecognitionPermission {
                activityRecognitionPermissionState.launchPermissionRequest()
            }
        }
        else -> {
            TestScreen()
        }
    }
}


@Preview(showBackground = true)
@Composable
fun TestScreen(
    modifier: Modifier = Modifier,
) {

    val application = LocalContext.current.applicationContext as BgMonitoringSampleApplication
    val dataFlow = remember { mutableStateOf<Flow<BackgroundMonitoringStats>?>(null) }

    // Launching effect to fetch the flow
    LaunchedEffect(Unit) {
        dataFlow.value = OneStep.monitoringStatsFlow()
    }

    // State to hold the latest data
    val dataState = remember { mutableStateOf(BackgroundMonitoringStats.empty()) }

    // Collecting the Flow
    dataFlow.value?.let { flow ->
        LaunchedEffect(flow) {
            flow.collect { latestData ->
                dataState.value = latestData
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp)
            .then(modifier)
    ) {

        Text(text = "SDK status: ${if (OneStep.isInitialized()) "Connected" else "Not connected"}")
        Card {
            Column {
                Text(text = "Bg monitoring active: ${dataState.value.bgMonitoringActivated}")
                Text(text = "hasPermissions: ${dataState.value.bgPermissions}")
            }
        }



        Button(onClick = {

        }) {
            Text("Start recording flow")

        }
    }
}

@Preview(showBackground = true)
@Composable
fun SDKnotInitialized(onInitialized: () -> Unit = {}) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        Text(
            text = "OneStep SDK is not initialized",
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.CenterHorizontally),
            fontSize = 36.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Button(
            modifier = Modifier.padding(16.dp),
            onClick = { onInitialized() } ) {
            Text("Initialize SDK")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NoActivityRecognitionPermission(
    onAskPermission: () -> Unit = {},
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        Text(
            text = "Activity Recognition Permission is not granted",
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.CenterHorizontally),
            fontSize = 36.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Button(
            modifier = Modifier.padding(16.dp),
            onClick = { onAskPermission() } ) {
            Text("Ask Permission")
        }
    }
}