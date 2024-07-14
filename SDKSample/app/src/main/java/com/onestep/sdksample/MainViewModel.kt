package com.onestep.sdksample

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.onestep.android.core.external.OneStep
import co.onestep.android.core.external.models.MotionMeasurement
import co.onestep.android.core.external.models.ParamName
import co.onestep.android.core.internal.recorder.OneStepRecorder
import co.onestep.android.core.internal.recorder.UserInputMetaData
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainViewModel: ViewModel() {

    val recorder by lazy {
        OneStep.getRecordingService()
    }

    var measurement = mutableStateOf<String?>(null)
        private set

    init {
        viewModelScope.launch {
            recorder.recorderState.collect {
                when (it) {
                    OneStepRecorder.RecorderState.INITIALIZED -> {
                        Log.d(TAG, "RecorderState.INITIALIZED")
                    }

                    OneStepRecorder.RecorderState.RECORDING -> {
                        Log.d(TAG, "RecorderState.RECORDING")
                    }

                    OneStepRecorder.RecorderState.FINALIZING -> {
                        Log.d(TAG, "RecorderState.FINALIZING")
                    }

                    OneStepRecorder.RecorderState.DONE -> {
                        Log.d(TAG, "RecorderState.DONE")
                        analyse()
                    }
                }
            }
        }
    }

    fun startRecording(duration: Long) {
        viewModelScope.launch {
            recorder.start(
                duration = duration,
                customMetadata = mapOf("app" to "DemoApp"),
                userInputMetadata = UserInputMetaData(
                    note = "DemoApp recording",
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
            measurement.value = recorder.analyzeMotionMeasurement()?.params?.get(ParamName.WALKING_WALK_SCORE).toString()
        }
    }

    companion object {
        val TAG: String = MainViewModel::class.simpleName ?: "MainViewModel"
    }
}
