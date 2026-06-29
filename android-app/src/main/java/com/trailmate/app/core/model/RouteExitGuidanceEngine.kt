package com.trailmate.app.core.model

import com.trailmate.app.core.location.TrailMateLocationFixReliability
import java.util.Locale

data class RouteExitGuidanceOption(
    val label: String,
    val distanceLabel: String,
    val caption: String,
    val emphasized: Boolean = false
)

enum class RouteExitGuidanceTone {
    READY,
    CAUTION
}

data class RouteExitGuidancePresentation(
    val title: String,
    val statusLabel: String,
    val caption: String,
    val primaryActionLabel: String,
    val options: List<RouteExitGuidanceOption>,
    val tone: RouteExitGuidanceTone
)

object RouteExitGuidanceEngine {
    fun present(
        route: ImportedRoute,
        plan: HikePlanSummary,
        locationStatus: LocationBackedHikeStatus,
        fix: HikeLocationFix?,
        trackRecording: TrackRecordingState,
        nowEpochMillis: Long = System.currentTimeMillis()
    ): RouteExitGuidancePresentation {
        if (locationStatus == LocationBackedHikeStatus.FINISHED) {
            return finished(trackRecording = trackRecording)
        }

        if (locationStatus == LocationBackedHikeStatus.CHECK_ROUTE) {
            return offRoute()
        }

        if (locationStatus == LocationBackedHikeStatus.LOW_ACCURACY ||
            locationStatus == LocationBackedHikeStatus.WAITING ||
            fix == null ||
            !fix.isReliable(nowEpochMillis)
        ) {
            return lowConfidence(trackRecording = trackRecording)
        }

        val progressKm = fix.distanceAlongRouteKm
            .takeIf { it.isFinite() && it >= 0.0 }
            ?.coerceAtMost(route.distanceKm.coerceAtLeast(0.0))
            ?: trackRecording.totalDistanceKm.coerceAtLeast(0.0)
        val nextReference = nextReference(plan = plan, progressKm = progressKm)
        val backtrackDistanceKm = progressKm.coerceAtLeast(0.0)
        val nextDistanceKm = (nextReference.distanceKm - progressKm).coerceAtLeast(0.0)
        val backtrack = RouteExitGuidanceOption(
            label = "原路返回",
            distanceLabel = backtrackDistanceKm.formatKm(),
            caption = "沿已走轨迹返回起点，不抄近路。"
        )
        val continueToReference = RouteExitGuidanceOption(
            label = "继续到${nextReference.title}",
            distanceLabel = nextDistanceKm.formatKm(),
            caption = "到达${nextReference.title}后停下复核体力、天气和路线。"
        )

        return if (backtrackDistanceKm <= nextDistanceKm) {
            RouteExitGuidancePresentation(
                title = "安全退出",
                statusLabel = "建议原路返回",
                caption = "当前位置离起点更近，优先沿已走轨迹返回；不要为了缩短距离离开可见路径。",
                primaryActionLabel = "原路返回",
                options = listOf(
                    backtrack.copy(emphasized = true),
                    continueToReference
                ),
                tone = RouteExitGuidanceTone.READY
            )
        } else {
            RouteExitGuidancePresentation(
                title = "安全退出",
                statusLabel = if (nextReference.type == HikePlanCheckpointType.FINISH) {
                    "先到终点"
                } else {
                    "先到下一检查点"
                },
                caption = "${nextReference.title}更近，先到那里停下复核，再决定继续、等待或分享位置。",
                primaryActionLabel = "前往${nextReference.title}",
                options = listOf(
                    continueToReference.copy(emphasized = true),
                    backtrack
                ),
                tone = RouteExitGuidanceTone.READY
            )
        }
    }

    private fun lowConfidence(trackRecording: TrackRecordingState): RouteExitGuidancePresentation =
        RouteExitGuidancePresentation(
            title = "安全退出",
            statusLabel = "先稳定定位",
            caption = "当前定位不适合判断撤退方向，不要凭旧位置判断；先移动到开阔处重新定位。",
            primaryActionLabel = "重新定位",
            options = listOf(
                RouteExitGuidanceOption(
                    label = "重新定位",
                    distanceLabel = "优先",
                    caption = "等待定位精度稳定后，再判断原路返回或前往检查点。"
                ),
                RouteExitGuidanceOption(
                    label = "查看已记录轨迹",
                    distanceLabel = trackRecording.totalDistanceKm.coerceAtLeast(0.0).formatKm(),
                    caption = "只作为参考，现场路标和可见路径优先。"
                )
            ),
            tone = RouteExitGuidanceTone.CAUTION
        )

    private fun finished(trackRecording: TrackRecordingState): RouteExitGuidancePresentation =
        RouteExitGuidancePresentation(
            title = "安全退出",
            statusLabel = "路线已完成",
            caption = "当前路线已完成，先保存轨迹、复核装备和返程交通。",
            primaryActionLabel = "查看轨迹",
            options = listOf(
                RouteExitGuidanceOption(
                    label = "保存轨迹",
                    distanceLabel = trackRecording.totalDistanceKm.coerceAtLeast(0.0).formatKm(),
                    caption = "保留实际轨迹，方便复盘和下次路线评估。"
                ),
                RouteExitGuidanceOption(
                    label = "返程复核",
                    distanceLabel = "收尾",
                    caption = "确认电量、天气、交通和同伴状态。"
                )
            ),
            tone = RouteExitGuidanceTone.READY
        )

    private fun offRoute(): RouteExitGuidancePresentation =
        RouteExitGuidancePresentation(
            title = "安全退出",
            statusLabel = "先回到路线",
            caption = "当前位置疑似偏离计划路线，不要用路线进度判断撤退方向；先停下核对地图、路标和现场路径。",
            primaryActionLabel = "重新定位",
            options = listOf(
                RouteExitGuidanceOption(
                    label = "停下核对",
                    distanceLabel = "优先",
                    caption = "先确认脚下路径是否安全，不要继续推进检查点。"
                ),
                RouteExitGuidanceOption(
                    label = "回到最近路线",
                    distanceLabel = "谨慎",
                    caption = "沿安全可见路径返回计划路线附近，避免直接抄近路。"
                )
            ),
            tone = RouteExitGuidanceTone.CAUTION
        )

    private fun nextReference(plan: HikePlanSummary, progressKm: Double): HikePlanCheckpoint =
        plan.checkpoints.firstOrNull { checkpoint ->
            checkpoint.distanceKm > progressKm + NEXT_REFERENCE_EPSILON_KM
        } ?: plan.checkpoints.lastOrNull()
            ?: HikePlanCheckpoint(
                type = HikePlanCheckpointType.FINISH,
                title = "终点",
                distanceKm = progressKm,
                timeFromStart = "",
                note = ""
            )

    private fun Double.formatKm(): String =
        String.format(Locale.US, "%.1f km", this)

    private fun HikeLocationFix.isReliable(nowEpochMillis: Long): Boolean =
        distanceAlongRouteKm.isFinite() &&
            distanceAlongRouteKm >= 0.0 &&
            crossTrackErrorMeters.isFinite() &&
            crossTrackErrorMeters >= 0.0 &&
            horizontalAccuracyMeters.isFinite() &&
            horizontalAccuracyMeters >= 0.0 &&
            horizontalAccuracyMeters <= MAX_EXIT_GUIDANCE_ACCURACY_METERS &&
            timestampEpochMillis > 0L &&
            timestampEpochMillis <= nowEpochMillis &&
            nowEpochMillis - timestampEpochMillis <=
            TrailMateLocationFixReliability.MAX_RELIABLE_FIX_AGE_MILLIS

    private const val NEXT_REFERENCE_EPSILON_KM = 0.05
    private const val MAX_EXIT_GUIDANCE_ACCURACY_METERS = 50.0
}
