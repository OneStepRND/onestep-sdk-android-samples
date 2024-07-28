package com.onestep.sdksample.viewmodels

import android.os.Build
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.onestep.android.core.external.OneStep
import co.onestep.android.core.external.models.ActivityType
import co.onestep.android.core.external.models.AnalyserState
import co.onestep.android.core.external.models.AssistiveDevice
import co.onestep.android.core.external.models.LevelOfAssistance
import co.onestep.android.core.external.models.MotionMeasurement
import co.onestep.android.core.external.models.RecorderState
import co.onestep.android.core.external.services.MotionDataService
import co.onestep.android.core.internal.data.domain.Norm
import co.onestep.android.core.internal.data.domain.Norms
import co.onestep.android.core.internal.data.domain.ParameterMetadata
import co.onestep.android.core.internal.data.domain.ParametersMetadata
import co.onestep.android.core.internal.recorder.UserInputMetaData
import kotlinx.coroutines.launch

class RecorderViewModel: ViewModel() {

    private val recorder by lazy {
        OneStep.getRecordingService()
    }

    var state = mutableStateOf<String?>(RecorderState.INITIALIZED.name)
        private set

    var result = mutableStateOf<MotionMeasurement?>(null)
        private set

    init {
        viewModelScope.launch {
            recorder.recorderState.collect {
                when (it) {
                    RecorderState.INITIALIZED -> {
                        Log.d(TAG, "RecorderState.INITIALIZED")
                        state.value = "Initialized"
                    }

                    RecorderState.RECORDING -> {
                        Log.d(TAG, "RecorderState.RECORDING")
                        state.value = "Recording"
                    }

                    RecorderState.FINALIZING -> {
                        Log.d(TAG, "RecorderState.FINALIZING")
                        state.value = "Finalizing"
                    }

                    RecorderState.DONE -> {
                        Log.d(TAG, "RecorderState.DONE")
                        state.value = "Done"

                        // trigger analysis when the recording is done - it may take some time
                        analyse()
                    }
                }
            }
        }

        viewModelScope.launch {
            recorder.analyserState.collect {
                when (it) {
                    AnalyserState.Idle -> {
                        Log.d(TAG, "AnalyserState.IDLE")
                    }

                    AnalyserState.Uploading -> {
                        Log.d(TAG, "AnalyserState.UPLOADING")
                        state.value = "Uploading"
                    }

                    AnalyserState.Analyzing -> {
                        Log.d(TAG, "AnalyserState.ANALYZING")
                        state.value = "Analyzing"
                    }

                    AnalyserState.Analyzed -> {
                        Log.d(TAG, "AnalyserState.ANALYZED")
                        state.value = "Analyzed"
                    }

                    is AnalyserState.Failed -> {
                        Log.d(TAG, "AnalyserState.FAILED with error: ${it.error}")
                        state.value = "Failed ${it.error.error}"
                    }
                }
            }
        }
    }

    fun startRecording() {
        // It's your responsibility to handle recorder state machine (i.e not calling start when already recording)
        // It's your responsibility to check runtime permission (Activity Recognition for Android 14.0 and above)

        // Reset the recorder before launching a new recording session
        recorder.reset()

        viewModelScope.launch {
            // Technical key-value properties that will be propagate to the measurement result.
            // Supporting primitive types like Boolean, Number, String
            val metadata = mapOf("app" to "DemoApp", "is_demo" to true, "version" to 1.1)

            // Optional user tagging of the activity including free-text note, tags,
            // and domain specific enums like assistive device and level of assistance.
            val userTagging = UserInputMetaData(
                note = "this is a free-text note",
                tags = listOf("tag1", "tag2", "tag3"),
                assistiveDevice = AssistiveDevice.CANE,
                levelOfAssistance = LevelOfAssistance.INDEPENDENT,
            )

            recorder.start(
                activityType = ActivityType.WALK,
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


    private fun analyse() {
        viewModelScope.launch {
            result.value = recorder.analyze()
            Log.d(TAG, "result: $result")
        }
    }

    /*
    This is an example of how to fetch norms and metadata for the measurements parameters
     */
    private fun getNorms() {
        viewModelScope.launch {
            // Access the motion data service (instantiated lazily)
            val motionDataService: MotionDataService? = OneStep.getMotionDataService()
            // Fetch all norms
            val norms: Norms? = motionDataService?.getAllNorms()
            // Fetch all parameter metadata
            val parametersMetadata: ParametersMetadata? = motionDataService?.getAllParametersMetadata()
            // Get the norms for the measurement result
            val normsForMeasurement: List<Norm>? = result.value?.params?.mapNotNull {
                motionDataService?.getNormByName(it.key)
            }
            // Get the parameter metadata for the measurement result
            val parametersForMeasurement: List<ParameterMetadata>? = result.value?.params?.mapNotNull {
                motionDataService?.getParameterMetadata(it.key)
            }
            Log.d(TAG, "norms: $norms")
            Log.d(TAG, "parametersMetadata: $parametersMetadata")
            Log.d(TAG, "normsForMeasurement: $normsForMeasurement")
            Log.d(TAG, "parametersForMeasurement: $parametersForMeasurement")
        }
    }

    companion object {
        val TAG: String = MainViewModel::class.simpleName ?: "MainViewModel"
    }
}
