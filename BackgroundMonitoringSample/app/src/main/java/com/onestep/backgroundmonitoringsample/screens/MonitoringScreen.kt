package com.onestep.backgroundmonitoringsample.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
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
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.onestep.android.core.external.OneStep
import co.onestep.android.core.external.models.BackgroundMonitoringStats
import com.onestep.backgroundmonitoringsample.components.SafeSDKButton
import com.onestep.backgroundmonitoringsample.ui.model.AggregateType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

const val TAG = "SDKDevScreen"

@Composable
fun MonitoringScreen(
    collectionData: BackgroundMonitoringStats,
    onShowRecords: (AggregateType) -> Unit,
    refreshCollectionData: () -> Unit,
) {
    val context = LocalContext.current
    var recordingButtonText by remember { mutableStateOf(if (OneStep.isBackgroundMonitoringActive() ) "Stop bg recording" else "Start bg recording") }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
    ) {

        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(color = Color.Gray, modifier = Modifier.padding(vertical = 8.dp))
        ScreenTitle(Modifier.align(CenterHorizontally), "Background Monitoring ${if (collectionData.activated) "On" else "Off"}")
        Spacer(modifier = Modifier.height(16.dp))
        SafeSDKButton(modifier = Modifier.align(CenterHorizontally), action = {
            if (!OneStep.hasActivityRecognitionPermission()) {
                Toast.makeText(context, "No permissions granted", Toast.LENGTH_SHORT).show()
                return@SafeSDKButton
            }
            recordingButtonText =
                if (!OneStep.isBackgroundMonitoringActive()) "DEACTIVATE" else "ACTIVATE"
            if (OneStep.isBackgroundMonitoringActive()) {
                OneStep.unregisterBackgroundMonitoring()
            } else {
                OneStep.registerBackgroundMonitoring()
            }
            refreshCollectionData()
        }) {
            Text(recordingButtonText)
        }
        Spacer(modifier = Modifier.height(8.dp))
        SafeSDKButton(modifier = Modifier.align(CenterHorizontally), action = {
            OneStep.testBackgroundRecording()
        }) {
            Text("Test background recording")
        }
        Spacer(modifier = Modifier.height(32.dp))
        HorizontalDivider(color = Color.Gray, modifier = Modifier.padding(vertical = 8.dp))
        Spacer(modifier = Modifier.height(32.dp))
        ScreenTitle(Modifier.align(CenterHorizontally), text = "Your daily walk score")
        Spacer(modifier = Modifier.height(32.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {

            Button(onClick = { onShowRecords(AggregateType.HOURLY_BG_RECORDS) }) {
                Text(text = "Hourly")
            }
            Button(onClick = { onShowRecords(AggregateType.DAILY_BG_RECORDS) }) {
                Text(text = "Daily")
            }

            Button(onClick = { onShowRecords(AggregateType.ALL_RECORDS) }) {
                Text(text = "All")
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        ScreenTitle(text = "Updated: ${collectionData.lastResultSync.toUiDate(SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault()))}")
        HorizontalDivider(color = Color.Gray, modifier = Modifier.padding(vertical = 8.dp))
        Spacer(modifier = Modifier.height(32.dp))
        SafeSDKButton(
            modifier = Modifier.align(CenterHorizontally),
            action = {
                Toast.makeText(context, "Syncing OneStep data", Toast.LENGTH_SHORT).show()
                OneStep.sync()
                refreshCollectionData()
            }) {
            Text(
                fontSize = 20.sp,
                text = "Sync all recorded samples"
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
            textAlign = TextAlign.Center,
            fontSize = 24.sp,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .fillMaxWidth()
                .then(modifier)
        )
    }
}

@Composable
private fun Long.toUiDate(
    dateFormat: SimpleDateFormat,
): String = if (this == 0L) "Unavailable" else dateFormat.format(Date(this))

