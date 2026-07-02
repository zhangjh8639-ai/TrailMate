package com.trailmate.app.feature.navigation

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

object TrackingRuntimePermissions {
    fun requiredForCurrentDevice(): Array<String> =
        requiredForSdk(Build.VERSION.SDK_INT).toTypedArray()

    fun requiredForSdk(sdkInt: Int): List<String> =
        buildList {
            add(Manifest.permission.ACCESS_FINE_LOCATION)
            add(Manifest.permission.ACCESS_COARSE_LOCATION)
            if (sdkInt >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

    fun hasForegroundLocation(grants: Map<String, Boolean>): Boolean =
        grants[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            grants[Manifest.permission.ACCESS_COARSE_LOCATION] == true

    fun grantResult(
        grants: Map<String, Boolean>,
        sdkInt: Int = Build.VERSION.SDK_INT,
    ): TrackingPermissionGrantResult =
        TrackingPermissionGrantResult(
            hasForegroundLocation = hasForegroundLocation(grants),
            hasNotificationPermission = sdkInt < Build.VERSION_CODES.TIRAMISU ||
                grants[Manifest.permission.POST_NOTIFICATIONS] == true,
        )

    fun hasForegroundLocationPermission(context: Context): Boolean =
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            ) == PackageManager.PERMISSION_GRANTED

    fun hasRequiredStartPermissions(context: Context): Boolean =
        hasForegroundLocationPermission(context) && hasNotificationPermissionIfNeeded(context)

    private fun hasNotificationPermissionIfNeeded(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
}

data class TrackingPermissionGrantResult(
    val hasForegroundLocation: Boolean,
    val hasNotificationPermission: Boolean,
) {
    val canStartTracking: Boolean = hasForegroundLocation && hasNotificationPermission
}
