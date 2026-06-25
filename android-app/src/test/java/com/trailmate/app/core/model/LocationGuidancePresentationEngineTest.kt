package com.trailmate.app.core.model

import org.junit.Assert.assertEquals
import org.junit.Test

class LocationGuidancePresentationEngineTest {
    @Test
    fun presentsWaitingStateAsChineseRouteCheck() {
        val presentation = LocationGuidancePresentationEngine.present(
            status = LocationBackedHikeStatus.WAITING,
            caption = "先开始徒步，再用定位推进检查点。"
        )

        assertEquals("等待定位推进", presentation.title)
        assertEquals("未开始", presentation.statusLabel)
        assertEquals(LocationGuidanceTone.NEUTRAL, presentation.tone)
        assertEquals("先开始徒步，再用定位推进检查点。", presentation.caption)
    }

    @Test
    fun presentsOnRouteStateAsNormalRouteCheck() {
        val presentation = LocationGuidancePresentationEngine.present(
            status = LocationBackedHikeStatus.ON_ROUTE,
            caption = "已对齐「补给检查」，路线进度 3.1 km。"
        )

        assertEquals("路线校验正常", presentation.title)
        assertEquals("在线路上", presentation.statusLabel)
        assertEquals(LocationGuidanceTone.GOOD, presentation.tone)
    }

    @Test
    fun presentsLowAccuracyStateAsHoldProgressWarning() {
        val presentation = LocationGuidancePresentationEngine.present(
            status = LocationBackedHikeStatus.LOW_ACCURACY,
            caption = "定位精度约 120 m，暂不推进检查点。"
        )

        assertEquals("定位精度不足", presentation.title)
        assertEquals("暂不推进", presentation.statusLabel)
        assertEquals(LocationGuidanceTone.WARNING, presentation.tone)
    }

    @Test
    fun presentsOffRouteStateAsRouteCheckWarning() {
        val presentation = LocationGuidancePresentationEngine.present(
            status = LocationBackedHikeStatus.CHECK_ROUTE,
            caption = "当前位置距计划路线约 110 m，请核对地图、路标和现场路径。"
        )

        assertEquals("疑似偏离路线", presentation.title)
        assertEquals("请核对路线", presentation.statusLabel)
        assertEquals(LocationGuidanceTone.DANGER, presentation.tone)
    }

    @Test
    fun presentsFinishedStateAsCompleted() {
        val presentation = LocationGuidancePresentationEngine.present(
            status = LocationBackedHikeStatus.FINISHED,
            caption = "本次路线已完成。"
        )

        assertEquals("路线已完成", presentation.title)
        assertEquals("已完成", presentation.statusLabel)
        assertEquals(LocationGuidanceTone.GOOD, presentation.tone)
    }
}
