package com.onestep.backgroundmonitoringsample

import android.app.Application
import android.util.Log
import co.onestep.android.core.external.OneStep
import co.onestep.android.core.external.models.sdkOut.NotificationConfig
import co.onestep.android.core.external.models.sdkOut.OSTInitResult
import co.onestep.android.core.internal.data.syncer.SyncConfigurations
import com.onestep.backgroundmonitoringsample.analytics.SampleAnalytics
import kotlinx.coroutines.flow.MutableSharedFlow

class BgMonitoringSampleApplication: Application() {

    private val TAG = BgMonitoringSampleApplication::class.simpleName

    val enableBackgroundMonitoring = true

    val sdkConnectionState = MutableSharedFlow<OSTInitResult>(1)

    override fun onCreate() {
        super.onCreate()
        connect {
           Log.d(TAG, "connection result $it")
           sdkConnectionState.tryEmit(it)
        }
    }

    private fun connect(
        onConnectionResult: (OSTInitResult) -> Unit
    ) {
        OneStep.Builder(
            this.applicationContext,
            apiKey = "<YOUR-API-KEY-HERE>",
            appId = "<YOUR-APP-ID-HERE>",
            distinctId = "<A-UUID-FOR CURRENT-USER-HERE>",
            identityVerification = null //<YOUR-IDENTITY-VERIFICATION-SECRET-HERE>, // or null if in development
        )
            // SDK will be used for background monitoring
            .setBackgroundMonitoringEnabled(enableBackgroundMonitoring)
            // syncConfigurations is an enum value that you can set to @Enhanced @Balanced or @Efficient
            .setSyncConfigurations(SyncConfigurations.Enhanced)
            // set the foreground notification configuration attributes for the background data collection
            .setBackgroundNotificationConfig(
                NotificationConfig(
                    title = "This is the Sample App monitoring",
                    icon = R.drawable.ic_launcher_foreground,
                ),
            )
            // implement the AnalyticsHandler interface to receive analytics events
            .setAnalyticsService(SampleAnalytics())
            // register to callback to get the initialization result
            .setInitializationCallback {
                onConnectionResult(it)
            }
            .build()
    }
}