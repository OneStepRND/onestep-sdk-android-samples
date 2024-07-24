package com.onestep.sdksample.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.TopStart
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import co.onestep.android.core.external.models.ParamName
import com.onestep.sdksample.viewmodels.RecorderViewModel


@Preview(showBackground = true)
@Composable
fun WalkRecordScreen(
    modifier: Modifier = Modifier,
    disconnect: () -> Unit = {}
) {
    val viewModel: RecorderViewModel = viewModel()
    val state = viewModel.state
    val motionMeasurement = viewModel.result

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp)
            .then(modifier)
    ) {
        Column(
            modifier = Modifier
                .padding(top = 40.dp)
                .align(Alignment.TopCenter)
        ) {
            Button(
                modifier = Modifier
                    .padding(8.dp)
                    .align(CenterHorizontally),
                onClick = {
                    viewModel.startRecording(1 * 60 * 1000) // duration in milliseconds
                }) {
                Text("Start Recording")
            }
            Button(
                modifier = Modifier
                    .padding(8.dp)
                    .align(CenterHorizontally),
                onClick = {
                    viewModel.stopRecording()
                }) {
                Text(text = "Stop Recording")
            }
            AnimatedContent(
                targetState = state.value,
                label = "State"
            ) { targetState ->
                Text(
                    modifier = Modifier.padding(8.dp),
                    fontSize = 28.sp,
                    text = "State = $targetState"
                )
            }
            AnimatedVisibility(
                visible = motionMeasurement.value?.params?.get(ParamName.WALKING_WALK_SCORE) != null,
                label = "Walk score"
            ) {
                Text(
                    modifier = Modifier.padding(8.dp),
                    fontSize = 28.sp,
                    text = "Walk score = ${motionMeasurement.value?.params?.get(ParamName.WALKING_WALK_SCORE)}"
                )
            }
            AnimatedVisibility(
                visible = motionMeasurement.value?.metadata?.steps != null,
                label = "Result"
            ) {
                Text(
                    modifier = Modifier.padding(8.dp),
                    fontSize = 28.sp,
                    text = "Steps = ${motionMeasurement.value?.metadata?.steps}"
                )
            }
            AnimatedVisibility(
                visible = motionMeasurement.value?.error != null,
                label = "Result"
            ) {
                Text(
                    modifier = Modifier.padding(8.dp),
                    fontSize = 28.sp,
                    color = Color.Red,
                    text = "Error = ${motionMeasurement.value?.error?.message}"
                )
            }
        }

        Card(
            Modifier
                .align(TopStart)
                .padding(start = 8.dp)
                .background(Color.Transparent)
                .clickable { disconnect() }
        ) {
            Text(
                modifier = Modifier.padding(8.dp),
                text = "DISCONNECT SDK"
            )
        }
    }
}
