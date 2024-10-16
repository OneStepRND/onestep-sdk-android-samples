package com.onestep.sdksample

import android.app.Application
import android.util.Log
import co.onestep.android.core.external.OneStep
import co.onestep.android.core.external.models.sdkOut.NotificationConfig
import co.onestep.android.core.external.models.sdkOut.OSTInitResult
import co.onestep.android.core.external.models.sdkOut.OSTSdkConfiguration
import co.onestep.android.core.external.models.sdkOut.OSTUserAttributes
import co.onestep.android.core.internal.utils.ISO_FORMAT
import co.onestep.android.core.internal.utils.toDate
import kotlinx.coroutines.flow.MutableSharedFlow

class SDKSampleApplication: Application() {

    private val TAG: String? = SDKSampleApplication::class.simpleName

    val sdkConnectionState = MutableSharedFlow<OSTInitResult>(1)

    override fun onCreate() {
        super.onCreate()
        connect {
            Log.d(TAG, "connection result $it")
            sdkConnectionState.tryEmit(it)
        }
    }

    fun connect(
        onConnectionResult: (OSTInitResult) -> Unit
    ) {
        OneStep.Builder(
            this.applicationContext,
            apiKey = "<YOUR-API-KEY-HERE>",
            appId = "<YOUR-APP-ID-HERE>",
            distinctId = "<A-UUID-FOR CURRENT-USER-HERE>",
            identityVerification = null //<YOUR-IDENTITY-VERIFICATION-SECRET-HERE>, // Activate this in production
        )
            // SDK will be used only for in-app recording; Completely deactivate background;
            .setBackgroundMonitoringEnabled(false)
            // set the user profile attributes
            .setUserAttributes(
                OSTUserAttributes.Builder()
                    .withFirstName("John")
                    .withLastName("Doe")
                    .withSex(OSTUserAttributes.Sex.MALE)
                    .withDateOfBirth("1977-05-25".toDate(dateFormat = ISO_FORMAT)!!)
                    .build()
            )
            // customize the foreground notification for active motion recorder
            .setInAppNotificationConfig(
                NotificationConfig(
                    title = "This is the Sample App recording",
                    icon = R.drawable.ic_launcher_foreground,
                ),
            )
            .setConfiguration(
                OSTSdkConfiguration(
                    mockIMU = false, // set to True when testing in emulator (mock IMU sensor data)
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