package com.trailmate.app.core.gpx

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TargetRouteImporterTest {
    @Test
    fun importsValidGpxText() {
        val state = TargetRouteImporter.importText(
            fileName = "longjing.gpx",
            content = """
                <gpx version="1.1">
                  <trk>
                    <name>Longjing Ridge</name>
                    <trkseg>
                      <trkpt lat="30.250000" lon="120.120000"><ele>100</ele></trkpt>
                      <trkpt lat="30.317000" lon="120.120000"><ele>420</ele></trkpt>
                    </trkseg>
                  </trk>
                </gpx>
            """.trimIndent()
        )

        assertTrue(state is TargetRouteImportState.Imported)
        state as TargetRouteImportState.Imported
        assertEquals("Longjing Ridge", state.route.routeName)
        assertEquals("longjing.gpx", state.route.fileName)
    }

    @Test
    fun returnsFailedStateForInvalidGpx() {
        val state = TargetRouteImporter.importText(
            fileName = "empty.gpx",
            content = "<gpx version=\"1.1\" />"
        )

        assertTrue(state is TargetRouteImportState.Failed)
        state as TargetRouteImportState.Failed
        assertEquals("empty.gpx", state.fileName)
        assertTrue(state.message.contains("two track or route points"))
    }
}
