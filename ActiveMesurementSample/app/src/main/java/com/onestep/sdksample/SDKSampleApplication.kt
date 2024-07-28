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
            apiKey = "my-3i3Ndsf7IAG0yB4iWAn-HVDmkWWStffQZ0p4Y5qo",//<YOUR-API-KEY-HERE>",
            appId = "6ddbcc62-5ad1-4cd1-bfa7-4e79af155309",//"<YOUR-APP-ID-HERE>",
            distinctId = "shahar@demo.com",
            identityVerification = null //<YOUR-IDENTITY-VERIFICATION-SECRET-HERE>, // Activate this in production
        )
            // SDK will be used only for in-app recording
            .setBackgroundMonitoringEnabled(false)
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
            .setConfiguration(
                SdkConfiguration(
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