package com.onestep.backgroundmonitoringsample.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.onestep.android.core.OneStep
import co.onestep.android.core.getOr
import co.onestep.android.core.monitoring.OSTMonitoringPreference
import co.onestep.android.core.monitoring.OSTMonitoringRuntimeState
import com.onestep.backgroundmonitoringsample.components.SafeSDKButton
import com.onestep.backgroundmonitoringsample.ui.model.MonitoringUiState
import com.onestep.backgroundmonitoringsample.ui.model.NotificationStyle
import kotlinx.coroutines.launch

@Composable
fun MonitoringScreen(
    monitoringState: MonitoringUiState,
    notificationStyle: NotificationStyle,
    onOptIn: () -> Unit,
    onOptOut: () -> Unit,
    onSelectNotificationStyle: (NotificationStyle) -> Unit,
    onShowRecords: () -> Unit,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val isActive = monitoringState.runtimeState is OSTMonitoringRuntimeState.Active

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
                isActive -> "Background Monitoring On"
                monitoringState.runtimeState is OSTMonitoringRuntimeState.Blocked -> {
                    val blockers = monitoringState.runtimeState.reasons
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
            action = { if (isActive) onOptOut() else onOptIn() },
        ) {
            Text(
                if (isActive) "Stop bg monitoring" else "Start bg monitoring",
                color = Color(0xFF4678B4),
            )
        }
        Spacer(modifier = Modifier.height(32.dp))
        HorizontalDivider(color = Color.Gray, modifier = Modifier.padding(vertical = 8.dp))
        Spacer(modifier = Modifier.height(16.dp))
        NotificationStyleSection(
            selected = notificationStyle,
            onSelect = onSelectNotificationStyle,
        )
        Spacer(modifier = Modifier.height(32.dp))
        HorizontalDivider(color = Color.Gray, modifier = Modifier.padding(vertical = 8.dp))
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            modifier = Modifier.align(CenterHorizontally),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            border = BorderStroke(1.dp, Color(0xFF4678B4)),
            onClick = { onShowRecords() },
        ) {
            Text(text = "View Daily Summaries", color = Color(0xFF4678B4))
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
                val oneStep = OneStep.getInstance().getOr(null) ?: return@SafeSDKButton
                coroutineScope.launch { oneStep.sync() }
            },
        ) {
            Text(
                fontSize = 20.sp,
                color = Color(0xFF4678B4),
                text = "Sync Now",
            )
        }
    }
}

@Composable
private fun ScreenTitle(
    modifier: Modifier = Modifier,
    text: String,
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
                .then(modifier),
        )
    }
}

/**
 * Lets the user switch the foreground-service notification style at runtime. Selecting an option
 * calls back into the SDK via the ViewModel; if monitoring is active the live notification updates
 * in place. Demonstrates the three OSTNotificationConfigScope builders (default / custom / native).
 */
@Composable
private fun NotificationStyleSection(
    selected: NotificationStyle,
    onSelect: (NotificationStyle) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        ScreenTitle(text = "Notification Style")
        Text(
            text = "How the ongoing background-monitoring notification is rendered.",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
        )
        Spacer(modifier = Modifier.height(8.dp))
        NotificationStyle.entries.forEach { style ->
            NotificationStyleOption(
                style = style,
                isSelected = style == selected,
                onSelect = { onSelect(style) },
            )
        }
    }
}

@Composable
private fun NotificationStyleOption(
    style: NotificationStyle,
    isSelected: Boolean,
    onSelect: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = isSelected, onClick = onSelect)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = isSelected, onClick = onSelect)
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = style.label,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = style.description,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MonitoringScreenPreview() {
    MonitoringScreen(
        monitoringState = MonitoringUiState(),
        notificationStyle = NotificationStyle.DEFAULT,
        onOptIn = {},
        onOptOut = {},
        onSelectNotificationStyle = {},
        onShowRecords = {},
    )
}

@Preview(showBackground = true)
@Composable
private fun NotificationStyleSectionPreview() {
    NotificationStyleSection(
        selected = NotificationStyle.CUSTOM,
        onSelect = {},
    )
}
