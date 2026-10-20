package com.onestep.backgroundmonitoringsample.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import co.onestep.android.core.OSTResult
import co.onestep.android.core.monitoring.OSTMonitoring
import com.onestep.backgroundmonitoringsample.R
import com.onestep.backgroundmonitoringsample.ui.model.NotificationStyle

/**
 * Background monitoring runs inside an Android *foreground service*, so the OS requires it to
 * show an ongoing notification for as long as it is active. The OneStep SDK lets you control how
 * that notification looks through a single entry point:
 *
 * ```
 * monitoring.setCustomMonitoringNotification { /* OSTNotificationConfigScope */ }
 * ```
 *
 * Inside the lambda you call exactly one of three builders, each producing a different
 * [co.onestep.android.core.monitoring.OSTNotificationConfig]:
 *
 *  - [default] — the SDK renders a simple title/text notification for you.
 *  - [custom]  — the SDK renders a richer notification showing your app name (or title), an
 *                optional line of text, and live progress toward a daily step goal.
 *  - [native]  — you build a fully custom [android.app.Notification] and hand it to the SDK.
 *
 * `setCustomMonitoringNotification` can be called at any time, including while monitoring is
 * already active — the live foreground-service notification updates in place. This object is the
 * single place this sample builds those configs so the call sites (startup + the in-app picker)
 * stay in sync.
 */
object MonitoringNotifications {

    /** Small icon shown in the status bar for every style. Must be a valid drawable resource. */
    private val NOTIFICATION_ICON = R.drawable.ic_launcher_foreground

    private const val APP_NAME = "Background Monitoring Sample"

    /** Step goal surfaced by the [NotificationStyle.CUSTOM] progress notification. */
    private const val DAILY_STEPS_GOAL = 8_000

    /**
     * Notification channel used only by the [NotificationStyle.NATIVE] example. The SDK manages
     * its own channel for the [NotificationStyle.DEFAULT] / [NotificationStyle.CUSTOM] styles; for
     * a native notification *you* own the channel, so we create it before building.
     */
    private const val NATIVE_CHANNEL_ID = "onestep_sample_native_monitoring"

    private const val PREFS_NAME = "onestep_sample_prefs"
    private const val KEY_STYLE = "notification_style"

    /** The notification style the user last selected, persisted across app restarts. */
    fun savedStyle(context: Context): NotificationStyle {
        val name = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_STYLE, null)
        return NotificationStyle.entries.firstOrNull { it.name == name } ?: NotificationStyle.DEFAULT
    }

    /**
     * Applies [style] to the SDK's foreground-service notification and remembers the choice. The
     * SDK persists the config and repaints the live notification if monitoring is running.
     *
     * @return [OSTResult.Success] when the SDK accepted the config, otherwise [OSTResult.Error].
     */
    fun apply(
        monitoring: OSTMonitoring,
        context: Context,
        style: NotificationStyle,
    ): OSTResult<Unit> {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_STYLE, style.name)
            .apply()
        return setNotificationConfig(monitoring, context, style)
    }

    private fun setNotificationConfig(
        monitoring: OSTMonitoring,
        context: Context,
        style: NotificationStyle,
    ): OSTResult<Unit> = monitoring.setCustomMonitoringNotification {
        when (style) {
            // Style 1 — DEFAULT: the SDK renders the notification. Title/text are lambdas, which
            // the SDK re-evaluates when it rebuilds the notification in this process. It also
            // saves their current values, which it shows after a headless restart.
            NotificationStyle.DEFAULT -> default(
                icon = NOTIFICATION_ICON,
                title = { "$APP_NAME is active" },
                text = { "OneStep is tracking your movement in the background" },
            )

            // Style 2 — CUSTOM: the SDK renders a richer notification with a live progress bar
            // toward `stepsGoal` and fills in the current step count. `title` replaces the
            // `appName` header and `text` adds one line under it (SDK 2.2.2+). Both are plain
            // strings because the SDK persists them to rebuild the notification after a restart.
            NotificationStyle.CUSTOM -> custom(
                appName = APP_NAME,
                stepsGoal = DAILY_STEPS_GOAL,
                icon = NOTIFICATION_ICON,
                title = "Your daily activity",
                text = "Steps count toward your daily goal",
            )

            // Style 3 — NATIVE: you build the entire android.app.Notification. Use this when you
            // need full control (custom layouts, actions, styles). The `icon` argument is the
            // small icon the foreground service falls back to.
            NotificationStyle.NATIVE -> native(
                notification = buildNativeNotification(context.applicationContext),
                icon = NOTIFICATION_ICON,
            )
        }
    }

    /**
     * Builds a fully custom foreground-service notification for [NotificationStyle.NATIVE].
     *
     * A foreground-service notification must use a channel that already exists, so we create the
     * channel first. minSdk for this sample is 26, where notification channels are always present.
     */
    private fun buildNativeNotification(context: Context): Notification {
        ensureNativeChannel(context)
        return NotificationCompat.Builder(context, NATIVE_CHANNEL_ID)
            .setSmallIcon(NOTIFICATION_ICON)
            .setContentTitle("OneStep is recording 🏃")
            .setContentText("Fully custom notification built by the host app")
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun ensureNativeChannel(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)
        if (manager.getNotificationChannel(NATIVE_CHANNEL_ID) == null) {
            manager.createNotificationChannel(
                NotificationChannel(
                    NATIVE_CHANNEL_ID,
                    "Background Monitoring (Native)",
                    NotificationManager.IMPORTANCE_LOW,
                ).apply {
                    description = "Foreground-service notification built by the sample app"
                },
            )
        }
    }
}
