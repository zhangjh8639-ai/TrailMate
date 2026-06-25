package com.trailmate.app

import com.trailmate.app.core.map.AmapOfflineMapLauncher
import com.trailmate.app.core.map.AmapOfflineBaseMapStatusReader
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AmapOfflineMapLauncherSmokeTest {
    @Test
    fun opensRegisteredAmapOfflineMapActivityFromAppContext() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val intent = AmapOfflineMapLauncher.buildIntent(context)

        assertTrue(AmapOfflineMapLauncher.isRegistered(context))
        assertNotNull(intent)

        val activity = instrumentation.startActivitySync(intent)
        try {
            assertNotNull(activity)
            assertEquals("com.amap.api.maps.offlinemap.OfflineMapActivity", activity::class.java.name)
        } finally {
            activity?.finish()
        }
    }

    @Test
    fun readsDownloadedOfflineBaseMapStatusFromAppContext() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        val status = AmapOfflineBaseMapStatusReader.readDownloadedStatus(context)

        assertNotNull(status)
        assertTrue(status!!.downloadedRegionCount >= 0)
    }
}
