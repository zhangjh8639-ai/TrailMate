package com.trailmate.app.core.model

import com.trailmate.app.core.location.TrailMateLocationFixReliability
import com.trailmate.app.core.location.TrailMateLocationSnapshot
import com.trailmate.app.core.location.TrailMateLocationStatus
import com.trailmate.app.core.map.TrailMapReadiness
import com.trailmate.app.core.map.TrailMapReadinessStepStatus

data class DepartureReadinessStep(
    val label: String,
    val value: String,
    val ready: Boolean
)

data class DepartureReadinessSummary(
    val title: String,
    val statusLabel: String,
    val caption: String,
    val primaryActionLabel: String,
    val steps: List<DepartureReadinessStep>
)

object DepartureReadinessEngine {
    fun build(
        mapReadiness: TrailMapReadiness,
        offlineRoutePackReady: Boolean,
        offlineBaseMapRequirement: OfflineBaseMapRequirement = OfflineBaseMapRequirement.REQUIRED,
        offlineBaseMapRegionCount: Int? = null,
        offlineBaseMapCoversTargetRoute: Boolean = false,
        offlineBaseMapTilesVerifiedWithoutNetwork: Boolean = false,
        targetOfflineBaseMapRegionLabel: String? = null,
        gpsEnabled: Boolean,
        locationSnapshot: TrailMateLocationSnapshot? = null,
        nowEpochMillis: Long = System.currentTimeMillis(),
        gearRecommendations: List<GearRecommendation>
    ): DepartureReadinessSummary {
        val routeStep = mapReadiness.setupSteps.firstOrNull { it.label == "路线" }
        val routeReady = routeStep?.status == TrailMapReadinessStepStatus.READY
        val missingGearCount = gearRecommendations.count { it.status == GearStatus.MISSING }
        val gearReady = missingGearCount == 0
        val locationReady = locationSnapshot?.isReadyForDeparture(nowEpochMillis) ?: gpsEnabled
        val locationValue = locationSnapshot?.departureValue(gpsEnabled) ?: if (gpsEnabled) "已授权" else "待授权"
        val offlineBaseMapRegionReady = offlineBaseMapRegionCount != null &&
            offlineBaseMapRegionCount > 0 &&
            offlineBaseMapCoversTargetRoute
        val offlineBaseMapReady = offlineBaseMapRegionReady && offlineBaseMapTilesVerifiedWithoutNetwork
        val targetRegionLabel = targetOfflineBaseMapRegionLabel?.trim()?.takeIf { it.isNotEmpty() }
        val offlineBaseMapBlocksDeparture =
            offlineBaseMapRequirement == OfflineBaseMapRequirement.REQUIRED && !offlineBaseMapReady
        val steps = listOf(
            DepartureReadinessStep(
                label = "路线",
                value = routeStep?.value ?: mapReadiness.title,
                ready = routeReady
            ),
            DepartureReadinessStep(
                label = "路线包",
                value = if (offlineRoutePackReady) "已保存" else "待保存",
                ready = offlineRoutePackReady
            ),
            DepartureReadinessStep(
                label = "离线底图",
                value = when {
                    offlineBaseMapRequirement == OfflineBaseMapRequirement.RECOMMENDED &&
                        !offlineBaseMapReady -> "建议下载"
                    offlineBaseMapRegionCount == null -> "待确认"
                    offlineBaseMapRegionCount <= 0 -> targetRegionLabel?.let { "${it}未下载" } ?: "未下载"
                    !offlineBaseMapCoversTargetRoute -> "已下载 $offlineBaseMapRegionCount 区域，待匹配目标路线"
                    !offlineBaseMapTilesVerifiedWithoutNetwork -> "已覆盖目标区域，待断网验证"
                    offlineBaseMapCoversTargetRoute -> "已覆盖目标区域"
                    else -> "已下载 $offlineBaseMapRegionCount 区域，待匹配目标路线"
                },
                ready = !offlineBaseMapBlocksDeparture
            ),
            DepartureReadinessStep(
                label = "定位",
                value = locationValue,
                ready = locationReady
            ),
            DepartureReadinessStep(
                label = "装备",
                value = if (gearReady) "关键装备已覆盖" else "缺 $missingGearCount 项",
                ready = gearReady
            )
        )

        if (!routeReady) {
            return DepartureReadinessSummary(
                title = "路线文件需处理",
                statusLabel = "暂不建议出发",
                caption = "当前路线缺少可用轨迹点，请重新导入 GPX 后再开始徒步。",
                primaryActionLabel = "重新导入 GPX",
                steps = steps
            )
        }

        val missingCount = steps.count { !it.ready }
        if (missingCount == 0) {
            return DepartureReadinessSummary(
                title = "出发检查完成",
                statusLabel = "可以出发",
                caption = if (offlineBaseMapRequirement == OfflineBaseMapRequirement.RECOMMENDED &&
                    !offlineBaseMapReady
                ) {
                    "路线、路线包、定位授权和关键装备已就绪；离线底图建议下载，但不阻断本次推荐路线。"
                } else {
                    "路线、离线包、断网底图验证、定位授权和关键装备已就绪。"
                },
                primaryActionLabel = "开始徒步",
                steps = steps
            )
        }

        val actions = buildList {
            if (!offlineRoutePackReady) add("保存路线包")
            if (offlineBaseMapBlocksDeparture) {
                add(
                    offlineBaseMapRepairAction(
                    offlineBaseMapRegionCount = offlineBaseMapRegionCount,
                    offlineBaseMapCoversTargetRoute = offlineBaseMapCoversTargetRoute,
                    offlineBaseMapTilesVerifiedWithoutNetwork = offlineBaseMapTilesVerifiedWithoutNetwork,
                    targetRegionLabel = targetRegionLabel
                    )
                )
            }
            if (!locationReady) add(locationSnapshot.locationRepairActionLabel(gpsEnabled))
            if (!gearReady) add("补齐 $missingGearCount 件关键装备")
        }
        val offlineBaseMapSafetyReason =
            if (offlineBaseMapBlocksDeparture) {
                "路线包只保存轨迹；离线底图用于弱网时保留道路、地名、水系、岔路和撤退参照。"
            } else {
                ""
            }

        return DepartureReadinessSummary(
            title = "出发前还差 $missingCount 项",
            statusLabel = "建议补齐",
            caption = "建议先${actions.joinToString("、")}，再开始徒步。$offlineBaseMapSafetyReason",
            primaryActionLabel = actions.firstOrNull() ?: "继续检查",
            steps = steps
        )
    }

    private fun offlineBaseMapRepairAction(
        offlineBaseMapRegionCount: Int?,
        offlineBaseMapCoversTargetRoute: Boolean,
        offlineBaseMapTilesVerifiedWithoutNetwork: Boolean,
        targetRegionLabel: String?
    ): String =
        if (offlineBaseMapRegionCount != null &&
            offlineBaseMapRegionCount > 0 &&
            offlineBaseMapCoversTargetRoute &&
            !offlineBaseMapTilesVerifiedWithoutNetwork
        ) {
            "飞行模式验证底图"
        } else {
            targetRegionLabel?.let { "下载${it}离线底图" } ?: "下载离线底图"
        }

    private fun TrailMateLocationSnapshot?.locationRepairActionLabel(gpsEnabled: Boolean): String =
        when (this?.status) {
            TrailMateLocationStatus.PROVIDER_DISABLED -> "打开系统定位"
            TrailMateLocationStatus.SEARCHING,
            TrailMateLocationStatus.LOW_ACCURACY,
            TrailMateLocationStatus.LOCATED -> "等待定位稳定"
            TrailMateLocationStatus.UNAVAILABLE -> "重试定位"
            TrailMateLocationStatus.DISABLED,
            TrailMateLocationStatus.PERMISSION_REQUIRED,
            null -> if (gpsEnabled) "等待定位稳定" else "授权定位"
        }

    private fun TrailMateLocationSnapshot.isReadyForDeparture(nowEpochMillis: Long): Boolean =
        TrailMateLocationFixReliability.isReliableForFieldUse(
            snapshot = this,
            nowEpochMillis = nowEpochMillis,
            maxAccuracyMeters = MAX_DEPARTURE_ACCURACY_METERS
        )

    private fun TrailMateLocationSnapshot.departureValue(gpsEnabled: Boolean): String =
        when (status) {
            TrailMateLocationStatus.LOCATED -> if (horizontalAccuracyMeters != null) {
                "精度约 ${horizontalAccuracyMeters.toInt()} m"
            } else {
                "等待精度"
            }
            TrailMateLocationStatus.SEARCHING -> "校准中"
            TrailMateLocationStatus.LOW_ACCURACY -> "精度弱"
            TrailMateLocationStatus.PROVIDER_DISABLED -> "系统未开启"
            TrailMateLocationStatus.UNAVAILABLE -> "不可用"
            TrailMateLocationStatus.PERMISSION_REQUIRED,
            TrailMateLocationStatus.DISABLED -> if (gpsEnabled) "待校准" else "待授权"
        }

    private const val MAX_DEPARTURE_ACCURACY_METERS = 50.0
}
