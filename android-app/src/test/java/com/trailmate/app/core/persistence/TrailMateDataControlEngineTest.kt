package com.trailmate.app.core.persistence

import com.trailmate.app.core.model.GearInventory
import com.trailmate.app.core.model.TrailMateSampleData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TrailMateDataControlEngineTest {
    @Test
    fun summarizesSavedProfileRouteAndGearForExportPreview() {
        val snapshot = TrailMateSnapshot(
            profile = TrailMateSampleData.baselineProfile,
            inventory = GearInventory(TrailMateSampleData.gearItems),
            importedRoute = TrailMateSampleData.importedTargetRoute
        )

        val summary = TrailMateDataControlEngine.summarize(snapshot)

        assertEquals("Profile saved", summary.profileLine)
        assertEquals("Longjing Ridge / 15.2 km / +860 m", summary.routeLine)
        assertEquals("3 items / 3 ready", summary.inventoryLine)
        assertTrue(summary.exportPreview.contains("profile=LOW"))
        assertTrue(summary.exportPreview.contains("route=Longjing Ridge"))
        assertTrue(summary.exportPreview.contains("gear=3"))
    }

    @Test
    fun summarizesEmptySnapshotWithoutFabricatingRouteOrProfile() {
        val summary = TrailMateDataControlEngine.summarize(TrailMateSnapshot.empty())

        assertEquals("No profile saved", summary.profileLine)
        assertEquals("No route imported", summary.routeLine)
        assertEquals("0 items / 0 ready", summary.inventoryLine)
        assertTrue(summary.exportPreview.contains("profile=none"))
        assertTrue(summary.exportPreview.contains("route=none"))
        assertTrue(summary.exportPreview.contains("gear=0"))
    }
}
