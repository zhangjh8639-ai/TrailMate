package com.trailmate.app.feature.routes

import com.trailmate.app.core.database.ImportedRouteGeometryRecords
import com.trailmate.app.core.database.ImportedRouteRecord
import com.trailmate.app.core.model.GeoCoordinate
import com.trailmate.app.core.model.RouteGeometry
import com.trailmate.app.core.model.RouteSourceType
import com.trailmate.app.core.routeimport.RouteImportFormat
import com.trailmate.app.core.routeimport.RouteImportResult
import com.trailmate.app.core.routeimport.RouteImportStatus
import com.trailmate.app.core.routeimport.RouteImportWarning
import java.security.MessageDigest
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
    val saveableImport: SaveableImportState?,
    val assets: List<RouteAssetCardState>,
    val routeDetail: RouteDetailState? = null,
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
            routeDetail?.let { addAll(it.visibleText()) }
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
    val detailActionLabel: String? = null,
    val startActionLabel: String? = null,
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
                listOfNotNull(saveActionLabel, detailActionLabel, startActionLabel)
            } else {
                listOf("重新选择文件")
            }
}

data class SaveableImportState(
    val key: String,
    val fileName: String,
    val routeName: String,
    val sourceType: RouteSourceType,
    val waypointCount: Int,
    val trackPointCount: Int,
    val hasElevation: Boolean,
    val geometry: RouteGeometry,
)

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
    val startActionLabel: String? = null,
    val detailActionLabel: String? = "查看详情",
    val identityKey: String? = null,
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
            startActionLabel?.let { add(it) }
            detailActionLabel?.let { add(it) }
            addAll(riskTags)
        }
}

data class RouteDetailState(
    val title: String,
    val subtitle: String,
    val sourceLabel: String,
    val offlineStatusLabel: String,
    val metrics: List<RouteDetailMetricState>,
    val confidenceLabel: String,
    val riskTags: List<String>,
    val boundaryNotes: List<String>,
    val backActionLabel: String = "返回路线",
    val startActionLabel: String? = null,
) {
    fun visibleText(): List<String> =
        buildList {
            add("路线详情")
            add(title)
            add(subtitle)
            add(sourceLabel)
            add(offlineStatusLabel)
            metrics.forEach { metric ->
                add(metric.label)
                add(metric.value)
            }
            add(confidenceLabel)
            add(backActionLabel)
            startActionLabel?.let { add(it) }
            addAll(riskTags)
            addAll(boundaryNotes)
        }
}

data class RouteDetailMetricState(
    val label: String,
    val value: String,
)

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
            saveableImport = null,
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
            add("仅轨迹可用")
            add("未验证")
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

    internal fun saveableImportFromResult(result: RouteImportResult): SaveableImportState? {
        val geometry = result.geometry ?: return null
        val format = result.format ?: return null

        return SaveableImportState(
            key = listOf(
                format.name,
                result.fileName,
                result.routeName,
                geometry.totalDistance.meters.roundToInt().toString(),
                result.trackPointCount.toString(),
                geometry.routeIdentityHash(),
            ).joinToString(separator = "|"),
            fileName = result.fileName,
            routeName = result.routeName,
            sourceType = format.sourceType,
            waypointCount = result.waypointCount,
            trackPointCount = result.trackPointCount,
            hasElevation = result.hasElevation,
            geometry = geometry,
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
        saveableImport = null,
    )

fun RoutesTabState.withImportCancelled(): RoutesTabState =
    copy(
        importFlowStatus = RouteImportFlowStatus.Cancelled,
        importPreview = null,
        saveableImport = null,
    )

fun RoutesTabState.withRouteDetailOpened(asset: RouteAssetCardState): RoutesTabState =
    copy(routeDetail = asset.toRouteDetailState())

fun RoutesTabState.withRouteDetailClosed(): RoutesTabState =
    copy(routeDetail = null)

fun RoutesTabState.withImportReadFailure(
    fileName: String,
    reason: String,
): RoutesTabState =
    copy(
        importFlowStatus = RouteImportFlowStatus.Failed,
        saveableImport = null,
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
        saveableImport = if (parsed) RoutesTabSampleState.saveableImportFromResult(result) else null,
    )
}

fun RoutesTabState.withSavedImport(
    savedRecord: ImportedRouteRecord? = saveableImport?.toPersistentImportedRouteRecord(),
): RoutesTabState {
    val savedAsset = savedRecord?.toRouteAssetCardState(
        lastUsedLabel = "本次已加入路线列表",
    ) ?: return this
    val currentPreviewWasSaved = saveableImport?.key == savedRecord.id

    return copy(
        importPreview = if (currentPreviewWasSaved) {
            importPreview?.copy(
                qualityNotes = importPreview.qualityNotes
                    .filterNot {
                        it == "未保存，仅本次查看" ||
                            it == "已保存到路线" ||
                            it == "本次已加入路线列表"
                    } + "本次已加入路线列表",
            )
        } else {
            importPreview
        },
        assets = listOf(savedAsset) + assets.filterNot { asset ->
            asset.identityKey == savedRecord.id ||
                (
                    asset.identityKey == null &&
                        asset.sourceLabel == savedAsset.sourceLabel &&
                        asset.name == savedAsset.name
                )
        },
    )
}

fun RoutesTabState.withPersistedImportedRoutes(records: List<ImportedRouteRecord>): RoutesTabState {
    val persistedAssets = records
        .associateBy { it.id }
        .values
        .map { record -> record.toRouteAssetCardState() }
    val persistedKeys = persistedAssets.mapNotNull { it.identityKey }.toSet()

    return copy(
        assets = persistedAssets + assets.filterNot { asset ->
            asset.identityKey in persistedKeys
        },
    )
}

internal fun SaveableImportState.toPersistentImportedRouteRecord(
    importedAtEpochMillis: Long = System.currentTimeMillis(),
): ImportedRouteRecord =
    ImportedRouteRecord(
        id = key,
        fileName = fileName,
        sourceType = sourceType,
        routeName = routeName,
        distanceMeters = geometry.totalDistance.meters,
        elevationGainMeters = geometry.elevationGain.meters,
        waypointCount = waypointCount,
        trackPointCount = trackPointCount,
        hasElevation = hasElevation,
        importedAtEpochMillis = importedAtEpochMillis,
        points = ImportedRouteGeometryRecords.fromGeometry(geometry),
    )

internal fun ImportedRouteRecord.toRouteAssetCardState(
    lastUsedLabel: String? = "已保存到本机",
): RouteAssetCardState =
    RouteAssetCardState(
        name = routeName,
        region = "导入路线",
        sourceLabel = sourceType.importSourceLabel(),
        offlineStatusLabel = "仅轨迹可用",
        distanceLabel = distanceMeters.kilometersLabel(),
        elevationGainLabel = elevationGainMeters.metersLabel(prefixPlus = true),
        estimatedDurationLabel = "待确认",
        difficultyLabel = "未验证",
        confidenceLabel = "可信度待确认",
        riskTags = listOf("导入轨迹", "未验证", "不含地图底图"),
        lastUsedLabel = lastUsedLabel,
        startActionLabel = null,
        detailActionLabel = "查看详情",
        identityKey = id,
    )

private fun RouteAssetCardState.toRouteDetailState(): RouteDetailState =
    RouteDetailState(
        title = name,
        subtitle = region,
        sourceLabel = sourceLabel,
        offlineStatusLabel = offlineStatusLabel,
        metrics = listOf(
            RouteDetailMetricState("距离", distanceLabel),
            RouteDetailMetricState("累计爬升", elevationGainLabel),
            RouteDetailMetricState("预计用时", estimatedDurationLabel),
            RouteDetailMetricState("难度", difficultyLabel),
        ),
        confidenceLabel = confidenceLabel,
        riskTags = riskTags,
        boundaryNotes = detailBoundaryNotes(),
    )

private fun RouteAssetCardState.detailBoundaryNotes(): List<String> {
    val imported = sourceLabel == "GPX 导入" || sourceLabel == "KML 导入"
    return if (imported) {
        listOf(
            "本机私密",
            "导入文件只包含路线轨迹和航点，用于轨迹导航、偏航判断和进度计算。",
            "不包含商业地图底图或完整离线地图。",
            "路线未经过平台验证，难度、耗时和风险需结合现场判断。",
        )
    } else {
        listOf(
            "路线信息用于后续轨迹导航、偏航判断和进度计算。",
            "出发前仍需结合天气、现场开放状态和个人体力判断。",
        )
    }
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

private fun RouteImportFormat.sourceLabel(): String =
    when (this) {
        RouteImportFormat.Gpx -> "GPX 导入"
        RouteImportFormat.Kml -> "KML 导入"
    }

private fun RouteSourceType.importSourceLabel(): String =
    when (this) {
        RouteSourceType.ImportedGpx -> "GPX 导入"
        RouteSourceType.ImportedKml -> "KML 导入"
        RouteSourceType.Platform -> "平台路线"
    }

private fun com.trailmate.app.core.model.Distance.kilometersLabel(): String =
    String.format(Locale.US, "%.1f km", meters / 1000.0)

private fun com.trailmate.app.core.model.Elevation.metersLabel(prefixPlus: Boolean): String {
    val rounded = meters.roundToInt()
    return if (prefixPlus) "+$rounded m" else "$rounded m"
}

private fun Double.kilometersLabel(): String =
    String.format(Locale.US, "%.1f km", this / 1000.0)

private fun Double.metersLabel(prefixPlus: Boolean): String {
    val rounded = roundToInt()
    return if (prefixPlus) "+$rounded m" else "$rounded m"
}

private fun RouteGeometry.routeIdentityHash(): String {
    val digest = MessageDigest.getInstance("SHA-256")
    coordinates.forEachIndexed { index, coordinate ->
        digest.update(index.toString().toByteArray())
        digest.update('|'.code.toByte())
        digest.update(coordinate.identityComponent().toByteArray())
        digest.update('|'.code.toByte())
        digest.update(cumulativeDistances[index].meters.roundToInt().toString().toByteArray())
        digest.update('\n'.code.toByte())
    }
    return digest.digest().joinToString(separator = "") { byte ->
        "%02x".format(byte)
    }
}

private fun GeoCoordinate.identityComponent(): String =
    listOf(
        latitude.roundToMicroDegrees(),
        longitude.roundToMicroDegrees(),
        elevation?.meters?.roundToInt()?.toString().orEmpty(),
    ).joinToString(separator = ",")

private fun Double.roundToMicroDegrees(): String =
    (this * 1_000_000.0).roundToInt().toString()
