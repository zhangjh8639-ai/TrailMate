package com.trailmate.app.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.ZoneId

class DaylightReturnWatchEngineTest {
    private val zoneId = ZoneId.of("Asia/Shanghai")
    private val route = ImportedRoute(
        routeName = "龙井山脊",
        fileName = "longjing.gpx",
        distanceKm = 15.2,
        ascentMeters = 860,
        status = RouteImportStatus.PARSED,
        pointCount = 4,
        routePoints = listOf(
            RoutePoint(latitude = 30.2200, longitude = 120.1000, elevationMeters = 52.0, distanceAlongRouteKm = 0.0),
            RoutePoint(latitude = 30.2300, longitude = 120.1050, elevationMeters = 220.0, distanceAlongRouteKm = 4.8),
            RoutePoint(latitude = 30.2420, longitude = 120.1150, elevationMeters = 310.0, distanceAlongRouteKm = 9.7),
            RoutePoint(latitude = 30.2550, longitude = 120.1220, elevationMeters = 260.0, distanceAlongRouteKm = 15.2)
        )
    )
    private val activeRecording = TrackRecordingState(
        status = TrackRecordingStatus.RECORDING,
        startedAtEpochMillis = Instant.parse("2026-06-19T01:00:00Z").toEpochMilli()
    )

    @Test
    fun missingRouteGeometryHidesDaylightWatch() {
        val presentation = DaylightReturnWatchEngine.present(
            route = route.copy(routePoints = emptyList()),
            trackRecording = activeRecording,
            expectedFinishEpochMillis = Instant.parse("2026-06-19T07:30:00Z").toEpochMilli(),
            nowEpochMillis = Instant.parse("2026-06-19T06:00:00Z").toEpochMilli(),
            zoneId = zoneId
        )

        assertFalse(presentation.visible)
        assertEquals(emptyList<DaylightReturnWatchDetail>(), presentation.details)
    }

    @Test
    fun inactiveRecordingHidesDaylightWatch() {
        val presentation = DaylightReturnWatchEngine.present(
            route = route,
            trackRecording = TrackRecordingState(status = TrackRecordingStatus.IDLE),
            expectedFinishEpochMillis = Instant.parse("2026-06-19T10:20:00Z").toEpochMilli(),
            nowEpochMillis = Instant.parse("2026-06-19T08:20:00Z").toEpochMilli(),
            zoneId = zoneId
        )

        assertFalse(presentation.visible)
    }

    @Test
    fun expectedFinishWellBeforeSunsetStaysHidden() {
        val presentation = DaylightReturnWatchEngine.present(
            route = route,
            trackRecording = activeRecording,
            expectedFinishEpochMillis = Instant.parse("2026-06-19T08:00:00Z").toEpochMilli(),
            nowEpochMillis = Instant.parse("2026-06-19T06:30:00Z").toEpochMilli(),
            zoneId = zoneId
        )

        assertFalse(presentation.visible)
        assertFalse(presentation.primaryActionRequiresSafetyShare)
    }

    @Test
    fun expectedFinishNearSunsetShowsCaution() {
        val presentation = DaylightReturnWatchEngine.present(
            route = route,
            trackRecording = activeRecording,
            expectedFinishEpochMillis = Instant.parse("2026-06-19T10:20:00Z").toEpochMilli(),
            nowEpochMillis = Instant.parse("2026-06-19T08:20:00Z").toEpochMilli(),
            zoneId = zoneId
        )

        assertTrue(presentation.visible)
        assertEquals("日照窗口", presentation.title)
        assertEquals("日照窗口收紧", presentation.statusLabel)
        assertEquals("复核天黑前路线", presentation.primaryActionLabel)
        assertEquals(DaylightReturnWatchTone.CAUTION, presentation.tone)
        assertFalse(presentation.primaryActionRequiresSafetyShare)
        assertTrue(presentation.caption.contains("日落估算"))
        assertTrue(presentation.caption.contains("头灯"))
        assertTrue(presentation.caption.contains("撤退"))
        assertTrue(presentation.caption.contains("缩短路线"))
        assertTrue(presentation.details.any { it.label == "预计完成" && it.value == "2026-06-19 18:20" })
        assertTrue(presentation.details.any { it.label == "日落估算" })
        assertPolicyBoundaries(presentation)
    }

    @Test
    fun expectedFinishAfterSunsetEscalatesToManualSafetyShare() {
        val presentation = DaylightReturnWatchEngine.present(
            route = route,
            trackRecording = activeRecording,
            expectedFinishEpochMillis = Instant.parse("2026-06-19T11:15:00Z").toEpochMilli(),
            nowEpochMillis = Instant.parse("2026-06-19T09:50:00Z").toEpochMilli(),
            zoneId = zoneId
        )

        assertTrue(presentation.visible)
        assertEquals("可能天黑后完成", presentation.statusLabel)
        assertEquals("分享当前位置", presentation.primaryActionLabel)
        assertEquals(DaylightReturnWatchTone.ALERT, presentation.tone)
        assertTrue(presentation.primaryActionRequiresSafetyShare)
        assertTrue(presentation.caption.contains("日落后"))
        assertTrue(presentation.caption.contains("安全退出"))
        assertTrue(presentation.caption.contains("手动"))
        assertPolicyBoundaries(presentation)
    }

    @Test
    fun expectedFinishAfterMidnightStillUsesCurrentDaylightWindow() {
        val presentation = DaylightReturnWatchEngine.present(
            route = route,
            trackRecording = activeRecording,
            expectedFinishEpochMillis = Instant.parse("2026-06-19T18:30:00Z").toEpochMilli(),
            nowEpochMillis = Instant.parse("2026-06-19T08:30:00Z").toEpochMilli(),
            zoneId = zoneId
        )

        assertTrue(presentation.visible)
        assertEquals("可能天黑后完成", presentation.statusLabel)
        assertEquals(DaylightReturnWatchTone.ALERT, presentation.tone)
        assertTrue(presentation.primaryActionRequiresSafetyShare)
        assertPolicyBoundaries(presentation)
    }

    @Test
    fun currentTimeAfterSunsetEscalatesEvenWhenExpectedFinishIsMissing() {
        val presentation = DaylightReturnWatchEngine.present(
            route = route,
            trackRecording = activeRecording,
            expectedFinishEpochMillis = null,
            nowEpochMillis = Instant.parse("2026-06-19T11:20:00Z").toEpochMilli(),
            zoneId = zoneId
        )

        assertTrue(presentation.visible)
        assertEquals("已经接近日落后", presentation.statusLabel)
        assertEquals(DaylightReturnWatchTone.ALERT, presentation.tone)
        assertTrue(presentation.primaryActionRequiresSafetyShare)
        assertTrue(presentation.caption.contains("当前位置"))
        assertPolicyBoundaries(presentation)
    }

    private fun assertPolicyBoundaries(presentation: DaylightReturnWatchPresentation) {
        val text = buildString {
            append(presentation.title)
            append(presentation.statusLabel)
            append(presentation.caption)
            append(presentation.primaryActionLabel)
            presentation.details.forEach { detail ->
                append(detail.label)
                append(detail.value)
            }
        }
        assertTrue(text.any { character -> character in '\u4e00'..'\u9fff' })
        assertFalse(text.contains("自动联系"))
        assertFalse(text.contains("自动救援"))
        assertFalse(text.contains("保证安全"))
        assertFalse(text.contains("保证可见"))
        assertFalse(text.contains("精准日落"))
    }
}
