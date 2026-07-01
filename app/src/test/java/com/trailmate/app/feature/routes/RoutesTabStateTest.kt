package com.trailmate.app.feature.routes

import com.trailmate.app.core.routeimport.RouteImportParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RoutesTabStateTest {
    @Test
    fun sampleStateBuildsImportPreviewFromParserOutput() {
        val state = RoutesTabSampleState.build()
        val preview = requireNotNull(state.importPreview)

        assertEquals("longjing-loop.gpx", preview.fileName)
        assertEquals("解析完成", preview.statusLabel)
        assertEquals("8.6 km", preview.distanceLabel)
        assertEquals("+430 m", preview.elevationGainLabel)
        assertEquals("2", preview.waypointCountLabel)
        assertEquals("6", preview.trackPointCountLabel)
        assertTrue(preview.hasElevation)
        assertTrue(preview.qualityNotes.contains("包含海拔数据"))
        assertEquals(
            "导入文件只包含路线轨迹和航点，用于轨迹导航、偏航判断和进度计算，不包含商业地图底图。",
            preview.routeOnlyCopy,
        )
    }

    @Test
    fun routeTabFiltersMatchRouteAssetCenter() {
        val state = RoutesTabSampleState.build()

        assertEquals(listOf("全部", "已离线", "已导入", "收藏", "最近"), state.filters.map { it.label })
    }

    @Test
    fun routeCardsDistinguishOfflineAndImportedReadiness() {
        val state = RoutesTabSampleState.build()

        assertTrue(state.assets.any { it.sourceLabel == "GPX 导入" && it.offlineStatusLabel == "仅轨迹可用" })
        assertTrue(state.assets.any { it.sourceLabel == "平台路线" && it.offlineStatusLabel == "可离线导航" })
    }

    @Test
    fun rejectedImportPreviewShowsFailureWithoutCrashingTheRouteTab() {
        val rejected = RouteImportParser.parse(
            fileName = "broken.gpx",
            content = "not xml",
        )

        val preview = RoutesTabSampleState.previewFromImportResult(rejected)

        assertEquals("文件解析失败", preview.statusLabel)
        assertEquals("不可用", preview.distanceLabel)
        assertEquals("不可用", preview.elevationGainLabel)
        assertTrue(preview.qualityNotes.contains("文件解析失败"))
    }

    @Test
    fun visibleTextDoesNotRestoreDeprecatedSurfaces() {
        val visibleText = RoutesTabSampleState.build().visibleText().joinToString(separator = "\n")

        listOf("规划", "装备", "社区", "商城").forEach { legacySurface ->
            assertFalse("Route tab must not expose $legacySurface", visibleText.contains(legacySurface))
        }
    }
}
