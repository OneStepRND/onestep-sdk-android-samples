package com.onestep.backgroundmonitoringsample

import android.app.Application
import android.util.Log
import co.onestep.android.core.OSTIdentifyResult
import co.onestep.android.core.OneStep
import co.onestep.android.core.monitoring.OSTMonitoringConfig
import co.onestep.android.core.monitoring.models.OSTDefaultNotificationConfig
import com.onestep.backgroundmonitoringsample.analytics.EventsCollector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class BgMonitoringSampleApplication : Application() {

    private val TAG = BgMonitoringSampleApplication::class.simpleName
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()
        initializeSdk()
    }

    private fun initializeSdk() {
        // Step 1: Initialize the SDK
        OneStep.initialize(
            application = this,
            clientToken = "<YOUR-CLIENT-TOKEN-HERE>",
        )

        // Step 2: Identify user (suspend function)
        applicationScope.launch {
            val result = OneStep.identify(
                userId = "<A-UUID-FOR CURRENT-USER-HERE>",
                identityVerification = null, //<YOUR-IDENTITY-VERIFICATION-SECRET-HERE>ת
            )
            when (result) {
                is OSTIdentifyResult.Success -> {
                    Log.d(TAG, "SDK identified successfully")
                    // Step 3: Initialize monitoring
                    OneStep.monitoring.initialize(OSTMonitoringConfig())
                    // Step 4: Set notification
                    OneStep.monitoring.setCustomMonitoringNotification(
                        OSTDefaultNotificationConfig(
                            title = { "This is the Sample App monitoring" },
                            text = null,
                            icon = R.drawable.ic_launcher_foreground,
                        )
                    )
                }
                is OSTIdentifyResult.Failure -> {
                    Log.e(TAG, "SDK identify failed: ${result.error} - ${result.message}")
                }
            }
        }

        // Collect SDK events
        EventsCollector.startCollecting(applicationScope)
    }
}
