package com.onestep.backgroundmonitoringsample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.onestep.backgroundmonitoringsample.screens.MainScreen
import com.onestep.backgroundmonitoringsample.viewmodels.MainViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.start()
        setContent {
            MainScreen(viewModel)
        }
    }
}
