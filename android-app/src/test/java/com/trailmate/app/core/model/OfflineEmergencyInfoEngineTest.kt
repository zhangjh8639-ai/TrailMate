package com.trailmate.app.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.ZoneId

class OfflineEmergencyInfoEngineTest {
    private val zoneId = ZoneId.of("Asia/Shanghai")
    private val now = Instant.parse("2026-06-19T01:00:00Z").toEpochMilli()
    private val route = OfflineEmergencyRouteSummary(
        routeName = "龙井山脊",
        distanceKm = 15.2,
        ascentMeters = 860
    )
    private val progress = OfflineEmergencyProgress(
        currentCheckpointLabel = "当前 CP2",
        nextCheckpointLabel = "下一站 CP3",
        recordedDistanceKm = 5.1,
        recordingActive = true
    )

    @Test
    fun includesFreshCoordinatesAndProgressInEmergencyText() {
        val presentation = OfflineEmergencyInfoEngine.present(
            route = route,
            location = OfflineEmergencyLocation(
                latitude = 30.25,
                longitude = 120.12,
                horizontalAccuracyMeters = 8.0,
                timestampEpochMillis = now - 30_000L
            ),
            progress = progress,
            nowEpochMillis = now,
            zoneId = zoneId
        )

        assertEquals("求助信息", presentation.title)
        assertEquals("定位可用", presentation.statusLabel)
        assertEquals("分享求助信息", presentation.primaryActionLabel)
        assertEquals("分享求助信息", presentation.chooserTitle)
        val shareText = presentation.shareText
        assertTrue(shareText.contains("TrailMate 求助信息"))
        assertTrue(shareText.contains("路线：龙井山脊"))
        assertTrue(shareText.contains("计划：15.2 km / +860 m"))
        assertTrue(shareText.contains("进度：当前 CP2，下一站 CP3，已记录 5.1 km"))
        assertTrue(shareText.contains("坐标：30.25000,120.12000（精度约 8 m，时间 2026-06-19 08:59）"))
        assertTrue(shareText.contains("高德地图：https://uri.amap.com/marker?position=120.12000,30.25000"))
        assertTrue(shareText.contains("静态求助信息，不是实时追踪链接"))
        assertTrue(shareText.contains("不会自动联系他人、持续监控或发起救援"))
        assertEquals(
            listOf(
                OfflineEmergencyInfoDetail(label = "路线", value = "15.2 km / +860 m"),
                OfflineEmergencyInfoDetail(label = "进度", value = "已记录 5.1 km"),
                OfflineEmergencyInfoDetail(label = "定位", value = "30.25000,120.12000")
            ),
            presentation.details
        )
    }

    @Test
    fun omitsCoordinateLinkWhenLocationIsStale() {
        val presentation = OfflineEmergencyInfoEngine.present(
            route = route,
            location = OfflineEmergencyLocation(
                latitude = 30.25,
                longitude = 120.12,
                horizontalAccuracyMeters = 8.0,
                timestampEpochMillis = now - 3 * 60_000L
            ),
            progress = progress,
            nowEpochMillis = now,
            zoneId = zoneId
        )

        assertEquals("定位待刷新", presentation.statusLabel)
        assertTrue(presentation.caption.contains("缺少可靠的当前 GPS 坐标"))
        assertTrue(presentation.shareText.contains("坐标：暂无可靠当前 GPS 坐标"))
        assertTrue(presentation.shareText.contains("位置建议：到开阔处刷新定位"))
        assertFalse(presentation.shareText.contains("高德地图：https://uri.amap.com/marker"))
        assertTrue(presentation.details.contains(OfflineEmergencyInfoDetail(label = "定位", value = "待刷新")))
    }

    @Test
    fun omitsCoordinateLinkWhenAccuracyIsPoorOrTimestampInvalid() {
        val unreliableLocations = listOf(
            OfflineEmergencyLocation(
                latitude = 30.25,
                longitude = 120.12,
                horizontalAccuracyMeters = 180.0,
                timestampEpochMillis = now - 30_000L
            ),
            OfflineEmergencyLocation(
                latitude = 30.25,
                longitude = 120.12,
                horizontalAccuracyMeters = 8.0,
                timestampEpochMillis = now + 1L
            ),
            OfflineEmergencyLocation(
                latitude = null,
                longitude = null,
                horizontalAccuracyMeters = null,
                timestampEpochMillis = null
            )
        )

        unreliableLocations.forEach { location ->
            val presentation = OfflineEmergencyInfoEngine.present(
                route = route,
                location = location,
                progress = progress.copy(recordingActive = false),
                nowEpochMillis = now,
                zoneId = zoneId
            )

            assertEquals("定位待刷新", presentation.statusLabel)
            assertTrue(presentation.shareText.contains("坐标：暂无可靠当前 GPS 坐标"))
            assertFalse(presentation.shareText.contains("uri.amap.com/marker"))
            assertTrue(presentation.shareText.contains("静态求助信息"))
        }
    }

    @Test
    fun nonRecordingProgressStillSharesRouteContext() {
        val presentation = OfflineEmergencyInfoEngine.present(
            route = route,
            location = OfflineEmergencyLocation(
                latitude = null,
                longitude = null,
                horizontalAccuracyMeters = null,
                timestampEpochMillis = null
            ),
            progress = OfflineEmergencyProgress(
                currentCheckpointLabel = "起点",
                nextCheckpointLabel = "下一站 CP1",
                recordedDistanceKm = 0.0,
                recordingActive = false
            ),
            nowEpochMillis = now,
            zoneId = zoneId
        )

        assertTrue(presentation.shareText.contains("进度：起点，下一站 CP1，未记录轨迹距离"))
        assertFalse(presentation.shareText.contains("正在记录"))
    }

    @Test
    fun shareActionRecomputesLocationFreshnessAtClickTime() {
        val location = OfflineEmergencyLocation(
            latitude = 30.25,
            longitude = 120.12,
            horizontalAccuracyMeters = 8.0,
            timestampEpochMillis = now - 30_000L
        )
        val initiallyShown = OfflineEmergencyInfoEngine.present(
            route = route,
            location = location,
            progress = progress,
            nowEpochMillis = now,
            zoneId = zoneId
        )

        val action = OfflineEmergencyInfoActionEngine.resolveShareAction(
            route = route,
            location = location,
            progress = progress,
            nowEpochMillis = now + 3 * 60_000L,
            zoneId = zoneId
        )

        assertTrue(initiallyShown.shareText.contains("高德地图：https://uri.amap.com/marker"))
        assertEquals("分享求助信息", action.chooserTitle)
        assertTrue(action.shareText.contains("坐标：暂无可靠当前 GPS 坐标"))
        assertFalse(action.shareText.contains("高德地图：https://uri.amap.com/marker"))
    }
}
