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
import co.onestep.android.core.monitoring.OSTSortOrder
import co.onestep.android.core.monitoring.getMonitoring
import co.onestep.android.core.onSuccess
import com.onestep.backgroundmonitoringsample.BgMonitoringSampleApplication
import com.onestep.backgroundmonitoringsample.notifications.MonitoringNotifications
import com.onestep.backgroundmonitoringsample.ui.model.ActivityItem
import com.onestep.backgroundmonitoringsample.ui.model.MonitoringUiState
import com.onestep.backgroundmonitoringsample.ui.model.NotificationStyle
import com.onestep.backgroundmonitoringsample.ui.model.ScreenState
import java.time.LocalDate
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class MainViewModel(app: Application) : AndroidViewModel(app) {

    // The SDK is initialised in BgMonitoringSampleApplication.onCreate(). MainActivity creates
    // this ViewModel only after that succeeded, so the process-singleton handle is available.
    private val oneStep: OneStep = OneStep.getInstance().getOr(null)
        ?: error("OneStep SDK not initialized")

    var screenState by mutableStateOf<ScreenState>(ScreenState.Loading)

    // Mirrors OneStep.identificationState, so composables do not read the SDK themselves.
    var isIdentified by mutableStateOf(false)
        private set

    private val _monitoringUiState = MutableStateFlow(MonitoringUiState())
    val monitoringUiState: StateFlow<MonitoringUiState> = _monitoringUiState

    // The selected notification style is host-app state (not reported by the SDK), so it is kept
    // separate from the SDK-driven monitoringUiState. It is seeded from the persisted choice so
    // the picker reflects what is actually applied after an app restart.
    private val _notificationStyle =
        MutableStateFlow(MonitoringNotifications.savedStyle(getApplication()))
    val notificationStyle: StateFlow<NotificationStyle> = _notificationStyle

    // Whether the user may swipe the monitoring notification away. The SDK persists this setting.
    private val _notificationDismissible = MutableStateFlow(
        oneStep.getMonitoring().getOr(null)?.isMonitoringNotificationDismissible ?: false,
    )
    val notificationDismissible: StateFlow<Boolean> = _notificationDismissible

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
     * persists the config and repaints the live notification when monitoring is active. The choice
     * is also saved in [MonitoringNotifications.apply], and the UI reflects it once the SDK confirms
     * it accepted the config.
     */
    fun setNotificationStyle(style: NotificationStyle) {
        val monitoring = oneStep.getMonitoring().getOr(null) ?: return
        MonitoringNotifications.apply(monitoring, getApplication(), style)
            .onSuccess { _notificationStyle.value = style }
    }

    /**
     * Lets the user swipe the always-on notification away (SDK 2.2.0+). Monitoring is not affected:
     * the foreground service keeps running. Android shows the notification again when the service
     * restarts (reboot, app update, process restart).
     */
    fun setNotificationDismissible(dismissible: Boolean) {
        val monitoring = oneStep.getMonitoring().getOr(null) ?: return
        monitoring.setMonitoringNotificationDismissible(dismissible)
            .onSuccess { _notificationDismissible.value = dismissible }
    }

    private var started = false
    private var monitoringJob: Job? = null

    /** Starts observing the SDK. Safe to call on every Activity onCreate: it runs once. */
    fun start() {
        if (started) return
        started = true
        val connectionError = getApplication<BgMonitoringSampleApplication>().connectionError
        viewModelScope.launch {
            // Unidentified alone means "still connecting"; with a connection error it means the
            // automatic setPatient() failed, so show the error instead of loading forever.
            combine(oneStep.identificationState, connectionError) { state, error -> state to error }
                .distinctUntilChanged()
                .collect { (state, error) ->
                    isIdentified = state is OSTIdentificationState.Identified
                    when (state) {
                        is OSTIdentificationState.Identified -> {
                            collectMonitoringState()
                            screenState = ScreenState.Initialized
                        }
                        is OSTIdentificationState.Lost ->
                            screenState = ScreenState.Error("Session lost: ${state.cause.type.name}")
                        OSTIdentificationState.Unidentified ->
                            screenState = error?.let { ScreenState.Error(it) } ?: ScreenState.Loading
                    }
                }
        }
    }

    /** Uploads pending OneStep data now instead of waiting for the SDK's background sync. */
    fun sync() {
        viewModelScope.launch { oneStep.sync() }
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
        // Identified can be reached again after Lost; keep a single monitoring collector.
        monitoringJob?.cancel()
        monitoringJob = viewModelScope.launch {
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
