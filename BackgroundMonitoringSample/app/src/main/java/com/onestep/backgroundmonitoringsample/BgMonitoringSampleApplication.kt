package com.onestep.backgroundmonitoringsample

import android.app.Application
import co.onestep.android.core.external.OneStep
import co.onestep.android.core.external.models.InitResult
import co.onestep.android.core.external.models.NotificationConfig
import co.onestep.android.core.external.models.SdkConfiguration
import co.onestep.android.core.internal.data.syncer.WalksSyncScheduler
import com.onestep.backgroundmonitoringsample.analytics.SampleAnalytics

class BgMonitoringSampleApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        //connect()
    }

    fun connect(
        onConnectionResult: (InitResult) -> Unit
    ) {
        OneStep.Builder(
            this.applicationContext,
            apiKey = "my-3i3Ndsf7IAG0yB4iWAn-HVDmkWWStffQZ0p4Y5qo",//<YOUR-API-KEY-HERE>",
            appId = "6ddbcc62-5ad1-4cd1-bfa7-4e79af155309",//"<YOUR-APP-ID-HERE>",
            distinctId = "ziv@bgSamplesApp.com",
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
            .setInitializationCallback {
                onConnectionResult(it)
            }
            .build()
    }
}