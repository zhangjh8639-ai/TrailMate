package com.trailmate.app.feature.routes

import com.trailmate.app.core.routeimport.RouteImportParser
import com.trailmate.app.core.routeimport.RouteImportResult
import com.trailmate.app.core.routeimport.RouteImportStatus
import com.trailmate.app.core.routeimport.RouteImportWarning
import java.util.Locale
import kotlin.math.roundToInt

data class RoutesTabState(
    val title: String,
    val subtitle: String,
    val importActionLabel: String,
    val searchPlaceholder: String,
    val filters: List<RouteFilterState>,
    val selectedFilter: RouteFilterKey,
    val importFlowStatus: RouteImportFlowStatus,
    val importEmptyState: RouteImportEmptyState,
    val importPreview: RouteImportPreviewState?,
    val assets: List<RouteAssetCardState>,
) {
    fun visibleText(): List<String> =
        buildList {
            add(title)
            add(subtitle)
            add(importActionLabel)
            add(searchPlaceholder)
            filters.forEach { add(it.label) }
            addAll(importFlowStatus.visibleText(importEmptyState))
            importPreview?.let { addAll(it.visibleText()) }
            assets.forEach { addAll(it.visibleText()) }
        }
}

enum class RouteFilterKey {
    All,
    Offline,
    Imported,
    Favorite,
    Recent,
}

data class RouteFilterState(
    val key: RouteFilterKey,
    val label: String,
)

enum class RouteImportFlowStatus {
    Idle,
    Importing,
    Cancelled,
    PreviewReady,
    Failed,
}

data class RouteImportEmptyState(
    val title: String = "导入 GPX / KML",
    val body: String = "选择 GPX/KML 文件后显示解析结果；仅导入轨迹和航点，不包含商业地图底图",
)

data class RouteImportPreviewState(
    val fileName: String,
    val statusLabel: String,
    val routeName: String,
    val distanceLabel: String,
    val elevationGainLabel: String,
    val waypointCountLabel: String,
    val trackPointCountLabel: String,
    val hasElevation: Boolean,
    val qualityNotes: List<String>,
    val routeOnlyCopy: String,
    val canUseRouteActions: Boolean,
    val saveActionLabel: String = "保存到路线",
    val detailActionLabel: String = "查看详情",
    val startActionLabel: String = "开始轨迹导航",
) {
    fun visibleText(): List<String> =
        listOf(
            fileName,
            statusLabel,
            routeName,
            distanceLabel,
            elevationGainLabel,
            waypointCountLabel,
            trackPointCountLabel,
            routeOnlyCopy,
        ) + qualityNotes +
            if (canUseRouteActions) {
                listOf(saveActionLabel, detailActionLabel, startActionLabel)
            } else {
                listOf("重新选择文件")
            }
}

data class RouteAssetCardState(
    val name: String,
    val region: String,
    val sourceLabel: String,
    val offlineStatusLabel: String,
    val distanceLabel: String,
    val elevationGainLabel: String,
    val estimatedDurationLabel: String,
    val difficultyLabel: String,
    val confidenceLabel: String,
    val riskTags: List<String>,
    val lastUsedLabel: String? = null,
    val startActionLabel: String = "开始导航",
    val detailActionLabel: String = "查看详情",
) {
    fun visibleText(): List<String> =
        buildList {
            add(name)
            add(region)
            add(sourceLabel)
            add(offlineStatusLabel)
            add(distanceLabel)
            add(elevationGainLabel)
            add(estimatedDurationLabel)
            add(difficultyLabel)
            add(confidenceLabel)
            lastUsedLabel?.let { add(it) }
            add(startActionLabel)
            add(detailActionLabel)
            addAll(riskTags)
        }
}

object RoutesTabSampleState {
    fun build(): RoutesTabState {
        return RoutesTabState(
            title = "路线",
            subtitle = "管理已保存、已导入和可离线导航的徒步路线",
            importActionLabel = "导入 GPX / KML",
            searchPlaceholder = "搜索路线",
            filters = listOf(
                RouteFilterState(RouteFilterKey.All, "全部"),
                RouteFilterState(RouteFilterKey.Offline, "已离线"),
                RouteFilterState(RouteFilterKey.Imported, "已导入"),
                RouteFilterState(RouteFilterKey.Favorite, "收藏"),
                RouteFilterState(RouteFilterKey.Recent, "最近"),
            ),
            selectedFilter = RouteFilterKey.All,
            importFlowStatus = RouteImportFlowStatus.Idle,
            importEmptyState = RouteImportEmptyState(),
            importPreview = null,
            assets = listOf(
                RouteAssetCardState(
                    name = "九溪十八涧 · 龙井环线",
                    region = "杭州 · 西湖群山",
                    sourceLabel = "平台路线",
                    offlineStatusLabel = "可离线导航",
                    distanceLabel = "8.6 km",
                    elevationGainLabel = "+430 m",
                    estimatedDurationLabel = "3h20m",
                    difficultyLabel = "中等",
                    confidenceLabel = "可信度 A",
                    riskTags = listOf("雨后湿滑", "岔路较多", "部分路段信号弱"),
                    lastUsedLabel = "上次 3 天前",
                ),
                RouteAssetCardState(
                    name = "灵隐北高峰短线",
                    region = "杭州 · 灵隐",
                    sourceLabel = "收藏路线",
                    offlineStatusLabel = "可离线导航",
                    distanceLabel = "5.2 km",
                    elevationGainLabel = "+360 m",
                    estimatedDurationLabel = "2h10m",
                    difficultyLabel = "轻中等",
                    confidenceLabel = "可信度 A",
                    riskTags = listOf("台阶密集", "节假日拥挤"),
                ),
            ),
        )
    }

    internal fun previewFromImportResult(result: RouteImportResult): RouteImportPreviewState {
        val geometry = result.geometry ?: return rejectedImportPreview(result)
        val qualityNotes = buildList {
            if (result.hasElevation) {
                add("包含海拔数据")
            } else {
                add("缺少海拔数据，剩余爬升仅作参考")
            }
            add("未保存，仅本次查看")
            result.warnings.mapNotNullTo(this) { it.qualityLabel() }
        }

        return RouteImportPreviewState(
            fileName = result.fileName,
            statusLabel = result.status.label(),
            routeName = result.routeName,
            distanceLabel = geometry.totalDistance.kilometersLabel(),
            elevationGainLabel = geometry.elevationGain.metersLabel(prefixPlus = true),
            waypointCountLabel = result.waypointCount.toString(),
            trackPointCountLabel = result.trackPointCount.toString(),
            hasElevation = result.hasElevation,
            qualityNotes = qualityNotes.distinct(),
            routeOnlyCopy = "导入文件只包含路线轨迹和航点，用于轨迹导航、偏航判断和进度计算，不包含商业地图底图。",
            canUseRouteActions = true,
        )
    }

    private fun rejectedImportPreview(result: RouteImportResult): RouteImportPreviewState =
        RouteImportPreviewState(
            fileName = result.fileName,
            statusLabel = result.status.label(),
            routeName = result.routeName,
            distanceLabel = "不可用",
            elevationGainLabel = "不可用",
            waypointCountLabel = "0",
            trackPointCountLabel = "0",
            hasElevation = false,
            qualityNotes = result.warnings.map { it.rejectedLabel() }.distinct(),
            routeOnlyCopy = "导入文件只包含路线轨迹和航点，用于轨迹导航、偏航判断和进度计算，不包含商业地图底图。",
            canUseRouteActions = false,
        )
}

fun RoutesTabState.withImporting(): RoutesTabState =
    copy(
        importFlowStatus = RouteImportFlowStatus.Importing,
        importPreview = null,
    )

fun RoutesTabState.withImportCancelled(): RoutesTabState =
    copy(
        importFlowStatus = RouteImportFlowStatus.Cancelled,
        importPreview = null,
    )

fun RoutesTabState.withImportReadFailure(
    fileName: String,
    reason: String,
): RoutesTabState =
    copy(
        importFlowStatus = RouteImportFlowStatus.Failed,
        importPreview = RouteImportPreviewState(
            fileName = fileName.ifBlank { "未知文件" },
            statusLabel = "导入失败",
            routeName = "未生成路线",
            distanceLabel = "不可用",
            elevationGainLabel = "不可用",
            waypointCountLabel = "0",
            trackPointCountLabel = "0",
            hasElevation = false,
            qualityNotes = listOf(reason),
            routeOnlyCopy = "导入文件只包含路线轨迹和航点，用于轨迹导航、偏航判断和进度计算，不包含商业地图底图。",
            canUseRouteActions = false,
        ),
    )

fun RoutesTabState.withImportResult(result: RouteImportResult): RoutesTabState {
    val preview = RoutesTabSampleState.previewFromImportResult(result)
    val parsed = result.geometry != null
    return copy(
        importFlowStatus = if (parsed) RouteImportFlowStatus.PreviewReady else RouteImportFlowStatus.Failed,
        importPreview = preview,
    )
}

private fun RouteImportFlowStatus.visibleText(emptyState: RouteImportEmptyState): List<String> =
    when (this) {
        RouteImportFlowStatus.Idle -> listOf(emptyState.title, emptyState.body)
        RouteImportFlowStatus.Importing -> listOf("正在解析路线文件")
        RouteImportFlowStatus.Cancelled -> listOf(emptyState.title, "已取消导入")
        RouteImportFlowStatus.PreviewReady,
        RouteImportFlowStatus.Failed -> emptyList()
    }

private fun RouteImportStatus.label(): String =
    when (this) {
        RouteImportStatus.Parsed -> "解析完成"
        RouteImportStatus.UnsupportedFormat -> "格式不支持"
        RouteImportStatus.MissingTrackGeometry -> "缺少可导航轨迹"
        RouteImportStatus.InvalidXml -> "文件解析失败"
    }

private fun RouteImportWarning.qualityLabel(): String? =
    when (this) {
        RouteImportWarning.MissingElevation -> "缺少海拔数据，剩余爬升仅作参考"
        RouteImportWarning.SparseTrack -> "轨迹点偏少，偏航判断可能不稳定"
        RouteImportWarning.LargePointGap -> "点间距偏大，进度计算可能不稳定"
        RouteImportWarning.UnsupportedFormat,
        RouteImportWarning.MissingTrackGeometry,
        RouteImportWarning.InvalidXml -> null
    }

private fun RouteImportWarning.rejectedLabel(): String =
    when (this) {
        RouteImportWarning.UnsupportedFormat -> "文件格式不支持"
        RouteImportWarning.MissingTrackGeometry -> "缺少可导航轨迹"
        RouteImportWarning.InvalidXml -> "文件解析失败"
        RouteImportWarning.MissingElevation -> "缺少海拔数据"
        RouteImportWarning.SparseTrack -> "轨迹点偏少"
        RouteImportWarning.LargePointGap -> "点间距偏大"
    }

private fun com.trailmate.app.core.model.Distance.kilometersLabel(): String =
    String.format(Locale.US, "%.1f km", meters / 1000.0)

private fun com.trailmate.app.core.model.Elevation.metersLabel(prefixPlus: Boolean): String {
    val rounded = meters.roundToInt()
    return if (prefixPlus) "+$rounded m" else "$rounded m"
}
