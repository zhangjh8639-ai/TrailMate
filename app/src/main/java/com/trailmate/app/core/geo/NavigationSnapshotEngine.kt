package com.trailmate.app.core.geo

import com.trailmate.app.core.model.BatteryLevel
import com.trailmate.app.core.model.CompassDirection
import com.trailmate.app.core.model.Distance
import com.trailmate.app.core.model.NearestRoutePointGuidance
import com.trailmate.app.core.model.NavigationSession
import com.trailmate.app.core.model.NavigationSnapshot
import com.trailmate.app.core.model.NavigationState
import com.trailmate.app.core.model.RouteGeometry
import kotlin.math.floor

data class NavigationSnapshotInput(
    val session: NavigationSession,
    val geometry: RouteGeometry,
    val sample: LocationSample,
    val batteryLevel: BatteryLevel,
    val previousEvidence: OffRouteEvidence? = null,
    val previousProgress: Distance? = null,
    val thresholds: OffRouteThresholds = OffRouteThresholds(),
)

data class NavigationSnapshotEngineResult(
    val snapshot: NavigationSnapshot,
    val offRouteEvidence: OffRouteEvidence,
    val projection: RouteProjection,
)

object NavigationSnapshotEngine {
    fun calculate(input: NavigationSnapshotInput): NavigationSnapshotEngineResult {
        val projection = RouteProjector.project(
            geometry = input.geometry,
            coordinate = input.sample.coordinate,
            previousProgress = input.previousProgress,
        )
        val progress = RouteProgressCalculator.calculate(
            geometry = input.geometry,
            projection = projection,
            direction = input.session.direction,
        )
        val offRouteEvidence = OffRouteDetector.evaluate(
            sample = input.sample,
            projection = projection,
            thresholds = input.thresholds,
            previousEvidence = input.previousEvidence,
        )
        val snapshotState = snapshotStateFor(
            sessionState = input.session.state,
            status = offRouteEvidence.status,
        )
        val hasDeviationGuidance = offRouteEvidence.status == OffRouteStatus.Suspected ||
            offRouteEvidence.status == OffRouteStatus.Confirmed

        val snapshot = NavigationSnapshot(
            sessionId = input.session.id,
            state = snapshotState,
            currentCoordinate = input.sample.coordinate,
            gpsAccuracy = input.sample.accuracy,
            batteryLevel = input.batteryLevel,
            remainingDistance = progress.remainingDistance,
            remainingElevation = progress.remainingElevation,
            nextWaypoint = progress.nextWaypoint,
            nearestExit = progress.nearestExit,
            deviationDistance = if (hasDeviationGuidance) offRouteEvidence.distanceFromRoute else null,
            nearestRoutePoint = if (hasDeviationGuidance) {
                NearestRoutePointGuidance(
                    directionText = compassDirectionLabel(projection.bearingToRouteDegrees),
                    distance = offRouteEvidence.distanceFromRoute,
                )
            } else {
                null
            },
            updatedAt = input.sample.recordedAt,
        )

        return NavigationSnapshotEngineResult(
            snapshot = snapshot,
            offRouteEvidence = offRouteEvidence,
            projection = projection,
        )
    }

    private fun snapshotStateFor(
        sessionState: NavigationState,
        status: OffRouteStatus,
    ): NavigationState =
        when {
            sessionState == NavigationState.Paused ||
                sessionState == NavigationState.Ended ||
                sessionState == NavigationState.Idle ||
                sessionState == NavigationState.ReturningOnTrack -> sessionState
            status == OffRouteStatus.GpsUnreliable -> sessionState
            status == OffRouteStatus.OnRoute -> NavigationState.Navigating
            status == OffRouteStatus.Suspected -> NavigationState.SuspectedOffRoute
            status == OffRouteStatus.Confirmed -> NavigationState.ConfirmedOffRoute
            else -> sessionState
        }

    private fun compassDirectionLabel(bearingDegrees: Double): String {
        val normalizedBearing = ((bearingDegrees % 360.0) + 360.0) % 360.0
        val directionIndex = floor((normalizedBearing + 22.5) / 45.0).toInt() % CompassDirection.entries.size
        return CompassDirection.entries[directionIndex].label
    }
}
