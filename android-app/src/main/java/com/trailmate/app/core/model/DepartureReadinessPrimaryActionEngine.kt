package com.trailmate.app.core.model

enum class DepartureReadinessPrimaryActionKind {
    START_HIKE_AND_RECORD,
    SAVE_OFFLINE_ROUTE_PACK,
    OPEN_OFFLINE_BASE_MAP,
    REQUEST_LOCATION,
    OPEN_LOCATION_SETTINGS,
    SHOW_GEAR,
    BLOCKED
}

data class DepartureReadinessPrimaryAction(
    val label: String,
    val kind: DepartureReadinessPrimaryActionKind,
    val enabled: Boolean = true
)

object DepartureReadinessPrimaryActionEngine {
    fun resolve(summary: DepartureReadinessSummary): DepartureReadinessPrimaryAction =
        summary.primaryActionLabel.toPrimaryAction()

    private fun String.toPrimaryAction(): DepartureReadinessPrimaryAction =
        when {
            this == START_HIKE_WITH_TRACK_LABEL -> DepartureReadinessPrimaryAction(
                label = this,
                kind = DepartureReadinessPrimaryActionKind.START_HIKE_AND_RECORD
            )
            isSaveRouteAction() -> DepartureReadinessPrimaryAction(
                label = this,
                kind = DepartureReadinessPrimaryActionKind.SAVE_OFFLINE_ROUTE_PACK
            )
            isOfflineBaseMapRepairAction() -> DepartureReadinessPrimaryAction(
                label = this,
                kind = DepartureReadinessPrimaryActionKind.OPEN_OFFLINE_BASE_MAP
            )
            this == "授权定位" -> DepartureReadinessPrimaryAction(
                label = this,
                kind = DepartureReadinessPrimaryActionKind.REQUEST_LOCATION
            )
            this == "打开系统定位" -> DepartureReadinessPrimaryAction(
                label = this,
                kind = DepartureReadinessPrimaryActionKind.OPEN_LOCATION_SETTINGS
            )
            this == "等待定位稳定" || this == "重试定位" -> DepartureReadinessPrimaryAction(
                label = this,
                kind = DepartureReadinessPrimaryActionKind.REQUEST_LOCATION
            )
            startsWith("补齐") -> DepartureReadinessPrimaryAction(
                label = this,
                kind = DepartureReadinessPrimaryActionKind.SHOW_GEAR
            )
            else -> DepartureReadinessPrimaryAction(
                label = this,
                kind = DepartureReadinessPrimaryActionKind.BLOCKED,
                enabled = false
            )
        }

    private fun String.isOfflineBaseMapRepairAction(): Boolean =
        this == "导入离线地图包" ||
            this == "导入底图" ||
            this == "导入离线底图" ||
            this == "下载底图" ||
            contains("离线地图包") ||
            contains("离线底图") ||
            this == "飞行模式验证底图"

    private fun String.isSaveRouteAction(): Boolean =
        this == "保存离线路线" || this == "保存 GPX 路线" || this == "保存路线包"

    private const val START_HIKE_WITH_TRACK_LABEL = "开始徒步并记录轨迹"
}
