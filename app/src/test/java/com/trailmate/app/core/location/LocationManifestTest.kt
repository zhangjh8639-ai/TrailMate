package com.trailmate.app.core.location

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LocationManifestTest {
    @Test
    fun manifestDeclaresForegroundLocationOnly() {
        val manifestText = manifestFile().readText()

        assertTrue(manifestText.contains("android.permission.ACCESS_FINE_LOCATION"))
        assertTrue(manifestText.contains("android.permission.ACCESS_COARSE_LOCATION"))
        assertFalse(manifestText.contains("android.permission.ACCESS_BACKGROUND_LOCATION"))
    }

    private fun manifestFile(): File {
        val moduleRelative = File("src/main/AndroidManifest.xml")
        if (moduleRelative.exists()) return moduleRelative

        return File("app/src/main/AndroidManifest.xml")
    }
}
