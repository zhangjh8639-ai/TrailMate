package com.trailmate.app.feature.navigation

import com.trailmate.app.core.database.TrackingSessionRecord
import com.trailmate.app.core.database.TrackingTrackPointRecord
import com.trailmate.app.core.model.GeoCoordinate
import com.trailmate.app.core.model.GpsAccuracy
import com.trailmate.app.core.model.NavigationDirection
import com.trailmate.app.core.model.NavigationSessionId
import com.trailmate.app.core.model.NavigationState
import com.trailmate.app.core.model.RouteId
import com.trailmate.app.core.routeimport.RouteImportParser
import com.trailmate.app.feature.routes.RoutesTabSampleState
import com.trailmate.app.feature.routes.withImportResult
import com.trailmate.app.feature.routes.withRouteDetailOpened
import com.trailmate.app.feature.routes.withSavedImport
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NavigationTabStateTest {
    @Test
    fun idleNavigationStateExplainsNoRouteSelectedWithoutDeprecatedSurfaces() {
        val state = NavigationTabSampleState.build()
        val visibleText = state.visibleText().joinToString("\n")

        assertNull(state.selectedRoute)
        assertTrue(visibleText.contains("导航"))
        assertTrue(visibleText.contains("尚未选择路线"))
        assertTrue(visibleText.contains("从路线页选择可导航路线"))
        assertFalse(visibleText.contains("开始导航"))
        assertNoDeprecatedSurfaces(visibleText)
    }

    @Test
    fun platformRouteDetailCanBeSelectedForNavigationReadiness() {
        val routes = RoutesTabSampleState.build()
        val platformRoute = routes.assets.first { it.sourceLabel == "平台路线" }
        val detail = requireNotNull(routes.withRouteDetailOpened(platformRoute).routeDetail)

        val navigation = NavigationTabSampleState.build().withSelectedRoute(detail)
        val selected = requireNotNull(navigation.selectedRoute)
        val visibleText = navigation.visibleText().joinToString("\n")

        assertEquals("选择为导航路线", detail.navigationActionLabel)
        assertEquals("轨迹导航待开始", selected.statusLabel)
        assertEquals(platformRoute.routeKey, detail.routeKey)
        assertEquals(platformRoute.routeKey, selected.routeKey)
        assertEquals(platformRoute.name, selected.routeName)
        assertTrue(visibleText.contains("平台路线"))
        assertTrue(visibleText.contains("可离线导航"))
        assertTrue(visibleText.contains("8.6 km"))
        assertTrue(visibleText.contains("+430 m"))
        assertTrue(visibleText.contains("可信度 A"))
        assertTrue(visibleText.contains("雨后湿滑"))
        assertTrue(visibleText.contains("准备开始轨迹导航"))
        assertTrue(visibleText.contains("开始轨迹导航"))
        assertTrue(visibleText.contains("定位权限"))
        assertTrue(visibleText.contains("更换路线"))
        assertFalse(visibleText.contains("已 GPS 匹配"))
    }

    @Test
    fun importedRouteReadinessKeepsPrivateTrackOnlyNoBasemapBoundaries() {
        val routes = RoutesTabSampleState.build()
            .withImportResult(RouteImportParser.parse("saved-route.gpx", successfulGpx()))
            .withSavedImport()
        val importedRoute = routes.assets.first { it.sourceLabel == "GPX 导入" }
        val detail = requireNotNull(routes.withRouteDetailOpened(importedRoute).routeDetail)

        val navigation = NavigationTabSampleState.build().withSelectedRoute(detail)
        val selected = requireNotNull(navigation.selectedRoute)
        val visibleText = navigation.visibleText().joinToString("\n")

        assertEquals("轨迹导航待开始", selected.statusLabel)
        assertEquals(importedRoute.routeKey, detail.routeKey)
        assertEquals(importedRoute.routeKey, selected.routeKey)
        assertTrue(visibleText.contains("本机私密"))
        assertTrue(visibleText.contains("仅轨迹可用"))
        assertTrue(visibleText.contains("未验证"))
        assertTrue(visibleText.contains("可信度待确认"))
        assertTrue(visibleText.contains("导入文件只包含路线轨迹和航点"))
        assertTrue(visibleText.contains("不包含商业地图底图"))
        assertTrue(visibleText.contains("更换路线"))
        assertTrue(visibleText.contains("开始轨迹导航"))
        assertFalse(visibleText.contains("模拟轨迹"))
        assertNoDeprecatedSurfaces(visibleText)
    }

    @Test
    fun permissionDeniedCopyKeepsRouteVisibleWithoutFakeNavigation() {
        val routes = RoutesTabSampleState.build()
        val platformRoute = routes.assets.first { it.sourceLabel == "平台路线" }
        val detail = requireNotNull(routes.withRouteDetailOpened(platformRoute).routeDetail)

        val navigation = NavigationTabSampleState.build()
            .withSelectedRoute(detail)
            .withTrackingStartState(TrackingStartUiState.permissionDenied())
        val visibleText = navigation.visibleText().joinToString("\n")

        assertTrue(visibleText.contains(platformRoute.name))
        assertTrue(visibleText.contains("定位权限未开启"))
        assertTrue(visibleText.contains("你仍可以查看路线信息"))
        assertFalse(visibleText.contains("已 GPS 匹配"))
        assertFalse(visibleText.contains("模拟轨迹"))
        assertNoDeprecatedSurfaces(visibleText)
    }

    @Test
    fun activeTrackingStateDoesNotOfferRouteChangeAction() {
        val routes = RoutesTabSampleState.build()
        val platformRoute = routes.assets.first { it.sourceLabel == "平台路线" }
        val detail = requireNotNull(routes.withRouteDetailOpened(platformRoute).routeDetail)

        val navigation = NavigationTabSampleState.build()
            .withSelectedRoute(detail)
            .withTrackingStartState(TrackingStartUiState.active())
        val visibleText = navigation.visibleText().joinToString("\n")

        assertFalse(visibleText.contains("更换路线"))
        assertTrue(visibleText.contains("前台导航服务运行中"))
    }

    @Test
    fun recoveredLocalSessionCopyIsPrivateAndDoesNotClaimLiveGpsOrRescue() {
        val recoveredSession = NavigationRecoveredTrackingSessionState.from(
            record = unfinishedSessionRecord(sampleCount = 2),
            points = listOf(trackPointAt(index = 0, epochMillis = 1_788_000_003_000)),
        )

        val navigation = NavigationTabSampleState.build()
            .withRecoveredTrackingSession(recoveredSession)
        val visibleText = navigation.visibleText().joinToString("\n")

        assertTrue(visibleText.contains("发现未结束的本地记录"))
        assertTrue(visibleText.contains("本机私密"))
        assertTrue(visibleText.contains("已记录 2 个定位点"))
        assertTrue(visibleText.contains("结束本地记录"))
        assertTrue(visibleText.contains("不会自动上传或分享"))
        assertFalse(visibleText.contains("前台导航服务运行中"))
        assertFalse(visibleText.contains("已恢复导航"))
        assertFalse(visibleText.contains("已恢复前台服务"))
        assertFalse(visibleText.contains("自动救援"))
        assertFalse(visibleText.contains("公开分享"))
        assertNoDeprecatedSurfaces(visibleText)
    }

    @Test
    fun activeTrackingStateSuppressesRecoveredSessionCopy() {
        val recoveredSession = NavigationRecoveredTrackingSessionState.from(
            record = unfinishedSessionRecord(sampleCount = 1),
            points = emptyList(),
        )

        val navigation = NavigationTabSampleState.build()
            .withRecoveredTrackingSession(recoveredSession)
            .withTrackingStartState(TrackingStartUiState.active())
        val visibleText = navigation.visibleText().joinToString("\n")

        assertFalse(visibleText.contains("发现未结束的本地记录"))
        assertFalse(visibleText.contains("结束本地记录"))
    }

    @Test
    fun recoveredLocalSessionBlocksStartingAnotherTrackingSession() {
        val routes = RoutesTabSampleState.build()
        val platformRoute = routes.assets.first { it.sourceLabel == "平台路线" }
        val detail = requireNotNull(routes.withRouteDetailOpened(platformRoute).routeDetail)
        val recoveredSession = NavigationRecoveredTrackingSessionState.from(
            record = unfinishedSessionRecord(sampleCount = 1),
            points = emptyList(),
        )

        val navigation = NavigationTabSampleState.build()
            .withSelectedRoute(detail)
            .withRecoveredTrackingSession(recoveredSession)
        val visibleText = navigation.visibleText().joinToString("\n")

        assertFalse(navigation.canStartNewTracking())
        assertTrue(visibleText.contains("发现未结束的本地记录"))
        assertTrue(visibleText.contains("请先结束本地记录，再开始新的轨迹导航"))
        assertTrue(visibleText.contains("结束本地记录"))
        assertFalse(visibleText.contains("开始轨迹导航"))
    }

    private fun assertNoDeprecatedSurfaces(visibleText: String) {
        listOf("规划", "装备", "社区", "商城", "出发前检查", "完成检查后开始").forEach { deprecated ->
            assertFalse("Navigation handoff must not expose $deprecated", visibleText.contains(deprecated))
        }
    }

    private fun unfinishedSessionRecord(sampleCount: Int): TrackingSessionRecord =
        TrackingSessionRecord(
            sessionId = NavigationSessionId("session-1"),
            routeId = RouteId("longjing"),
            startedAtEpochMillis = 1_788_000_000_000,
            endedAtEpochMillis = null,
            state = NavigationState.Navigating,
            direction = NavigationDirection.Forward,
            sampleCount = sampleCount,
        )

    private fun trackPointAt(
        index: Int,
        epochMillis: Long,
    ): TrackingTrackPointRecord =
        TrackingTrackPointRecord(
            sessionId = NavigationSessionId("session-1"),
            pointIndex = index,
            coordinate = GeoCoordinate(latitude = 30.245, longitude = 120.116),
            accuracy = GpsAccuracy(4.5),
            recordedAtEpochMillis = epochMillis,
            bearingDegrees = null,
            speedMetersPerSecond = null,
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
}
