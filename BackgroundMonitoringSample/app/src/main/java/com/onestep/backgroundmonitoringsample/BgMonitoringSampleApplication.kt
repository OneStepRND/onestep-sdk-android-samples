package com.onestep.backgroundmonitoringsample

import android.app.Application
import co.onestep.android.core.external.OneStep
import co.onestep.android.core.external.models.NotificationConfig
import co.onestep.android.core.external.models.SdkConfiguration
import co.onestep.android.core.internal.data.syncer.WalksSyncScheduler

class BgMonitoringSampleApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        connect()
    }

    fun connect() {
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
            .build()
    }
}