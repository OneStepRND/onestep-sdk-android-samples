package com.onestep.backgroundmonitoringsample.ui.model

/**
 * The three foreground-service notification styles the OneStep SDK supports, surfaced in the UI so
 * consumers can switch between them at runtime and see the result live. Each entry maps to one
 * builder on the SDK's `OSTNotificationConfigScope` (see `MonitoringNotifications`).
 *
 * Enums are a stable Compose type, so this can be passed straight into composables.
 */
enum class NotificationStyle(
    val label: String,
    val description: String,
) {
    DEFAULT(
        label = "Default",
        description = "SDK-rendered title and text.",
    ),
    CUSTOM(
        label = "Custom",
        description = "SDK-rendered app name with live progress toward a step goal.",
    ),
    NATIVE(
        label = "Native",
        description = "A fully custom android.app.Notification built by this app.",
    ),
}
