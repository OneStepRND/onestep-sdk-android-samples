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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.BottomEnd
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import co.onestep.android.core.OSTParamName
import com.onestep.sdksample.viewmodels.RecorderUiState
import com.onestep.sdksample.viewmodels.RecorderViewModel

@Composable
fun WalkRecordScreen(
    modifier: Modifier = Modifier,
    disconnect: () -> Unit = {},
) {
    val viewModel: RecorderViewModel = viewModel()
    val state by viewModel.state.collectAsState()

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
                modifier = Modifier.padding(horizontal = 16.dp),
                text = "Start Recording",
                icon = Icons.Default.PlayArrow,
                enabled = state.canStart,
                onClick = viewModel::startRecording,
            )
            MainButton(
                modifier = Modifier.padding(horizontal = 16.dp),
                text = "Stop Recording",
                icon = Icons.Default.MailOutline,
                enabled = state is RecorderUiState.Recording,
                onClick = viewModel::stopRecording,
            )

            AnimatedContent(
                modifier = Modifier.align(CenterHorizontally),
                targetState = state.label,
                label = "phase",
            ) { phase ->
                Text(
                    modifier = Modifier.padding(8.dp),
                    fontSize = 28.sp,
                    text = "State = $phase",
                )
            }

            AnimatedVisibility(
                visible = state is RecorderUiState.Analyzed,
                label = "result",
            ) {
                val analyzed = state as? RecorderUiState.Analyzed ?: return@AnimatedVisibility
                Column {
                    Text(
                        modifier = Modifier.padding(8.dp),
                        fontSize = 28.sp,
                        text = "Walk score = ${analyzed.measurement.params[OSTParamName.WALKING_WALK_SCORE] ?: "N/A"}",
                    )
                    Text(
                        modifier = Modifier.padding(8.dp),
                        fontSize = 28.sp,
                        text = "Steps = ${analyzed.measurement.metadata.steps}",
                    )
                }
            }

            AnimatedVisibility(
                visible = state is RecorderUiState.Failed,
                label = "error",
            ) {
                val failed = state as? RecorderUiState.Failed ?: return@AnimatedVisibility
                Text(
                    modifier = Modifier.padding(8.dp),
                    fontSize = 28.sp,
                    color = Color.Red,
                    text = "Error = ${failed.reason}",
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
                text = "DISCONNECT SDK",
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
    onClick: () -> Unit,
) {
    Button(
        enabled = enabled,
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
        ) {
            Icon(imageVector = icon, contentDescription = text, tint = Color(0xFF4678B4))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = text, fontSize = 20.sp, color = Color(0xFF4678B4))
        }
    }
}

private val RecorderUiState.label: String
    get() = when (this) {
        RecorderUiState.Idle -> "Idle"
        RecorderUiState.Recording -> "Recording"
        RecorderUiState.Finalizing -> "Finalizing"
        RecorderUiState.Uploading -> "Uploading"
        RecorderUiState.Analyzing -> "Analyzing"
        is RecorderUiState.Analyzed -> "Analyzed"
        is RecorderUiState.Failed -> "Failed"
    }

private val RecorderUiState.canStart: Boolean
    get() = this is RecorderUiState.Idle ||
            this is RecorderUiState.Analyzed ||
            this is RecorderUiState.Failed
