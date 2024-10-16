package com.onestep.sdksample.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.BottomEnd
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.TopStart
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import co.onestep.android.core.external.models.OSTParamName
import co.onestep.android.core.external.models.recording.OSTRecorderState
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
                .padding(top = 20.dp)
                .align(Alignment.TopCenter)
        ) {
            MainButton(
                Modifier.padding(horizontal = 16.dp),
                text = "Start Recording",
                icon = Icons.Default.PlayArrow,
                enabled = state.value != OSTRecorderState.RECORDING.name || state.value == OSTRecorderState.FINALIZING.name,
            ) {
                viewModel.startRecording() // 60 seconds recording
            }
            MainButton(
                Modifier.padding(horizontal = 16.dp),
                text = "Stop Recording",
                icon = Icons.Default.MailOutline,
                enabled = state.value == OSTRecorderState.RECORDING.name,
            ) {
                viewModel.stopRecording()
            }
            AnimatedContent(
                modifier = Modifier.align(CenterHorizontally),
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
                visible = motionMeasurement.value?.params?.get(OSTParamName.WALKING_WALK_SCORE) != null,
                label = "Walk score"
            ) {
                Text(
                    modifier = Modifier.padding(8.dp),
                    fontSize = 28.sp,
                    text = "Walk score = ${motionMeasurement.value?.params?.get(OSTParamName.WALKING_WALK_SCORE)}"
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
                .align(BottomEnd)
                .padding(32.dp)
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

@Composable
private fun MainButton(
    modifier: Modifier = Modifier,
    text: String,
    icon: ImageVector,
    enabled: Boolean = true,
    action: () -> Unit
) {
    Button(
        enabled = enabled,
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
        onClick = action
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = Color(0xFF4678B4)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                fontSize = 20.sp,
                color = Color(0xFF4678B4),
            )
        }
    }
}
