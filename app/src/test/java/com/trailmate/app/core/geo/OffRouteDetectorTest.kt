package com.trailmate.app.core.geo

import com.trailmate.app.core.model.Distance
import com.trailmate.app.core.model.GeoCoordinate
import com.trailmate.app.core.model.GpsAccuracy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Duration
import java.time.Instant

class OffRouteDetectorTest {
    @Test
    fun poorGpsAccuracyDoesNotTriggerOffRouteWarning() {
        val evidence = OffRouteDetector.evaluate(
            sample = sampleAt(Instant.parse("2026-07-01T01:00:00Z"), accuracyMeters = 80.0),
            projection = projection(distanceFromRouteMeters = 120.0),
            thresholds = thresholds(),
        )

        assertEquals(OffRouteStatus.GpsUnreliable, evidence.status)
        assertNull(evidence.offRouteStartedAt)
    }

    @Test
    fun poorGpsAccuracyResetsPreviousOffRouteEvidence() {
        val previous = OffRouteEvidence(
            status = OffRouteStatus.Confirmed,
            distanceFromRoute = Distance.meters(95.0),
            offRouteStartedAt = Instant.parse("2026-07-01T01:00:00Z"),
            consecutiveOffRouteSamples = 4,
        )

        val evidence = OffRouteDetector.evaluate(
            sample = sampleAt(Instant.parse("2026-07-01T01:01:00Z"), accuracyMeters = 80.0),
            projection = projection(distanceFromRouteMeters = 120.0),
            thresholds = thresholds(),
            previousEvidence = previous,
        )

        assertEquals(OffRouteStatus.GpsUnreliable, evidence.status)
        assertNull(evidence.offRouteStartedAt)
        assertEquals(0, evidence.consecutiveOffRouteSamples)
    }

    @Test
    fun singleAccurateFarPointIsSuspectedOffRoute() {
        val now = Instant.parse("2026-07-01T01:00:00Z")

        val evidence = OffRouteDetector.evaluate(
            sample = sampleAt(now, accuracyMeters = 8.0),
            projection = projection(distanceFromRouteMeters = 82.0),
            thresholds = thresholds(),
        )

        assertEquals(OffRouteStatus.Suspected, evidence.status)
        assertEquals(now, evidence.offRouteStartedAt)
    }

    @Test
    fun sustainedAccurateDeviationBecomesConfirmedOffRoute() {
        val startedAt = Instant.parse("2026-07-01T01:00:00Z")
        val now = startedAt.plusSeconds(50)
        val previous = OffRouteEvidence(
            status = OffRouteStatus.Suspected,
            distanceFromRoute = Distance.meters(86.0),
            offRouteStartedAt = startedAt,
            consecutiveOffRouteSamples = 2,
        )

        val evidence = OffRouteDetector.evaluate(
            sample = sampleAt(now, accuracyMeters = 8.0),
            projection = projection(distanceFromRouteMeters = 90.0),
            thresholds = thresholds(),
            previousEvidence = previous,
        )

        assertEquals(OffRouteStatus.Confirmed, evidence.status)
        assertEquals(startedAt, evidence.offRouteStartedAt)
        assertEquals(3, evidence.consecutiveOffRouteSamples)
    }

    @Test
    fun frequentSamplesDoNotConfirmBeforeDurationThreshold() {
        val startedAt = Instant.parse("2026-07-01T01:00:00Z")
        val previous = OffRouteEvidence(
            status = OffRouteStatus.Suspected,
            distanceFromRoute = Distance.meters(86.0),
            offRouteStartedAt = startedAt,
            consecutiveOffRouteSamples = 4,
        )

        val evidence = OffRouteDetector.evaluate(
            sample = sampleAt(startedAt.plusSeconds(8), accuracyMeters = 8.0),
            projection = projection(distanceFromRouteMeters = 90.0),
            thresholds = thresholds(),
            previousEvidence = previous,
        )

        assertEquals(OffRouteStatus.Suspected, evidence.status)
        assertEquals(5, evidence.consecutiveOffRouteSamples)
    }

    private fun thresholds(): OffRouteThresholds =
        OffRouteThresholds(
            suspectedDistance = Distance.meters(70.0),
            maxAcceptableAccuracy = GpsAccuracy(30.0),
            confirmationDuration = Duration.ofSeconds(45),
            confirmationSamples = 3,
        )

    private fun sampleAt(recordedAt: Instant, accuracyMeters: Double): LocationSample =
        LocationSample(
            coordinate = GeoCoordinate(latitude = 0.0, longitude = 0.0),
            accuracy = GpsAccuracy(accuracyMeters),
            recordedAt = recordedAt,
        )

    private fun projection(distanceFromRouteMeters: Double): RouteProjection =
        RouteProjection(
            coordinate = GeoCoordinate(latitude = 0.0, longitude = 0.0),
            segmentIndex = 0,
            distanceFromRoute = Distance.meters(distanceFromRouteMeters),
            progress = Distance.ZERO,
            segmentBearingDegrees = 90.0,
            bearingToRouteDegrees = 0.0,
        )
}
