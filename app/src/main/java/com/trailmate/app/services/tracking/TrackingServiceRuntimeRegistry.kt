package com.trailmate.app.services.tracking

import com.trailmate.app.core.model.NavigationDirection
import com.trailmate.app.core.model.NavigationSessionId
import com.trailmate.app.core.model.RouteId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class TrackingServiceRuntimeSnapshot(
    val sessionId: NavigationSessionId,
    val routeId: RouteId,
    val startedAtEpochMillis: Long,
    val direction: NavigationDirection,
)

data class TrackingServiceRuntimeStatus(
    val snapshot: TrackingServiceRuntimeSnapshot? = null,
    val sequence: Long = 0,
)

class TrackingServiceRuntimeRegistry {
    private val mutableStatus = MutableStateFlow(TrackingServiceRuntimeStatus())

    val status: StateFlow<TrackingServiceRuntimeStatus> = mutableStatus.asStateFlow()

    fun markRunning(request: TrackingServiceStartRequest) {
        updateStatus(
            snapshot = TrackingServiceRuntimeSnapshot(
                sessionId = request.sessionId,
                routeId = request.routeId,
                startedAtEpochMillis = request.startedAtEpochMillis,
                direction = request.direction,
            ),
        )
    }

    fun clearRunning() {
        updateStatus(snapshot = null)
    }

    fun snapshot(): TrackingServiceRuntimeSnapshot? = mutableStatus.value.snapshot

    private fun updateStatus(snapshot: TrackingServiceRuntimeSnapshot?) {
        val current = mutableStatus.value
        mutableStatus.value = TrackingServiceRuntimeStatus(
            snapshot = snapshot,
            sequence = current.sequence + 1,
        )
    }

    companion object {
        val Default = TrackingServiceRuntimeRegistry()
    }
}
