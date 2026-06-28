package com.trailmate.app.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.ZoneId

class SafetyShareEngineTest {
    @Test
    fun waitsForLocationBeforeSharing() {
        val presentation = SafetyShareEngine.present(
            routeName = "龙井山脊",
            location = SafetyShareLocation(
                latitude = null,
                longitude = null,
                horizontalAccuracyMeters = null
            ),
            trackRecording = TrackRecordingState()
        )

        assertEquals("等待定位后分享", presentation.title)
        assertEquals("待定位", presentation.statusLabel)
        assertEquals("授权定位", presentation.primaryActionLabel)
        assertNull(presentation.shareText)
        assertTrue(presentation.caption.contains("授权定位后可分享当前位置"))
    }

    @Test
    fun sharesCurrentAmapLocationWhenLocated() {
        val presentation = SafetyShareEngine.present(
            routeName = "龙井山脊",
            location = SafetyShareLocation(
                latitude = 30.25,
                longitude = 120.12,
                horizontalAccuracyMeters = 12.4,
                timestampEpochMillis = SHARE_NOW - 30_000L
            ),
            trackRecording = TrackRecordingState(),
            nowEpochMillis = SHARE_NOW
        )

        assertEquals("可分享当前位置", presentation.title)
        assertEquals("位置可用", presentation.statusLabel)
        assertEquals("分享当前位置", presentation.primaryActionLabel)
        val shareText = requireNotNull(presentation.shareText)
        assertTrue(shareText.contains("TrailMate 安全分享"))
        assertTrue(shareText.contains("路线：龙井山脊"))
        assertTrue(shareText.contains("状态：当前位置"))
        assertTrue(shareText.contains("位置：30.25000,120.12000"))
        assertTrue(shareText.contains("精度约 12 m"))
        assertTrue(shareText.contains("https://uri.amap.com/marker?position=120.12000,30.25000"))
        assertTrue(shareText.contains("name=TrailMate+%C2%B7+%E9%BE%99%E4%BA%95%E5%B1%B1%E8%84%8A"))
        assertTrue(shareText.contains("coordinate=wgs84"))
        assertTrue(shareText.contains("src=TrailMate"))
        assertTrue(shareText.contains("callnative=1"))
    }

    @Test
    fun sharesLocationWhenTimestampIsExactlyTwoMinutesOld() {
        val presentation = SafetyShareEngine.present(
            routeName = "龙井山脊",
            location = SafetyShareLocation(
                latitude = 30.25,
                longitude = 120.12,
                horizontalAccuracyMeters = 12.4,
                timestampEpochMillis = SHARE_NOW - 2 * 60_000L
            ),
            trackRecording = TrackRecordingState(),
            nowEpochMillis = SHARE_NOW
        )

        assertEquals("位置可用", presentation.statusLabel)
        assertTrue(requireNotNull(presentation.shareText).contains("位置：30.25000,120.12000"))
    }

    @Test
    fun waitsForReliableAccuracyBeforeSharingLocation() {
        val presentation = SafetyShareEngine.present(
            routeName = "龙井山脊",
            location = SafetyShareLocation(
                latitude = 30.25,
                longitude = 120.12,
                horizontalAccuracyMeters = 180.0,
                timestampEpochMillis = SHARE_NOW - 30_000L
            ),
            trackRecording = TrackRecordingState(),
            nowEpochMillis = SHARE_NOW
        )

        assertEquals("等待定位稳定后分享", presentation.title)
        assertEquals("精度待稳定", presentation.statusLabel)
        assertEquals("重新定位", presentation.primaryActionLabel)
        assertNull(presentation.shareText)
        assertTrue(presentation.caption.contains("当前定位精度约 180 m"))
    }

    @Test
    fun waitsForAccuracyValueBeforeSharingCoordinates() {
        val presentation = SafetyShareEngine.present(
            routeName = "龙井山脊",
            location = SafetyShareLocation(
                latitude = 30.25,
                longitude = 120.12,
                horizontalAccuracyMeters = null,
                timestampEpochMillis = SHARE_NOW - 30_000L
            ),
            trackRecording = TrackRecordingState(),
            nowEpochMillis = SHARE_NOW
        )

        assertEquals("等待定位稳定后分享", presentation.title)
        assertEquals("精度待稳定", presentation.statusLabel)
        assertNull(presentation.shareText)
        assertTrue(presentation.caption.contains("缺少定位精度"))
    }

    @Test
    fun encodesRouteNameForAmapMarkerLink() {
        val presentation = SafetyShareEngine.present(
            routeName = "龙井山脊 & 夜徒",
            location = SafetyShareLocation(
                latitude = 30.25,
                longitude = 120.12,
                horizontalAccuracyMeters = 12.4,
                timestampEpochMillis = SHARE_NOW - 30_000L
            ),
            trackRecording = TrackRecordingState(),
            nowEpochMillis = SHARE_NOW
        )

        val shareText = requireNotNull(presentation.shareText)
        assertTrue(
            shareText.contains(
                "name=TrailMate+%C2%B7+%E9%BE%99%E4%BA%95%E5%B1%B1%E8%84%8A+%26+%E5%A4%9C%E5%BE%92"
            )
        )
    }

    @Test
    fun sharesRecordingDistanceWhenTrackIsActive() {
        val presentation = SafetyShareEngine.present(
            routeName = "龙井山脊",
            location = SafetyShareLocation(
                latitude = 30.25,
                longitude = 120.12,
                horizontalAccuracyMeters = 8.0,
                timestampEpochMillis = SHARE_NOW - 30_000L
            ),
            trackRecording = TrackRecordingState(
                status = TrackRecordingStatus.RECORDING,
                routeName = "龙井山脊",
                totalDistanceKm = 1.234
            ),
            nowEpochMillis = SHARE_NOW
        )

        assertEquals("安全分享可用", presentation.title)
        assertEquals("记录中", presentation.statusLabel)
        assertEquals("分享当前记录位置", presentation.primaryActionLabel)
        val shareText = requireNotNull(presentation.shareText)
        assertTrue(shareText.contains("状态：正在记录，已记录 1.2 km"))
        assertTrue(shareText.contains("说明：这是当前时刻的静态位置，不是实时追踪链接。"))
    }

    @Test
    fun sharesRoutePlanAndExpectedFinishTimeForSafetyContact() {
        val presentation = SafetyShareEngine.present(
            routeName = "龙井山脊",
            location = SafetyShareLocation(
                latitude = 30.25,
                longitude = 120.12,
                horizontalAccuracyMeters = 8.0,
                timestampEpochMillis = SHARE_NOW - 30_000L
            ),
            trackRecording = TrackRecordingState(),
            routePlan = SafetyShareRoutePlan(
                distanceKm = 15.2,
                ascentMeters = 860,
                estimatedDurationMinutes = 410
            ),
            nowEpochMillis = SHARE_NOW,
            zoneId = ZoneId.of("Asia/Shanghai")
        )

        val shareText = requireNotNull(presentation.shareText)
        assertTrue(shareText.contains("计划：15.2 km / +860 m"))
        assertTrue(shareText.contains("预计完成：2026-06-19 15:50"))
        assertTrue(shareText.contains("超时提示：若超过预计完成 60 分钟仍未联系"))
        assertEquals(
            listOf(
                SafetyShareDetail(label = "路线", value = "15.2 km / +860 m"),
                SafetyShareDetail(label = "预计完成", value = "2026-06-19 15:50"),
                SafetyShareDetail(label = "超时确认", value = "预计完成 +60 分钟")
            ),
            presentation.details
        )
    }

    @Test
    fun sharesAbsoluteExpectedFinishWithoutRecalculatingFromShareTime() {
        val expectedFinish = SHARE_NOW + 410 * 60_000L
        val presentation = SafetyShareEngine.present(
            routeName = "龙井山脊",
            location = SafetyShareLocation(
                latitude = 30.25,
                longitude = 120.12,
                horizontalAccuracyMeters = 8.0,
                timestampEpochMillis = SHARE_NOW + 500 * 60_000L - 30_000L
            ),
            trackRecording = TrackRecordingState(
                status = TrackRecordingStatus.RECORDING,
                startedAtEpochMillis = SHARE_NOW,
                totalDistanceKm = 12.4
            ),
            routePlan = SafetyShareRoutePlan(
                distanceKm = 15.2,
                ascentMeters = 860,
                estimatedDurationMinutes = 410,
                expectedFinishEpochMillis = expectedFinish
            ),
            nowEpochMillis = SHARE_NOW + 500 * 60_000L,
            zoneId = ZoneId.of("Asia/Shanghai")
        )

        val shareText = requireNotNull(presentation.shareText)
        assertTrue(shareText.contains("预计完成：2026-06-19 15:50"))
        assertTrue(shareText.contains("逾期提示：已超过预计完成 1h30"))
        assertFalse(shareText.contains("2026-06-20 00:10"))
        assertEquals(
            listOf(
                SafetyShareDetail(label = "路线", value = "15.2 km / +860 m"),
                SafetyShareDetail(label = "预计完成", value = "2026-06-19 15:50"),
                SafetyShareDetail(label = "超时确认", value = "预计完成 +60 分钟"),
                SafetyShareDetail(label = "当前状态", value = "已超过 1h30")
            ),
            presentation.details
        )
    }

    @Test
    fun omitsExpectedFinishWhenRoutePlanHasNoDuration() {
        val presentation = SafetyShareEngine.present(
            routeName = "龙井山脊",
            location = SafetyShareLocation(
                latitude = 30.25,
                longitude = 120.12,
                horizontalAccuracyMeters = 8.0,
                timestampEpochMillis = SHARE_NOW - 30_000L
            ),
            trackRecording = TrackRecordingState(),
            routePlan = SafetyShareRoutePlan(
                distanceKm = 15.2,
                ascentMeters = 860,
                estimatedDurationMinutes = null
            ),
            nowEpochMillis = SHARE_NOW,
            zoneId = ZoneId.of("Asia/Shanghai")
        )

        val shareText = requireNotNull(presentation.shareText)
        assertTrue(shareText.contains("计划：15.2 km / +860 m"))
        assertFalse(shareText.contains("预计完成"))
        assertFalse(shareText.contains("超时提示"))
        assertEquals(
            listOf(SafetyShareDetail(label = "路线", value = "15.2 km / +860 m")),
            presentation.details
        )
    }

    @Test
    fun waitsForFreshLocationBeforeSharingStaleCoordinates() {
        val presentation = SafetyShareEngine.present(
            routeName = "龙井山脊",
            location = SafetyShareLocation(
                latitude = 30.25,
                longitude = 120.12,
                horizontalAccuracyMeters = 8.0,
                timestampEpochMillis = SHARE_NOW - 3 * 60_000L
            ),
            trackRecording = TrackRecordingState(),
            nowEpochMillis = SHARE_NOW
        )

        assertEquals("刷新定位后分享", presentation.title)
        assertEquals("位置已过期", presentation.statusLabel)
        assertEquals("重新定位", presentation.primaryActionLabel)
        assertNull(presentation.shareText)
        assertTrue(presentation.caption.contains("上次定位已超过 2 分钟"))
    }

    @Test
    fun waitsForFreshLocationWhenTimestampIsMissing() {
        val presentation = SafetyShareEngine.present(
            routeName = "龙井山脊",
            location = SafetyShareLocation(
                latitude = 30.25,
                longitude = 120.12,
                horizontalAccuracyMeters = 8.0,
                timestampEpochMillis = null
            ),
            trackRecording = TrackRecordingState(),
            nowEpochMillis = SHARE_NOW
        )

        assertEquals("刷新定位后分享", presentation.title)
        assertEquals("位置时间未知", presentation.statusLabel)
        assertNull(presentation.shareText)
        assertTrue(presentation.caption.contains("缺少定位时间"))
    }

    @Test
    fun waitsForFreshLocationWhenTimestampIsInvalidOrFuture() {
        listOf(0L, -1L, SHARE_NOW + 1L).forEach { timestamp ->
            val presentation = SafetyShareEngine.present(
                routeName = "龙井山脊",
                location = SafetyShareLocation(
                    latitude = 30.25,
                    longitude = 120.12,
                    horizontalAccuracyMeters = 8.0,
                    timestampEpochMillis = timestamp
                ),
                trackRecording = TrackRecordingState(),
                nowEpochMillis = SHARE_NOW
            )

            assertEquals("刷新定位后分享", presentation.title)
            assertEquals("位置时间未知", presentation.statusLabel)
            assertNull(presentation.shareText)
            assertTrue(presentation.caption.contains("需要重新获取 GPS 定位"))
        }
    }

    @Test
    fun clickActionRechecksLocationAgeAtShareTime() {
        val location = SafetyShareLocation(
            latitude = 30.25,
            longitude = 120.12,
            horizontalAccuracyMeters = 8.0,
            timestampEpochMillis = SHARE_NOW - 30_000L
        )
        val initiallyAvailable = SafetyShareEngine.present(
            routeName = "龙井山脊",
            location = location,
            trackRecording = TrackRecordingState(),
            nowEpochMillis = SHARE_NOW
        )

        val action = SafetyShareActionEngine.resolveShareAction(
            routeName = "龙井山脊",
            location = location,
            trackRecording = TrackRecordingState(),
            routePlan = null,
            nowEpochMillis = SHARE_NOW + 2 * 60_000L
        )

        assertTrue(requireNotNull(initiallyAvailable.shareText).contains("TrailMate 安全分享"))
        assertNull(action.shareText)
        assertTrue(action.shouldRequestLocation)
    }

    private companion object {
        val SHARE_NOW: Long = Instant.parse("2026-06-19T01:00:00Z").toEpochMilli()
    }
}
