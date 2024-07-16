@file:OptIn(ExperimentalPermissionsApi::class)
package com.onestep.onestepuikitsample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.onestep.android.core.external.OneStep
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.onestep.onestepuikitsample.ui.theme.OneStepUiKitSampleTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val onInitializeSDK = {
            (application as UiKitSampleApplication).connect()
        }

        if (OneStep.isInitialized()) {
            onInitializeSDK()
        }

        if (!OneStep.hasActivityRecognitionPermission()) {
            startActivity(Per)
        }
        setContent {
            PermissionAwareScreen { onInitializeSDK() }
        }
    }
}

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
    Column(
        modifier = Modifier.fillMaxSize().padding(top = 16.dp).then(modifier)
    ) {
        Button(
            modifier = Modifier.padding(8.dp),
            onClick = {
                viewModel.startRecording(1 * 60) // duration in seconds
            }) {
            Text("Start recording flow")
        }
        Button(
            modifier = Modifier.padding(8.dp),
            onClick = {
                viewModel.stopRecording()
            }) {
            Text(text = "start permissions flow Recording")
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