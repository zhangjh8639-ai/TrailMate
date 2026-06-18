package com.trailmate.app.core.persistence

import com.trailmate.app.core.model.GearInventory
import com.trailmate.app.core.model.TrailMateSampleData
import org.junit.Assert.assertEquals
import org.junit.Test

class TrailMateDataControlEngineTest {
    @Test
    fun summarizesSavedProfileRouteAndGearForExportPreview() {
        val snapshot = TrailMateSnapshot(
            profile = TrailMateSampleData.baselineProfile,
            inventory = GearInventory(TrailMateSampleData.gearItems),
            importedRoute = TrailMateSampleData.importedTargetRoute,
            historicalActivities = TrailMateSampleData.historicalActivities
        )

        val summary = TrailMateDataControlEngine.summarize(snapshot)

        assertEquals("Profile saved", summary.profileLine)
        assertEquals("Longjing Ridge / 15.2 km / +860 m", summary.routeLine)
        assertEquals("3 items / 3 ready", summary.inventoryLine)
        assertEquals(
            "Profile: saved; Route: Longjing Ridge, 15.2 km, +860 m; History: 3 GPX activities; Gear: 3 items, 3 ready",
            summary.exportPreview
        )
    }

    @Test
    fun summarizesEmptySnapshotWithoutFabricatingRouteOrProfile() {
        val summary = TrailMateDataControlEngine.summarize(TrailMateSnapshot.empty())

        assertEquals("No profile saved", summary.profileLine)
        assertEquals("No route imported", summary.routeLine)
        assertEquals("0 items / 0 ready", summary.inventoryLine)
        assertEquals(
            "Profile: not saved; Route: none; History: 0 GPX activities; Gear: 0 items, 0 ready",
            summary.exportPreview
        )
    }
}
