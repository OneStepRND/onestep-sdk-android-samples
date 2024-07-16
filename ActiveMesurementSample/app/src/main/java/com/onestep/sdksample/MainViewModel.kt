package com.onestep.sdksample

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.onestep.android.core.external.OneStep
import co.onestep.android.core.external.models.AnalysisState
import co.onestep.android.core.external.models.ParamName
import co.onestep.android.core.internal.recorder.OneStepRecorder
import co.onestep.android.core.internal.recorder.UserInputMetaData
import kotlinx.coroutines.launch

class MainViewModel: ViewModel() {

    private val recorder by lazy {
        OneStep.getRecordingService()
    }

    var state = mutableStateOf<String?>(OneStepRecorder.RecorderState.INITIALIZED.name)
        private set

    var result = mutableStateOf<String?>(null)
        private set

    init {
        viewModelScope.launch {
            recorder.recorderState.collect {
                when (it) {
                    OneStepRecorder.RecorderState.INITIALIZED -> {
                        Log.d(TAG, "RecorderState.INITIALIZED")
                        state.value = "Initialized"
                    }

                    OneStepRecorder.RecorderState.RECORDING -> {
                        Log.d(TAG, "RecorderState.RECORDING")
                        state.value = "Recording"
                    }

                    OneStepRecorder.RecorderState.FINALIZING -> {
                        Log.d(TAG, "RecorderState.FINALIZING")
                        state.value = "Finalizing"
                    }

                    OneStepRecorder.RecorderState.DONE -> {
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
                    AnalysisState.Idle -> {
                        Log.d(TAG, "AnalyserState.IDLE")
                    }

                    AnalysisState.Uploading -> {
                        Log.d(TAG, "AnalyserState.UPLOADING")
                        state.value = "Uploading"
                    }

                    AnalysisState.Analyzing -> {
                        Log.d(TAG, "AnalyserState.ANALYZING")
                        state.value = "Analyzing"
                    }

                    AnalysisState.Analyzed -> {
                        Log.d(TAG, "AnalyserState.ANALYZED")
                        state.value = "Analyzed"
                    }

                    is AnalysisState.Failed -> {
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
                customMetadata = mapOf("app" to "sampleApp"),
                userInputMetadata = UserInputMetaData(
                    note = "sampleApp recording",
                    tags = listOf("walkType", "demo"),
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
            result.value = recorder.analyze()?.params?.get(ParamName.WALKING_WALK_SCORE).toString()
        }
    }

    companion object {
        val TAG: String = MainViewModel::class.simpleName ?: "MainViewModel"
    }
}
