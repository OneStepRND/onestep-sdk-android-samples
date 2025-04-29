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
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import co.onestep.android.core.external.models.OSTBackgroundRegistrationResult
import co.onestep.android.core.external.models.sdkOut.OSTBackgroundMonitoringStats
import com.onestep.backgroundmonitoringsample.components.SafeSDKButton
import com.onestep.backgroundmonitoringsample.ui.model.AggregateType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MonitoringScreen(
    collectionData: OSTBackgroundMonitoringStats,
    onShowRecords: (AggregateType) -> Unit,
) {
    val context = LocalContext.current

    var regResult by remember { mutableStateOf<OSTBackgroundRegistrationResult?>(null) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
    ) {

        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(color = Color.Gray, modifier = Modifier.padding(vertical = 8.dp))
        ScreenTitle(Modifier.align(CenterHorizontally),
            when {
                regResult != null -> when (regResult)  {
                    OSTBackgroundRegistrationResult.success() -> "Background monitoring started"
                    OSTBackgroundRegistrationResult.alreadyActivated() -> "Background monitoring activated"
                    OSTBackgroundRegistrationResult.noPermission() -> "Error: Permissions not granted"
                    OSTBackgroundRegistrationResult.featureNotEnabled() -> "Error: Background monitoring not available"
                    else -> "Error: Unknown error"
                }
                else -> "Background Monitoring ${if (collectionData.activated) "On" else "Off"}"
            }
        )
        Spacer(modifier = Modifier.height(16.dp))
        SafeSDKButton(
            modifier = Modifier.align(CenterHorizontally),
            icon = Icons.Default.PlayArrow,
            action = {
                /**
                 * This is the main function to start or stop the background monitoring.
                  */
                if (collectionData.activated) {
                OneStep.unregisterBackgroundMonitoring()
            } else {
                regResult = OneStep.registerBackgroundMonitoring()
            }
        }) {
            Text(if (collectionData.activated) "Stop bg monitoring" else "Start bg monitoring", color = Color(0xFF4678B4),)
        }
        Spacer(modifier = Modifier.height(8.dp))
        SafeSDKButton(
            modifier = Modifier.align(CenterHorizontally),
            icon = Icons.Default.Star,
            action = {
            OneStep.testBackgroundRecording()
            Toast.makeText(context, "Look at the notifications tray to see the foreground notification", Toast.LENGTH_SHORT).show()
        }) {
            Text("Test background recording", color = Color(0xFF4678B4),)
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
                onClick = { onShowRecords(AggregateType.DAILY_BG_RECORDS) }) {
                Text(text = "Daily", color = Color(0xFF4678B4))
            }
            Button(
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                border = BorderStroke(1.dp, Color(0xFF4678B4)),
                onClick = { onShowRecords(AggregateType.HOURLY_BG_RECORDS) }) {
                Text(text = "Hourly", color = Color(0xFF4678B4))
            }
            Button(
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                border = BorderStroke(1.dp, Color(0xFF4678B4)),
                onClick = { onShowRecords(AggregateType.ALL_RECORDS) }) {
                Text(text = "Raw", color = Color(0xFF4678B4))
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        ScreenTitle(text = "Syncing")
        HorizontalDivider(color = Color.Gray, modifier = Modifier.padding(vertical = 8.dp))
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Updated: ${collectionData.lastResultSync.toUiDate(SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault()))}")
        SafeSDKButton(
            modifier = Modifier.align(CenterHorizontally),
            icon = Icons.Default.Create,
            action = {
                Toast.makeText(context, "Syncing OneStep data", Toast.LENGTH_SHORT).show()
                // data is periodically sync according to WalksSyncScheduler.SyncConfigurations
                // you can trigger "sync now", forcing uploading all pending samples and pulling new analyzed results.
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

@Composable
private fun Long.toUiDate(
    dateFormat: SimpleDateFormat,
): String = if (this == 0L) "Unavailable" else dateFormat.format(Date(this))

