package com.trailmate.app.core.model

import java.util.Locale

data class RouteDeviationRecoveryStep(
    val label: String,
    val value: String
)

data class RouteDeviationRecoveryDetail(
    val label: String,
    val value: String
)

enum class RouteDeviationRecoveryTone {
    OFF_ROUTE,
    REJOINED
}

data class RouteDeviationRecoveryPresentation(
    val visible: Boolean,
    val title: String,
    val statusLabel: String,
    val caption: String,
    val primaryActionLabel: String,
    val steps: List<RouteDeviationRecoveryStep>,
    val details: List<RouteDeviationRecoveryDetail>,
    val tone: RouteDeviationRecoveryTone = RouteDeviationRecoveryTone.OFF_ROUTE
)

object RouteDeviationRecoveryEngine {
    fun present(
        status: LocationBackedHikeStatus,
        fix: HikeLocationFix?,
        safetyShareAvailable: Boolean,
        wasRecentlyOffRoute: Boolean = false
    ): RouteDeviationRecoveryPresentation {
        if (status == LocationBackedHikeStatus.ON_ROUTE && wasRecentlyOffRoute) {
            return RouteDeviationRecoveryPresentation(
                visible = true,
                title = "已回到路线",
                statusLabel = "可继续推进",
                caption = "当前位置已回到计划路线附近，继续观察下一段路况。",
                primaryActionLabel = "继续导航",
                steps = listOf(
                    RouteDeviationRecoveryStep(
                        label = "确认下一检查点",
                        value = "核对当前提示点和现场路标，再继续前进。"
                    ),
                    RouteDeviationRecoveryStep(
                        label = "保持路线校验",
                        value = "如果再次偏离，TrailMate 会暂停自动推进。"
                    )
                ),
                details = fix?.recoveryDetails().orEmpty(),
                tone = RouteDeviationRecoveryTone.REJOINED
            )
        }

        if (status != LocationBackedHikeStatus.CHECK_ROUTE) {
            return hidden()
        }

        if (fix != null && fix.horizontalAccuracyMeters > MAX_RECOVERY_ACCURACY_METERS) {
            return RouteDeviationRecoveryPresentation(
                visible = true,
                title = "等待定位稳定",
                statusLabel = "先校准位置",
                caption = "当前定位精度约 ${fix.horizontalAccuracyMeters.toInt()} m，暂不判断是否偏离路线。",
                primaryActionLabel = "重新定位",
                steps = listOf(
                    RouteDeviationRecoveryStep(
                        label = "移动到开阔处",
                        value = "等待定位精度稳定后再核对路线。"
                    ),
                    RouteDeviationRecoveryStep(
                        label = "暂不推进检查点",
                        value = "在定位稳定前不要手动确认下一检查点。"
                    )
                ),
                details = fix.recoveryDetails(),
                tone = RouteDeviationRecoveryTone.OFF_ROUTE
            )
        }

        return RouteDeviationRecoveryPresentation(
            visible = true,
            title = "偏离恢复",
            statusLabel = "停止自动推进",
            caption = fix?.let { "疑似偏离路线约 ${it.crossTrackErrorMeters.toInt()} m，请先停下核对当前位置。" }
                ?: "疑似偏离路线，请先停下核对当前位置。",
            primaryActionLabel = if (safetyShareAvailable) "分享当前位置" else "授权定位",
            steps = listOf(
                RouteDeviationRecoveryStep(
                    label = "确认方向",
                    value = "查看地图、路标和现场路径，确认是否仍在计划路线附近。"
                ),
                RouteDeviationRecoveryStep(
                    label = "返回最近路径",
                    value = "沿安全可见路线回到上一段明确路径，避免直接抄近路。"
                ),
                RouteDeviationRecoveryStep(
                    label = "人工推进",
                    value = "回到路线后再手动标记检查点，TrailMate 不会自动推进。"
                )
            ),
            details = fix?.recoveryDetails().orEmpty(),
            tone = RouteDeviationRecoveryTone.OFF_ROUTE
        )
    }

    private fun hidden(): RouteDeviationRecoveryPresentation =
        RouteDeviationRecoveryPresentation(
            visible = false,
            title = "",
            statusLabel = "",
            caption = "",
            primaryActionLabel = "",
            steps = emptyList(),
            details = emptyList()
        )

    private fun HikeLocationFix.recoveryDetails(): List<RouteDeviationRecoveryDetail> =
        listOf(
            RouteDeviationRecoveryDetail(
                label = "路线进度",
                value = String.format(Locale.US, "%.1f km", distanceAlongRouteKm)
            ),
            RouteDeviationRecoveryDetail(
                label = "定位精度",
                value = "约 ${horizontalAccuracyMeters.toInt()} m"
            )
        )

    private const val MAX_RECOVERY_ACCURACY_METERS = 50.0
}
