package com.onestep.sdksample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import co.onestep.android.core.external.OneStep
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val onInitializeSDK = {
            (application as SDKSampleApplication).connect()
        }
        setContent {
            PermissionAwareScreen { onInitializeSDK() }
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
    modifier: Modifier = Modifier
) {
    val viewModel: MainViewModel = viewModel()
    val state = viewModel.state
    val result = viewModel.result

    Column(
        modifier = Modifier.fillMaxSize().padding(top = 16.dp).then(modifier)
    ) {
        Button(
            modifier = Modifier.padding(8.dp),
            onClick = {
            viewModel.startRecording(1 * 60) // duration in seconds
        }) {
            Text("Start Recording")
        }
        Button(
            modifier = Modifier.padding(8.dp),
            onClick = {
            viewModel.stopRecording()
        }) {
            Text(text = "Stop Recording")
        }
        Text(
            modifier = Modifier.padding(8.dp),
            text = "State = ${state.value}"
        )

        Text(
            modifier = Modifier.padding(8.dp),
            text = "Result = ${result.value}"
        )
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