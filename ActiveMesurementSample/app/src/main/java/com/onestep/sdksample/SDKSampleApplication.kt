package com.onestep.sdksample

import android.app.Application
import android.util.Log
import co.onestep.android.core.OSTConfiguration
import co.onestep.android.core.OneStep
import co.onestep.android.core.OSTIdentifyResult
import co.onestep.android.core.platform.models.OSTUserAttributes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class SDKSampleApplication: Application() {

    private val TAG: String? = SDKSampleApplication::class.simpleName
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()
        applicationScope.launch {
            initializeSdk()
        }
    }

    suspend fun initializeSdk() {
        OneStep.initialize(
            application = this@SDKSampleApplication,
            clientToken = "<YOUR-CLIENT-TOKEN>",
        )
        val result = OneStep.identify(
            userId = "<YOUR-USER-DISTINCT-ID>",
        )

        when (result) {
            is OSTIdentifyResult.Success -> {
                Log.d(TAG, "SDK identified successfully")

                // Set user attributes
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                OneStep.updateUserAttributes(
                    OSTUserAttributes.Builder()
                        .withFirstName("John")
                        .withLastName("Doe")
                        .withSex(OSTUserAttributes.Sex.MALE)
                        .withDateOfBirth(dateFormat.parse("1977-05-25")!!)
                        .build()
                )

                // Start collecting events (replaces analytics service)
                EventsCollector.startCollecting(applicationScope)
            }

            is OSTIdentifyResult.Failure -> {
                Log.e(TAG, "SDK identify failed: ${result.error} - ${result.message}")
            }
        }
    }
}
