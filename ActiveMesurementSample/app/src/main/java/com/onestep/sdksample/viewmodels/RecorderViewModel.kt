package com.onestep.sdksample.viewmodels

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.onestep.android.core.external.OneStep
import co.onestep.android.core.external.models.AnalyserState
import co.onestep.android.core.external.models.AssistiveDevice
import co.onestep.android.core.external.models.LevelOfAssistance
import co.onestep.android.core.external.models.MotionMeasurement
import co.onestep.android.core.external.models.RecorderState
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

    /*
        * Start recording with a given duration in seconds
     */
    fun startRecording(duration: Long) {
        recorder.reset()
        viewModelScope.launch {
            recorder.start(
                duration = duration,
                customMetadata = mapOf("app" to "DemoApp", "is_demo" to true, "version" to 1.1),
                userInputMetadata = UserInputMetaData(
                    note = "sampleApp recording",
                    tags = listOf("walkType", "demo"),
                    assistiveDevice = AssistiveDevice.CANE,
                    levelOfAssistance = LevelOfAssistance.MODERATE_ASSISTANCE,
                ),
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

    private fun getNorms() {
        viewModelScope.launch {
            val motionDataService = OneStep.getMotionDataService()
            val norms = motionDataService?.getAllNorms()
            val parametersMetadata = motionDataService?.getAllParameters()
            val normsForMeasurement = result.value?.params?.mapNotNull {
                motionDataService?.getNormByName(it.key)
            }
            val parametersForMeasurement = result.value?.params?.mapNotNull {
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
