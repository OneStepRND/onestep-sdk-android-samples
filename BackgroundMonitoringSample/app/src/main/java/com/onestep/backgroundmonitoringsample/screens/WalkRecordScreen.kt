package com.onestep.backgroundmonitoringsample.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import co.onestep.android.core.external.OneStep
import co.onestep.android.core.external.models.BackgroundMonitoringStats


@Composable
fun WalkRecordScreen(
    modifier: Modifier = Modifier,
) {
    var stats by remember { mutableStateOf<BackgroundMonitoringStats?>(null) }

    LaunchedEffect(Unit) {
        OneStep.monitoringStatsFlow().collect {
            stats = it
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp)
            .then(modifier)
    ) {

        Text(
            modifier = Modifier.padding(8.dp),
            text = "bgMonitoringActivated = ${stats?.bgMonitoringActivated}"
        )
    }
}
