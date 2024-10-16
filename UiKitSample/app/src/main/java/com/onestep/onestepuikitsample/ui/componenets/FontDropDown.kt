package com.onestep.onestepuikitsample.ui.componenets

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.List
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.onestep.android.uikit.OSTTheme
import com.onestep.onestepuikitsample.ui.theme.font.getName

@Composable
fun FontDropDown(
    font: MutableState<FontFamily>,
    expanded: MutableState<Boolean>,
    fontFamilies: List<FontFamily>,
    onFontChange: (FontFamily) -> Unit,
) {
    Column(
        modifier = Modifier
            .wrapContentWidth()

    ) {
        Button(
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = OSTTheme.colorScheme.primary,
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
                    imageVector = Icons.Default.List,
                    contentDescription = "Start Recording Flow",
                    tint = OSTTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = font.value.getName().orEmpty(),
                    fontWeight = FontWeight.Bold,
                    fontFamily = font.value
                )
            }
        }

        DropdownMenu(
            expanded = expanded.value,
            onDismissRequest = { expanded.value = false },
            modifier = Modifier.width(400.dp).padding(horizontal = 26.dp),
        ) {
            fontFamilies.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = option.getName() ?: "",
                            fontFamily = option,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal,
                        )
                    },
                    onClick = {
                        expanded.value = false
                        onFontChange(option)
                    },
                )
            }
        }
    }
}
