package com.trailmate.app.core.geo

import com.trailmate.app.core.model.BatteryLevel
import com.trailmate.app.core.model.Distance
import com.trailmate.app.core.model.NavigationEvent
import com.trailmate.app.core.model.NavigationSession
import com.trailmate.app.core.model.NavigationSnapshot
import com.trailmate.app.core.model.NavigationState
import com.trailmate.app.core.model.RouteGeometry

data class NavigationRuntimeState(
    val session: NavigationSession,
    val geometry: RouteGeometry,
    val latestSnapshot: NavigationSnapshot? = null,
    val latestOffRouteEvidence: OffRouteEvidence? = null,
    val latestRouteProgress: Distance? = null,
    val lastReliableOffRouteEvidence: OffRouteEvidence? = null,
)

sealed interface NavigationRuntimeAction {
    data class Start(
        val session: NavigationSession,
        val geometry: RouteGeometry,
    ) : NavigationRuntimeAction

    data class LocationUpdated(
        val sample: LocationSample,
        val batteryLevel: BatteryLevel,
        val thresholds: OffRouteThresholds = OffRouteThresholds(),
    ) : NavigationRuntimeAction

    data object Pause : NavigationRuntimeAction
    data object Resume : NavigationRuntimeAction
    data object EnterReturnMode : NavigationRuntimeAction
    data object End : NavigationRuntimeAction
}

object NavigationRuntimeReducer {
    fun reduce(
        state: NavigationRuntimeState?,
        action: NavigationRuntimeAction,
    ): NavigationRuntimeState? =
        when (action) {
            is NavigationRuntimeAction.Start -> start(action)
            is NavigationRuntimeAction.LocationUpdated -> state?.reduceLocation(action)
            NavigationRuntimeAction.Pause -> state?.copy(
                session = state.session.reduce(NavigationEvent.Pause),
            )
            NavigationRuntimeAction.Resume -> state?.copy(
                session = state.session.reduce(NavigationEvent.Resume),
            )
            NavigationRuntimeAction.EnterReturnMode -> state?.copy(
                session = state.session.enterReturnMode(),
            )
            NavigationRuntimeAction.End -> state?.end()
        }

    private fun start(action: NavigationRuntimeAction.Start): NavigationRuntimeState =
        NavigationRuntimeState(
            session = action.session.reduce(NavigationEvent.StartNavigation),
            geometry = action.geometry,
        )

    private fun NavigationRuntimeState.reduceLocation(
        action: NavigationRuntimeAction.LocationUpdated,
    ): NavigationRuntimeState {
        if (session.state == NavigationState.Ended) return this

        val result = NavigationSnapshotEngine.calculate(
            NavigationSnapshotInput(
                session = session,
                geometry = geometry,
                sample = action.sample,
                batteryLevel = action.batteryLevel,
                previousEvidence = lastReliableOffRouteEvidence ?: latestOffRouteEvidence,
                previousProgress = latestRouteProgress,
                thresholds = action.thresholds,
            ),
        )

        return copy(
            session = session.copy(state = result.snapshot.state),
            latestSnapshot = result.snapshot,
            latestOffRouteEvidence = result.offRouteEvidence,
            latestRouteProgress = nextRouteProgress(result),
            lastReliableOffRouteEvidence = nextReliableEvidence(result.offRouteEvidence),
        )
    }

    private fun NavigationRuntimeState.end(): NavigationRuntimeState =
        copy(
            session = session.reduce(NavigationEvent.End),
            latestSnapshot = null,
            latestOffRouteEvidence = null,
            latestRouteProgress = null,
            lastReliableOffRouteEvidence = null,
        )

    private fun NavigationRuntimeState.nextRouteProgress(
        result: NavigationSnapshotEngineResult,
    ): Distance =
        if (result.offRouteEvidence.status == OffRouteStatus.GpsUnreliable) {
            latestRouteProgress ?: result.projection.progress
        } else {
            result.projection.progress
        }

    private fun NavigationRuntimeState.nextReliableEvidence(
        evidence: OffRouteEvidence,
    ): OffRouteEvidence? =
        when (evidence.status) {
            OffRouteStatus.Suspected,
            OffRouteStatus.Confirmed -> evidence
            OffRouteStatus.OnRoute -> null
            OffRouteStatus.GpsUnreliable -> lastReliableOffRouteEvidence
        }

    private fun NavigationSession.enterReturnMode(): NavigationSession =
        when (state) {
            NavigationState.Navigating,
            NavigationState.SuspectedOffRoute,
            NavigationState.ConfirmedOffRoute,
            NavigationState.ReturningOnTrack -> copy(
                state = NavigationState.ReturningOnTrack,
                pausedFromState = null,
            )
            NavigationState.Idle,
            NavigationState.Paused,
            NavigationState.Ended -> this
        }
}
