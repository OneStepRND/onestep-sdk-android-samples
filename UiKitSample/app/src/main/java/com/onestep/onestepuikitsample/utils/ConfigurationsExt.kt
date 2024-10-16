package com.onestep.onestepuikitsample.utils

import co.onestep.android.uikit.features.recordFlow.configurations.OSTPrepareDuration
import co.onestep.android.uikit.features.recordFlow.configurations.OSTRecordingConfiguration
import co.onestep.android.uikit.features.recordFlow.screens.instructions.OSTMeasurementInstructionsData

fun OSTRecordingConfiguration.sixMinutesTest() = OSTRecordingConfiguration(
    instructions = OSTMeasurementInstructionsData(
        activityDisplayName = "6 Minutes Walk Test",
        instructions = listOf("Walk 6 minutes", "Take a break", "shower"),
        hints = listOf("If you are tired it's ok to take a break"),
        gifUrl = "https://media.giphy.com/media/3orif3FIX9vEfCWOJy/giphy.gif",
    ),
    duration = 360,
    isCountingDown = false,
    prepareScreenDuration = OSTPrepareDuration.FIVE_SECONDS,
    playVoiceOver = false,
    showPhonePositionScreen = false,
)
