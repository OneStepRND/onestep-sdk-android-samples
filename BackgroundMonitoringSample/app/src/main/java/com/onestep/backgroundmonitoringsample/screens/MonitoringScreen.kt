package com.onestep.backgroundmonitoringsample.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Column
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
import com.onestep.backgroundmonitoringsample.components.CollectionDataValue
import com.onestep.backgroundmonitoringsample.components.PermissionTestButton
import com.onestep.backgroundmonitoringsample.components.SafeSDKButton
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

const val TAG = "SDKDevScreen"

@Composable
fun MonitoringScreen(
    collectionData: BackgroundMonitoringStats,
    onConnect: () -> Unit,
    refreshCollectionData: () -> Unit,
) {
    val context = LocalContext.current
    var connectionButtonText by remember { mutableStateOf(if (OneStep.isInitialized()) "DISCONNECT SDK" else "CONNECT SDK") }
    var recordingButtonText by remember { mutableStateOf(if (OneStep.isBackgroundMonitoringActive() ) "Stop bg recording" else "Start bg recording") }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
    ) {

        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(color = Color.Gray, modifier = Modifier.padding(vertical = 8.dp))
        ScreenTitle(Modifier.align(CenterHorizontally), "Background Monitoring")
        Spacer(modifier = Modifier.height(8.dp))
        PermissionTestButton()
        SafeSDKButton(action = { OneStep.testBackgroundRecording() }) {
            Text("Trigger a background recording")
        }
        SafeSDKButton(action = {
            if (!OneStep.hasActivityRecognitionPermission()) {
                Toast.makeText(context, "No permissions granted", Toast.LENGTH_SHORT).show()
                return@SafeSDKButton
            }
            recordingButtonText =
                if (!OneStep.isBackgroundMonitoringActive()) "Stop bg recording" else "Start bg recording"
            if (OneStep.isBackgroundMonitoringActive()) {
                OneStep.unregisterBackgroundMonitoring()
            } else {
                OneStep.registerBackgroundMonitoring()
            }
            refreshCollectionData()
        }) {
            Text(recordingButtonText)
        }
        CollectionDataValue("Background monitoring: ${if (collectionData.bgMonitoringActivated) "enabled" else "disabled"}")
        HorizontalDivider(color = Color.Gray, modifier = Modifier.padding(vertical = 8.dp))
        ScreenTitle(Modifier.align(CenterHorizontally), text = "Data & Sync")
        Spacer(modifier = Modifier.height(8.dp))
        SafeSDKButton(
            action = {
                Toast.makeText(context, "Syncing OneStep data", Toast.LENGTH_SHORT).show()
                OneStep.sync()
                refreshCollectionData()
            }) {
            Text("Sync all recorded samples")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = {
            if (OneStep.isInitialized()) {
                OneStep.disconnect()
                connectionButtonText = "CONNECT SDK"
            } else {
               onConnect()
                connectionButtonText = "DISCONNECT SDK"
            }
            refreshCollectionData()
        }) {
            Text(connectionButtonText)
        }
        HorizontalDivider(color = Color.Gray, modifier = Modifier.padding(vertical = 8.dp))
        ScreenTitle(text = "Monitoring stats")
        Spacer(modifier = Modifier.height(8.dp))
        CollectionDataWidget(collectionData = collectionData)
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun ScreenTitle(
    modifier: Modifier = Modifier,
    text: String
) {
    Text(
        text = text,
        textAlign = TextAlign.Start,
        fontSize = 24.sp,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier
            .fillMaxWidth()
            .then(modifier)
    )
}

@Composable
fun CollectionDataWidget(
    modifier: Modifier = Modifier,
    collectionData: BackgroundMonitoringStats,
) {
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault()) }

    Column(modifier = modifier) {
        CollectionDataValue("Background permissions: ${if (collectionData.bgPermissions) "granted" else "missing"}")
        CollectionDataValue("Last BG sample recorded: ${collectionData.lastBgSampleCollected.toUiDate(dateFormat)}")
        CollectionDataValue("Data sync: ${if (collectionData.pendingData) "pending data on device" else "up-to-date"}")
        CollectionDataValue("Last BG upload sync: ${collectionData.lastBgUploadSync.toUiDate(dateFormat)}")
        CollectionDataValue("Last BG result sync: ${collectionData.lastBgResultSync.toUiDate(dateFormat)}")
    }
}

@Composable
private fun Long.toUiDate(
    dateFormat: SimpleDateFormat,
): String = if (this == 0L) "Unavailable" else dateFormat.format(Date(this))

