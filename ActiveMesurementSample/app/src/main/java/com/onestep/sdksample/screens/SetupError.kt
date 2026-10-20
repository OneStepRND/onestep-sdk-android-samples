package com.onestep.sdksample.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

const val MISSING_CREDENTIALS_MESSAGE =
    "Add onestep.clientToken and onestep.customerPatientId to local.properties, then rebuild. " +
        "See README → Keys."

const val SDK_INIT_FAILED_MESSAGE =
    "OneStep SDK failed to initialize. See Logcat for the error."

/**
 * Shown instead of the sample when the build or the SDK is not set up, so the cause is clear
 * at launch rather than as a later, unrelated-looking failure.
 */
@Composable
fun SetupError(
    message: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = message,
            fontSize = 20.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.error,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SetupErrorPreview() {
    SetupError(message = MISSING_CREDENTIALS_MESSAGE)
}
