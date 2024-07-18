package com.onestep.sdksample.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.onestep.sdksample.viewmodels.RecorderViewModel


@Composable
fun WalkRecordScreen(
    modifier: Modifier = Modifier,
) {
    val viewModel: RecorderViewModel = viewModel()
    val state = viewModel.state
    val result = viewModel.result

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp)
            .then(modifier)
    ) {
        Button(
            modifier = Modifier.padding(8.dp),
            onClick = {
                viewModel.startRecording(1 * 60 * 1000) // duration in milliseconds
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
