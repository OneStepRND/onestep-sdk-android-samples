package com.onestep.onestepuikitsample.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.onestep.android.core.external.models.sdkOut.OSTInitResult
import com.onestep.onestepuikitsample.MainViewModel
import kotlinx.coroutines.launch


@Composable
fun SDKnotInitialized(
    viewModel: MainViewModel,
    onInitialized: suspend () -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        Text(
            text = "OneStep SDK is not initialized",
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.CenterHorizontally),
            fontSize = 36.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Button(
            modifier = Modifier.padding(16.dp),
            onClick = {
                scope.launch {
                    onInitialized()
                }
            }
        ) {
            Text("Initialize SDK")
        }

        if (viewModel.sdkInitialized is OSTInitResult.Error) {
            val error = (viewModel.sdkInitialized as OSTInitResult.Error).message
            Text(
                text = "OneStep SDK is not initialized: $error",
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.CenterHorizontally),
                fontSize = 36.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }

    }
}