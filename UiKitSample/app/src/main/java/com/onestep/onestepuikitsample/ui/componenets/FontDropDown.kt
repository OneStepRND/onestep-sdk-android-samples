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
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.onestep.android.uikit.OSTTheme
import com.onestep.onestepuikitsample.ui.theme.font.getName

@Composable
fun FontDropDown(
    font: FontFamily,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    fontFamilies: List<FontFamily>,
    onFontChange: (FontFamily) -> Unit,
) {
    val primary = OSTTheme.colorScheme.collectAsStateWithLifecycle().value.primary

    Column(
        modifier = Modifier
            .wrapContentWidth()

    ) {
        Button(
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = primary,
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
            onClick = { onExpandedChange(true) }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                horizontalArrangement= Arrangement.Start
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.List,
                    contentDescription = "Start Recording Flow",
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = font.getName().orEmpty(),
                    fontWeight = FontWeight.Bold,
                    fontFamily = font
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) },
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
                        onExpandedChange(false)
                        onFontChange(option)
                    },
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FontDropDownPreview() {
    FontDropDown(
        font = FontFamily.Default,
        expanded = false,
        onExpandedChange = {},
        fontFamilies = listOf(FontFamily.Default),
        onFontChange = {},
    )
}
