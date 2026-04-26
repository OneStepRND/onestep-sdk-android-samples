package com.onestep.backgroundmonitoringsample.ui.model

import co.onestep.android.core.OSTParamName
import co.onestep.android.core.common.utils.toDateString
import co.onestep.android.core.monitoring.OSTDailyBackgroundMeasurement
import java.util.UUID

data class ActivityItem(
    val uuid: String,
    val title: String,
    val startTime: String,
    val details: Map<String, String> = emptyMap(),
) {
    companion object {

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
