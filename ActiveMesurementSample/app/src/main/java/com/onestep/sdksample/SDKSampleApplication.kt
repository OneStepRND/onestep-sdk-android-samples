package com.onestep.sdksample

import android.app.Application
import android.util.Log
import co.onestep.android.core.OSTUserAttributes
import co.onestep.android.core.OneStep
import co.onestep.android.core.onError
import co.onestep.android.core.onSuccess
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import java.text.SimpleDateFormat
import java.util.Locale

class SDKSampleApplication : Application() {

    private val TAG: String? = SDKSampleApplication::class.simpleName
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    lateinit var oneStepSdk: OneStep
        private set

    override fun onCreate() {
        super.onCreate()
        OneStep.initialize(
            application = this,
            onAuthLost = {
                Log.w(TAG, "Auth lost")
            },
        ).onSuccess { oneStep ->
            oneStepSdk = oneStep
            EventsCollector.startCollecting(oneStep, applicationScope)
        }.onError { error ->
            Log.e(TAG, "SDK initialize failed: ${error.cause.message}")
        }
    }

    suspend fun connectUser() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        oneStepSdk.setPatient(
            apiKey = "<YOUR-CLIENT-TOKEN>",
            customerPatientId = "<YOUR-USER-DISTINCT-ID>",
            identityVerification = null,
            userAttributes = {
                withFirstName("John")
                withLastName("Doe")
                withSex(OSTUserAttributes.Sex.MALE)
                withDateOfBirth(dateFormat.parse("1977-05-25")!!)
            },
        ).onSuccess {
            Log.d(TAG, "SDK identified successfully")
        }.onError { error ->
            Log.e(TAG, "SDK setPatient failed: ${error.cause.message}")
        }
    }
}
