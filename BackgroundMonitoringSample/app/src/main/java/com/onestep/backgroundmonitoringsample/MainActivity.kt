package com.onestep.backgroundmonitoringsample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.onestep.backgroundmonitoringsample.screens.MISSING_CREDENTIALS_MESSAGE
import com.onestep.backgroundmonitoringsample.screens.MainScreen
import com.onestep.backgroundmonitoringsample.screens.SDK_INIT_FAILED_MESSAGE
import com.onestep.backgroundmonitoringsample.screens.SetupError
import com.onestep.backgroundmonitoringsample.viewmodels.MainViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // targetSdk 35+ draws edge to edge; safeDrawingPadding keeps content clear of system bars.
        enableEdgeToEdge()
        val app = application as BgMonitoringSampleApplication
        // Fail fast: without an initialized SDK or credentials, nothing below can work.
        // The ViewModel needs the SDK, so it is not created on this path.
        if (app.oneStepSdk == null || app.missingCredentials.isNotEmpty()) {
            val message =
                if (app.oneStepSdk == null) SDK_INIT_FAILED_MESSAGE else MISSING_CREDENTIALS_MESSAGE
            setContent {
                Box(Modifier.safeDrawingPadding()) { SetupError(message = message) }
            }
            return
        }
        viewModel.start()
        setContent {
            val monitoringState by viewModel.monitoringUiState.collectAsStateWithLifecycle()
            val notificationStyle by viewModel.notificationStyle.collectAsStateWithLifecycle()
            val notificationDismissible by viewModel.notificationDismissible.collectAsStateWithLifecycle()
            val records by viewModel.records.collectAsStateWithLifecycle()
            Box(Modifier.safeDrawingPadding()) {
                MainScreen(
                    screenState = viewModel.screenState,
                    monitoringState = monitoringState,
                    notificationStyle = notificationStyle,
                    notificationDismissible = notificationDismissible,
                    records = records,
                    onScreenStateChange = viewModel::setState,
                    onOptIn = viewModel::optInToMonitoring,
                    onOptOut = viewModel::optOutOfMonitoring,
                    onSelectNotificationStyle = viewModel::setNotificationStyle,
                    onNotificationDismissibleChange = viewModel::setNotificationDismissible,
                    onLoadRecords = viewModel::loadRecords,
                    isIdentified = viewModel.isIdentified,
                    onSync = viewModel::sync,
                )
            }
        }
    }
}
