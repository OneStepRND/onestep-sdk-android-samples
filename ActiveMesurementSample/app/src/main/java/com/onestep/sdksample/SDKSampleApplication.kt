package com.onestep.sdksample

import android.app.Application
import android.util.Log
import co.onestep.android.core.external.OneStep
import co.onestep.android.core.external.models.InitResult
import co.onestep.android.core.external.models.NotificationConfig
import co.onestep.android.core.external.models.SdkConfiguration
import co.onestep.android.core.external.models.UserAttributes
import co.onestep.android.core.internal.utils.ISO_FORMAT
import co.onestep.android.core.internal.utils.toDate
import kotlinx.coroutines.flow.MutableSharedFlow

class SDKSampleApplication: Application() {

    val sdkConnectionState = MutableSharedFlow<InitResult>(1)

    override fun onCreate() {
        super.onCreate()
        connect {
            Log.d("SDKSampleApplication", "connection result $it")
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
                    // SDK will be used only for in-app recording
                    enableMonitoringFeature = false,
                    // use the mock IMU for testing in emulator
                    mockIMU = false,
                ),
            )
            // set the user profile attributes
            .setUserAttributes(
                UserAttributes.Builder()
                    .withFirstName("John")
                    .withLastName("Doe")
                    .withSex(UserAttributes.Gender.MALE.description)
                    .withDateOfBirth("1977-05-25".toDate(dateFormat = ISO_FORMAT))
                    .build()
            )
            // customize the foreground notification for active motion recorder
            .setInAppNotificationConfig(
                NotificationConfig(
                    title = "This is the Demo App recording",
                    icon = R.drawable.ic_launcher_foreground,
                ),
            )
            // implement the AnalyticsHandler interface to receive analytics events
            .setAnalyticsService(SampleAnalytics())
            .setInitializationCallback {
                onConnectionResult(it)
            }
            .build()
    }
}