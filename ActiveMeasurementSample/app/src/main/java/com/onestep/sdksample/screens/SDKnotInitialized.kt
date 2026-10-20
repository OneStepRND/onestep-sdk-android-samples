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


/**
 * @param sessionLostMessage shown under the button when the SDK reported a lost session.
 */
@Composable
fun SDKnotInitialized(
    sessionLostMessage: String?,
    onInitialized: () -> Unit = {},
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        Text(
            text = "OneStep SDK is not identified",
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.CenterHorizontally),
            fontSize = 36.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Button(
            modifier = Modifier.padding(16.dp),
            onClick = onInitialized
        ) {
            Text("Identify user")
        }

        if (sessionLostMessage != null) {
            Text(
                text = sessionLostMessage,
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.CenterHorizontally),
                fontSize = 36.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }

    }
}

@Preview(showBackground = true)
@Composable
private fun SDKnotInitializedPreview() {
    SDKnotInitialized(sessionLostMessage = "Session lost: sample error")
}
