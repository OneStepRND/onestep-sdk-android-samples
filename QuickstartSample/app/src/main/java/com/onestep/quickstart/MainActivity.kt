package com.onestep.quickstart

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory

class MainActivity : ComponentActivity() {

    private val viewModel: QuickstartViewModel by viewModels {
        viewModelFactory { initializer { QuickstartViewModel((application as QuickstartApplication).oneStep) } }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            val state by viewModel.state.collectAsStateWithLifecycle()
            // The SDK needs ACTIVITY_RECOGNITION to count steps, so ask right before recording.
            // Without it, start() fails with MissingPermission on Android 14+. On Android 10-13
            // the recording starts but counts no steps.
            val permissionLauncher = rememberLauncherForActivityResult(RequestPermission(), viewModel::recordWalk)
            MaterialTheme {
                QuickstartScreen(
                    state = state,
                    onConnect = viewModel::connect,
                    onRecord = {
                        // ACTIVITY_RECOGNITION is a runtime permission from Android 10 (API 29).
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            permissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
                        } else {
                            viewModel.recordWalk(permissionGranted = true)
                        }
                    },
                )
            }
        }
    }
}

@Composable
fun QuickstartScreen(
    state: QuickstartUiState,
    onConnect: () -> Unit,
    onRecord: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.safeDrawingPadding().padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            when (state) {
                is QuickstartUiState.Blocked -> Text(state.reason, color = MaterialTheme.colorScheme.error)
                is QuickstartUiState.Disconnected -> {
                    state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    Button(onClick = onConnect) { Text("Connect") }
                }
                is QuickstartUiState.Busy -> {
                    CircularProgressIndicator()
                    Text(state.label)
                }
                is QuickstartUiState.Connected -> {
                    state.message?.let { Text(it, style = MaterialTheme.typography.titleMedium) }
                    Button(onClick = onRecord) { Text("Record 30 s walk") }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BlockedPreview() = MaterialTheme {
    QuickstartScreen(QuickstartUiState.Blocked("Set onestep.clientToken in local.properties, then rebuild."), {}, {})
}

@Preview(showBackground = true)
@Composable
private fun DisconnectedPreview() = MaterialTheme {
    QuickstartScreen(QuickstartUiState.Disconnected(), {}, {})
}

@Preview(showBackground = true)
@Composable
private fun BusyPreview() = MaterialTheme {
    QuickstartScreen(QuickstartUiState.Busy("Recording a 30 s walk. Walk now."), {}, {})
}

@Preview(showBackground = true)
@Composable
private fun ResultPreview() = MaterialTheme {
    QuickstartScreen(QuickstartUiState.Connected("Walk score: 87.0\nSteps: 42"), {}, {})
}
