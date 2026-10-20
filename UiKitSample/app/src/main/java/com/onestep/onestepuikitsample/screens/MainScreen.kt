package com.onestep.onestepuikitsample.screens

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.onestep.android.uikit.OSTTheme
import co.onestep.android.uikit.ui.theme.OSTThemeManager
import co.onestep.android.uikit.ui.theme.bad
import co.onestep.android.uikit.ui.theme.goodBackground
import co.onestep.android.uikit.ui.theme.gray300
import co.onestep.android.uikit.ui.theme.med
import com.onestep.onestepuikitsample.R
import com.onestep.onestepuikitsample.ui.componenets.ColorsDropDown
import com.onestep.onestepuikitsample.ui.componenets.FontDropDown
import com.onestep.onestepuikitsample.ui.theme.font.FunnyToysFontFamily
import com.onestep.onestepuikitsample.ui.theme.font.NoirFontFamily
import com.onestep.onestepuikitsample.ui.theme.font.ParadiseFontFamily
import com.onestep.onestepuikitsample.ui.theme.font.VintageBrushFontFamily

@Composable
fun MainScreen(
    modifier: Modifier,
    onStartRecording: (RecordingFlow) -> Unit,
    onStartPermissionsFlow: () -> Unit,
    onStartCareLogActivity: () -> Unit,
    currentFont: FontFamily,
    onFontChange: (FontFamily) -> Unit,
    colorsDropdownExpanded: Boolean,
    onColorsDropdownExpandedChange: (Boolean) -> Unit,
    fontsDropdownExpanded: Boolean,
    onFontsDropdownExpandedChange: (Boolean) -> Unit,
) {
    val primary = OSTTheme.colorScheme.collectAsStateWithLifecycle().value.primary

    Column(modifier.padding(32.dp).verticalScroll(rememberScrollState())) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = "UIKit Examples",
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Start,
            fontFamily = currentFont,
        )

        RecordingFlow.entries.forEach { flow ->
            MainButton(flow.label, flow.icon, primary) { onStartRecording(flow) }
        }

        MainButton(
            "Carelog",
            co.onestep.android.uikit.R.drawable.ic_clock,
            primary
        ) {
            onStartCareLogActivity()
        }

        MainButton(
            "Permissions flow",
            R.drawable.ic_route,
            primary
        ) {
            onStartPermissionsFlow()
        }

        Spacer(modifier = Modifier.height(36.dp))
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = "Theming",
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Start,
            fontFamily = currentFont,
        )

        Spacer(modifier = Modifier.height(8.dp))
        ColorsDropDown(
            color = primary,
            font = currentFont,
            expanded = colorsDropdownExpanded,
            onExpandedChange = onColorsDropdownExpandedChange,
            listOf(goodBackground, med, bad, gray300),
        ) { selectedColor ->

            /// To change primary color of the theme, use the following code
            OSTThemeManager.updateColorScheme(
                OSTTheme.colorScheme.value.copy(
                    primary = selectedColor,
                )
                // YOU CAN ALSO CHANGE THE SECONDARY COLOR HERE
                // secondary = selectedColor,
            )
        }

        FontDropDown(
            currentFont,
            fontsDropdownExpanded,
            onFontsDropdownExpandedChange,
            // Observe the creation of those font families in the Font.kt file
            listOf(NoirFontFamily, FunnyToysFontFamily, ParadiseFontFamily, VintageBrushFontFamily),
        ) { selectedFont ->
            onFontChange(selectedFont)

            /// To change font of the theme, use the following code
            /// Create a Font.kt file with the desired font families
            OSTTheme.font = selectedFont
        }
    }
}

@Composable
private fun MainButton(
    text: String,
    @DrawableRes icon: Int,
    color: Color,
    action: () -> Unit
) {
    Button(
        modifier = Modifier.padding(8.dp).wrapContentHeight(),
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
                modifier = Modifier.size(40.dp),
                painter = painterResource(icon),
                contentDescription = text,
                tint = color,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                color = color,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MainScreenPreview() {
    MainScreen(
        modifier = Modifier,
        onStartRecording = {},
        onStartPermissionsFlow = {},
        onStartCareLogActivity = {},
        currentFont = NoirFontFamily,
        onFontChange = {},
        colorsDropdownExpanded = false,
        onColorsDropdownExpandedChange = {},
        fontsDropdownExpanded = false,
        onFontsDropdownExpandedChange = {},
    )
}

@Preview(showBackground = true)
@Composable
private fun MainButtonPreview() {
    MainButton("Walk recording", R.drawable.ic_walks, Color.Blue) {}
}
