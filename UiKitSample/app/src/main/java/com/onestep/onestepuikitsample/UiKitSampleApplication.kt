package com.onestep.onestepuikitsample

import android.app.Application
import co.onestep.android.core.external.OneStep
import co.onestep.android.core.external.models.NotificationConfig
import co.onestep.android.core.external.models.SdkConfiguration
import co.onestep.android.core.external.models.UserAttributes
import co.onestep.android.core.internal.data.syncer.WalksSyncScheduler
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class UiKitSampleApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        connect()
    }

    fun connect() {
        OneStep.Builder(
            this.applicationContext,
            apiKey = <YOUR-API-KEY-HERE>,
            appId = <YOUR-APP-ID-HERE>,
            distinctId = <A-UUID-FOR CURRENT-USER-HERE>,
            identityVerification = <YOUR-IDENTITY-VERIFICATION-SECRET-HERE>, // or null if in development
        )
            .setConfiguration(
                SdkConfiguration(
                    backgroundMonitoring = true,
                    collectPedometer = true,
                    // retentionPeriodHours is an integer value that you can set to the
                    // number of hours you want to retain data for.
                    // or set to 0 to retain data indefinitely
                    retentionPeriodHours = 0,
                    // syncConfigurations is an enum value that you can set to @Enhanced @Balanced or @Efficient
                    syncConfigurations = WalksSyncScheduler.SyncConfigurations.Enhanced,
                ),
            )

            .setUserAttributes(
                UserAttributes.Builder()
                    .withEmail("email")
                    .withFirstName("first")
                    .withLastName("last")
                    .build(),
                // you can set this to false if you don't want to expose user attributes to the server
                shouldExposeToServer = false,
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
            .build()
    }
}