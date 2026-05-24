package com.onestep.backgroundmonitoringsample.analytics

import android.util.Log
import co.onestep.android.core.OneStep
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

object EventsCollector {
    private const val TAG = "OneStepEvents"

    fun startCollecting(oneStep: OneStep, scope: CoroutineScope) {
        scope.launch {
            oneStep.events.collect { event ->
                Log.d(TAG, "Event: ${event.name}, props: ${event.properties}")
            }
        }
    }
}
