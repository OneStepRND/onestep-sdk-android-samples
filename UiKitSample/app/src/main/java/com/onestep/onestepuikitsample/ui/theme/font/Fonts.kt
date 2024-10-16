package com.onestep.onestepuikitsample.ui.theme.font

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.onestep.onestepuikitsample.R

val NoirFontFamily = FontFamily(
    Font(co.onestep.android.uikit.R.font.noir_no1, FontWeight.Normal),
    Font(co.onestep.android.uikit.R.font.noir_no1_bold, FontWeight.Bold),
    Font(co.onestep.android.uikit.R.font.noir_no1_demibold, FontWeight.W500),
)

val FunnyToysFontFamily = FontFamily(
    Font(R.font.funny_toys),
)

val ParadiseFontFamily = FontFamily(
    Font(R.font.paradise),
)

val VintageBrushFontFamily = FontFamily(
    Font(R.font.vintage_brush),
)

// Create a map to hold the font families and their names.
private val fontFamilyNames = mapOf(
    NoirFontFamily to "Noir",
    FunnyToysFontFamily to "Funny Toys",
    ParadiseFontFamily to "Paradise",
    VintageBrushFontFamily to "Vintage Brush"
)

// Extension function to get the name of the font family.
fun FontFamily.getName(): String? = fontFamilyNames[this]
