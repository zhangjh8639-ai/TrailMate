package com.trailmate.app.core.geo

import com.trailmate.app.core.model.Distance
import com.trailmate.app.core.model.GeoCoordinate
import com.trailmate.app.core.model.GpsAccuracy
import java.time.Duration
import java.time.Instant

data class LocationSample(
    val coordinate: GeoCoordinate,
    val accuracy: GpsAccuracy,
    val recordedAt: Instant,
    val bearingDegrees: Double? = null,
    val speedMetersPerSecond: Double? = null,
)

data class OffRouteThresholds(
    val suspectedDistance: Distance = Distance.meters(70.0),
    val maxAcceptableAccuracy: GpsAccuracy = GpsAccuracy(30.0),
    val confirmationDuration: Duration = Duration.ofSeconds(45),
    val confirmationSamples: Int = 3,
) {
    init {
        require(confirmationDuration >= Duration.ZERO) {
            "Confirmation duration must be non-negative."
        }
        require(confirmationSamples >= 1) {
            "Confirmation sample count must be at least 1."
        }
    }
}

enum class OffRouteStatus {
    OnRoute,
    GpsUnreliable,
    Suspected,
    Confirmed,
}

data class OffRouteEvidence(
    val status: OffRouteStatus,
    val distanceFromRoute: Distance,
    val offRouteStartedAt: Instant? = null,
    val consecutiveOffRouteSamples: Int = 0,
)

object OffRouteDetector {
    fun evaluate(
        sample: LocationSample,
        projection: RouteProjection,
        thresholds: OffRouteThresholds = OffRouteThresholds(),
        previousEvidence: OffRouteEvidence? = null,
    ): OffRouteEvidence {
        if (sample.accuracy.meters > thresholds.maxAcceptableAccuracy.meters) {
            return OffRouteEvidence(
                status = OffRouteStatus.GpsUnreliable,
                distanceFromRoute = projection.distanceFromRoute,
            )
        }

        if (projection.distanceFromRoute.meters <= thresholds.suspectedDistance.meters) {
            return OffRouteEvidence(
                status = OffRouteStatus.OnRoute,
                distanceFromRoute = projection.distanceFromRoute,
            )
        }

        val previousOffRouteEvidence = previousEvidence?.takeIf {
            it.status == OffRouteStatus.Suspected || it.status == OffRouteStatus.Confirmed
        }
        val startedAt = previousOffRouteEvidence?.offRouteStartedAt ?: sample.recordedAt
        val consecutiveSamples = (previousOffRouteEvidence?.consecutiveOffRouteSamples ?: 0) + 1
        val elapsed = Duration.between(startedAt, sample.recordedAt).coerceAtLeast(Duration.ZERO)
        val status = if (
            elapsed >= thresholds.confirmationDuration &&
            consecutiveSamples >= thresholds.confirmationSamples
        ) {
            OffRouteStatus.Confirmed
        } else {
            OffRouteStatus.Suspected
        }

        return OffRouteEvidence(
            status = status,
            distanceFromRoute = projection.distanceFromRoute,
            offRouteStartedAt = startedAt,
            consecutiveOffRouteSamples = consecutiveSamples,
        )
    }
}
