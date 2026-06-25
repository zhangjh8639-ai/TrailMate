package com.trailmate.app.core.model

import com.trailmate.app.core.location.TrailMateLocationFixReliability
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
        fix: HikeLocationFix,
        nowEpochMillis: Long
    ): LocationBackedHikeUpdate {
        when (session.status) {
            HikeSessionStatus.READY -> return LocationBackedHikeUpdate(
                session = session,
                status = LocationBackedHikeStatus.WAITING,
                caption = "先开始徒步，再用定位推进检查点。"
            )
            HikeSessionStatus.PAUSED -> return LocationBackedHikeUpdate(
                session = session,
                status = LocationBackedHikeStatus.WAITING,
                caption = "徒步已暂停，定位不会推进检查点。"
            )
            HikeSessionStatus.COMPLETED -> return LocationBackedHikeUpdate(
                session = session,
                status = LocationBackedHikeStatus.FINISHED,
                caption = "本次路线已完成。"
            )
            HikeSessionStatus.ACTIVE -> Unit
        }

        if (!fix.isUsableDistance()) {
            return LocationBackedHikeUpdate(
                session = session,
                status = LocationBackedHikeStatus.LOW_ACCURACY,
                caption = "定位数据不可用，暂不推进检查点。"
            )
        }

        if (fix.horizontalAccuracyMeters > MAX_USABLE_ACCURACY_METERS) {
            return LocationBackedHikeUpdate(
                session = session,
                status = LocationBackedHikeStatus.LOW_ACCURACY,
                caption = "定位精度约 ${fix.horizontalAccuracyMeters.formatMeters()}，暂不推进检查点。"
            )
        }

        if (!fix.isFresh(nowEpochMillis)) {
            return LocationBackedHikeUpdate(
                session = session,
                status = LocationBackedHikeStatus.LOW_ACCURACY,
                caption = "定位已超过 60 秒未更新，暂不推进检查点。"
            )
        }

        if (fix.crossTrackErrorMeters > OFF_ROUTE_METERS) {
            return LocationBackedHikeUpdate(
                session = session,
                status = LocationBackedHikeStatus.CHECK_ROUTE,
                caption = "当前位置距计划路线约 ${fix.crossTrackErrorMeters.formatMeters()}，请核对地图、路标和现场路径。"
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
            "已对齐「${checkpoint.title}」，路线进度 ${fix.distanceAlongRouteKm.formatKm()}。"
        } ?: "定位已对齐路线。"

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

    private fun HikeLocationFix.isFresh(nowEpochMillis: Long): Boolean =
        (nowEpochMillis - timestampEpochMillis).coerceAtLeast(0L) <=
            TrailMateLocationFixReliability.MAX_RELIABLE_FIX_AGE_MILLIS

    private fun Double.formatMeters(): String =
        "${this.toInt()} m"

    private fun Double.formatKm(): String =
        String.format(Locale.US, "%.1f km", this)
}
