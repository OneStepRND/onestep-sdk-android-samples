package com.onestep.backgroundmonitoringsample.viewmodels

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.onestep.android.core.external.OneStep
import co.onestep.android.core.external.models.AggregatedTimedDataRequest
import co.onestep.android.core.external.models.TimeRangeSlicer
import com.onestep.backgroundmonitoringsample.ui.model.ActivityItem
import com.onestep.backgroundmonitoringsample.ui.model.AggregateType
import kotlinx.coroutines.launch

class RecordsListViewModel: ViewModel() {
    var activityItems = mutableStateListOf<ActivityItem>()
        private set

    var listType = mutableStateOf<AggregateType>(AggregateType.ALL_RECORDS)
        private set

    var title = mutableStateOf("Care Log")
        private set

    private fun queryAllRecords() {
        viewModelScope.launch {
            val rawBgRecords = OneStep.rawBackgroundRecords().map { ActivityItem(it) }
            activityItems.clear()
            activityItems.addAll(rawBgRecords.sortedByDescending { it.startTime })
            title.value = "All Background records"
            listType.value = AggregateType.ALL_RECORDS
        }
    }

    private fun queryRecordsByDay() {
        viewModelScope.launch {
            // query records by calling OneStep.aggregateBackgroundRecords() with AggregatedTimedDataRequest
            val aggregatedBackgroundActivities = OneStep.aggregateBackgroundRecords(
                AggregatedTimedDataRequest(window = TimeRangeSlicer.DAY)
            )
                // Map to ui elements
                .map { ActivityItem(it) }
            activityItems.clear()
            activityItems.addAll(aggregatedBackgroundActivities.sortedByDescending { it.startTime })
            title.value = "Daily Background records"
            listType.value = AggregateType.DAILY_BG_RECORDS
        }
    }

    private fun queryRecordsByHour() {
        viewModelScope.launch {
            viewModelScope.launch {

                // query records by calling OneStep.aggregateBackgroundRecords() with AggregatedTimedDataRequest
                val aggregatedBackgroundActivities = OneStep.aggregateBackgroundRecords(
                        AggregatedTimedDataRequest(window = TimeRangeSlicer.HOUR)
                    )

                    // Map to ui elements
                    .map { ActivityItem(it) }
                activityItems.clear()
                activityItems.addAll(aggregatedBackgroundActivities.sortedByDescending { it.startTime })
                title.value = "Hourly Background records"
                listType.value = AggregateType.HOURLY_BG_RECORDS
            }
        }
    }

    fun refreshList(aggregateType: AggregateType) {
        when (aggregateType) {
            AggregateType.ALL_RECORDS -> queryAllRecords()
            AggregateType.DAILY_BG_RECORDS -> queryRecordsByDay()
            AggregateType.HOURLY_BG_RECORDS -> queryRecordsByHour()
        }
    }
}