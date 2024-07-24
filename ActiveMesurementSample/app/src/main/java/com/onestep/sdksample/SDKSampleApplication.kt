package com.onestep.sdksample

import android.app.Application
import android.util.Log
import co.onestep.android.core.external.OneStep
import co.onestep.android.core.external.models.InitResult
import co.onestep.android.core.external.models.NotificationConfig
import co.onestep.android.core.external.models.SdkConfiguration
import co.onestep.android.core.internal.data.syncer.WalksSyncScheduler
import kotlinx.coroutines.flow.MutableSharedFlow

class SDKSampleApplication: Application() {

    private val TAG: String? = SDKSampleApplication::class.simpleName

    val sdkConnectionState = MutableSharedFlow<InitResult>(1)

    override fun onCreate() {
        super.onCreate()
        connect {
            Log.d(TAG, "connection result $it")
            sdkConnectionState.tryEmit(it)
        }
    }

    fun connect(
        onConnectionResult: (InitResult) -> Unit
    ) {
        OneStep.Builder(
            this.applicationContext,
            apiKey = "<YOUR-API-KEY-HERE>",
            appId = "<YOUR-APP-ID-HERE>",
            distinctId = "<A-UUID-FOR CURRENT-USER-HERE>",
            identityVerification = null //<YOUR-IDENTITY-VERIFICATION-SECRET-HERE>, // Activate this in production
        )
            .setConfiguration(
                SdkConfiguration(
                    // syncConfigurations is an enum value that you can set
                    // to @Enhanced @Balanced or @Efficient
                    syncConfigurations = WalksSyncScheduler.SyncConfigurations.Enhanced,
                ),
            )
            // implement the AnalyticsHandler interface to receive analytics events
            .setAnalyticsService(SampleAnalytics())
            // set the foreground notification configuration attributes for the background data collection
            .setBackgroundNotificationConfig(
                NotificationConfig(
                    title = "This is the Demo App recording",
                    icon = R.drawable.ic_launcher_foreground,
                ),
            )
            // set the foreground notification configuration attributes for the active measurements
            .setInAppNotificationConfig(
                NotificationConfig(
                    title = "This is the Demo App recording",
                    icon = R.drawable.ic_launcher_foreground,
                ),
            )
            .setInitializationCallback {
                onConnectionResult(it)
            }
            .build()
    }
}