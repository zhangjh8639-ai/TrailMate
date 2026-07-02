package com.trailmate.app.feature.routes

import com.trailmate.app.core.database.ImportedRouteGeometryRecords
import com.trailmate.app.core.model.PrivacyVisibility
import com.trailmate.app.core.model.RouteConfidence
import com.trailmate.app.core.model.RouteOfflineStatus
import com.trailmate.app.core.model.RouteSourceType
import com.trailmate.app.core.routeimport.RouteImportParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.roundToInt

class ImportedRoutePersistenceMapperTest {
    @Test
    fun parsedImportMapsToPrivateTrackOnlyPersistentRecord() {
        val parsed = RouteImportParser.parse("longjing-loop.gpx", successfulGpx())
        val saveable = requireNotNull(RoutesTabSampleState.saveableImportFromResult(parsed))

        val record = saveable.toPersistentImportedRouteRecord(
            importedAtEpochMillis = ImportedAtEpochMillis,
        )

        assertEquals(saveable.key, record.id)
        assertEquals("longjing-loop.gpx", record.fileName)
        assertEquals(RouteSourceType.ImportedGpx, record.sourceType)
        assertEquals("测试路线", record.routeName)
        assertEquals(2, record.trackPointCount)
        assertEquals(1, record.waypointCount)
        assertTrue(record.hasElevation)
        assertEquals(PrivacyVisibility.Private, record.visibility)
        assertEquals(RouteOfflineStatus.TrackOnly, record.offlineStatus)
        assertEquals(RouteConfidence.Unverified, record.confidence)
        assertEquals(ImportedAtEpochMillis, record.importedAtEpochMillis)
        assertEquals(2, record.points.size)
        assertEquals(0.0, record.points.first().cumulativeDistanceMeters, 0.0)
    }

    @Test
    fun importedRouteIdentityIncludesGeometryToAvoidOverwritingDifferentFiles() {
        val first = requireNotNull(
            RoutesTabSampleState.saveableImportFromResult(
                RouteImportParser.parse("same-name.gpx", successfulGpx()),
            ),
        )
        val second = requireNotNull(
            RoutesTabSampleState.saveableImportFromResult(
                RouteImportParser.parse("same-name.gpx", shiftedSuccessfulGpx()),
            ),
        )

        assertEquals(first.fileName, second.fileName)
        assertEquals(first.routeName, second.routeName)
        assertEquals(first.trackPointCount, second.trackPointCount)
        assertEquals(first.geometry.totalDistance.meters.roundToInt(), second.geometry.totalDistance.meters.roundToInt())
        assertFalse(first.key == second.key)
    }

    @Test
    fun persistedImportMapsToBoundedRouteAssetCard() {
        val record = persistentRecordFromGpx()

        val asset = record.toRouteAssetCardState()

        assertEquals("测试路线", asset.name)
        assertEquals("导入路线", asset.region)
        assertEquals("GPX 导入", asset.sourceLabel)
        assertEquals("仅轨迹可用", asset.offlineStatusLabel)
        assertEquals("待确认", asset.estimatedDurationLabel)
        assertEquals("未验证", asset.difficultyLabel)
        assertEquals("可信度待确认", asset.confidenceLabel)
        assertNull(asset.startActionLabel)
        assertNull(asset.detailActionLabel)
        assertTrue(asset.riskTags.contains("导入轨迹"))
        assertTrue(asset.riskTags.contains("未验证"))
        assertTrue(asset.riskTags.contains("不含地图底图"))
    }

    @Test
    fun persistedImportsMergeBeforeSampleRoutesAndDeduplicateByIdentity() {
        val record = persistentRecordFromGpx()
        val duplicate = record.copy(routeName = "测试路线重命名")
        val base = RoutesTabSampleState.build()

        val state = base.withPersistedImportedRoutes(listOf(record, duplicate))

        assertEquals("测试路线重命名", state.assets.first().name)
        assertEquals(1, state.assets.count { it.identityKey == record.id })
        assertEquals("平台路线", state.assets.drop(1).first().sourceLabel)
        assertFalse(state.visibleText().contains("我的装备"))
        assertFalse(state.visibleText().contains("规划"))
    }

    @Test
    fun staleSaveCompletionDoesNotMarkCurrentDifferentPreviewAsSaved() {
        val firstRecord = persistentRecordFromGpx()
        val currentState = RoutesTabSampleState.build().withImportResult(
            RouteImportParser.parse("other-route.gpx", shiftedSuccessfulGpx()),
        )

        val state = currentState.withSavedImport(firstRecord)

        val currentPreview = requireNotNull(state.importPreview)
        assertEquals("other-route.gpx", currentPreview.fileName)
        assertTrue(currentPreview.qualityNotes.contains("未保存，仅本次查看"))
        assertFalse(currentPreview.qualityNotes.contains("本次已加入路线列表"))
        assertEquals(firstRecord.routeName, state.assets.first().name)
    }

    @Test
    fun geometryPointRecordsRoundTripNavigationInputs() {
        val geometry = requireNotNull(RouteImportParser.parse("longjing-loop.gpx", successfulGpx()).geometry)

        val points = ImportedRouteGeometryRecords.fromGeometry(geometry)
        val restored = ImportedRouteGeometryRecords.toGeometry(points)

        assertEquals(geometry.coordinates, restored.coordinates)
        assertEquals(geometry.cumulativeDistances, restored.cumulativeDistances)
        assertEquals(geometry.totalDistance, restored.totalDistance)
        assertEquals(geometry.elevationGain, restored.elevationGain)
    }

    private fun persistentRecordFromGpx() =
        requireNotNull(
            RoutesTabSampleState.saveableImportFromResult(
                RouteImportParser.parse("longjing-loop.gpx", successfulGpx()),
            ),
        ).toPersistentImportedRouteRecord(
            importedAtEpochMillis = ImportedAtEpochMillis,
        )

    private fun successfulGpx(): String =
        """
        <gpx version="1.1" creator="TrailMate">
          <metadata><name>测试路线</name></metadata>
          <wpt lat="30.0000" lon="120.0000"><name>起点</name></wpt>
          <trk>
            <name>测试路线</name>
            <trkseg>
              <trkpt lat="30.0000" lon="120.0000"><ele>10</ele></trkpt>
              <trkpt lat="30.0010" lon="120.0010"><ele>30</ele></trkpt>
            </trkseg>
          </trk>
        </gpx>
        """.trimIndent()

    private fun shiftedSuccessfulGpx(): String =
        """
        <gpx version="1.1" creator="TrailMate">
          <metadata><name>测试路线</name></metadata>
          <wpt lat="30.0005" lon="120.0005"><name>起点</name></wpt>
          <trk>
            <name>测试路线</name>
            <trkseg>
              <trkpt lat="30.0005" lon="120.0005"><ele>10</ele></trkpt>
              <trkpt lat="30.0015" lon="120.0015"><ele>30</ele></trkpt>
            </trkseg>
          </trk>
        </gpx>
        """.trimIndent()

    private companion object {
        const val ImportedAtEpochMillis = 1_783_036_800_000L
    }
}
