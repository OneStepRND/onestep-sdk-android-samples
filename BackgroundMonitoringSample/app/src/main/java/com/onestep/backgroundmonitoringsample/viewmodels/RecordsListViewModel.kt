package com.onestep.backgroundmonitoringsample.viewmodels

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.onestep.android.core.OneStep
import com.onestep.backgroundmonitoringsample.ui.model.ActivityItem
import com.onestep.backgroundmonitoringsample.ui.model.ScreenType
import kotlinx.coroutines.launch

class RecordsListViewModel : ViewModel() {
    var activityItems = mutableStateListOf<ActivityItem>()
        private set

    private var listType = mutableStateOf(ScreenType.WALKING_BOUTS)

    var title = mutableStateOf("Care Log")
        private set

    private fun queryWalkingBouts() {
        viewModelScope.launch {
            val bouts = OneStep.monitoring.stepBouts.getOSTWalkingBouts(from = null, to = null)
                .map { ActivityItem.fromWalkingBout(it) }
            activityItems.clear()
            activityItems.addAll(bouts.sortedByDescending { it.startTime })
            title.value = "Walking Bouts"
            listType.value = ScreenType.WALKING_BOUTS
        }
    }

    private fun queryDailySummaries() {
        viewModelScope.launch {
            val summaries = OneStep.monitoring.getDailySummaries()
                .map { ActivityItem.fromDailySummary(it) }
            activityItems.clear()
            activityItems.addAll(summaries.sortedByDescending { it.startTime })
            title.value = "Daily Step Summaries"
            listType.value = ScreenType.DAILY_SUMMARIES
        }
    }

    fun refreshList(screenType: ScreenType) {
        when (screenType) {
            ScreenType.WALKING_BOUTS -> queryWalkingBouts()
            ScreenType.DAILY_SUMMARIES -> queryDailySummaries()
        }
    }
}
