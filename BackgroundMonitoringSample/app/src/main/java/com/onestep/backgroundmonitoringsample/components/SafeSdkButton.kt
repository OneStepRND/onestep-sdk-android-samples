package com.onestep.backgroundmonitoringsample.components

import android.widget.Toast
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import co.onestep.android.core.external.OneStep

@Composable
fun SafeSDKButton(
    modifier: Modifier = Modifier,
    action: () -> Unit,
    text:  @Composable () -> Unit
) {
    val context = LocalContext.current
    Button(
        modifier = modifier,
        onClick = {
            if (!OneStep.isInitialized()) {
                Toast.makeText(context, "SDK not initialized", Toast.LENGTH_SHORT).show()
            } else {
                action.invoke()
            }
        }) {
        text()
    }
}