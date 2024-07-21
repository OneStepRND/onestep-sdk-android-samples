package com.onestep.backgroundmonitoringsample.analytics

import android.util.Log
import co.onestep.android.core.external.services.OneStepAnalytics

class SampleAnalytics : OneStepAnalytics {
    override fun onEvent(eventName: String, properties: Map<String, Any>) {
        Log.d(TAG, "trackEvent: $eventName, $properties")
    }

    companion object {
        val TAG: String = SampleAnalytics::class.simpleName ?: "SampleAnalytics"
    }
}
