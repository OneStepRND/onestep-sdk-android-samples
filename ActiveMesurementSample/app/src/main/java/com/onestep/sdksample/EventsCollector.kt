package com.onestep.sdksample

import android.util.Log
import co.onestep.android.core.OneStep
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

object EventsCollector {
    private const val TAG = "EventsCollector"

    fun startCollecting(oneStep: OneStep, scope: CoroutineScope) {
        scope.launch {
            oneStep.events.collect { event ->
                Log.i(TAG, "Event: ${event.name}, ${event.properties}")
            }
        }
    }
}
