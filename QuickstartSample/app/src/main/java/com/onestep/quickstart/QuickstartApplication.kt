package com.onestep.quickstart

import android.app.Application
import android.util.Log
import co.onestep.android.core.OneStep
import co.onestep.android.core.getOr
import co.onestep.android.core.onError

class QuickstartApplication : Application() {

    /** The SDK handle, or null when initialization failed (for example, the device has no gyroscope). */
    var oneStep: OneStep? = null
        private set

    override fun onCreate() {
        super.onCreate()
        // Initialize once, in Application.onCreate(): the SDK's background workers and recording
        // service can restart the process without opening an Activity.
        oneStep = OneStep.initialize(
            application = this,
            // The server rejected the session (401/403). Call setPatient() again to reconnect.
            onAuthLost = { Log.w(TAG, "OneStep auth lost; call setPatient() again") },
        ).onError { error ->
            Log.e(TAG, "OneStep.initialize failed: ${error.cause.type.name}")
        }.getOr(null)
    }

    private companion object {
        const val TAG = "Quickstart"
    }
}
