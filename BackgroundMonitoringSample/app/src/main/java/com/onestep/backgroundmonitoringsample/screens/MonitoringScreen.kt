package com.onestep.backgroundmonitoringsample.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.onestep.android.core.OneStep
import co.onestep.android.core.monitoring.OSTMonitoringPreference
import co.onestep.android.core.monitoring.OSTMonitoringRuntimeState
import com.onestep.backgroundmonitoringsample.components.SafeSDKButton
import com.onestep.backgroundmonitoringsample.ui.model.ScreenType
import com.onestep.backgroundmonitoringsample.ui.model.MonitoringUiState

@Composable
fun MonitoringScreen(
    monitoringState: MonitoringUiState,
    onOptIn: () -> Unit,
    onOptOut: () -> Unit,
    onShowRecords: (ScreenType) -> Unit,
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
    ) {

        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(color = Color.Gray, modifier = Modifier.padding(vertical = 8.dp))
        ScreenTitle(
            Modifier.align(CenterHorizontally),
            when {
                monitoringState.isActive -> "Background Monitoring On"
                monitoringState.runtimeState is OSTMonitoringRuntimeState.Blocked -> {
                    val blockers = (monitoringState.runtimeState as OSTMonitoringRuntimeState.Blocked).reasons
                    "Monitoring Blocked: ${blockers.joinToString()}"
                }
                monitoringState.runtimeState is OSTMonitoringRuntimeState.Error -> "Monitoring Error"
                monitoringState.preference == OSTMonitoringPreference.OPTED_OUT -> "Monitoring Opted Out"
                else -> "Background Monitoring Off"
            }
        )
        Spacer(modifier = Modifier.height(16.dp))
        SafeSDKButton(
            modifier = Modifier.align(CenterHorizontally),
            icon = Icons.Default.PlayArrow,
            action = {
                if (monitoringState.isActive) {
                    onOptOut()
                } else {
                    onOptIn()
                }
            }) {
            Text(
                if (monitoringState.isActive) "Stop bg monitoring" else "Start bg monitoring",
                color = Color(0xFF4678B4),
            )
        }
        Spacer(modifier = Modifier.height(32.dp))
        HorizontalDivider(color = Color.Gray, modifier = Modifier.padding(vertical = 8.dp))
        Spacer(modifier = Modifier.height(32.dp))
        ScreenTitle(Modifier.align(CenterHorizontally), text = "Aggregated Data")
        Spacer(modifier = Modifier.height(32.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Button(
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                border = BorderStroke(1.dp, Color(0xFF4678B4)),
                onClick = { onShowRecords(ScreenType.DAILY_SUMMARIES) }) {
                Text(text = "Daily", color = Color(0xFF4678B4))
            }
            Button(
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                border = BorderStroke(1.dp, Color(0xFF4678B4)),
                onClick = { onShowRecords(ScreenType.WALKING_BOUTS) }) {
                Text(text = "Walking Bouts", color = Color(0xFF4678B4))
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        ScreenTitle(text = "Syncing")
        HorizontalDivider(color = Color.Gray, modifier = Modifier.padding(vertical = 8.dp))
        Spacer(modifier = Modifier.height(8.dp))
        SafeSDKButton(
            modifier = Modifier.align(CenterHorizontally),
            icon = Icons.Default.Create,
            action = {
                Toast.makeText(context, "Syncing OneStep data", Toast.LENGTH_SHORT).show()
                OneStep.sync()
            }) {
            Text(
                fontSize = 20.sp,
                color = Color(0xFF4678B4),
                text = "Sync Now"
            )
        }
    }
}

@Composable
private fun ScreenTitle(
    modifier: Modifier = Modifier,
    text: String
) {
    AnimatedContent(
        targetState = text,
        label = "",
    ) { title ->
        Text(
            text = title,
            textAlign = TextAlign.Start,
            fontSize = 24.sp,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .fillMaxWidth()
                .then(modifier)
        )
    }
}
