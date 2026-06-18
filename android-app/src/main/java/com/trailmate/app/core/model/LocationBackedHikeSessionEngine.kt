package com.trailmate.app.core.model

import java.util.Locale

enum class LocationBackedHikeStatus {
    WAITING,
    ON_ROUTE,
    LOW_ACCURACY,
    CHECK_ROUTE,
    FINISHED
}

data class HikeLocationFix(
    val distanceAlongRouteKm: Double,
    val crossTrackErrorMeters: Double,
    val horizontalAccuracyMeters: Double,
    val timestampEpochMillis: Long
)

data class LocationBackedHikeUpdate(
    val session: HikeSessionState,
    val status: LocationBackedHikeStatus,
    val caption: String
)

object LocationBackedHikeSessionEngine {
    private const val MAX_USABLE_ACCURACY_METERS = 50.0
    private const val OFF_ROUTE_METERS = 75.0
    private const val CHECKPOINT_RADIUS_KM = 0.10

    fun applyLocationFix(
        plan: HikePlanSummary,
        session: HikeSessionState,
        fix: HikeLocationFix
    ): LocationBackedHikeUpdate {
        when (session.status) {
            HikeSessionStatus.READY -> return LocationBackedHikeUpdate(
                session = session,
                status = LocationBackedHikeStatus.WAITING,
                caption = "Start the hike before location updates affect checkpoint progress."
            )
            HikeSessionStatus.PAUSED -> return LocationBackedHikeUpdate(
                session = session,
                status = LocationBackedHikeStatus.WAITING,
                caption = "Hike is paused; location updates keep manual checkpoint progress unchanged."
            )
            HikeSessionStatus.COMPLETED -> return LocationBackedHikeUpdate(
                session = session,
                status = LocationBackedHikeStatus.FINISHED,
                caption = "Hike is already complete."
            )
            HikeSessionStatus.ACTIVE -> Unit
        }

        if (!fix.isUsableDistance() || fix.horizontalAccuracyMeters > MAX_USABLE_ACCURACY_METERS) {
            return LocationBackedHikeUpdate(
                session = session,
                status = LocationBackedHikeStatus.LOW_ACCURACY,
                caption = "Location accuracy is ${fix.horizontalAccuracyMeters.formatMeters()}; keeping manual progress."
            )
        }

        if (fix.crossTrackErrorMeters > OFF_ROUTE_METERS) {
            return LocationBackedHikeUpdate(
                session = session,
                status = LocationBackedHikeStatus.CHECK_ROUTE,
                caption = "GPS is ${fix.crossTrackErrorMeters.formatMeters()} from the planned route. Check the map and trail signs."
            )
        }

        val nextIndex = nextReachedCheckpointIndex(
            plan = plan,
            session = session,
            distanceAlongRouteKm = fix.distanceAlongRouteKm
        )
        val updatedSession = if (nextIndex > session.reachedCheckpointIndex) {
            session.copy(
                status = if (nextIndex >= plan.checkpoints.lastIndex) {
                    HikeSessionStatus.COMPLETED
                } else {
                    session.status
                },
                reachedCheckpointIndex = nextIndex
            )
        } else {
            session
        }
        val current = HikeSessionEngine.currentCheckpoint(plan, updatedSession)
        val status = if (updatedSession.status == HikeSessionStatus.COMPLETED) {
            LocationBackedHikeStatus.FINISHED
        } else {
            LocationBackedHikeStatus.ON_ROUTE
        }
        val caption = current?.let { checkpoint ->
            "Location aligned with ${checkpoint.title}; ${fix.distanceAlongRouteKm.formatKm()} along route."
        } ?: "Location aligned with the route."

        return LocationBackedHikeUpdate(
            session = updatedSession,
            status = status,
            caption = caption
        )
    }

    private fun nextReachedCheckpointIndex(
        plan: HikePlanSummary,
        session: HikeSessionState,
        distanceAlongRouteKm: Double
    ): Int {
        if (plan.checkpoints.isEmpty()) {
            return 0
        }

        var reachedIndex = session.reachedCheckpointIndex.coerceIn(0, plan.checkpoints.lastIndex)
        plan.checkpoints.forEachIndexed { index, checkpoint ->
            if (index > reachedIndex && distanceAlongRouteKm + CHECKPOINT_RADIUS_KM >= checkpoint.distanceKm) {
                reachedIndex = index
            }
        }

        return reachedIndex
    }

    private fun HikeLocationFix.isUsableDistance(): Boolean =
        distanceAlongRouteKm.isFinite() &&
            distanceAlongRouteKm >= 0.0 &&
            horizontalAccuracyMeters.isFinite() &&
            horizontalAccuracyMeters >= 0.0 &&
            crossTrackErrorMeters.isFinite() &&
            crossTrackErrorMeters >= 0.0

    private fun Double.formatMeters(): String =
        "${this.toInt()} m"

    private fun Double.formatKm(): String =
        String.format(Locale.US, "%.1f km", this)
}
