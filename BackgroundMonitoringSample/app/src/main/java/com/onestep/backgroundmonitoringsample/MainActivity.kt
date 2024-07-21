package com.onestep.backgroundmonitoringsample

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.onestep.android.core.external.OneStep
import co.onestep.android.core.external.models.BackgroundMonitoringStats
import co.onestep.android.core.external.models.InitResult
import com.onestep.backgroundmonitoringsample.screens.MainScreen
import com.onestep.backgroundmonitoringsample.ui.model.ScreenState
import com.onestep.backgroundmonitoringsample.viewmodels.MainViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainScreen(
                viewModel,
                connect = { connect() }
            )
        }
    }

    private fun connect() {
        viewModel.setState(ScreenState.Loading)
        (application as BgMonitoringSampleApplication).connect { result ->
            when (result) {
                is InitResult.Success -> {
                    viewModel.setState(ScreenState.Initialized)
                }

                is InitResult.Error -> {
                    viewModel.setState(ScreenState.NotInitialized(result.message))
                }
            }
        }
    }
}