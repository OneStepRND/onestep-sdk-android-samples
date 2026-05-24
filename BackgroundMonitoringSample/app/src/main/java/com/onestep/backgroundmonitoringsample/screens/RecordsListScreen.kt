package com.onestep.backgroundmonitoringsample.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.onestep.android.core.OneStep
import co.onestep.android.core.getOr
import co.onestep.android.core.monitoring.getMonitoring
import com.onestep.backgroundmonitoringsample.ui.model.ActivityItem
import com.onestep.backgroundmonitoringsample.ui.model.ActivityItemUI

@Composable
fun RecordsListScreen() {
    var items by remember { mutableStateOf(emptyList<ActivityItem>()) }

    LaunchedEffect(Unit) {
        val oneStep = OneStep.getInstance().getOr(null) ?: return@LaunchedEffect
        val monitoring = oneStep.getMonitoring().getOr(null) ?: return@LaunchedEffect
        items = monitoring.getDailySummaries().getOr(emptyList())
            .map(ActivityItem::fromDailySummary)
            .sortedByDescending { it.startTime }
    }

    if (items.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "Your care log is empty")
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(8.dp),
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                Text(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    text = "Daily Step Summaries",
                )
            }
            items(items) { ActivityItemUI(activityItem = it) }
        }
    }
}
