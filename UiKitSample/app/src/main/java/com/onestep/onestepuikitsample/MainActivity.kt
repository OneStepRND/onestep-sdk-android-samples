package com.onestep.onestepuikitsample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import co.onestep.android.core.external.models.InitResult
import co.onestep.android.core.external.models.SdkConfiguration
import co.onestep.android.uikit.features.inaapWalkFlow.RecordWalkFlowActivity
import co.onestep.android.uikit.features.inaapWalkFlow.configurations.WalkRecordConfiguration
import com.onestep.onestepuikitsample.screens.MainScreen

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainScreen(
                viewModel,
                connect = { connect() },
                onsStartUikit = {
                    startActivity(
                        RecordWalkFlowActivity.buildIntent(
                            this,
                            WalkRecordConfiguration.default(),
                        ),
                    )
                }
            )
        }
    }


    private fun connect() {
        viewModel.isConnecting = true
        (application as UiKitSampleApplication).connect { result ->
            viewModel.sdkInitialized = when (result) {
                is InitResult.Success -> {
                    viewModel.isConnecting = false
                    result
                }

                is InitResult.Error -> {
                    viewModel.isConnecting = false
                    result
                }
            }
        }
    }
}