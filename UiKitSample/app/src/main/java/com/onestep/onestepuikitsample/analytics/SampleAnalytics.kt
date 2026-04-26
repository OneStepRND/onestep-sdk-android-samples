package com.onestep.onestepuikitsample.analytics

import android.util.Log
import co.onestep.android.core.OneStep
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

object EventsCollector {

    private const val TAG = "EventsCollector"

    fun startCollecting(scope: CoroutineScope) {
        scope.launch {
            OneStep.events.collect { event ->
                Log.d(TAG, "Event: ${event.name}, ${event.properties}")
            }
        }
    }
}
