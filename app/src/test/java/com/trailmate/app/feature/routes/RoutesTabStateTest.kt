package com.trailmate.app.feature.routes

import com.trailmate.app.core.model.PrivacyVisibility
import com.trailmate.app.core.model.RouteConfidence
import com.trailmate.app.core.model.RouteId
import com.trailmate.app.core.model.RouteOfflineStatus
import com.trailmate.app.core.routeimport.RouteImportParser
import java.time.Instant
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
        assertNull(preview.detailActionLabel)
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
    fun parsedImportCanBeSavedAsPrivateTrackOnlyAsset() {
        val parsed = RouteImportParser.parse("saved-route.gpx", successfulGpx())
        val imported = parsed.toImportedRoute(
            id = RouteId("import-test"),
            region = "导入路线",
            importedAt = Instant.parse("2026-07-01T00:00:00Z"),
        )

        assertEquals(PrivacyVisibility.Private, imported.visibility)
        assertEquals(RouteOfflineStatus.TrackOnly, imported.offlineStatus)
        assertEquals(RouteConfidence.Unverified, imported.confidence)

        val state = RoutesTabSampleState.build()
            .withImportResult(parsed)
            .withSavedImport()

        val saved = state.assets.first()
        assertEquals("测试路线", saved.name)
        assertEquals("导入路线", saved.region)
        assertEquals("GPX 导入", saved.sourceLabel)
        assertEquals("仅轨迹可用", saved.offlineStatusLabel)
        assertEquals("待确认", saved.estimatedDurationLabel)
        assertEquals("未验证", saved.difficultyLabel)
        assertEquals("可信度待确认", saved.confidenceLabel)
        assertNull(saved.startActionLabel)
        assertEquals("查看详情", saved.detailActionLabel)
        assertTrue(saved.riskTags.contains("导入轨迹"))
        assertTrue(saved.riskTags.contains("未验证"))
        assertTrue(saved.riskTags.contains("不含地图底图"))
        assertTrue(requireNotNull(state.importPreview).qualityNotes.contains("本次已加入路线列表"))
        assertFalse(requireNotNull(state.importPreview).qualityNotes.contains("未保存，仅本次查看"))
    }

    @Test
    fun parsedKmlImportCanBeSavedWithKmlSourceLabel() {
        val parsed = RouteImportParser.parse("saved-route.kml", successfulKml())

        val state = RoutesTabSampleState.build()
            .withImportResult(parsed)
            .withSavedImport()

        val saved = state.assets.first()
        assertEquals("测试 KML 路线", saved.name)
        assertEquals("KML 导入", saved.sourceLabel)
        assertEquals("仅轨迹可用", saved.offlineStatusLabel)
        assertTrue(saved.riskTags.contains("不含地图底图"))
    }

    @Test
    fun parsedImportPreviewShowsTrackOnlyUnverifiedBoundary() {
        val state = RoutesTabSampleState.build().withImportResult(
            RouteImportParser.parse("my-route.gpx", successfulGpx()),
        )
        val preview = requireNotNull(state.importPreview)

        assertTrue(preview.qualityNotes.contains("仅轨迹可用"))
        assertTrue(preview.qualityNotes.contains("未验证"))
        assertTrue(preview.qualityNotes.contains("未保存，仅本次查看"))
    }

    @Test
    fun savingSameImportTwiceDoesNotDuplicateAsset() {
        val parsed = RouteImportParser.parse("saved-route.gpx", successfulGpx())
        val state = RoutesTabSampleState.build()
            .withImportResult(parsed)
            .withSavedImport()
            .withSavedImport()

        assertEquals(1, state.assets.count { it.sourceLabel == "GPX 导入" && it.name == "测试路线" })
        assertEquals("测试路线", state.assets.first().name)
    }

    @Test
    fun failedImportCannotBeSaved() {
        val state = RoutesTabSampleState.build()
            .withImportReadFailure("broken.gpx", "文件解析失败")
            .withSavedImport()

        assertFalse(state.assets.any { it.sourceLabel == "GPX 导入" || it.sourceLabel == "KML 导入" })
        assertFalse(state.visibleText().contains("保存到路线"))
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
    fun platformAssetCanOpenReadOnlyRouteDetail() {
        val state = RoutesTabSampleState.build()
        val platformRoute = state.assets.first { it.sourceLabel == "平台路线" }

        val opened = state.withRouteDetailOpened(platformRoute)
        val detail = requireNotNull(opened.routeDetail)
        val visibleText = detail.visibleText()

        assertEquals(platformRoute.name, detail.title)
        assertEquals(platformRoute.routeKey, detail.routeKey)
        assertEquals("返回路线", detail.backActionLabel)
        assertEquals("选择为导航路线", detail.navigationActionLabel)
        assertNull(detail.startActionLabel)
        assertTrue(visibleText.contains("平台路线"))
        assertTrue(visibleText.contains("可离线导航"))
        assertTrue(visibleText.contains("可信度 A"))
        assertTrue(visibleText.contains("8.6 km"))
        assertTrue(visibleText.contains("+430 m"))
        assertTrue(visibleText.contains("雨后湿滑"))
        assertTrue(visibleText.contains("路线详情"))
    }

    @Test
    fun routeDetailCanBeRecoveredByStableRouteKey() {
        val state = RoutesTabSampleState.build()
        val platformRoute = state.assets.first { it.sourceLabel == "平台路线" }

        val recovered = requireNotNull(state.routeDetailForNavigationKey(platformRoute.routeKey))

        assertEquals(platformRoute.routeKey, recovered.routeKey)
        assertEquals(platformRoute.name, recovered.title)
        assertEquals(platformRoute.sourceLabel, recovered.sourceLabel)
    }

    @Test
    fun savedImportedAssetDetailShowsPrivateTrackOnlyBoundaries() {
        val state = RoutesTabSampleState.build()
            .withImportResult(RouteImportParser.parse("saved-route.gpx", successfulGpx()))
            .withSavedImport()
        val importedRoute = state.assets.first { it.sourceLabel == "GPX 导入" }

        val detail = requireNotNull(state.withRouteDetailOpened(importedRoute).routeDetail)
        val visibleText = detail.visibleText().joinToString("\n")

        assertEquals("查看详情", importedRoute.detailActionLabel)
        assertEquals(importedRoute.routeKey, detail.routeKey)
        assertNull(detail.startActionLabel)
        assertTrue(visibleText.contains("本机私密"))
        assertTrue(visibleText.contains("仅轨迹可用"))
        assertTrue(visibleText.contains("未验证"))
        assertTrue(visibleText.contains("可信度待确认"))
        assertTrue(visibleText.contains("导入文件只包含路线轨迹和航点"))
        assertTrue(visibleText.contains("不包含商业地图底图"))
        assertFalse(visibleText.contains("开始导航"))
    }

    @Test
    fun closingRouteDetailPreservesImportPreviewAndAssets() {
        val withPreview = RoutesTabSampleState.build().withImportResult(
            RouteImportParser.parse("preview-route.gpx", successfulGpx()),
        )
        val opened = withPreview.withRouteDetailOpened(withPreview.assets.first())

        val closed = opened.withRouteDetailClosed()

        assertEquals(withPreview.importPreview, closed.importPreview)
        assertEquals(withPreview.assets, closed.assets)
        assertNull(closed.routeDetail)
    }

    @Test
    fun routeTabDoesNotExposeFakeStartNavigationBeforeNavigationIsImplemented() {
        val base = RoutesTabSampleState.build()
        val imported = base.withImportResult(RouteImportParser.parse("preview-route.gpx", successfulGpx()))
        val visibleText = listOf(base, imported).flatMap { it.visibleText() }.joinToString("\n")

        assertFalse(visibleText.contains("开始导航"))
        assertFalse(visibleText.contains("开始轨迹导航"))
    }

    @Test
    fun importPreviewDoesNotExposeStaticDetailActionBeforeSavedAsRouteAsset() {
        val state = RoutesTabSampleState.build().withImportResult(
            RouteImportParser.parse("preview-route.gpx", successfulGpx()),
        )
        val preview = requireNotNull(state.importPreview)

        assertNull(preview.detailActionLabel)
        assertFalse(preview.visibleText().contains("查看详情"))
        assertTrue(preview.visibleText().contains("保存到路线"))
    }

    @Test
    fun visibleTextDoesNotRestoreDeprecatedSurfaces() {
        val base = RoutesTabSampleState.build()
        val states = listOf(
            base,
            base.withImporting(),
            base.withImportCancelled(),
            base.withImportResult(RouteImportParser.parse("my-route.gpx", successfulGpx())),
            base.withImportResult(RouteImportParser.parse("my-route.gpx", successfulGpx())).withSavedImport(),
            base.withImportReadFailure("broken.gpx", "文件解析失败"),
            base.withRouteDetailOpened(base.assets.first()),
            base.withImportResult(RouteImportParser.parse("my-route.gpx", successfulGpx()))
                .withSavedImport()
                .let { it.withRouteDetailOpened(it.assets.first()) },
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

    private fun successfulKml(): String =
        """
        <kml xmlns="http://www.opengis.net/kml/2.2">
          <Document>
            <name>测试 KML 路线</name>
            <Placemark>
              <name>测试 KML 路线</name>
              <LineString>
                <coordinates>
                  120.0000,30.0000,10
                  120.0010,30.0010,30
                </coordinates>
              </LineString>
            </Placemark>
          </Document>
        </kml>
        """.trimIndent()
}
