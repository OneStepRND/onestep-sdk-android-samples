package com.onestep.backgroundmonitoringsample.viewmodels

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import co.onestep.android.core.OSTIdentificationState
import co.onestep.android.core.OneStep
import co.onestep.android.core.getOr
import co.onestep.android.core.monitoring.OSTMonitoringRuntimeState
import co.onestep.android.core.monitoring.OSTSortOrder
import co.onestep.android.core.monitoring.getMonitoring
import co.onestep.android.core.onSuccess
import com.onestep.backgroundmonitoringsample.notifications.MonitoringNotifications
import com.onestep.backgroundmonitoringsample.ui.model.ActivityItem
import com.onestep.backgroundmonitoringsample.ui.model.MonitoringUiState
import com.onestep.backgroundmonitoringsample.ui.model.NotificationStyle
import com.onestep.backgroundmonitoringsample.ui.model.ScreenState
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class MainViewModel(app: Application) : AndroidViewModel(app) {

    // The SDK is initialised in BgMonitoringSampleApplication.onCreate(), so by the
    // time this ViewModel is constructed the process-singleton handle is available.
    private val oneStep: OneStep = OneStep.getInstance().getOr(null)
        ?: error("OneStep SDK not initialized")

    var screenState by mutableStateOf<ScreenState>(ScreenState.Loading)

    private val _monitoringUiState = MutableStateFlow(MonitoringUiState())
    val monitoringUiState: StateFlow<MonitoringUiState> = _monitoringUiState

    // The selected notification style is host-app state (not reported by the SDK), so it is kept
    // separate from the SDK-driven monitoringUiState. It is seeded from the persisted choice so
    // the picker reflects what is actually applied after an app restart.
    private val _notificationStyle =
        MutableStateFlow(MonitoringNotifications.savedStyle(getApplication()))
    val notificationStyle: StateFlow<NotificationStyle> = _notificationStyle

    // Daily step summaries shown on the Records screen. Loaded through the ViewModel (rather than
    // the screen reaching into the SDK directly) so all SDK access stays in one place.
    private val _records = MutableStateFlow<List<ActivityItem>>(emptyList())
    val records: StateFlow<List<ActivityItem>> = _records

    fun setState(state: ScreenState) {
        screenState = state
    }

    /**
     * Loads daily summaries via [co.onestep.android.core.monitoring.OSTMonitoring.getDailySummaries].
     * The query is a DSL: set [from]/[to] (java.time.LocalDate), [order], and [maxDays] to scope it.
     * Here we ask for the last 30 days, newest first. Call getDailySummaries() with no block for the
     * SDK defaults.
     * Docs: https://glorious-caboc-cd3.notion.site/onestep-collect-for-android
     */
    fun loadRecords() {
        viewModelScope.launch {
            val monitoring = oneStep.getMonitoring().getOr(null) ?: return@launch
            val summaries = monitoring.getDailySummaries {
                from = LocalDate.now().minusDays(30)
                to = LocalDate.now()
                order = OSTSortOrder.DESC
            }.getOr(emptyList())
            _records.value = summaries
                .map(ActivityItem::fromDailySummary)
                .sortedByDescending { it.startTime }
        }
    }

    /**
     * Switches the foreground-service notification style at runtime. setCustomMonitoringNotification
     * only persists the config, so when monitoring is active we ask the running service to rebuild
     * the live notification. The choice is persisted (in [MonitoringNotifications.apply]) and the UI
     * reflects it once the SDK confirms it accepted the config.
     */
    fun setNotificationStyle(style: NotificationStyle) {
        viewModelScope.launch {
            val monitoring = oneStep.getMonitoring().getOr(null) ?: return@launch
            MonitoringNotifications.apply(monitoring, getApplication(), style)
                .onSuccess {
                    _notificationStyle.value = style
                    if (monitoring.state.value is OSTMonitoringRuntimeState.Active) {
                        MonitoringNotifications.refreshLiveNotification(getApplication())
                    }
                }
        }
    }

    fun start() {
        viewModelScope.launch {
            oneStep.identificationState.collect { state ->
                when (state) {
                    is OSTIdentificationState.Identified -> {
                        collectMonitoringState()
                        screenState = ScreenState.Initialized
                    }
                    is OSTIdentificationState.Lost ->
                        screenState = ScreenState.Error("SDK session lost: ${state.cause.message}")
                    OSTIdentificationState.Unidentified ->
                        screenState = ScreenState.Loading
                }
            }
        }
    }

    fun optInToMonitoring() {
        viewModelScope.launch {
            if (oneStep.identificationState.value is OSTIdentificationState.Identified) {
                oneStep.getMonitoring().getOr(null)?.optIn()
            }
        }
    }

    fun optOutOfMonitoring() {
        viewModelScope.launch {
            if (oneStep.identificationState.value is OSTIdentificationState.Identified) {
                oneStep.getMonitoring().getOr(null)?.optOut()
            }
        }
    }

    private fun collectMonitoringState() {
        viewModelScope.launch {
            val monitoring = oneStep.getMonitoring().getOr(null) ?: return@launch
            combine(
                monitoring.state,
                monitoring.preference,
            ) { runtimeState, preference ->
                MonitoringUiState(
                    preference = preference,
                    runtimeState = runtimeState,
                )
            }.collect {
                _monitoringUiState.value = it
            }
        }
    }
}
