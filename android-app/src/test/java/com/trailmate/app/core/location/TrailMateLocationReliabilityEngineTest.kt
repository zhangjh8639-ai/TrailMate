package com.trailmate.app.core.location

import com.trailmate.app.core.model.LocationBackedHikeStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TrailMateLocationReliabilityEngineTest {
    @Test
    fun presentsReliableLocatedFixWithRouteMatchingDetails() {
        val presentation = TrailMateLocationReliabilityEngine.present(
            snapshot = TrailMateLocationSnapshot(
                status = TrailMateLocationStatus.LOCATED,
                latitude = 30.25,
                longitude = 120.15,
                elevationMeters = 142.0,
                horizontalAccuracyMeters = 8.4,
                timestampEpochMillis = 2_000L
            ),
            routePointCount = 52,
            guidanceStatus = LocationBackedHikeStatus.ON_ROUTE,
            guidanceCaption = "已对齐「补给检查」，路线进度 3.1 km。",
            nowEpochMillis = 7_000L
        )

        assertEquals("定位可用于导航", presentation.title)
        assertEquals("可靠", presentation.statusLabel)
        assertEquals(LocationReliabilityLevel.GOOD, presentation.level)
        assertEquals("已对齐「补给检查」，路线进度 3.1 km。", presentation.caption)
        assertEquals(
            listOf(
                LocationReliabilityDetail(label = "定位精度", value = "约 8 m"),
                LocationReliabilityDetail(label = "路线匹配", value = "可校验偏离"),
                LocationReliabilityDetail(label = "最近更新", value = "刚刚")
            ),
            presentation.details
        )
        assertChinese(presentation)
    }

    @Test
    fun presentsLowAccuracyAsCautionBeforeRelyingOnNavigation() {
        val presentation = TrailMateLocationReliabilityEngine.present(
            snapshot = TrailMateLocationSnapshot(
                status = TrailMateLocationStatus.LOW_ACCURACY,
                latitude = 30.25,
                longitude = 120.15,
                elevationMeters = null,
                horizontalAccuracyMeters = 118.7,
                timestampEpochMillis = 2_000L
            ),
            routePointCount = 52,
            guidanceStatus = LocationBackedHikeStatus.LOW_ACCURACY,
            guidanceCaption = "定位精度约 118 m，暂不推进检查点。",
            nowEpochMillis = 185_000L
        )

        assertEquals("定位精度偏低", presentation.title)
        assertEquals("谨慎使用", presentation.statusLabel)
        assertEquals(LocationReliabilityLevel.CAUTION, presentation.level)
        assertEquals("尽量到开阔处等待信号稳定，再依赖路线校验。", presentation.caption)
        assertEquals("继续校准", presentation.actionLabel)
        assertEquals("约 118 m", presentation.details[0].value)
        assertEquals("3 分钟前", presentation.details[2].value)
        assertChinese(presentation)
    }

    @Test
    fun presentsSlowFirstFixAsOutdoorCalibrationHint() {
        val presentation = TrailMateLocationReliabilityEngine.present(
            snapshot = TrailMateLocationSnapshot(
                status = TrailMateLocationStatus.SEARCHING,
                latitude = null,
                longitude = null,
                elevationMeters = null,
                horizontalAccuracyMeters = null,
                timestampEpochMillis = 1_000L
            ),
            routePointCount = 52,
            guidanceStatus = LocationBackedHikeStatus.WAITING,
            guidanceCaption = "授权定位后，可用当前位置辅助检查点推进。",
            nowEpochMillis = 47_000L
        )

        assertEquals("仍在等待 GPS 信号", presentation.title)
        assertEquals("校准中", presentation.statusLabel)
        assertEquals(LocationReliabilityLevel.CAUTION, presentation.level)
        assertEquals("请移到开阔处，保持屏幕点亮继续等待首个定位点。", presentation.caption)
        assertEquals("继续校准", presentation.actionLabel)
        assertChinese(presentation)
    }

    @Test
    fun presentsLocatedFixWithoutAccuracyAsCalibrationCaution() {
        val presentation = TrailMateLocationReliabilityEngine.present(
            snapshot = TrailMateLocationSnapshot(
                status = TrailMateLocationStatus.LOCATED,
                latitude = 30.25,
                longitude = 120.15,
                elevationMeters = null,
                horizontalAccuracyMeters = null,
                timestampEpochMillis = 2_000L
            ),
            routePointCount = 52,
            guidanceStatus = LocationBackedHikeStatus.ON_ROUTE,
            guidanceCaption = "已对齐「补给检查」，路线进度 3.1 km。",
            nowEpochMillis = 7_000L
        )

        assertEquals("等待定位精度", presentation.title)
        assertEquals("校准中", presentation.statusLabel)
        assertEquals(LocationReliabilityLevel.CAUTION, presentation.level)
        assertEquals("已获取位置，正在等待精度数据稳定。", presentation.caption)
        assertEquals("等待信号", presentation.details[0].value)
        assertChinese(presentation)
    }

    @Test
    fun presentsStaleLocatedFixAsCalibrationCaution() {
        val presentation = TrailMateLocationReliabilityEngine.present(
            snapshot = TrailMateLocationSnapshot(
                status = TrailMateLocationStatus.LOCATED,
                latitude = 30.25,
                longitude = 120.15,
                elevationMeters = 142.0,
                horizontalAccuracyMeters = 8.4,
                timestampEpochMillis = 2_000L
            ),
            routePointCount = 52,
            guidanceStatus = LocationBackedHikeStatus.ON_ROUTE,
            guidanceCaption = "已对齐「补给检查」，路线进度 3.1 km。",
            nowEpochMillis = 185_000L
        )

        assertEquals("定位已过期", presentation.title)
        assertEquals("需校准", presentation.statusLabel)
        assertEquals(LocationReliabilityLevel.CAUTION, presentation.level)
        assertEquals("当前位置已超过 60 秒未更新，请等待新的定位点。", presentation.caption)
        assertEquals("继续校准", presentation.actionLabel)
        assertEquals("3 分钟前", presentation.details[2].value)
        assertChinese(presentation)
    }

    @Test
    fun presentsProviderDisabledAsBlockedAction() {
        val presentation = TrailMateLocationReliabilityEngine.present(
            snapshot = TrailMateLocationSnapshot.providerDisabled(),
            routePointCount = 52,
            guidanceStatus = LocationBackedHikeStatus.WAITING,
            guidanceCaption = "授权定位后，可用当前位置辅助检查点推进。",
            nowEpochMillis = 7_000L
        )

        assertEquals("系统定位未开启", presentation.title)
        assertEquals("无法定位", presentation.statusLabel)
        assertEquals(LocationReliabilityLevel.BLOCKED, presentation.level)
        assertEquals("请先在系统设置里打开定位服务。", presentation.caption)
        assertEquals("打开系统定位", presentation.actionLabel)
        assertEquals(
            listOf(
                LocationReliabilityDetail(label = "定位精度", value = "等待信号"),
                LocationReliabilityDetail(label = "路线匹配", value = "等待定位"),
                LocationReliabilityDetail(label = "最近更新", value = "未定位")
            ),
            presentation.details
        )
        assertChinese(presentation)
    }

    private fun assertChinese(presentation: LocationReliabilityPresentation) {
        val text = buildString {
            append(presentation.title)
            append(presentation.statusLabel)
            append(presentation.caption)
            presentation.details.forEach {
                append(it.label)
                append(it.value)
            }
        }
        assertTrue(text.any { character -> character in '\u4e00'..'\u9fff' })
        assertFalse(text.contains("Location"))
        assertFalse(text.contains("GPS accuracy is low"))
    }
}
