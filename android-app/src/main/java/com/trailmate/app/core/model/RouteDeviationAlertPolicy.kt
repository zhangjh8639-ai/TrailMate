package com.trailmate.app.core.model

import com.trailmate.app.core.location.TrailMateLocationFixReliability

enum class RouteDeviationAlertKind {
    NONE,
    WAIT_FOR_RELIABLE_FIX,
    OFF_ROUTE,
    OFF_ROUTE_SILENT,
    OFF_ROUTE_ESCALATED,
    REJOINED_ROUTE
}

data class RouteDeviationAlertState(
    val activeEpisode: Boolean = false,
    val lastAlertEpochMillis: Long? = null,
    val lastAlertCrossTrackErrorMeters: Double = 0.0,
    val rejoinNoticeEmitted: Boolean = true
)

data class RouteDeviationAlertDecision(
    val kind: RouteDeviationAlertKind,
    val shouldNotify: Boolean,
    val shouldVibrate: Boolean,
    val title: String,
    val caption: String,
    val primaryActionLabel: String,
    val nextState: RouteDeviationAlertState
)

object RouteDeviationAlertPolicy {
    private const val MAX_ALERT_ACCURACY_METERS = 50.0
    private const val COOLDOWN_MILLIS = 120_000L
    private const val ESCALATION_DELTA_METERS = 50.0

    fun evaluate(
        status: LocationBackedHikeStatus,
        fix: HikeLocationFix?,
        state: RouteDeviationAlertState,
        nowEpochMillis: Long
    ): RouteDeviationAlertDecision {
        if (status == LocationBackedHikeStatus.ON_ROUTE && state.activeEpisode) {
            val reliableFix = fix.takeIf { it.isReliable(nowEpochMillis) }
            if (reliableFix == null) {
                return waitForReliableFix(fix, state)
            }

            return RouteDeviationAlertDecision(
                kind = RouteDeviationAlertKind.REJOINED_ROUTE,
                shouldNotify = true,
                shouldVibrate = false,
                title = "已回到路线",
                caption = "当前位置已回到计划路线附近，请确认下一检查点后继续。",
                primaryActionLabel = "继续导航",
                nextState = RouteDeviationAlertState(rejoinNoticeEmitted = true)
            )
        }

        if (status != LocationBackedHikeStatus.CHECK_ROUTE) {
            return none(state)
        }

        val reliableFix = fix.takeIf { it.isReliable(nowEpochMillis) }
        if (reliableFix == null) {
            return waitForReliableFix(fix, state)
        }

        val lastAlertAt = state.lastAlertEpochMillis
        val inCooldown = lastAlertAt != null && nowEpochMillis - lastAlertAt < COOLDOWN_MILLIS
        val worsened = lastAlertAt != null &&
            reliableFix.crossTrackErrorMeters - state.lastAlertCrossTrackErrorMeters >= ESCALATION_DELTA_METERS

        if (state.activeEpisode && inCooldown && !worsened) {
            return RouteDeviationAlertDecision(
                kind = RouteDeviationAlertKind.OFF_ROUTE_SILENT,
                shouldNotify = false,
                shouldVibrate = false,
                title = "偏离恢复中",
                caption = "继续核对路线，暂不重复提醒。当前位置距计划路线约 ${reliableFix.crossTrackErrorMeters.toInt()} m。",
                primaryActionLabel = "查看恢复建议",
                nextState = state.copy(activeEpisode = true, rejoinNoticeEmitted = false)
            )
        }

        val escalated = state.activeEpisode && worsened
        return RouteDeviationAlertDecision(
            kind = if (escalated) RouteDeviationAlertKind.OFF_ROUTE_ESCALATED else RouteDeviationAlertKind.OFF_ROUTE,
            shouldNotify = true,
            shouldVibrate = true,
            title = if (escalated) "偏离距离增加" else "疑似偏离路线",
            caption = if (escalated) {
                "你可能正在远离计划路线，当前偏离约 ${reliableFix.crossTrackErrorMeters.toInt()} m。请停下确认是否需要原路返回。"
            } else {
                "请先停下，当前位置距计划路线约 ${reliableFix.crossTrackErrorMeters.toInt()} m。核对地图、路标和现场路径后再继续。"
            },
            primaryActionLabel = "停下核对路线",
            nextState = RouteDeviationAlertState(
                activeEpisode = true,
                lastAlertEpochMillis = nowEpochMillis,
                lastAlertCrossTrackErrorMeters = reliableFix.crossTrackErrorMeters,
                rejoinNoticeEmitted = false
            )
        )
    }

    private fun none(state: RouteDeviationAlertState): RouteDeviationAlertDecision =
        RouteDeviationAlertDecision(
            kind = RouteDeviationAlertKind.NONE,
            shouldNotify = false,
            shouldVibrate = false,
            title = "",
            caption = "",
            primaryActionLabel = "",
            nextState = state
        )

    private fun waitForReliableFix(
        fix: HikeLocationFix?,
        state: RouteDeviationAlertState
    ): RouteDeviationAlertDecision =
        RouteDeviationAlertDecision(
            kind = RouteDeviationAlertKind.WAIT_FOR_RELIABLE_FIX,
            shouldNotify = false,
            shouldVibrate = false,
            title = "等待定位稳定",
            caption = fix?.horizontalAccuracyMeters?.takeIf { it.isFinite() && it >= 0.0 }?.let { accuracyMeters ->
                "当前定位精度约 ${accuracyMeters.toInt()} m，先到开阔处等待可靠定位，再判断是否偏离路线。"
            } ?: "尚未获得可靠定位，先到开阔处等待 GPS 稳定，再判断是否偏离路线。",
            primaryActionLabel = "重新定位",
            nextState = state
        )

    private fun HikeLocationFix?.isReliable(nowEpochMillis: Long): Boolean =
        this != null &&
            distanceAlongRouteKm.isFinite() &&
            distanceAlongRouteKm >= 0.0 &&
            crossTrackErrorMeters.isFinite() &&
            crossTrackErrorMeters >= 0.0 &&
            horizontalAccuracyMeters.isFinite() &&
            horizontalAccuracyMeters >= 0.0 &&
            horizontalAccuracyMeters <= MAX_ALERT_ACCURACY_METERS &&
            timestampEpochMillis > 0L &&
            timestampEpochMillis <= nowEpochMillis &&
            nowEpochMillis - timestampEpochMillis <=
            TrailMateLocationFixReliability.MAX_RELIABLE_FIX_AGE_MILLIS
}
