package com.trailmate.app.services.tracking

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TrackingServiceManifestTest {
    @Test
    fun manifestDeclaresForegroundTrackingService() {
        val manifestText = manifestFile().readText()

        assertTrue(manifestText.contains("android.permission.FOREGROUND_SERVICE"))
        assertTrue(manifestText.contains("android.permission.FOREGROUND_SERVICE_LOCATION"))
        assertTrue(manifestText.contains("android.permission.POST_NOTIFICATIONS"))
        assertTrue(manifestText.contains(".services.tracking.TrackingForegroundService"))
        assertTrue(manifestText.contains("android:foregroundServiceType=\"location\""))
    }

    @Test
    fun manifestDoesNotDeclareBackgroundLocation() {
        val manifestText = manifestFile().readText()

        assertFalse(manifestText.contains("android.permission.ACCESS_BACKGROUND_LOCATION"))
    }

    private fun manifestFile(): File {
        val moduleRelative = File("src/main/AndroidManifest.xml")
        if (moduleRelative.exists()) return moduleRelative

        return File("app/src/main/AndroidManifest.xml")
    }
}
