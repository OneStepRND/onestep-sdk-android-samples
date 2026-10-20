package com.onestep.onestepuikitsample.screens

import androidx.annotation.DrawableRes
import com.onestep.onestepuikitsample.R

/**
 * The UIKit recording flows this sample can launch. MainActivity maps each entry to an
 * OSTRecordingConfiguration factory, so adding a flow is one entry here and one branch there.
 */
enum class RecordingFlow(val label: String, @param:DrawableRes val icon: Int) {
    WALK("Walk recording", R.drawable.ic_walks),
    TUG("Timed up and go", R.drawable.ic_tug),
    STS("Sit to stand", R.drawable.ic_sts),
    KNEE_ROM("Knee extension test", co.onestep.android.uikit.R.drawable.ic_knee_extention),
    BALANCE("Balance test", R.drawable.balance_test),
    DUAL_TASK("Dual task", R.drawable.ic_dual_task),
    SIX_MINUTE_WALK("6-minute walk test", R.drawable.ic_walks),
    TWO_MINUTE_WALK("2-minute walk test", R.drawable.ic_walks),
    STAIRS("Stairs", R.drawable.ic_physical_activity),
    GENERIC("Generic recording", R.drawable.ic_clock),
}
