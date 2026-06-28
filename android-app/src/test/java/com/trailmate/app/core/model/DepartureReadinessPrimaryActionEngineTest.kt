package com.trailmate.app.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DepartureReadinessPrimaryActionEngineTest {
    @Test
    fun resolvesReadyActionAsStartHikeAndRecord() {
        val action = DepartureReadinessPrimaryActionEngine.resolve(summary("开始徒步并记录轨迹"))

        assertEquals("开始徒步并记录轨迹", action.label)
        assertEquals(DepartureReadinessPrimaryActionKind.START_HIKE_AND_RECORD, action.kind)
        assertTrue(action.enabled)
    }

    @Test
    fun resolvesOfflineRouteRepair() {
        val action = DepartureReadinessPrimaryActionEngine.resolve(summary("保存离线路线"))

        assertEquals(DepartureReadinessPrimaryActionKind.SAVE_OFFLINE_ROUTE_PACK, action.kind)
        assertTrue(action.enabled)
    }

    @Test
    fun resolvesOfflineBaseMapRepair() {
        val action = DepartureReadinessPrimaryActionEngine.resolve(summary("飞行模式验证底图"))

        assertEquals(DepartureReadinessPrimaryActionKind.OPEN_OFFLINE_BASE_MAP, action.kind)
        assertTrue(action.enabled)
    }

    @Test
    fun resolvesOfflineBaseMapImportRepair() {
        val action = DepartureReadinessPrimaryActionEngine.resolve(summary("导入离线地图包"))

        assertEquals(DepartureReadinessPrimaryActionKind.OPEN_OFFLINE_BASE_MAP, action.kind)
        assertTrue(action.enabled)
    }

    @Test
    fun resolvesLocationAuthorizationRepair() {
        val action = DepartureReadinessPrimaryActionEngine.resolve(summary("授权定位"))

        assertEquals(DepartureReadinessPrimaryActionKind.REQUEST_LOCATION, action.kind)
        assertTrue(action.enabled)
    }

    @Test
    fun resolvesLocationRepair() {
        val action = DepartureReadinessPrimaryActionEngine.resolve(summary("等待定位稳定"))

        assertEquals(DepartureReadinessPrimaryActionKind.REQUEST_LOCATION, action.kind)
        assertTrue(action.enabled)
    }

    @Test
    fun resolvesLocationRetryRepair() {
        val action = DepartureReadinessPrimaryActionEngine.resolve(summary("重试定位"))

        assertEquals(DepartureReadinessPrimaryActionKind.REQUEST_LOCATION, action.kind)
        assertTrue(action.enabled)
    }

    @Test
    fun resolvesSystemLocationRepair() {
        val action = DepartureReadinessPrimaryActionEngine.resolve(summary("打开系统定位"))

        assertEquals(DepartureReadinessPrimaryActionKind.OPEN_LOCATION_SETTINGS, action.kind)
        assertTrue(action.enabled)
    }

    @Test
    fun resolvesGearRepair() {
        val action = DepartureReadinessPrimaryActionEngine.resolve(summary("补齐 2 件关键装备"))

        assertEquals(DepartureReadinessPrimaryActionKind.SHOW_GEAR, action.kind)
        assertTrue(action.enabled)
    }

    @Test
    fun disablesUnknownAction() {
        val action = DepartureReadinessPrimaryActionEngine.resolve(summary("重新导入 GPX"))

        assertEquals(DepartureReadinessPrimaryActionKind.BLOCKED, action.kind)
        assertFalse(action.enabled)
    }

    private fun summary(primaryActionLabel: String) = DepartureReadinessSummary(
        title = "出发检查",
        statusLabel = "待处理",
        caption = "测试",
        primaryActionLabel = primaryActionLabel,
        steps = emptyList()
    )
}
