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
    fun importsHistoricalGpxTimestampRangeAsActivityDuration() {
        val state = HistoricalActivityImporter.importText(
            fileName = "timed-training.gpx",
            content = """
                <gpx version="1.1" creator="TrailMate">
                  <trk>
                    <name>Timed Training Ridge</name>
                    <trkseg>
                      <trkpt lat="30.2500" lon="120.1200">
                        <ele>100</ele>
                        <time>2026-06-18T08:00:00Z</time>
                      </trkpt>
                      <trkpt lat="30.2600" lon="120.1300">
                        <ele>165</ele>
                        <time>2026-06-18T10:45:00Z</time>
                      </trkpt>
                    </trkseg>
                  </trk>
                </gpx>
            """.trimIndent()
        )

        val activity = (state as HistoricalActivityImportState.Imported).activity

        assertEquals("Timed Training Ridge", activity.routeName)
        assertEquals(165, activity.durationMinutes)
    }

    @Test
    fun importsHistoricalGpxFirstAndLastAvailableTimestampsAsDuration() {
        val state = HistoricalActivityImporter.importText(
            fileName = "partial-timed-training.gpx",
            content = """
                <gpx version="1.1" creator="TrailMate">
                  <trk>
                    <name>Partial Timed Ridge</name>
                    <trkseg>
                      <trkpt lat="30.2500" lon="120.1200">
                        <ele>100</ele>
                      </trkpt>
                      <trkpt lat="30.2550" lon="120.1250">
                        <ele>118</ele>
                        <time>2026-06-18T08:30:00Z</time>
                      </trkpt>
                      <trkpt lat="30.2600" lon="120.1300">
                        <ele>132</ele>
                        <time>2026-06-18T09:15:00Z</time>
                      </trkpt>
                    </trkseg>
                  </trk>
                </gpx>
            """.trimIndent()
        )

        val activity = (state as HistoricalActivityImportState.Imported).activity

        assertEquals("Partial Timed Ridge", activity.routeName)
        assertEquals(45, activity.durationMinutes)
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
