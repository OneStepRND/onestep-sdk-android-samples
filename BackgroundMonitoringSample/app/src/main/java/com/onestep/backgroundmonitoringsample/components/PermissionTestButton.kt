package com.onestep.backgroundmonitoringsample.components

import android.app.AlertDialog
import android.content.Context
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import androidx.annotation.ColorInt
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import co.onestep.android.core.external.OneStep
import com.onestep.backgroundmonitoringsample.R


/*
* This composable demonstrates how you can use the OneStep SDK to check the status of permissions
 */
@Composable
fun PermissionTestButton() {
    val context = LocalContext.current
    SafeSDKButton(
        action = {
            val permissionsText =
                // Get a map of permissions and their status
                OneStep.getManifestPermissionsWithStatus(context.applicationContext)
                    .map { (permission, isGranted) ->
                        val simpleName = permission.substringAfterLast('.')
                        val statusText = if (isGranted) "Granted" else "Denied"
                        @ColorInt val color: Int = if (isGranted) {
                            ContextCompat.getColor(context.applicationContext, R.color.purple_700)
                        } else {
                            ContextCompat.getColor(context.applicationContext, R.color.red)
                        }
                        val spannableString =
                            SpannableStringBuilder("$simpleName: $statusText\n")
                        spannableString.setSpan(
                            ForegroundColorSpan(color),
                            0,
                            spannableString.length,
                            0
                        )
                        spannableString
                    }
                    .reduce { acc, spannableString -> acc.append(spannableString) }

            AlertDialog.Builder(context)
                .setTitle("Permissions")
                .setMessage(permissionsText)
                .setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        },
    ) {
        Text("SDK Permissions")
    }
}
