package com.onestep.sdksample

import android.app.Application
import android.util.Log
import co.onestep.android.core.OSTUserAttributes
import co.onestep.android.core.OneStep
import co.onestep.android.core.getOr
import co.onestep.android.core.motionLab.getMotionLab
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
        OneStep.initialize(
            application = this,
            onAuthLost = {
                Log.w(TAG, "Auth lost")
            },
        ).onSuccess { oneStep ->
            oneStepSdk = oneStep
            EventsCollector.startCollecting(oneStep, applicationScope)
            configureRecordingNotification(oneStep)
        }.onError { error ->
            Log.e(TAG, "SDK initialize failed: ${error.cause.message}")
        }
    }

    /**
     * A recording runs in a foreground service, so Android shows a notification while it lasts.
     * Brand it with your own icon and copy (SDK 2.2.0+).
     *
     * MotionLab is available only once a patient is identified, so call this after setPatient()
     * succeeds, and again in Application.onCreate() for a patient restored from a previous run
     * (the recorder can restart in a process where none of your other code has run).
     */
    private fun configureRecordingNotification(oneStep: OneStep) {
        val motionLab = oneStep.getMotionLab().getOr(null) ?: run {
            Log.d(TAG, "MotionLab not available yet; recording notification set after setPatient")
            return
        }
        motionLab.setCustomRecordingNotification {
            default(
                icon = R.drawable.ic_launcher_foreground,
                title = { getString(R.string.recording_notification_title) },
                text = { getString(R.string.recording_notification_text) },
            )
        }.onError { error ->
            Log.w(TAG, "setCustomRecordingNotification failed: ${error.cause.message}")
        }
    }

    /** Identifies the user. Returns true when setPatient() succeeded. */
    suspend fun connectUser(): Boolean {
        val oneStep = oneStepSdk ?: return false
        if (missingCredentials.isNotEmpty()) return false
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        // Credentials come from local.properties via BuildConfig, so no keys live in source.
        // identityVerification is the precomputed HMAC digest of customerPatientId (or blank);
        // pass null when blank to connect without identity verification. See the README "Keys".
        return oneStep.setPatient(
            apiKey = BuildConfig.CLIENT_TOKEN,
            customerPatientId = BuildConfig.CUSTOMER_PATIENT_ID,
            identityVerification = BuildConfig.IDENTITY_VERIFICATION.ifBlank { null },
            userAttributes = {
                withFirstName("John")
                withLastName("Doe")
                withSex(OSTUserAttributes.Sex.MALE)
                withDateOfBirth(dateFormat.parse("1977-05-25")!!)
            },
        ).onSuccess {
            Log.d(TAG, "SDK identified successfully")
            configureRecordingNotification(oneStep)
        }.onError { error ->
            Log.e(TAG, "SDK setPatient failed: ${error.cause.message}")
        }.isSuccess
    }
}
