package com.onestep.sdksample

import android.util.Log
import co.onestep.android.core.external.services.OSTAnalytics

class SampleAnalytics : OSTAnalytics {
    override fun onEvent(eventName: String, properties: Map<String, Any>) {
        Log.i(TAG, "trackEvent: $eventName, $properties")
    }

    companion object {
        val TAG: String = SampleAnalytics::class.simpleName ?: "SampleAnalytics"
    }
}
