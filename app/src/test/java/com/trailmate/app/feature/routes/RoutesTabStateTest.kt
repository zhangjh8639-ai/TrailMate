package com.trailmate.app.feature.routes

import com.trailmate.app.core.routeimport.RouteImportParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RoutesTabStateTest {
    @Test
    fun defaultRouteTabDoesNotShowFakeSuccessfulImport() {
        val state = RoutesTabSampleState.build()

        assertEquals(RouteImportFlowStatus.Idle, state.importFlowStatus)
        assertNull(state.importPreview)
        assertTrue(state.visibleText().joinToString("\n").contains("选择 GPX/KML 文件后显示解析结果"))
        assertTrue(state.visibleText().joinToString("\n").contains("不包含商业地图底图"))
        assertFalse(state.visibleText().joinToString("\n").contains("longjing-loop.gpx"))
    }

    @Test
    fun importingAndCancelledStatesUseExplicitChineseCopy() {
        val importing = RoutesTabSampleState.build().withImporting()
        val cancelled = RoutesTabSampleState.build().withImportCancelled()

        assertEquals(RouteImportFlowStatus.Importing, importing.importFlowStatus)
        assertTrue(importing.visibleText().contains("正在解析路线文件"))
        assertNull(importing.importPreview)

        assertEquals(RouteImportFlowStatus.Cancelled, cancelled.importFlowStatus)
        assertTrue(cancelled.visibleText().contains("已取消导入"))
        assertNull(cancelled.importPreview)
    }

    @Test
    fun selectedImportResultShowsRealFileMetrics() {
        val result = RouteImportParser.parse(
            fileName = "my-route.gpx",
            content = successfulGpx(),
        )

        val state = RoutesTabSampleState.build().withImportResult(result)
        val preview = requireNotNull(state.importPreview)

        assertEquals(RouteImportFlowStatus.PreviewReady, state.importFlowStatus)
        assertEquals("my-route.gpx", preview.fileName)
        assertEquals("解析完成", preview.statusLabel)
        assertEquals("测试路线", preview.routeName)
        assertEquals("2", preview.trackPointCountLabel)
        assertEquals(
            "导入文件只包含路线轨迹和航点，用于轨迹导航、偏航判断和进度计算，不包含商业地图底图。",
            preview.routeOnlyCopy,
        )
        assertTrue(preview.qualityNotes.contains("未保存，仅本次查看"))
        assertTrue(preview.canUseRouteActions)
        assertFalse(state.assets.any { it.name == "测试路线" && it.sourceLabel == "GPX 导入" })
    }

    @Test
    fun failedReadShowsFailureWithoutCreatingImportedAsset() {
        val state = RoutesTabSampleState.build().withImportReadFailure(
            fileName = "large-route.gpx",
            reason = "文件过大，暂不支持直接导入",
        )

        val preview = requireNotNull(state.importPreview)

        assertEquals(RouteImportFlowStatus.Failed, state.importFlowStatus)
        assertEquals("large-route.gpx", preview.fileName)
        assertEquals("导入失败", preview.statusLabel)
        assertEquals("不可用", preview.distanceLabel)
        assertEquals("不可用", preview.elevationGainLabel)
        assertTrue(preview.qualityNotes.contains("文件过大，暂不支持直接导入"))
        assertFalse(preview.canUseRouteActions)
        assertFalse(state.visibleText().contains("保存到路线"))
        assertFalse(state.visibleText().contains("开始轨迹导航"))
        assertTrue(state.visibleText().contains("重新选择文件"))
        assertFalse(state.assets.any { it.name == preview.routeName && it.sourceLabel == "GPX 导入" })
    }

    @Test
    fun routeTabFiltersMatchRouteAssetCenter() {
        val state = RoutesTabSampleState.build()

        assertEquals(listOf("全部", "已离线", "已导入", "收藏", "最近"), state.filters.map { it.label })
    }

    @Test
    fun parsedImportPreviewDoesNotSilentlyCreateRouteAsset() {
        val initial = RoutesTabSampleState.build()
        val imported = initial.withImportResult(
            RouteImportParser.parse(
                fileName = "my-route.gpx",
                content = successfulGpx(),
            ),
        )

        assertFalse(initial.assets.any { it.sourceLabel == "GPX 导入" })
        assertFalse(imported.assets.any { it.sourceLabel == "GPX 导入" && it.offlineStatusLabel == "仅轨迹可用" })
        assertTrue(imported.assets.any { it.sourceLabel == "平台路线" && it.offlineStatusLabel == "可离线导航" })
    }

    @Test
    fun rejectedImportPreviewShowsFailureWithoutCrashingTheRouteTab() {
        val rejected = RouteImportParser.parse(
            fileName = "broken.gpx",
            content = "not xml",
        )

        val state = RoutesTabSampleState.build().withImportResult(rejected)
        val preview = requireNotNull(state.importPreview)

        assertEquals(RouteImportFlowStatus.Failed, state.importFlowStatus)
        assertEquals("文件解析失败", preview.statusLabel)
        assertEquals("不可用", preview.distanceLabel)
        assertEquals("不可用", preview.elevationGainLabel)
        assertTrue(preview.qualityNotes.contains("文件解析失败"))
        assertFalse(preview.canUseRouteActions)
    }

    @Test
    fun visibleTextDoesNotRestoreDeprecatedSurfaces() {
        val base = RoutesTabSampleState.build()
        val states = listOf(
            base,
            base.withImporting(),
            base.withImportCancelled(),
            base.withImportResult(RouteImportParser.parse("my-route.gpx", successfulGpx())),
            base.withImportReadFailure("broken.gpx", "文件解析失败"),
        )
        val visibleText = states.flatMap { it.visibleText() }.joinToString(separator = "\n")

        listOf("规划", "装备", "社区", "商城", "出发前检查", "完成检查后开始").forEach { legacySurface ->
            assertFalse("Route tab must not expose $legacySurface", visibleText.contains(legacySurface))
        }
    }

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
}
