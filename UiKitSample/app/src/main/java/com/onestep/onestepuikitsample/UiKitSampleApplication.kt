package com.onestep.onestepuikitsample

import android.app.Application
import android.util.Log
import co.onestep.android.core.external.OneStep
import co.onestep.android.core.external.models.sdkOut.NotificationConfig
import co.onestep.android.core.external.models.sdkOut.OSTInitResult
import co.onestep.android.core.external.models.sdkOut.OSTSdkConfiguration
import co.onestep.android.core.external.models.sdkOut.OSTUserAttributes
import co.onestep.android.uikit.OSTTheme
import co.onestep.android.uikit.ui.theme.OSTThemeDefaults
import co.onestep.android.uikit.ui.theme.primary
import co.onestep.android.uikit.ui.theme.secondary
import co.onestep.android.uikit.ui.typography.NoirFontFamily
import com.onestep.onestepuikitsample.analytics.SampleAnalytics
import kotlinx.coroutines.flow.MutableSharedFlow

class UiKitSampleApplication: Application() {


    val sdkConnectionState = MutableSharedFlow<OSTInitResult>(1)

    var isConnecting = false

    override fun onCreate() {
        super.onCreate()

        // This should be managed in a view model or a state holder
        isConnecting = true
        connect {
            Log.d("UiKitSampleApplication", "connection result $it")
            isConnecting = false
            sdkConnectionState.tryEmit(it)
        }

        // Customize the uikit theme
        OSTTheme.colorScheme = OSTThemeDefaults.colors(
            primary = primary,
            secondary = secondary,
        )
        OSTTheme.font = NoirFontFamily
    }

    /**
     * The UIKit requires a connection to the OneStep SDK.
     */
    fun connect(
        onConnectionResult: (OSTInitResult) -> Unit
    ) {
        OneStep.Builder(
            this.applicationContext,
            apiKey = "<YOUR-API-KEY-HERE>",
            appId = "<YOUR-APP-ID-HERE>",
            distinctId = "<YOUR-USER-DISTINCT-ID",
            identityVerification = null //<YOUR-IDENTITY-VERIFICATION-SECRET-HERE>, // or null if in development
        )
            // UIKit currently focus on the in-app activity
            .setBackgroundMonitoringEnabled(false)
            // optional user profile - gait norms are age and sex adjusted
            .setUserAttributes(
                OSTUserAttributes.Builder()
                    .withAge(60)
                    .withSex(OSTUserAttributes.Sex.FEMALE)
                    .build()
            )
            // implement the AnalyticsHandler interface to receive analytics events
            .setAnalyticsService(SampleAnalytics())
            // set the foreground notification configuration attributes for the active measurements
            .setInAppNotificationConfig(
                NotificationConfig(
                    title = "This is the Sample App recording",
                    icon = R.drawable.ic_launcher_foreground,
                ),
            )
            .setConfiguration(
                OSTSdkConfiguration(
                    mockIMU = false,  // set to true to mock the recording data
                )
            )
            .setInitializationCallback {
                onConnectionResult(it)
            }
            .build()
    }
}