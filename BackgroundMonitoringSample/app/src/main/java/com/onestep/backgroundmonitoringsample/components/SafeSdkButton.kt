package com.onestep.backgroundmonitoringsample.components

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import co.onestep.android.core.external.OneStep

@Composable
fun SafeSDKButton(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    action: () -> Unit,
    text:  @Composable () -> Unit
) {
    val context = LocalContext.current
    MainButton(
        modifier = modifier,
        text = { text() },
        icon = icon,
        action = {
            if (!OneStep.isInitialized()) {
                Toast.makeText(context, "SDK not initialized", Toast.LENGTH_SHORT).show()
            } else {
                action.invoke()
            }
        }
    )

}

@Composable
private fun MainButton(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    enabled: Boolean = true,
    text: @Composable () -> Unit,
    action: () -> Unit
) {
    Button(
        enabled = enabled,
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
        onClick = action
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF4678B4)
            )
            Spacer(modifier = Modifier.width(8.dp))
            text()
        }
    }
}
