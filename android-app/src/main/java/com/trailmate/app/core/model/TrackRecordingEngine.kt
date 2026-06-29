package com.trailmate.app.core.model

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

object TrackRecordingEngine {
    private const val MAX_RECORDING_ACCURACY_METERS = 50.0
    private const val MAX_RECORDING_SPEED_METERS_PER_SECOND = 12.5
    private const val EARTH_RADIUS_METERS = 6_371_000.0

    fun start(
        routeName: String,
        nowEpochMillis: Long,
        routeKey: String? = null
    ): TrackRecordingState =
        TrackRecordingState(
            status = TrackRecordingStatus.RECORDING,
            routeName = routeName,
            startedAtEpochMillis = nowEpochMillis,
            recordingActiveSinceEpochMillis = nowEpochMillis,
            routeKey = routeKey
        )

    fun pause(state: TrackRecordingState, nowEpochMillis: Long): TrackRecordingState =
        if (state.status == TrackRecordingStatus.RECORDING) {
            state.copy(
                status = TrackRecordingStatus.PAUSED,
                pausedAtEpochMillis = nowEpochMillis,
                recordingActiveSinceEpochMillis = null
            )
        } else {
            state
        }

    fun resume(state: TrackRecordingState, nowEpochMillis: Long): TrackRecordingState =
        if (state.status == TrackRecordingStatus.PAUSED) {
            state.copy(
                status = TrackRecordingStatus.RECORDING,
                pausedAtEpochMillis = null,
                recordingActiveSinceEpochMillis = nowEpochMillis
            )
        } else {
            state
        }

    fun finish(state: TrackRecordingState, nowEpochMillis: Long): TrackRecordingState =
        if (state.status == TrackRecordingStatus.RECORDING || state.status == TrackRecordingStatus.PAUSED) {
            state.copy(
                status = TrackRecordingStatus.FINISHED,
                pausedAtEpochMillis = null,
                recordingActiveSinceEpochMillis = null,
                finishedAtEpochMillis = nowEpochMillis
            )
        } else {
            state
        }

    fun appendLocation(
        state: TrackRecordingState,
        point: RecordedTrackPoint,
        nowEpochMillis: Long
    ): TrackRecordingState {
        if (state.status != TrackRecordingStatus.RECORDING ||
            !point.isUsableForRecording() ||
            !point.hasValidTimestamp(nowEpochMillis) ||
            point.isOlderThanActiveRecordingWindow(state)
        ) {
            return state
        }

        val lastPoint = state.points.lastOrNull()
        if (lastPoint != null && point.timestampEpochMillis <= lastPoint.timestampEpochMillis) {
            return state
        }

        val segmentMeters = lastPoint?.let { previous ->
            haversineMeters(
                fromLatitude = previous.latitude,
                fromLongitude = previous.longitude,
                toLatitude = point.latitude,
                toLongitude = point.longitude
            )
        } ?: 0.0
        if (lastPoint != null && point.isImplausibleJumpFrom(lastPoint, segmentMeters)) {
            return state
        }

        return state.copy(
            points = state.points + point,
            totalDistanceKm = ((state.totalDistanceKm + segmentMeters / 1000.0) * 1000.0).roundToInt() / 1000.0
        )
    }

    private fun RecordedTrackPoint.isUsableForRecording(): Boolean =
        latitude.isFinite() &&
            longitude.isFinite() &&
            horizontalAccuracyMeters.isFinite() &&
            horizontalAccuracyMeters in 0.0..MAX_RECORDING_ACCURACY_METERS

    private fun RecordedTrackPoint.hasValidTimestamp(nowEpochMillis: Long): Boolean =
        timestampEpochMillis > 0L &&
            timestampEpochMillis <= nowEpochMillis

    private fun RecordedTrackPoint.isOlderThanActiveRecordingWindow(state: TrackRecordingState): Boolean {
        val activeSince = state.recordingActiveSinceEpochMillis ?: state.startedAtEpochMillis
        return activeSince?.let { cutoff -> timestampEpochMillis < cutoff } == true
    }

    private fun RecordedTrackPoint.isImplausibleJumpFrom(
        previous: RecordedTrackPoint,
        segmentMeters: Double
    ): Boolean {
        val elapsedSeconds = (timestampEpochMillis - previous.timestampEpochMillis) / 1000.0

        return elapsedSeconds > 0.0 &&
            segmentMeters / elapsedSeconds > MAX_RECORDING_SPEED_METERS_PER_SECOND
    }

    private fun haversineMeters(
        fromLatitude: Double,
        fromLongitude: Double,
        toLatitude: Double,
        toLongitude: Double
    ): Double {
        val fromLatRad = Math.toRadians(fromLatitude)
        val toLatRad = Math.toRadians(toLatitude)
        val deltaLat = Math.toRadians(toLatitude - fromLatitude)
        val deltaLon = Math.toRadians(toLongitude - fromLongitude)
        val a = sin(deltaLat / 2) * sin(deltaLat / 2) +
            cos(fromLatRad) * cos(toLatRad) * sin(deltaLon / 2) * sin(deltaLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return EARTH_RADIUS_METERS * c
    }
}
