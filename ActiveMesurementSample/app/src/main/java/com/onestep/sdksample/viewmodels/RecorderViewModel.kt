package com.onestep.sdksample.viewmodels

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.onestep.android.core.external.OneStep
import co.onestep.android.core.external.models.NormPart
import co.onestep.android.core.external.models.OSTInsights
import co.onestep.android.core.external.models.OSTNorm
import co.onestep.android.core.external.models.OSTParamName
import co.onestep.android.core.external.models.OSTParameterMetadata
import co.onestep.android.core.external.models.OSTResult
import co.onestep.android.core.external.models.OSTUserInputMetaData
import co.onestep.android.core.external.models.aggregate.OSTTimeRangeFilter
import co.onestep.android.core.external.models.aggregate.TimeRangedDataRequest
import co.onestep.android.core.external.models.measurement.OSTActivityType
import co.onestep.android.core.external.models.measurement.OSTAssistiveDevice
import co.onestep.android.core.external.models.measurement.OSTLevelOfAssistance
import co.onestep.android.core.external.models.measurement.OSTMotionMeasurement
import co.onestep.android.core.external.models.measurement.OSTResultState
import co.onestep.android.core.external.models.recording.OSTAnalyserState
import co.onestep.android.core.external.models.recording.OSTRecorderState
import co.onestep.android.core.external.services.OSTMotionDataService
import kotlinx.coroutines.launch
import java.util.Calendar

class RecorderViewModel: ViewModel() {

    private val recorder by lazy {
        // Access the OneStep recording interface
        OneStep.getRecordingService()
    }

    var state = mutableStateOf<String?>(OSTRecorderState.INITIALIZED.name)
        private set

    var result = mutableStateOf<OSTMotionMeasurement?>(null)
        private set

    init {
        viewModelScope.launch {
            recorder.recorderState.collect {
                when (it) {
                    OSTRecorderState.INITIALIZED -> {
                        // Recorder is ready to start a new recording session
                        Log.d(TAG, "RecorderState.INITIALIZED")
                        state.value = it.name
                    }

                    OSTRecorderState.RECORDING -> {
                        // Recorder is currently recording. Don't call `start` again.
                        Log.d(TAG, "RecorderState.RECORDING")
                        state.value = it.name
                    }

                    OSTRecorderState.FINALIZING -> {
                        // Recorder is finalizing the recording session and preparing for analysis
                        Log.d(TAG, "RecorderState.FINALIZING")
                        state.value = it.name
                    }

                    OSTRecorderState.DONE -> {
                        // New measurement was saved and ready for analysis.
                        // You may call analyze() if you want immediate feedback,
                        // Otherwise the SDK will handle the data sync and analysis in the background.
                        Log.d(TAG, "RecorderState.DONE")
                        state.value = it.name
                        analyse()
                    }
                }
            }
        }

        viewModelScope.launch {
            recorder.analyserState.collect {
                when (it) {
                    OSTAnalyserState.Idle -> {
                        Log.d(TAG, "AnalyserState.IDLE")
                    }

                    OSTAnalyserState.Uploading -> {
                        Log.d(TAG, "AnalyserState.UPLOADING")
                        state.value = "Uploading"
                    }

                    OSTAnalyserState.Analyzing -> {
                        Log.d(TAG, "AnalyserState.ANALYZING")
                        state.value = "Analyzing"
                    }

                    OSTAnalyserState.Analyzed -> {
                        // This means the technical analysis process is done and the result is ready
                        Log.d(TAG, "AnalyserState.ANALYZED")
                        state.value = "Analyzed"
                    }

                    is OSTAnalyserState.Failed -> {
                        // This means the analysis process was technically failed (network error, timeout, etc..)
                        // You can try recovery from the different cases:
                        // - Network error: retry the analysis
                        // - OneStep server error: try again later (or let the SDK background worker to handle it)
                        // - Timeout: offer the user to wait more, or get notification when the result is ready
                        Log.d(TAG, "AnalyserState.FAILED with error: ${it.error}")
                        state.value = "Failed ${it.error.error}"
                    }
                }
            }
        }
    }

    /**
     * Start recording a new session: timed walk (60 seconds).
     *
     * Developer responsibility: handling recorder state machine (i.e not calling `start` when already recording)
     * Developer responsibility: checking runtime permission (Activity Recognition is required since Android 14.0)
     */
    fun startRecording() {
        // Reset the recorder before launching a new recording session
        recorder.reset()
        result.value = null
        viewModelScope.launch {
            // Technical key-value properties that will be propagate to the measurement result.
            // Supporting primitive types like Boolean, Number, String
            val metadata = mapOf("app" to "DemoApp", "is_demo" to true, "version" to 1.1)

            // Optional user tagging of the activity including free-text note, tags,
            // and domain specific enums like assistive device and level of assistance.
            val userTagging = OSTUserInputMetaData(
                note = "this is a free-text note",
                tags = listOf("tag1", "tag2", "tag3"),
                assistiveDevice = OSTAssistiveDevice.CANE,
                levelOfAssistance = OSTLevelOfAssistance.INDEPENDENT,
            )

            recorder.start(
                activityType = OSTActivityType.WALK,
                // Duration is the duration of the recording session in milli-seconds.
                // The user can always stop the recording manually.
                // If the duration is not provided, there is a technical limit of 6 minutes;
                duration = 60 * 1000L,
                customMetadata = metadata,
                userInputMetadata = userTagging,
            )
        }
    }

    fun stopRecording() {
        viewModelScope.launch {
            recorder.stop()
        }
    }


    /**
     * Analyze the data is a multi-step transaction:
     * 1. Uploading raw motion data to the OneStep servers
     * 3. Data analysis pipeline will process the data asynchronously
     * 4. SDK is pulling the analysis result when available
     *
     * For short walk, the process usually takes 5-15 seconds.
     * Rarely it make take much longer, due to:
     * - Bad internet connectivity
     * - Server load
     * - Long recording
     *
     * You can pass a "timeout" parameter to the analyze() method (default is 60 seconds).
     */
    private fun analyse() {
        viewModelScope.launch {
            recorder.analyze(timeout = 60 * 1000L)?.let {
                result.value = it  // update UI with the result

                // Pay attention: getting the analysis result doesn't guarantee that the analysis is successful!
                when (it.resultState) {
                    OSTResultState.FULL_ANALYSIS -> {
                        Log.d(TAG, "Full analysis result is available")
                        Log.d(TAG,
                            "measurementId=${it.id}" +
                                    " - steps=${it.metadata.steps}" +
                                    " - walkScore=${it.params[OSTParamName.WALKING_WALK_SCORE] ?: "N/A"}"
                        )

                        // Example - Build summary screen
                        motionBusinessLogicHere(it)

                        // Example - compare to the weekly average
                        weeklyAverageWalkScore()
                    }

                    OSTResultState.PARTIAL_ANALYSIS -> {
                        Log.d(TAG, "Partial analysis result is available")
                    }

                    OSTResultState.EMPTY_ANALYSIS -> {
                        Log.d(TAG, "Analysis error: ${it.error}")
                    }

                    else -> {
                        Log.d(TAG, "Analysis result is not available")
                    }
                }
            } ?: run {
                // The transaction technically failed (network error, timeout, etc..)
                Log.w(TAG, "Result is not available, check recorder.analyserState for more details")
                result.value = null
            }
        }
    }

    /**
     * This is an example of enriching the analyzed parameters with:
     * - Metadata (i.e Display name, units, ..)
     * - Norms (adjusted to sex, age, ..)
     * - Motion Insights from the OneStep Engine (remote)
     */
    private suspend fun motionBusinessLogicHere(measurement: OSTMotionMeasurement) {
        OneStep.getMotionDataService().let { motionService ->
            // You can enrich each parameters with metadata and norms
            val focusParams = listOf(
                OSTParamName.WALKING_VELOCITY,
                OSTParamName.WALKING_DOUBLE_SUPPORT,
                OSTParamName.WALKING_STRIDE_LENGTH
            )
            for (param in focusParams) {
                measurement.params[param]?.let { value ->
                    val metadata = motionService.getParameterMetadata(param)
                    val displayName = "${metadata.displayName}: $value ${metadata.units ?: ""}"
                    val isWithinNorms = motionService.isWithinNorms(param, value) ?: "N/A"
                    val score = motionService.discreteScore(param, value) ?: "N/A"
                    Log.d(TAG, displayName)
                    Log.d(TAG, "Within norms?: $isWithinNorms")
                    Log.d(TAG, "Score (red-yellow-green): $score")
                    Log.d(TAG, "---")
                } ?: run {
                    Log.d(TAG, "Parameter $param is not available")
                }
            }

            // You can query the insight service
            when (val insights: OSTResult<OSTInsights> = motionService.getInsights(measurement.id)) {
                is OSTResult.Error -> {
                    Log.e(TAG, "Failed to fetch insights: ${insights.exception}")
                }

                is OSTResult.Success -> {
                    Log.d(TAG, "Successfully fetched insights")
                    insights.data.insights.take(3).forEach { insight ->
                        Log.d(
                            TAG,
                            "Insight: ${insight.textMarkdown}" +
                                    " - type=${insight.insightType}" +
                                    " - intent=${insight.intent}" +
                                    " - param?=${insight.paramName ?: "N/A"}"
                        )
                    }
                }
            }
        }
    }

    /**
     * This is an example of how to fetch norms and metadata for the measurements parameters.
     *
     * This API interface is still experimental and may change in the future.
     */
    private suspend fun getNorms() {
        // Access the motion data service (instantiated lazily)
        val motionDataService: OSTMotionDataService = OneStep.getMotionDataService()
        // Get the norms for the measurement result
        val normsForMeasurement: List<OSTNorm>? = result.value?.params?.mapNotNull {
            motionDataService.getNormByName(it.key)
        }
        // Get the parameter metadata for the measurement result
        val parametersForMeasurement: List<OSTParameterMetadata>? = result.value?.params?.mapNotNull {
            motionDataService.getParameterMetadata(it.key)
        }
        Log.d(TAG, "normsForMeasurement: $normsForMeasurement")
        Log.d(TAG, "parametersForMeasurement: $parametersForMeasurement")

        val velocityScore = result.value?.params?.get(OSTParamName.WALKING_VELOCITY)
        Log.d(TAG, "velocityScore: $velocityScore")
        val velocityNorm = motionDataService.getNormByName(OSTParamName.WALKING_VELOCITY)
        Log.d(TAG, "velocityNorm: $velocityNorm")
        val velocityMetadata = motionDataService.getParameterMetadata(OSTParamName.WALKING_VELOCITY)
        Log.d(TAG, "velocityMetadata: $velocityMetadata")
        val isVelocityWithinNorms = velocityScore?.let {
            motionDataService.isWithinNorms(OSTParamName.WALKING_VELOCITY, it)
        }
        Log.d(TAG, "isVelocityWithinNorms: $isVelocityWithinNorms")

        // A list of the parts of the scale that can be used to draw " ====|====|====" " in the UI
        //                                                             Red  Green Yellow
        val velocityScaleParts: List<NormPart>? = motionDataService.getNormByName(OSTParamName.WALKING_VELOCITY)?.parts
        Log.d(TAG, "velocityScaleParts: $velocityScaleParts")
    }

    /**
     * This sample demonstrate reading motion measurement records collected by the SDK;
     *
     * It is useful for features like:
     * - Sync data to the server (in case you don't activate BE<->BE integration)
     * - Activity history
     * - Trends
     * - Widgets like "today" vs "past week" vs "baseline"
     */
    private fun weeklyAverageWalkScore() {
        // read measurements
        viewModelScope.launch {
            val startOfWeek = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            var records = OneStep.readMotionMeasurements(
                request = TimeRangedDataRequest(
                    timeRangeFilter = OSTTimeRangeFilter.after(startOfWeek),
                )
            )
            // filter fully analyzed walks
            records = records.filter { it.type == OSTActivityType.WALK }
            records = records.filter { it.resultState == OSTResultState.FULL_ANALYSIS }

            // access parameters and do logic
            val walkScores = records.mapNotNull { it.params[OSTParamName.WALKING_WALK_SCORE] }
            val averageWalkScore = walkScores.average()
            Log.d(TAG, "average walk score during calendar week: $averageWalkScore")
            Log.d(TAG, "calculated over ${walkScores.size} walks")
        }
    }

    companion object {
        val TAG: String = MainViewModel::class.simpleName ?: "MainViewModel"
    }
}
