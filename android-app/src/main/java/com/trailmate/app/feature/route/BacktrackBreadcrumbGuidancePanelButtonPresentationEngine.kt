package com.trailmate.app.feature.route

import com.trailmate.app.core.model.BacktrackBreadcrumbGuidancePresentation
import com.trailmate.app.core.model.BacktrackBreadcrumbGuidanceTone
import com.trailmate.app.core.model.TrackRecordingStatus

enum class BacktrackBreadcrumbGuidancePanelActionKind {
    NONE,
    VIEW_TRACK,
    REQUEST_LOCATION,
    CONTINUE_RECORDING
}

data class BacktrackBreadcrumbGuidancePanelButtonPresentation(
    val label: String,
    val visible: Boolean,
    val kind: BacktrackBreadcrumbGuidancePanelActionKind
)

object BacktrackBreadcrumbGuidancePanelButtonPresentationEngine {
    fun present(
        presentation: BacktrackBreadcrumbGuidancePresentation,
        trackRecordingStatus: TrackRecordingStatus,
        currentTrackActionLabel: String,
        trackActionEnabled: Boolean
    ): BacktrackBreadcrumbGuidancePanelButtonPresentation {
        val label = presentation.primaryActionLabel
        val kind = when {
            label.isBlank() -> BacktrackBreadcrumbGuidancePanelActionKind.NONE
            presentation.tone == BacktrackBreadcrumbGuidanceTone.UNAVAILABLE ->
                BacktrackBreadcrumbGuidancePanelActionKind.NONE
            presentation.tone == BacktrackBreadcrumbGuidanceTone.READY ->
                BacktrackBreadcrumbGuidancePanelActionKind.VIEW_TRACK
            presentation.tone == BacktrackBreadcrumbGuidanceTone.ALERT ->
                BacktrackBreadcrumbGuidancePanelActionKind.REQUEST_LOCATION
            trackRecordingStatus == TrackRecordingStatus.PAUSED &&
                trackActionEnabled &&
                currentTrackActionLabel == label ->
                BacktrackBreadcrumbGuidancePanelActionKind.CONTINUE_RECORDING
            trackRecordingStatus == TrackRecordingStatus.FINISHED ->
                BacktrackBreadcrumbGuidancePanelActionKind.VIEW_TRACK
            else -> BacktrackBreadcrumbGuidancePanelActionKind.NONE
        }

        return BacktrackBreadcrumbGuidancePanelButtonPresentation(
            label = label,
            visible = kind != BacktrackBreadcrumbGuidancePanelActionKind.NONE,
            kind = kind
        )
    }
}
