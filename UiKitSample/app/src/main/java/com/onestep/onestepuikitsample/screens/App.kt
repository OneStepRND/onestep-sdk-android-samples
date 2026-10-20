package com.onestep.onestepuikitsample.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import com.onestep.onestepuikitsample.ui.componenets.Loading

@Composable
fun App(
    modifier: Modifier,
    isConnecting: Boolean,
    isIdentified: Boolean,
    sessionLostMessage: String?,
    currentFont: FontFamily,
    onFontChange: (FontFamily) -> Unit,
    colorsDropdownExpanded: Boolean,
    onColorsDropdownExpandedChange: (Boolean) -> Unit,
    fontsDropdownExpanded: Boolean,
    onFontsDropdownExpandedChange: (Boolean) -> Unit,
    connect: () -> Unit,
    onStartRecording: (RecordingFlow) -> Unit,
    onStartPermissionsFlow: () -> Unit,
    onStartCareLogActivity: () -> Unit,
) {
    when {
        isConnecting -> Loading()
        // Unidentified or Lost
        !isIdentified -> SDKnotInitialized(sessionLostMessage) { connect() }
        else -> MainScreen(
            modifier,
            onStartRecording,
            onStartPermissionsFlow,
            onStartCareLogActivity,
            currentFont = currentFont,
            onFontChange = onFontChange,
            colorsDropdownExpanded = colorsDropdownExpanded,
            onColorsDropdownExpandedChange = onColorsDropdownExpandedChange,
            fontsDropdownExpanded = fontsDropdownExpanded,
            onFontsDropdownExpandedChange = onFontsDropdownExpandedChange,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AppNotIdentifiedPreview() {
    App(
        modifier = Modifier,
        isConnecting = false,
        isIdentified = false,
        sessionLostMessage = null,
        currentFont = FontFamily.Default,
        onFontChange = {},
        colorsDropdownExpanded = false,
        onColorsDropdownExpandedChange = {},
        fontsDropdownExpanded = false,
        onFontsDropdownExpandedChange = {},
        connect = {},
        onStartRecording = {},
        onStartPermissionsFlow = {},
        onStartCareLogActivity = {},
    )
}
