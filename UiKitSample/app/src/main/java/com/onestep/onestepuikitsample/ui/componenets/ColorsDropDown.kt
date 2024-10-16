package com.onestep.onestepuikitsample.ui.componenets

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp


@Composable
fun ColorsDropDown(
    color: Color,
    font: FontFamily,
    expanded: MutableState<Boolean>,
    colorOptions: List<Color>,
    onColorChange: (Color) -> Unit,
) {
    Column(
        modifier = Modifier
            .wrapContentWidth(),
    ) {
        Button(
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = color,
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
            onClick = { expanded.value = true }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                horizontalArrangement= Arrangement.Start
            ) {
                Icon(
                    imageVector = Icons.Default.Create,
                    contentDescription = "Start Recording Flow",
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Primary color", fontWeight = FontWeight.Bold, style = TextStyle(
                    fontFamily = font
                ))
            }
        }

        DropdownMenu(
            expanded = expanded.value,
            onDismissRequest = { expanded.value = false },
            modifier = Modifier.width(400.dp).padding(horizontal = 26.dp),
        ) {
            colorOptions.forEach { option ->
                DropdownMenuItem(
                    modifier = Modifier.background(option),
                    text = { },
                    onClick = {
                        expanded.value = false
                        onColorChange(option)
                        // Trigger theme change logic here based on 'option'
                    },
                )
            }
        }
    }
}