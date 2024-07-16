package com.onestep.sdksample

import android.util.Log
import co.onestep.android.core.external.services.AnalyticsHandler

class SampleAnalytics : AnalyticsHandler {
    override fun trackEvent(eventName: String, properties: Map<String, Any>) {
        Log.d(TAG, "trackEvent: $eventName, $properties")
    }

    override fun setUserProperties(properties: Map<String, Any>) {
        Log.d(TAG, "setUserProperties: $properties")
    }

    override fun setSuperProperties(properties: Map<String, Any>) {
        Log.d(TAG, "setSuerProperties: $properties")
    }

    override fun identifyUser(distinctId: String) {
        Log.d(TAG, "identifyUser: $distinctId")
    }

    companion object {
        val TAG: String = SampleAnalytics::class.simpleName ?: "SampleAnalytics"
    }
}
