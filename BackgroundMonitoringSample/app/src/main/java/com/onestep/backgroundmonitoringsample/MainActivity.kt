package com.onestep.backgroundmonitoringsample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.ui.Modifier
import com.onestep.backgroundmonitoringsample.screens.MainScreen
import com.onestep.backgroundmonitoringsample.viewmodels.MainViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // targetSdk 35+ draws edge to edge; safeDrawingPadding keeps content clear of system bars.
        enableEdgeToEdge()
        viewModel.start()
        setContent {
            Box(Modifier.safeDrawingPadding()) {
                MainScreen(viewModel)
            }
        }
    }
}
