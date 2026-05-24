package com.onestep.backgroundmonitoringsample

import android.app.Application
import android.util.Log
import co.onestep.android.core.OneStep
import co.onestep.android.core.getOr
import co.onestep.android.core.monitoring.getMonitoring
import co.onestep.android.core.onError
import co.onestep.android.core.onSuccess
import com.onestep.backgroundmonitoringsample.analytics.EventsCollector
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
        // Step 2: Identify user
        oneStep.setPatient(
            apiKey = "<YOUR-CLIENT-TOKEN-HERE>",
            customerPatientId = "<A-UUID-FOR-CURRENT-USER-HERE>",
            identityVerification = null, // <YOUR-IDENTITY-VERIFICATION-SECRET-HERE>
        ).onSuccess {
            Log.d(TAG, "SDK identified successfully")
            val monitoring = oneStep.getMonitoring().getOr(null) ?: return@onSuccess
            // Step 3: Initialize monitoring
            monitoring.enable()
            // Step 4: Set notification
            monitoring.setCustomMonitoringNotification {
                default(
                    icon = R.drawable.ic_launcher_foreground,
                    title = { "This is the Sample App monitoring" },
                    text = null,
                )
            }
        }.onError { error ->
            Log.e(TAG, "SDK setPatient failed: ${error.cause.message}")
        }
    }
}
