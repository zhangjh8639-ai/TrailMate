package com.trailmate.app.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RouteDeviationRecoveryEngineTest {
    @Test
    fun presentsOffRouteRecoveryStepsWhenRouteCheckFails() {
        val presentation = RouteDeviationRecoveryEngine.present(
            status = LocationBackedHikeStatus.CHECK_ROUTE,
            fix = HikeLocationFix(
                distanceAlongRouteKm = 5.12,
                crossTrackErrorMeters = 112.0,
                horizontalAccuracyMeters = 8.0,
                timestampEpochMillis = 1_000L
            ),
            safetyShareAvailable = true
        )

        assertTrue(presentation.visible)
        assertEquals("偏离恢复", presentation.title)
        assertEquals("停止自动推进", presentation.statusLabel)
        assertEquals("疑似偏离路线约 112 m，请先停下核对当前位置。", presentation.caption)
        assertEquals("分享当前位置", presentation.primaryActionLabel)
        assertEquals(
            listOf(
                RouteDeviationRecoveryStep(label = "确认方向", value = "查看地图、路标和现场路径，确认是否仍在计划路线附近。"),
                RouteDeviationRecoveryStep(label = "回到最近路线", value = "沿安全可见路径回到计划路线附近，避免直接抄近路。"),
                RouteDeviationRecoveryStep(label = "人工推进", value = "回到路线后再手动标记检查点，TrailMate 不会自动推进。")
            ),
            presentation.steps
        )
        assertEquals(
            listOf(
                RouteDeviationRecoveryDetail(label = "偏离距离", value = "约 112 m"),
                RouteDeviationRecoveryDetail(label = "路线进度", value = "5.1 km"),
                RouteDeviationRecoveryDetail(label = "定位精度", value = "约 8 m")
            ),
            presentation.details
        )
        assertEquals(
            listOf(
                RouteDeviationRecoveryAction(
                    kind = RouteDeviationRecoveryActionKind.STOP_AND_CONFIRM,
                    label = "停下核对",
                    value = "暂停前进，确认地图、路标和现场路径。",
                    emphasized = true
                ),
                RouteDeviationRecoveryAction(
                    kind = RouteDeviationRecoveryActionKind.RETURN_TO_ROUTE,
                    label = "回到最近路线",
                    value = "计划路线在约 112 m 外，沿安全可见路径返回。"
                ),
                RouteDeviationRecoveryAction(
                    kind = RouteDeviationRecoveryActionKind.SHARE_LOCATION,
                    label = "分享当前位置",
                    value = "发送坐标和路线信息。"
                )
            ),
            presentation.actions
        )
        assertChinese(presentation)
    }

    @Test
    fun keepsRecoveryHiddenWhenRouteCheckIsHealthy() {
        val presentation = RouteDeviationRecoveryEngine.present(
            status = LocationBackedHikeStatus.ON_ROUTE,
            fix = HikeLocationFix(
                distanceAlongRouteKm = 5.12,
                crossTrackErrorMeters = 12.0,
                horizontalAccuracyMeters = 8.0,
                timestampEpochMillis = 1_000L
            ),
            safetyShareAvailable = true,
            wasRecentlyOffRoute = false
        )

        assertFalse(presentation.visible)
        assertEquals(emptyList<RouteDeviationRecoveryStep>(), presentation.steps)
        assertEquals(emptyList<RouteDeviationRecoveryAction>(), presentation.actions)
    }

    @Test
    fun hidesShareActionWhenSafetyShareIsUnavailable() {
        val presentation = RouteDeviationRecoveryEngine.present(
            status = LocationBackedHikeStatus.CHECK_ROUTE,
            fix = HikeLocationFix(
                distanceAlongRouteKm = 5.12,
                crossTrackErrorMeters = 112.0,
                horizontalAccuracyMeters = 8.0,
                timestampEpochMillis = 1_000L
            ),
            safetyShareAvailable = false
        )

        assertTrue(presentation.visible)
        assertEquals(
            listOf(RouteDeviationRecoveryActionKind.STOP_AND_CONFIRM, RouteDeviationRecoveryActionKind.RETURN_TO_ROUTE),
            presentation.actions.map { action -> action.kind }
        )
        assertFalse(presentation.actions.any { action -> action.label == "分享当前位置" })
    }

    @Test
    fun keepsRecoveryHiddenWhenWaitingEvenIfLastFixIsLowAccuracy() {
        val presentation = RouteDeviationRecoveryEngine.present(
            status = LocationBackedHikeStatus.WAITING,
            fix = HikeLocationFix(
                distanceAlongRouteKm = 0.0,
                crossTrackErrorMeters = 0.0,
                horizontalAccuracyMeters = 120.0,
                timestampEpochMillis = 1_000L
            ),
            safetyShareAvailable = false
        )

        assertFalse(presentation.visible)
        assertEquals(emptyList<RouteDeviationRecoveryAction>(), presentation.actions)
    }

    @Test
    fun presentsRejoinedRouteConfirmationAfterRecentDeviation() {
        val presentation = RouteDeviationRecoveryEngine.present(
            status = LocationBackedHikeStatus.ON_ROUTE,
            fix = HikeLocationFix(
                distanceAlongRouteKm = 5.38,
                crossTrackErrorMeters = 18.0,
                horizontalAccuracyMeters = 7.0,
                timestampEpochMillis = 2_000L
            ),
            safetyShareAvailable = true,
            wasRecentlyOffRoute = true
        )

        assertTrue(presentation.visible)
        assertEquals(RouteDeviationRecoveryTone.REJOINED, presentation.tone)
        assertEquals("已回到路线", presentation.title)
        assertEquals("可继续推进", presentation.statusLabel)
        assertEquals("当前位置已回到计划路线附近，继续观察下一段路况。", presentation.caption)
        assertEquals("继续导航", presentation.primaryActionLabel)
        assertEquals(
            listOf(
                RouteDeviationRecoveryStep(label = "确认下一检查点", value = "核对当前提示点和现场路标，再继续前进。"),
                RouteDeviationRecoveryStep(label = "保持路线校验", value = "如果再次偏离，TrailMate 会暂停自动推进。")
            ),
            presentation.steps
        )
        assertEquals(
            listOf(
                RouteDeviationRecoveryDetail(label = "路线进度", value = "5.4 km"),
                RouteDeviationRecoveryDetail(label = "定位精度", value = "约 7 m")
            ),
            presentation.details
        )
        assertEquals(
            listOf(
                RouteDeviationRecoveryAction(
                    kind = RouteDeviationRecoveryActionKind.CONTINUE_NAVIGATION,
                    label = "继续导航",
                    value = "已回到路线附近，先核对下一检查点。",
                    emphasized = true
                ),
                RouteDeviationRecoveryAction(
                    kind = RouteDeviationRecoveryActionKind.CHECK_NEXT_CHECKPOINT,
                    label = "确认下一检查点",
                    value = "以现场路标和地图一致为准再继续。"
                )
            ),
            presentation.actions
        )
        assertChinese(presentation)
    }

    @Test
    fun fallsBackToGeneralCopyWhenOffRouteFixIsMissing() {
        val presentation = RouteDeviationRecoveryEngine.present(
            status = LocationBackedHikeStatus.CHECK_ROUTE,
            fix = null,
            safetyShareAvailable = false
        )

        assertTrue(presentation.visible)
        assertEquals("疑似偏离路线，请先停下核对当前位置。", presentation.caption)
        assertEquals("授权定位", presentation.primaryActionLabel)
        assertEquals(emptyList<RouteDeviationRecoveryDetail>(), presentation.details)
        assertEquals(
            listOf(
                RouteDeviationRecoveryAction(
                    kind = RouteDeviationRecoveryActionKind.STOP_AND_CONFIRM,
                    label = "停下核对",
                    value = "暂停前进，先确认当前位置。",
                    emphasized = true
                ),
                RouteDeviationRecoveryAction(
                    kind = RouteDeviationRecoveryActionKind.WAIT_FOR_GPS,
                    label = "等待定位",
                    value = "获得可靠坐标后再判断回到路线方向。"
                )
            ),
            presentation.actions
        )
        assertChinese(presentation)
    }

    @Test
    fun presentsLowAccuracyRecoveryWhenFixAccuracyIsTooLow() {
        val presentation = RouteDeviationRecoveryEngine.present(
            status = LocationBackedHikeStatus.LOW_ACCURACY,
            fix = HikeLocationFix(
                distanceAlongRouteKm = 5.12,
                crossTrackErrorMeters = 112.0,
                horizontalAccuracyMeters = 120.0,
                timestampEpochMillis = 1_000L
            ),
            safetyShareAvailable = true
        )

        assertTrue(presentation.visible)
        assertEquals("等待定位稳定", presentation.title)
        assertEquals("先校准位置", presentation.statusLabel)
        assertEquals("当前定位精度约 120 m，暂不判断是否偏离路线。", presentation.caption)
        assertEquals("重新定位", presentation.primaryActionLabel)
        assertFalse(presentation.caption.contains("偏离路线约"))
        assertFalse(presentation.details.any { detail -> detail.label == "偏离距离" })
        assertEquals(
            listOf(
                RouteDeviationRecoveryStep(label = "移动到开阔处", value = "等待定位精度稳定后再核对路线。"),
                RouteDeviationRecoveryStep(label = "暂不推进检查点", value = "在定位稳定前不要手动确认下一检查点。")
            ),
            presentation.steps
        )
        assertEquals(
            listOf(
                RouteDeviationRecoveryAction(
                    kind = RouteDeviationRecoveryActionKind.STOP_AND_CONFIRM,
                    label = "停下等待",
                    value = "不要继续推进检查点，先让定位稳定。",
                    emphasized = true
                ),
                RouteDeviationRecoveryAction(
                    kind = RouteDeviationRecoveryActionKind.WAIT_FOR_GPS,
                    label = "重新定位",
                    value = "移动到开阔处，等待精度优于 50 m。"
                )
            ),
            presentation.actions
        )
        assertChinese(presentation)
    }

    @Test
    fun presentsLowAccuracyRecoveryWhenCheckRouteFixAccuracyIsTooLow() {
        val presentation = RouteDeviationRecoveryEngine.present(
            status = LocationBackedHikeStatus.CHECK_ROUTE,
            fix = HikeLocationFix(
                distanceAlongRouteKm = 5.12,
                crossTrackErrorMeters = 112.0,
                horizontalAccuracyMeters = 120.0,
                timestampEpochMillis = 1_000L
            ),
            safetyShareAvailable = true
        )

        assertTrue(presentation.visible)
        assertEquals("等待定位稳定", presentation.title)
        assertEquals("当前定位精度约 120 m，暂不判断是否偏离路线。", presentation.caption)
        assertEquals("重新定位", presentation.primaryActionLabel)
        assertFalse(presentation.caption.contains("偏离路线约"))
        assertFalse(presentation.details.any { detail -> detail.label == "偏离距离" })
        assertEquals(
            listOf(RouteDeviationRecoveryActionKind.STOP_AND_CONFIRM, RouteDeviationRecoveryActionKind.WAIT_FOR_GPS),
            presentation.actions.map { action -> action.kind }
        )
        assertChinese(presentation)
    }

    private fun assertChinese(presentation: RouteDeviationRecoveryPresentation) {
        val text = buildString {
            append(presentation.title)
            append(presentation.statusLabel)
            append(presentation.caption)
            append(presentation.primaryActionLabel)
            presentation.steps.forEach {
                append(it.label)
                append(it.value)
            }
            presentation.actions.forEach {
                append(it.label)
                append(it.value)
            }
        }
        assertTrue(text.any { character -> character in '\u4e00'..'\u9fff' })
        assertFalse(text.contains("off route"))
        assertFalse(text.contains("recovery"))
    }
}
