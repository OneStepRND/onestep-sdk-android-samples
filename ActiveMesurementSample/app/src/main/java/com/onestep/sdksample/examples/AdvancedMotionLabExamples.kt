/**
 * Reference examples for advanced MotionLab features.
 *
 * These functions are NOT invoked by the live app — they exist as a documentation
 * artefact you can copy from. Each function is independently runnable and assumes
 * the SDK has already been initialized and a user identified.
 *
 * Topics covered here:
 *  - Insights enrichment (norms + AI-generated insights for a measurement)
 *  - Parameter metadata + norm structure (for rendering scales, ranges, units)
 *  - Historical reads (querying past measurements with a time-range filter)
 */
package com.onestep.sdksample.examples

import android.util.Log
import co.onestep.android.core.OSTActivityType
import co.onestep.android.core.OSTParamName
import co.onestep.android.core.OSTResult
import co.onestep.android.core.OneStep
import co.onestep.android.core.insights.OSTNorm
import co.onestep.android.core.insights.OSTNormPart
import co.onestep.android.core.insights.OSTParameterMetadata
import co.onestep.android.core.motionLab.OSTMotionMeasurement
import co.onestep.android.core.motionLab.OSTResultState
import co.onestep.android.core.motionLab.OSTTimeRangeFilter
import co.onestep.android.core.motionLab.OSTTimeRangedDataRequest
import java.util.Calendar

private const val TAG = "MotionLabExamples"

/**
 * Enrich a measurement with parameter metadata, norms, and AI-generated insights.
 *
 * Use this when you want to render a result screen with friendly labels, scoring,
 * and explanatory text rather than raw parameter values.
 */
suspend fun enrichWithInsightsAndNorms(measurement: OSTMotionMeasurement) {
    val motionService = OneStep.insights.getMotionDataService()

    // For each parameter you care about, look up its metadata and compare the value
    // to the clinical norm.
    val focusParams = listOf(
        OSTParamName.WALKING_VELOCITY,
        OSTParamName.WALKING_DOUBLE_SUPPORT,
        OSTParamName.WALKING_STRIDE_LENGTH,
    )
    for (param in focusParams) {
        val value = measurement.params[param] ?: continue
        val metadata = motionService.getParameterMetadata(param)
        val isWithinNorms = motionService.isWithinNorms(param, value)
        val score = motionService.discreteScore(param, value)
        Log.d(TAG, "${metadata.displayName}: $value ${metadata.units ?: ""}")
        Log.d(TAG, "  within norms? $isWithinNorms")
        Log.d(TAG, "  score: $score")
    }

    // Fetch AI-generated insights (markdown summaries) for the measurement.
    when (val insights = OneStep.insights.getMeasurementInsights(measurement.id)) {
        is OSTResult.Success -> {
            insights.data.insights.take(3).forEach { insight ->
                Log.d(
                    TAG,
                    "Insight: ${insight.textMarkdown}" +
                        " - type=${insight.insightType}" +
                        " - intent=${insight.intent}" +
                        " - param?=${insight.paramName ?: "N/A"}",
                )
            }
        }
        is OSTResult.Error -> {
            Log.e(TAG, "Failed to fetch insights: ${insights.error} - ${insights.message}")
        }
    }
}

/**
 * Look up norms and metadata for the parameters in a measurement.
 *
 * The norm's [OSTNormPart] segments describe the boundary colours and ranges,
 * which is what you need for a "red / yellow / green" UI gauge.
 */
suspend fun fetchNormsAndMetadata(measurement: OSTMotionMeasurement) {
    val motionService = OneStep.insights.getMotionDataService()

    val norms: List<OSTNorm> = measurement.params.mapNotNull {
        motionService.getNormByName(it.key)
    }
    val metadata: List<OSTParameterMetadata> = measurement.params.mapNotNull {
        motionService.getParameterMetadata(it.key)
    }
    Log.d(TAG, "norms: $norms")
    Log.d(TAG, "metadata: $metadata")

    // Walking velocity, drilled down: value vs norm vs scale parts.
    val velocity = measurement.params[OSTParamName.WALKING_VELOCITY] ?: return
    val velocityNorm = motionService.getNormByName(OSTParamName.WALKING_VELOCITY)
    val velocityMetadata = motionService.getParameterMetadata(OSTParamName.WALKING_VELOCITY)
    val withinNorms = motionService.isWithinNorms(OSTParamName.WALKING_VELOCITY, velocity)
    val scaleParts: List<OSTNormPart>? = velocityNorm?.parts
    Log.d(
        TAG,
        "velocity=$velocity norm=$velocityNorm meta=$velocityMetadata withinNorms=$withinNorms parts=$scaleParts",
    )
}

/**
 * Query historical motion measurements collected by the SDK.
 *
 * Useful for: server sync (when you don't use BE↔BE integration), activity history,
 * trend widgets ("today" vs "past week" vs "baseline").
 */
suspend fun weeklyAverageWalkScore() {
    val startOfWeek = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    val records = OneStep.motionLab.readMotionMeasurements(
        request = OSTTimeRangedDataRequest(
            timeRangeFilter = OSTTimeRangeFilter.after(startOfWeek),
        ),
    )
        .filter { it.type == OSTActivityType.WALK }
        .filter { it.resultState == OSTResultState.FULL_ANALYSIS }

    val walkScores = records.mapNotNull { it.params[OSTParamName.WALKING_WALK_SCORE] }
    val averageWalkScore = if (walkScores.isNotEmpty()) walkScores.average() else 0.0
    Log.d(TAG, "average walk score this week: $averageWalkScore (over ${walkScores.size} walks)")
}
