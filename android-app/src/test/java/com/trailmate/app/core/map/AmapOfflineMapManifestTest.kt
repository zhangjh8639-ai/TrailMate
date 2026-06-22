package com.trailmate.app.core.map

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class AmapOfflineMapManifestTest {
    @Test
    fun registersOfficialOfflineMapActivity() {
        val manifest = manifestFile().readText()

        assertTrue(manifest.contains("com.amap.api.maps.offlinemap.OfflineMapActivity"))
        assertTrue(manifest.contains("android:screenOrientation=\"portrait\""))
    }

    private fun manifestFile(): File =
        listOf(
            File("src/main/AndroidManifest.xml"),
            File("android-app/src/main/AndroidManifest.xml")
        ).first(File::exists)
}
