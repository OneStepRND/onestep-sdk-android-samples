package com.onestep.onestepuikitsample

import android.app.Application
import android.util.Log
import co.onestep.android.core.OSTResult
import co.onestep.android.core.OSTUserAttributes
import co.onestep.android.core.OneStep
import co.onestep.android.uikit.OSTTheme
import co.onestep.android.uikit.ui.typography.NoirFontFamily
import com.onestep.onestepuikitsample.analytics.EventsCollector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class UiKitSampleApplication: Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()
        OSTTheme.font = NoirFontFamily
    }

    suspend fun initializeSdk() {
        OneStep.initialize(
            this,
            clientToken = "<YOUR_CLIENT_TOKEN_HERE>",
        )

        val result = OneStep.identify(
            userId = "<YOUR_IDENTITY_HERE>",
            identityVerification = null
        )

        when (result) {
            is OSTResult.Success -> {
                OneStep.updateUserAttributes(
                    OSTUserAttributes.Builder()
                        .withAge(60)
                        .withSex(OSTUserAttributes.Sex.FEMALE)
                        .build()
                )
                EventsCollector.startCollecting(applicationScope)
            }
            is OSTResult.Error -> {
                Log.e("UiKitSampleApplication", "Identify failed: ${result.message}")
            }
        }
    }
}
