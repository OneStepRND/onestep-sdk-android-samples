package com.onestep.sdksample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import co.onestep.android.core.external.models.InitResult
import com.onestep.sdksample.screens.MainScreen
import com.onestep.sdksample.viewmodels.MainViewModel

class MainActivity : ComponentActivity() {


    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainScreen(
                viewModel,
                connect = { connect(it) }
            )
        }
    }


    private fun connect(viewModel: MainViewModel) {
        (application as SDKSampleApplication).connect { result ->
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
