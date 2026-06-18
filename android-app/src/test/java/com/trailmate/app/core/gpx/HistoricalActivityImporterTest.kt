package com.trailmate.app.core.gpx

import com.trailmate.app.core.model.TrailMateSampleData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HistoricalActivityImporterTest {
    @Test
    fun importsHistoricalGpxAsActivityEvidence() {
        val state = HistoricalActivityImporter.importText(
            fileName = "morning-ridge.gpx",
            content = TrailMateSampleData.sampleTargetGpx
        )

        val activity = (state as HistoricalActivityImportState.Imported).activity

        assertEquals("Longjing Ridge", activity.routeName)
        assertEquals(15.2, activity.distanceKm, 0.0)
        assertEquals(860, activity.ascentMeters)
        assertTrue(activity.durationMinutes > 0)
    }

    @Test
    fun batchImportKeepsValidActivitiesAndReportsFailures() {
        val batch = HistoricalActivityImporter.importFiles(
            listOf(
                HistoricalActivityImportFile(
                    fileName = "valid-history.gpx",
                    content = TrailMateSampleData.sampleTargetGpx
                ),
                HistoricalActivityImportFile(
                    fileName = "bad-history.gpx",
                    content = "<gpx><trk></trk></gpx>"
                )
            )
        )

        assertEquals(1, batch.activities.size)
        assertEquals(1, batch.failures.size)
        assertEquals("bad-history.gpx", batch.failures.single().fileName)
        assertTrue(batch.summaryCaption().contains("bad-history.gpx"))
    }
}
