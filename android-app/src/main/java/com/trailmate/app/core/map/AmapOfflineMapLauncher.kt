package com.trailmate.app.core.map

import android.app.Activity
import android.content.Context
import android.content.Intent

object AmapOfflineMapLauncher {
    private const val OFFLINE_MAP_ACTIVITY_CLASS = "com.amap.api.maps.offlinemap.OfflineMapActivity"

    fun buildIntent(context: Context): Intent? =
        runCatching {
            Intent(context, Class.forName(OFFLINE_MAP_ACTIVITY_CLASS)).apply {
                if (context !is Activity) {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            }
        }.getOrNull()

    fun isRegistered(context: Context): Boolean {
        val intent = buildIntent(context) ?: return false
        return intent.resolveActivity(context.packageManager) != null
    }

    fun open(context: Context): Boolean {
        val intent = buildIntent(context) ?: return false
        AmapSdkInitializer.initialize(context)
        context.startActivity(intent)
        return true
    }
}
