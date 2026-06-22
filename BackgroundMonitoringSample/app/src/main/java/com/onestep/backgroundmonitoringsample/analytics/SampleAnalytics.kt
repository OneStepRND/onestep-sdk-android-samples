package com.onestep.backgroundmonitoringsample.analytics

import android.util.Log
import co.onestep.android.core.OneStep
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * The SDK emits lifecycle/telemetry events on the cold [OneStep.events] flow (identification,
 * monitoring state changes, sync results, errors, …). Collect it once from an
 * application-scoped coroutine. This sample just logs each event; a real app would typically
 * forward them to its own analytics pipeline and/or react to specific event names.
 *
 * Docs: https://glorious-caboc-cd3.notion.site/onestep-collect-for-android
 */
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
