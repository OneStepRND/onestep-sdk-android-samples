package com.onestep.backgroundmonitoringsample.ui.model

import co.onestep.android.core.external.models.OSTBackgroundRecord
import co.onestep.android.core.external.models.OSTParamName
import co.onestep.android.core.external.models.aggregate.OSTAggregatedBackgroundRecord
import co.onestep.android.core.internal.utils.toDateString
import co.onestep.android.core.internal.utils.toIsoString
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID
import java.util.concurrent.TimeUnit

data class ActivityItem(
    val uuid: String,
    val title: String,
    val startTime: String,
    val endTime: String,
    val aggregated: Boolean = false,
    val params: Map<OSTParamName, Float> = emptyMap(),
) {
    constructor(sample: OSTBackgroundRecord) : this(
        uuid = sample.timestamp.toString(),
        title = sample.timestamp.toDateString(dateFormat = "yyyy-MM-dd | HH:mm"), // Assuming the timestamp is current
        startTime = sample.timestamp.toDateString(),
        endTime = "Unavailable",
        params = sample.params,
    )

    constructor(aggregate: OSTAggregatedBackgroundRecord, roundToDays: Boolean? = null) : this(
        uuid = UUID.randomUUID().toString(),
        title = createTitle(aggregate.startTime, aggregate.endTime, roundToDays),
        startTime = aggregate.startTime.toDateString(),
        endTime = aggregate.endTime.toDateString(),
        params = aggregate.params,
    )
}

fun createTitle(startTime: Long, endTime: Long, roundToDays: Boolean? = null): String {
    val diffMinutes = TimeUnit.MILLISECONDS.toMinutes(endTime - startTime)

    // Timezone and date formatting
    val localTimeZone = TimeZone.getDefault()
    val dateFormat = SimpleDateFormat("yyyy-M-d", Locale.getDefault()).apply { timeZone = localTimeZone }
    val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).apply { timeZone = localTimeZone }
    val hourOnlyFormat = SimpleDateFormat("H", Locale.getDefault()).apply { timeZone = localTimeZone }

    return when {
        diffMinutes < 60 && roundToDays == null-> {
            // Round to the nearest whole hour for start and end times
            val startHour = hourOnlyFormat.format(Date(startTime)).toInt() * 100
            val endHour = (hourOnlyFormat.format(Date(endTime)).toInt() + 1) * 100
            "${Date(endTime).toIsoString()} | between ${startHour / 100}:00 - ${endHour / 100}:00 "
        }
        diffMinutes <= 1440 || roundToDays == true -> {
            // Day format for differences between 60 minutes and 24 hours
            dateFormat.format(Date(startTime))
        }
        else -> {
            // Month format for differences more than 24 hours
            monthFormat.format(Date(startTime))
        }
    }
}
