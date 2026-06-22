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
import kotlinx.coroutines.launch

class BgMonitoringSampleApplication : Application() {

    private val TAG = BgMonitoringSampleApplication::class.simpleName
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    lateinit var oneStepSdk: OneStep
        private set

    override fun onCreate() {
        super.onCreate()

        // Step 1: Initialize the SDK
        OneStep.initialize(
            application = this,
            onAuthLost = { error ->
                Log.w(TAG, "Auth lost: ${error.message}")
            },
        ).onSuccess { oneStep ->
            oneStepSdk = oneStep
            // Collect SDK events
            EventsCollector.startCollecting(oneStep, applicationScope)
            applicationScope.launch { identifyAndStartMonitoring(oneStep) }
        }.onError { error ->
            Log.e(TAG, "SDK initialize failed: ${error.cause.message}")
        }
    }

    private suspend fun identifyAndStartMonitoring(oneStep: OneStep) {
        // Step 2: Identify user. Credentials come from local.properties via BuildConfig, so no
        // keys live in source. identityVerification is the precomputed HMAC digest (or blank);
        // pass null when blank to connect without identity verification.
        oneStep.setPatient(
            apiKey = BuildConfig.CLIENT_TOKEN,
            customerPatientId = BuildConfig.CUSTOMER_PATIENT_ID,
            identityVerification = BuildConfig.IDENTITY_VERIFICATION.ifBlank { null },
        ).onSuccess {
            Log.d(TAG, "SDK identified successfully")
            val monitoring = oneStep.getMonitoring().getOr(null) ?: return@onSuccess
            // Step 3: Enable monitoring. enable() wires up the monitoring subsystem for this
            // session; it does NOT start collecting on its own. The user must then OPT IN
            // (monitoring.optIn(), called from MainViewModel after permissions are granted) to
            // actually begin background collection, and can optOut() to stop. Think of it as:
            // enable = "make the feature available", optIn/optOut = "the user's consent toggle".
            // Docs: https://glorious-caboc-cd3.notion.site/onestep-collect-for-android
            monitoring.enable()
            // Step 4: Set the foreground-service notification. We re-apply the user's last choice
            // (default on first run) so the live notification matches the in-app picker after an
            // app restart. All three styles are built in MonitoringNotifications so the startup and
            // in-app paths stay in sync.
            MonitoringNotifications.apply(monitoring, this, MonitoringNotifications.savedStyle(this))
        }.onError { error ->
            Log.e(TAG, "SDK setPatient failed: ${error.cause.message}")
        }
    }
}
