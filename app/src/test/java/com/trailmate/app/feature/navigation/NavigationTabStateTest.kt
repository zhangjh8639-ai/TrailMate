package com.trailmate.app.feature.navigation

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
        assertTrue(visibleText.contains("更换路线"))
        assertFalse(visibleText.contains("开始导航"))
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
        assertFalse(visibleText.contains("开始轨迹导航"))
        assertNoDeprecatedSurfaces(visibleText)
    }

    private fun assertNoDeprecatedSurfaces(visibleText: String) {
        listOf("规划", "装备", "社区", "商城", "出发前检查", "完成检查后开始").forEach { deprecated ->
            assertFalse("Navigation handoff must not expose $deprecated", visibleText.contains(deprecated))
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
