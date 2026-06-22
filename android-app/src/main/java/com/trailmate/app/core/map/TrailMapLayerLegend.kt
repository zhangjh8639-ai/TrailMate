package com.trailmate.app.core.map

enum class TrailMapLayerLegendItemStatus {
    READY,
    INACTIVE,
    BLOCKED
}

data class TrailMapLayerLegendItem(
    val label: String,
    val value: String,
    val status: TrailMapLayerLegendItemStatus
)

data class TrailMapLayerLegend(
    val title: String,
    val items: List<TrailMapLayerLegendItem>
)

object TrailMapLayerLegendEngine {
    fun build(
        readiness: TrailMapReadiness,
        routePointCount: Int,
        checkpointCount: Int,
        recordedTrackPointCount: Int,
        showUserLocation: Boolean
    ): TrailMapLayerLegend {
        val hasRouteGeometry = routePointCount >= 2
        return TrailMapLayerLegend(
            title = "地图图层",
            items = listOf(
                TrailMapLayerLegendItem(
                    label = "计划路线",
                    value = if (hasRouteGeometry) "$routePointCount 点" else "不可用",
                    status = if (hasRouteGeometry) {
                        TrailMapLayerLegendItemStatus.READY
                    } else {
                        TrailMapLayerLegendItemStatus.BLOCKED
                    }
                ),
                TrailMapLayerLegendItem(
                    label = "路线提示点",
                    value = when {
                        !hasRouteGeometry -> "等待路线"
                        checkpointCount > 0 -> "$checkpointCount 个"
                        else -> "待生成"
                    },
                    status = when {
                        !hasRouteGeometry -> TrailMapLayerLegendItemStatus.BLOCKED
                        checkpointCount > 0 -> TrailMapLayerLegendItemStatus.READY
                        else -> TrailMapLayerLegendItemStatus.INACTIVE
                    }
                ),
                TrailMapLayerLegendItem(
                    label = "实走轨迹",
                    value = if (recordedTrackPointCount > 0) "$recordedTrackPointCount 点" else "未记录",
                    status = if (recordedTrackPointCount > 0) {
                        TrailMapLayerLegendItemStatus.READY
                    } else {
                        TrailMapLayerLegendItemStatus.INACTIVE
                    }
                ),
                TrailMapLayerLegendItem(
                    label = "当前位置",
                    value = if (showUserLocation) "已开启" else "未开启",
                    status = if (showUserLocation) {
                        TrailMapLayerLegendItemStatus.READY
                    } else {
                        TrailMapLayerLegendItemStatus.INACTIVE
                    }
                ),
                TrailMapLayerLegendItem(
                    label = "底图",
                    value = if (readiness.provider == TrailMapProvider.AMAP_SDK) {
                        "在线底图"
                    } else {
                        "本地预览"
                    },
                    status = if (readiness.provider == TrailMapProvider.AMAP_SDK) {
                        TrailMapLayerLegendItemStatus.READY
                    } else {
                        TrailMapLayerLegendItemStatus.INACTIVE
                    }
                )
            )
        )
    }
}
