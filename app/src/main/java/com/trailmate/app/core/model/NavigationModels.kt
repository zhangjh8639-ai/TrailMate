package com.trailmate.app.core.model

import java.time.Instant

@JvmInline
value class NavigationSessionId(val value: String) {
    init {
        require(value.isNotBlank()) { "Navigation session id must not be blank." }
    }
}

enum class NavigationState {
    Idle,
    Navigating,
    SuspectedOffRoute,
    ConfirmedOffRoute,
    ReturningOnTrack,
    Paused,
    Ended,
}

enum class NavigationDirection {
    Forward,
    Reverse,
}

enum class NavigationEvent {
    StartNavigation,
    SuspectOffRoute,
    ConfirmOffRoute,
    ReturnOnTrack,
    Pause,
    Resume,
    End,
}

data class NavigationSession(
    val id: NavigationSessionId,
    val routeId: RouteId,
    val startedAt: Instant,
    val state: NavigationState = NavigationState.Idle,
    val direction: NavigationDirection = NavigationDirection.Forward,
    val visibility: PrivacyVisibility = PrivacyVisibility.Private,
    val pausedFromState: NavigationState? = null,
) {
    fun reduce(event: NavigationEvent): NavigationSession {
        val nextState = NavigationStateReducer.reduce(
            state = state,
            event = event,
            pausedFromState = pausedFromState,
        )
        val nextPausedFrom = when (event) {
            NavigationEvent.Pause ->
                if (state == NavigationState.Paused) pausedFromState else state
            NavigationEvent.Resume,
            NavigationEvent.End -> null
            else -> pausedFromState
        }
        return copy(
            state = nextState,
            pausedFromState = nextPausedFrom,
        )
    }

    companion object {
        fun create(
            id: NavigationSessionId,
            routeId: RouteId,
            startedAt: Instant,
            direction: NavigationDirection = NavigationDirection.Forward,
        ): NavigationSession =
            NavigationSession(
                id = id,
                routeId = routeId,
                startedAt = startedAt,
                direction = direction,
                visibility = PrivacyVisibility.Private,
            )
    }
}

object NavigationStateReducer {
    fun reduce(
        state: NavigationState,
        event: NavigationEvent,
        pausedFromState: NavigationState? = null,
    ): NavigationState {
        if (state == NavigationState.Ended) return NavigationState.Ended

        return when (event) {
            NavigationEvent.StartNavigation ->
                if (state == NavigationState.Idle) NavigationState.Navigating else state
            NavigationEvent.SuspectOffRoute ->
                if (state == NavigationState.Navigating) NavigationState.SuspectedOffRoute else state
            NavigationEvent.ConfirmOffRoute ->
                if (state == NavigationState.SuspectedOffRoute) NavigationState.ConfirmedOffRoute else state
            NavigationEvent.ReturnOnTrack ->
                if (state == NavigationState.SuspectedOffRoute || state == NavigationState.ConfirmedOffRoute) {
                    NavigationState.ReturningOnTrack
                } else {
                    state
                }
            NavigationEvent.Pause ->
                when (state) {
                    NavigationState.Navigating,
                    NavigationState.SuspectedOffRoute,
                    NavigationState.ConfirmedOffRoute,
                    NavigationState.ReturningOnTrack -> NavigationState.Paused
                    else -> state
                }
            NavigationEvent.Resume ->
                if (state == NavigationState.Paused) pausedFromState ?: NavigationState.Navigating else state
            NavigationEvent.End -> NavigationState.Ended
        }
    }
}

enum class CompassDirection(val label: String) {
    North("北方向"),
    Northeast("东北方向"),
    East("东方向"),
    Southeast("东南方向"),
    South("南方向"),
    Southwest("西南方向"),
    West("西方向"),
    Northwest("西北方向"),
}

data class NavigationSnapshot(
    val sessionId: NavigationSessionId,
    val state: NavigationState,
    val currentCoordinate: GeoCoordinate,
    val gpsAccuracy: GpsAccuracy,
    val batteryLevel: BatteryLevel,
    val remainingDistance: Distance,
    val remainingElevation: Elevation,
    val nextWaypoint: RouteWaypoint?,
    val nearestExit: RouteExitPoint?,
    val deviationDistance: Distance?,
    val nearestRoutePoint: NearestRoutePointGuidance?,
    val updatedAt: Instant,
)

data class NearestRoutePointGuidance(
    val directionText: String,
    val distance: Distance,
) {
    init {
        require(directionText.isNotBlank()) { "Direction text must not be blank." }
    }
}
