package com.onestep.sdksample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import co.onestep.android.core.external.OneStep
import co.onestep.android.core.external.models.ParamName
import com.onestep.sdksample.ui.theme.OneStepSDKSampleAppTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Box {
                // Before calling using the SDK, check if the OneStep SDK is initialized
                if (OneStep.isInitialized()) {
                    TestScreen()
                } else {
                    SDKnotInitialized()
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun TestScreen() {
    val viewModel: MainViewModel = viewModel()
    val result = remember { mutableStateOf(viewModel.measurement.value) }
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Button(onClick = {
            viewModel.startRecording(1 * 60 * 1000)
        }) {
            Text("Start Recording")
        }
        Button(onClick = {
            viewModel.stopRecording()
        }) {
            Text("Stop Recording")
        }
        Text("Result = ${result.value}")
    }
}

@Preview(showBackground = true)
@Composable
fun SDKnotInitialized() {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = "OneStep SDK is not initialized",
            modifier = Modifier.padding(16.dp).align(Center),
            fontSize = 36.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}