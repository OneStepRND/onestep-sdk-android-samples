package com.onestep.backgroundmonitoringsample

import android.app.Application
import android.util.Log
import co.onestep.android.core.OneStep
import co.onestep.android.core.getOr
import co.onestep.android.core.monitoring.getMonitoring
import co.onestep.android.core.onError
import co.onestep.android.core.onSuccess
import com.onestep.backgroundmonitoringsample.analytics.EventsCollector
import com.onestep.backgroundmonitoringsample.notifications.MonitoringNotifications
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BgMonitoringSampleApplication : Application() {

    private val TAG = BgMonitoringSampleApplication::class.simpleName
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    // Null when OneStep.initialize() failed. MainActivity then shows an error instead of crashing.
    var oneStepSdk: OneStep? = null
        private set

    // local.properties keys left blank in this build. setPatient() cannot succeed without them,
    // so MainActivity shows a setup message instead of a late "session lost" error.
    val missingCredentials: List<String> = buildList {
        if (BuildConfig.CLIENT_TOKEN.isBlank()) add("onestep.clientToken")
        if (BuildConfig.CUSTOMER_PATIENT_ID.isBlank()) add("onestep.customerPatientId")
    }

    // Set when the automatic setPatient() below fails, so the UI can show why instead of loading
    // forever. Holds the error code only: a server message can carry details we do not show.
    private val _connectionError = MutableStateFlow<String?>(null)
    val connectionError: StateFlow<String?> = _connectionError.asStateFlow()

    override fun onCreate() {
        super.onCreate()

        // Step 1: Initialize the SDK
        OneStep.initialize(
            application = this,
            onAuthLost = { error ->
                Log.w(TAG, "Auth lost: ${error.type.name}")
            },
        ).onSuccess { oneStep ->
            oneStepSdk = oneStep
            // Collect SDK events
            EventsCollector.startCollecting(oneStep, applicationScope)
            // Set the foreground-service notification right after initialize(), before any network
            // call. The SDK can restart the monitoring service in a headless process (boot, app
            // update); onCreate() runs there too, so even a native(...) notification is restored.
            // We re-apply the user's last choice (default on first run) so the live notification
            // matches the in-app picker. All three styles are built in MonitoringNotifications.
            oneStep.getMonitoring().onSuccess { monitoring ->
                MonitoringNotifications.apply(monitoring, this, MonitoringNotifications.savedStyle(this))
            }
            applicationScope.launch { identifyAndStartMonitoring(oneStep) }
        }.onError { error ->
            Log.e(TAG, "SDK initialize failed: ${error.cause.type.name}")
        }
    }

    private suspend fun identifyAndStartMonitoring(oneStep: OneStep) {
        if (missingCredentials.isNotEmpty()) {
            // Log only the key names, never credential values.
            Log.e(TAG, "Missing in local.properties: ${missingCredentials.joinToString()}")
            return
        }
        // Step 2: Identify user. Credentials come from local.properties via BuildConfig, so no
        // keys live in source. identityVerification is the precomputed HMAC digest (or blank);
        // pass null when blank to connect without identity verification.
        oneStep.setPatient(
            apiKey = BuildConfig.CLIENT_TOKEN,
            customerPatientId = BuildConfig.CUSTOMER_PATIENT_ID,
            identityVerification = BuildConfig.IDENTITY_VERIFICATION.ifBlank { null },
        ).onSuccess {
            Log.d(TAG, "SDK identified successfully")
            _connectionError.value = null
            val monitoring = oneStep.getMonitoring().getOr(null) ?: return@onSuccess
            // Step 3: Enable monitoring. enable() wires up the monitoring subsystem for this
            // session; it does NOT start collecting on its own. The user must then OPT IN
            // (monitoring.optIn(), called from MainViewModel after permissions are granted) to
            // actually begin background collection, and can optOut() to stop. Think of it as:
            // enable = "make the feature available", optIn/optOut = "the user's consent toggle".
            // Docs: https://glorious-caboc-cd3.notion.site/onestep-collect-for-android
            monitoring.enable()
        }.onError { error ->
            // type.name is a stable code such as INVALID_CLIENT_TOKEN. Never print the type itself:
            // ServerError is a data class whose toString() includes the server's message.
            Log.e(TAG, "SDK setPatient failed: ${error.cause.type.name}")
            _connectionError.value = "Could not connect (${error.cause.type.name}). See README → Keys."
        }
    }
}
