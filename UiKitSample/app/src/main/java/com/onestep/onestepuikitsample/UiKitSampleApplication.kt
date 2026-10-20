package com.onestep.onestepuikitsample

import android.app.Application
import android.util.Log
import co.onestep.android.core.OSTUserAttributes
import co.onestep.android.core.OneStep
import co.onestep.android.core.onError
import co.onestep.android.core.onSuccess
import co.onestep.android.uikit.OSTTheme
import co.onestep.android.uikit.ui.typography.NoirFontFamily
import com.onestep.onestepuikitsample.analytics.EventsCollector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class UiKitSampleApplication : Application() {

    private val TAG = UiKitSampleApplication::class.simpleName
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    // Null when OneStep.initialize() failed. MainActivity then shows an error instead of crashing.
    var oneStepSdk: OneStep? = null
        private set

    // local.properties keys left blank in this build. setPatient() cannot succeed without them,
    // so MainActivity shows a setup message instead of a late "session lost" error.
    val missingCredentials: List<String> = buildList {
        if (BuildConfig.CLIENT_TOKEN.isBlank()) add("onestep.clientToken")
        if (BuildConfig.CUSTOMER_PATIENT_ID.isBlank()) add("onestep.customerPatientId")
    }

    override fun onCreate() {
        super.onCreate()
        if (missingCredentials.isNotEmpty()) {
            // Log only the key names, never credential values.
            Log.e(TAG, "Missing in local.properties: ${missingCredentials.joinToString()}")
        }
        OSTTheme.font = NoirFontFamily

        OneStep.initialize(
            application = this,
            onAuthLost = { error ->
                Log.w(TAG, "Auth lost: ${error.message}")
            },
        ).onSuccess { oneStep ->
            oneStepSdk = oneStep
            EventsCollector.startCollecting(oneStep, applicationScope)
        }.onError { error ->
            Log.e(TAG, "SDK initialize failed: ${error.cause.message}")
        }
    }

    suspend fun connectUser() {
        val oneStep = oneStepSdk ?: return
        if (missingCredentials.isNotEmpty()) return
        // Credentials come from local.properties via BuildConfig, so no keys live in source.
        // identityVerification is the precomputed HMAC digest of customerPatientId (or blank);
        // pass null when blank to connect without identity verification. See the README "API Keys".
        oneStep.setPatient(
            apiKey = BuildConfig.CLIENT_TOKEN,
            customerPatientId = BuildConfig.CUSTOMER_PATIENT_ID,
            identityVerification = BuildConfig.IDENTITY_VERIFICATION.ifBlank { null },
            userAttributes = {
                withAge(60)
                withSex(OSTUserAttributes.Sex.FEMALE)
            },
        ).onError { error ->
            Log.e(TAG, "Identify failed: ${error.cause.message}")
        }
    }
}
