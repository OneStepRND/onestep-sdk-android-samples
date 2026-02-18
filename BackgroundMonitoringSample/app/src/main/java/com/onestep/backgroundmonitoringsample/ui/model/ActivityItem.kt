package com.onestep.backgroundmonitoringsample.ui.model

import co.onestep.android.core.common.models.OSTParamName
import co.onestep.android.core.common.utils.toDateString
import co.onestep.android.core.monitoring.OSTStepHourlySummary
import co.onestep.android.core.monitoring.OSTWalkingBout
import co.onestep.android.core.monitoring.models.OSTDailyBackgroundMeasurement
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.concurrent.TimeUnit

data class ActivityItem(
    val uuid: String,
    val title: String,
    val startTime: String,
    val details: Map<String, String> = emptyMap(),
) {
    companion object {
        private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

        fun fromWalkingBout(bout: OSTWalkingBout): ActivityItem {
            val startDate = Date(bout.startTimeMillis)
            val durationMinutes = TimeUnit.MILLISECONDS.toMinutes(bout.durationMillis)
            val durationSeconds = TimeUnit.MILLISECONDS.toSeconds(bout.durationMillis) % 60
            return ActivityItem(
                uuid = UUID.randomUUID().toString(),
                title = dateFormat.format(startDate),
                startTime = bout.startTimeMillis.toString(),
                details = mapOf(
                    "Steps" to bout.steps.toString(),
                    "Duration" to "${durationMinutes}m ${durationSeconds}s",
                    "Start" to dateFormat.format(startDate),
                ),
            )
        }

        fun fromDailySummary(summary: OSTDailyBackgroundMeasurement): ActivityItem {
            return ActivityItem(
                uuid = UUID.randomUUID().toString(),
                title = "${summary.dateLocal} - ${summary.parameters[OSTParamName.WALKING_STEPS]} steps",
                startTime = summary.timestamp.toDateString(),
                details = mapOf(
                    "Date" to summary.timestamp.toDateString(),
                    "Total Steps" to summary.parameters[OSTParamName.WALKING_STEPS].toString(),
                ),
            )
        }
    }
}
