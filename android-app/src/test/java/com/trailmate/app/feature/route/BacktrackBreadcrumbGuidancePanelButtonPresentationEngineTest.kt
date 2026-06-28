package com.trailmate.app.feature.route

import com.trailmate.app.core.model.BacktrackBreadcrumbGuidanceDetail
import com.trailmate.app.core.model.BacktrackBreadcrumbGuidancePresentation
import com.trailmate.app.core.model.BacktrackBreadcrumbGuidanceTone
import com.trailmate.app.core.model.TrackRecordingStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BacktrackBreadcrumbGuidancePanelButtonPresentationEngineTest {
    @Test
    fun readyBreadcrumbShowsViewTrackAction() {
        val button = BacktrackBreadcrumbGuidancePanelButtonPresentationEngine.present(
            presentation = breadcrumb(
                primaryActionLabel = "查看实走轨迹",
                tone = BacktrackBreadcrumbGuidanceTone.READY
            ),
            trackRecordingStatus = TrackRecordingStatus.RECORDING,
            currentTrackActionLabel = "暂停记录",
            trackActionEnabled = true
        )

        assertEquals("查看实走轨迹", button.label)
        assertEquals(BacktrackBreadcrumbGuidancePanelActionKind.VIEW_TRACK, button.kind)
        assertTrue(button.visible)
    }

    @Test
    fun staleBreadcrumbShowsRefreshLocationAction() {
        val button = BacktrackBreadcrumbGuidancePanelButtonPresentationEngine.present(
            presentation = breadcrumb(
                primaryActionLabel = "刷新定位",
                tone = BacktrackBreadcrumbGuidanceTone.ALERT
            ),
            trackRecordingStatus = TrackRecordingStatus.RECORDING,
            currentTrackActionLabel = "暂停记录",
            trackActionEnabled = true
        )

        assertEquals("刷新定位", button.label)
        assertEquals(BacktrackBreadcrumbGuidancePanelActionKind.REQUEST_LOCATION, button.kind)
        assertTrue(button.visible)
    }

    @Test
    fun pausedBreadcrumbShowsContinueRecordingActionWhenTrackActionIsEnabled() {
        val button = BacktrackBreadcrumbGuidancePanelButtonPresentationEngine.present(
            presentation = breadcrumb(
                primaryActionLabel = "继续记录",
                tone = BacktrackBreadcrumbGuidanceTone.CAUTION
            ),
            trackRecordingStatus = TrackRecordingStatus.PAUSED,
            currentTrackActionLabel = "继续记录",
            trackActionEnabled = true
        )

        assertEquals("继续记录", button.label)
        assertEquals(BacktrackBreadcrumbGuidancePanelActionKind.CONTINUE_RECORDING, button.kind)
        assertTrue(button.visible)
    }

    @Test
    fun finishedBreadcrumbShowsSavedTrackReviewAction() {
        val button = BacktrackBreadcrumbGuidancePanelButtonPresentationEngine.present(
            presentation = breadcrumb(
                primaryActionLabel = "查看轨迹",
                tone = BacktrackBreadcrumbGuidanceTone.CAUTION
            ),
            trackRecordingStatus = TrackRecordingStatus.FINISHED,
            currentTrackActionLabel = "开始记录",
            trackActionEnabled = true
        )

        assertEquals("查看轨迹", button.label)
        assertEquals(BacktrackBreadcrumbGuidancePanelActionKind.VIEW_TRACK, button.kind)
        assertTrue(button.visible)
    }

    @Test
    fun activeWarmingBreadcrumbHidesContinueRecordingPseudoAction() {
        val button = BacktrackBreadcrumbGuidancePanelButtonPresentationEngine.present(
            presentation = breadcrumb(
                primaryActionLabel = "继续记录",
                tone = BacktrackBreadcrumbGuidanceTone.CAUTION
            ),
            trackRecordingStatus = TrackRecordingStatus.RECORDING,
            currentTrackActionLabel = "暂停记录",
            trackActionEnabled = true
        )

        assertEquals("继续记录", button.label)
        assertEquals(BacktrackBreadcrumbGuidancePanelActionKind.NONE, button.kind)
        assertFalse(button.visible)
    }

    @Test
    fun pausedBreadcrumbHidesContinueRecordingActionWhenTrackActionIsDisabled() {
        val button = BacktrackBreadcrumbGuidancePanelButtonPresentationEngine.present(
            presentation = breadcrumb(
                primaryActionLabel = "继续记录",
                tone = BacktrackBreadcrumbGuidanceTone.CAUTION
            ),
            trackRecordingStatus = TrackRecordingStatus.PAUSED,
            currentTrackActionLabel = "继续记录",
            trackActionEnabled = false
        )

        assertEquals("继续记录", button.label)
        assertEquals(BacktrackBreadcrumbGuidancePanelActionKind.NONE, button.kind)
        assertFalse(button.visible)
    }

    @Test
    fun pausedBreadcrumbHidesContinueRecordingActionWhenCurrentTrackActionNeedsRepair() {
        val button = BacktrackBreadcrumbGuidancePanelButtonPresentationEngine.present(
            presentation = breadcrumb(
                primaryActionLabel = "继续记录",
                tone = BacktrackBreadcrumbGuidanceTone.CAUTION
            ),
            trackRecordingStatus = TrackRecordingStatus.PAUSED,
            currentTrackActionLabel = "授权定位",
            trackActionEnabled = true
        )

        assertEquals("继续记录", button.label)
        assertEquals(BacktrackBreadcrumbGuidancePanelActionKind.NONE, button.kind)
        assertFalse(button.visible)
    }

    @Test
    fun unavailableBreadcrumbHidesRouteMapPseudoAction() {
        val button = BacktrackBreadcrumbGuidancePanelButtonPresentationEngine.present(
            presentation = breadcrumb(
                primaryActionLabel = "查看路线地图",
                tone = BacktrackBreadcrumbGuidanceTone.UNAVAILABLE
            ),
            trackRecordingStatus = TrackRecordingStatus.IDLE,
            currentTrackActionLabel = "开始记录",
            trackActionEnabled = true
        )

        assertEquals("查看路线地图", button.label)
        assertEquals(BacktrackBreadcrumbGuidancePanelActionKind.NONE, button.kind)
        assertFalse(button.visible)
    }

    @Test
    fun unavailablePausedBreadcrumbHidesRouteMapPseudoAction() {
        val button = BacktrackBreadcrumbGuidancePanelButtonPresentationEngine.present(
            presentation = breadcrumb(
                primaryActionLabel = "查看路线地图",
                tone = BacktrackBreadcrumbGuidanceTone.UNAVAILABLE
            ),
            trackRecordingStatus = TrackRecordingStatus.PAUSED,
            currentTrackActionLabel = "继续记录",
            trackActionEnabled = true
        )

        assertEquals("查看路线地图", button.label)
        assertEquals(BacktrackBreadcrumbGuidancePanelActionKind.NONE, button.kind)
        assertFalse(button.visible)
    }

    @Test
    fun unavailableFinishedBreadcrumbHidesRouteMapPseudoAction() {
        val button = BacktrackBreadcrumbGuidancePanelButtonPresentationEngine.present(
            presentation = breadcrumb(
                primaryActionLabel = "查看路线地图",
                tone = BacktrackBreadcrumbGuidanceTone.UNAVAILABLE
            ),
            trackRecordingStatus = TrackRecordingStatus.FINISHED,
            currentTrackActionLabel = "开始记录",
            trackActionEnabled = true
        )

        assertEquals("查看路线地图", button.label)
        assertEquals(BacktrackBreadcrumbGuidancePanelActionKind.NONE, button.kind)
        assertFalse(button.visible)
    }

    private fun breadcrumb(
        primaryActionLabel: String,
        tone: BacktrackBreadcrumbGuidanceTone
    ) = BacktrackBreadcrumbGuidancePresentation(
        visible = true,
        title = "原路参照",
        statusLabel = "原路参照可用",
        caption = "已记录的实走轨迹可作为原路返回参照。",
        primaryActionLabel = primaryActionLabel,
        tone = tone,
        details = listOf(
            BacktrackBreadcrumbGuidanceDetail(label = "已记录", value = "0.4 km")
        )
    )
}
