package com.onestep.sdksample.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Preview(showBackground = true)
@Composable
fun NoActivityRecognitionPermission(
    onAskPermission: () -> Unit = {},
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        Text(
            text = "Activity Recognition Permission is not granted",
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.CenterHorizontally),
            fontSize = 36.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Button(
            modifier = Modifier.padding(16.dp),
            onClick = { onAskPermission() } ) {
            Text("Ask Permission")
        }
    }
}