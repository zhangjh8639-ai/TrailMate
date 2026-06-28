package com.trailmate.app.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TrackRecordingDepartureGateEngineTest {
    @Test
    fun idleRecordingStartUsesOfflineBaseMapRepairBeforeTrackAction() {
        val action = TrackRecordingDepartureGateEngine.present(
            hikeSessionStatus = HikeSessionStatus.READY,
            trackRecordingStatus = TrackRecordingStatus.IDLE,
            currentTrackActionLabel = "开始记录",
            departureReadiness = departureReadiness(primaryActionLabel = "导入离线地图包")
        )

        assertEquals("导入离线地图包", action.label)
        assertEquals(TrackRecordingDepartureGateActionKind.OPEN_OFFLINE_BASE_MAP, action.kind)
        assertTrue(action.enabled)
    }

    @Test
    fun finishedRecordingRestartUsesGearRepairBeforeTrackAction() {
        val action = TrackRecordingDepartureGateEngine.present(
            hikeSessionStatus = HikeSessionStatus.READY,
            trackRecordingStatus = TrackRecordingStatus.FINISHED,
            currentTrackActionLabel = "开始记录",
            departureReadiness = departureReadiness(primaryActionLabel = "补齐 1 件关键装备")
        )

        assertEquals("补齐 1 件关键装备", action.label)
        assertEquals(TrackRecordingDepartureGateActionKind.SHOW_GEAR, action.kind)
        assertTrue(action.enabled)
    }

    @Test
    fun idleRecordingStartUsesOfflineRoutePackRepairBeforeTrackAction() {
        val action = TrackRecordingDepartureGateEngine.present(
            hikeSessionStatus = HikeSessionStatus.READY,
            trackRecordingStatus = TrackRecordingStatus.IDLE,
            currentTrackActionLabel = "开始记录",
            departureReadiness = departureReadiness(primaryActionLabel = "保存离线路线")
        )

        assertEquals("保存离线路线", action.label)
        assertEquals(TrackRecordingDepartureGateActionKind.SAVE_OFFLINE_ROUTE_PACK, action.kind)
        assertTrue(action.enabled)
    }

    @Test
    fun idleRecordingStartUsesLocationRepairBeforeTrackAction() {
        val action = TrackRecordingDepartureGateEngine.present(
            hikeSessionStatus = HikeSessionStatus.READY,
            trackRecordingStatus = TrackRecordingStatus.IDLE,
            currentTrackActionLabel = "开始记录",
            departureReadiness = departureReadiness(primaryActionLabel = "等待定位稳定")
        )

        assertEquals("等待定位稳定", action.label)
        assertEquals(TrackRecordingDepartureGateActionKind.REQUEST_LOCATION, action.kind)
        assertTrue(action.enabled)
    }

    @Test
    fun idleRecordingStartUsesSystemLocationRepairBeforeTrackAction() {
        val action = TrackRecordingDepartureGateEngine.present(
            hikeSessionStatus = HikeSessionStatus.READY,
            trackRecordingStatus = TrackRecordingStatus.IDLE,
            currentTrackActionLabel = "开始记录",
            departureReadiness = departureReadiness(primaryActionLabel = "打开系统定位")
        )

        assertEquals("打开系统定位", action.label)
        assertEquals(TrackRecordingDepartureGateActionKind.OPEN_LOCATION_SETTINGS, action.kind)
        assertTrue(action.enabled)
    }

    @Test
    fun readyDepartureKeepsExistingTrackGateAction() {
        val action = TrackRecordingDepartureGateEngine.present(
            hikeSessionStatus = HikeSessionStatus.READY,
            trackRecordingStatus = TrackRecordingStatus.IDLE,
            currentTrackActionLabel = "允许通知",
            departureReadiness = departureReadiness(primaryActionLabel = "开始徒步并记录轨迹")
        )

        assertEquals("允许通知", action.label)
        assertEquals(TrackRecordingDepartureGateActionKind.APPLY_TRACK_ACTION, action.kind)
        assertTrue(action.enabled)
    }

    @Test
    fun activeRecordingKeepsPauseEvenWhenDepartureRepairAppears() {
        val action = TrackRecordingDepartureGateEngine.present(
            hikeSessionStatus = HikeSessionStatus.READY,
            trackRecordingStatus = TrackRecordingStatus.RECORDING,
            currentTrackActionLabel = "暂停记录",
            departureReadiness = departureReadiness(primaryActionLabel = "保存离线路线")
        )

        assertEquals("暂停记录", action.label)
        assertEquals(TrackRecordingDepartureGateActionKind.APPLY_TRACK_ACTION, action.kind)
        assertTrue(action.enabled)
    }

    @Test
    fun pausedRecordingKeepsResumeEvenWhenDepartureRepairAppears() {
        val action = TrackRecordingDepartureGateEngine.present(
            hikeSessionStatus = HikeSessionStatus.READY,
            trackRecordingStatus = TrackRecordingStatus.PAUSED,
            currentTrackActionLabel = "继续记录",
            departureReadiness = departureReadiness(primaryActionLabel = "导入离线地图包")
        )

        assertEquals("继续记录", action.label)
        assertEquals(TrackRecordingDepartureGateActionKind.APPLY_TRACK_ACTION, action.kind)
        assertTrue(action.enabled)
    }

    @Test
    fun activeHikeWithIdleRecordingKeepsExistingTrackAction() {
        val action = TrackRecordingDepartureGateEngine.present(
            hikeSessionStatus = HikeSessionStatus.ACTIVE,
            trackRecordingStatus = TrackRecordingStatus.IDLE,
            currentTrackActionLabel = "开始记录",
            departureReadiness = departureReadiness(primaryActionLabel = "保存离线路线")
        )

        assertEquals("开始记录", action.label)
        assertEquals(TrackRecordingDepartureGateActionKind.APPLY_TRACK_ACTION, action.kind)
        assertTrue(action.enabled)
    }

    @Test
    fun unknownDepartureRepairDisablesSecondaryTrackAction() {
        val action = TrackRecordingDepartureGateEngine.present(
            hikeSessionStatus = HikeSessionStatus.READY,
            trackRecordingStatus = TrackRecordingStatus.IDLE,
            currentTrackActionLabel = "开始记录",
            departureReadiness = departureReadiness(primaryActionLabel = "重新导入 GPX")
        )

        assertEquals("重新导入 GPX", action.label)
        assertEquals(TrackRecordingDepartureGateActionKind.BLOCKED, action.kind)
        assertFalse(action.enabled)
    }

    private fun departureReadiness(primaryActionLabel: String) = DepartureReadinessSummary(
        title = "出发检查",
        statusLabel = "待处理",
        caption = "测试",
        primaryActionLabel = primaryActionLabel,
        steps = emptyList()
    )
}
