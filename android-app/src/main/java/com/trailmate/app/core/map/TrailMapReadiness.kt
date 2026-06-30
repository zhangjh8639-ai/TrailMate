package com.trailmate.app.core.map

enum class TrailMapProvider {
    LOCAL_GPX_PREVIEW,
    MAPLIBRE_PMTILES,
    AMAP_SDK
}

enum class TrailMapReadinessStepStatus {
    READY,
    NEEDS_ACTION,
    BLOCKED
}

data class TrailMapReadinessStep(
    val label: String,
    val value: String,
    val status: TrailMapReadinessStepStatus
)

data class TrailMapSetupHint(
    val title: String,
    val caption: String,
    val statusLabel: String
)

data class TrailMapReadiness(
    val provider: TrailMapProvider,
    val title: String,
    val caption: String,
    val layerChips: List<String>,
    val actionLabel: String,
    val isProductionMapReady: Boolean,
    val setupHint: TrailMapSetupHint = TrailMapSetupHint(
        title = "地图准备待确认",
        caption = "请复核路线、离线包、定位和底图配置。",
        statusLabel = "待确认"
    ),
    val setupSteps: List<TrailMapReadinessStep> = emptyList()
)

data class TrailMapLoadingPresentation(
    val showOverlay: Boolean,
    val title: String,
    val caption: String,
    val blocksRouteActions: Boolean
)

object TrailMapLoadingPresentationEngine {
    fun present(
        provider: TrailMapProvider,
        mapLoaded: Boolean,
        elapsedMillis: Long
    ): TrailMapLoadingPresentation {
        if (provider != TrailMapProvider.AMAP_SDK || mapLoaded) {
            return TrailMapLoadingPresentation(
                showOverlay = false,
                title = "",
                caption = "",
                blocksRouteActions = false
            )
        }

        return if (elapsedMillis >= SLOW_MAP_LOAD_MILLIS) {
            TrailMapLoadingPresentation(
                showOverlay = true,
                title = "底图加载较慢",
                caption = "可继续查看本地路线，路线操作仍可使用。",
                blocksRouteActions = false
            )
        } else {
            TrailMapLoadingPresentation(
                showOverlay = true,
                title = "在线底图加载中",
                caption = "路线和操作仍可使用。",
                blocksRouteActions = false
            )
        }
    }

    private const val SLOW_MAP_LOAD_MILLIS = 3_000L
}

object TrailMapReadinessEngine {
    fun resolve(
        hasAmapKey: Boolean,
        amapSdkAvailable: Boolean = false,
        amapPrivacyConsentAccepted: Boolean = false,
        mapLibreRuntimeAvailable: Boolean = false,
        pmTilesBasemapPackReady: Boolean = false,
        pmTilesStyleAssetReadiness: MapLibrePmTilesStyleAssetReadiness =
            MapLibrePmTilesStyleAssetReadinessEngine.resolve(MapLibrePmTilesStyleAssetManifest.unavailable()),
        offlineRoutePackReady: Boolean,
        gpsEnabled: Boolean,
        locationReadyForFieldUse: Boolean = gpsEnabled,
        routePointCount: Int
    ): TrailMapReadiness {
        val hasRouteGeometry = routePointCount >= 2
        val pmTilesReady = mapLibreRuntimeAvailable && pmTilesBasemapPackReady
        val amapReady = hasAmapKey && amapSdkAvailable && amapPrivacyConsentAccepted
        val setupSteps = buildSetupSteps(
            hasAmapKey = hasAmapKey,
            amapSdkAvailable = amapSdkAvailable,
            amapPrivacyConsentAccepted = amapPrivacyConsentAccepted,
            mapLibreRuntimeAvailable = mapLibreRuntimeAvailable,
            pmTilesBasemapPackReady = pmTilesBasemapPackReady,
            offlineRoutePackReady = offlineRoutePackReady,
            gpsEnabled = gpsEnabled,
            locationReadyForFieldUse = locationReadyForFieldUse,
            routePointCount = routePointCount,
            pmTilesReady = pmTilesReady,
            amapReady = amapReady,
            pmTilesStyleAssetReadiness = pmTilesStyleAssetReadiness
        )
        val routeGeometryChip = if (routePointCount > 0) {
            "$routePointCount 点"
        } else {
            "无路线点"
        }
        val baseLayers = listOf("GPX 折线", "检查点", routeGeometryChip)

        if (!hasRouteGeometry) {
            return TrailMapReadiness(
                provider = TrailMapProvider.LOCAL_GPX_PREVIEW,
                title = "路线几何缺失",
                caption = "当前 GPX 缺少可绘制轨迹点，请重新导入包含轨迹点的 GPX。",
                layerChips = baseLayers,
                actionLabel = "重新导入 GPX",
                isProductionMapReady = false,
                setupHint = TrailMapSetupHint(
                    title = "路线文件不可用",
                    caption = "当前 GPX 少于 2 个轨迹点，无法形成可靠路线；请重新导入包含 trkpt 或 rtept 的文件。",
                    statusLabel = "需重新导入"
                ),
                setupSteps = setupSteps
            )
        }

        if (pmTilesReady) {
            return TrailMapReadiness(
                provider = TrailMapProvider.MAPLIBRE_PMTILES,
                title = "离线底图",
                caption = "本地离线底图已就绪，可结合 GPX 路线、检查点、定位与实走轨迹使用。",
                layerChips = baseLayers + "PMTiles 底图",
                actionLabel = "查看路线辅助",
                isProductionMapReady = true,
                setupHint = TrailMapSetupHint(
                    title = "离线底图已就绪",
                    caption = "MapLibre 渲染器和本地离线底图已可用；出发前仍需确认 OSM 数据署名、目标区域覆盖和定位状态。",
                    statusLabel = "离线可用"
                ),
                setupSteps = setupSteps
            )
        }

        val fieldReady = offlineRoutePackReady && locationReadyForFieldUse
        val pmTilesSetupCaption = when {
            !mapLibreRuntimeAvailable -> "MapLibre 渲染器待接入，当前使用本地 GPX 路线预览。"
            mapLibreRuntimeAvailable && !pmTilesBasemapPackReady -> "离线底图待准备，当前使用本地 GPX 路线预览。"
            gpsEnabled -> "定位正在校准，当前位置尚不能作为实走证据；当前使用本地 GPX 路线预览。"
            else -> "离线底图待准备，当前使用本地 GPX 路线预览。"
        }
        return TrailMapReadiness(
            provider = TrailMapProvider.LOCAL_GPX_PREVIEW,
            title = when {
                fieldReady -> "定位与记录"
                else -> "本地路线预览"
            },
            caption = when {
                fieldReady -> "离线路线已保存，当前位置可用于检查点推进；当前使用本地路线。"
                else -> pmTilesSetupCaption
            },
            layerChips = baseLayers,
            actionLabel = when {
                mapLibreRuntimeAvailable && !pmTilesBasemapPackReady -> "准备离线底图"
                fieldReady -> "查看路线辅助"
                else -> "使用本地路线"
            },
            isProductionMapReady = false,
            setupHint = buildSetupHint(
                mapLibreRuntimeAvailable = mapLibreRuntimeAvailable,
                pmTilesBasemapPackReady = pmTilesBasemapPackReady,
                fieldReady = fieldReady
            ),
            setupSteps = setupSteps
        )
    }

    private fun buildSetupHint(
        mapLibreRuntimeAvailable: Boolean,
        pmTilesBasemapPackReady: Boolean,
        fieldReady: Boolean
    ): TrailMapSetupHint =
        when {
            !mapLibreRuntimeAvailable -> TrailMapSetupHint(
                title = "当前使用本地路线",
                caption = "MapLibre 渲染器未就绪，路线页继续使用本地 GPX 预览。",
                statusLabel = "本地预览"
            )
            !pmTilesBasemapPackReady -> TrailMapSetupHint(
                title = "离线底图待准备",
                caption = "路线页先显示本地 GPX 折线；准备目标区域离线底图后启用完整地图上下文。",
                statusLabel = "本地预览"
            )
            fieldReady -> TrailMapSetupHint(
                title = "离线与定位已可用",
                caption = "当前可用本地 GPX 预览和实走轨迹记录；离线底图准备完成后会获得完整地图上下文。",
                statusLabel = "实走可用"
            )
            else -> TrailMapSetupHint(
                title = "离线底图待确认",
                caption = "当前继续使用本地 GPX 预览，路线评估和轨迹记录不受影响。",
                statusLabel = "待确认"
            )
        }

    private fun buildSetupSteps(
        hasAmapKey: Boolean,
        amapSdkAvailable: Boolean,
        amapPrivacyConsentAccepted: Boolean,
        mapLibreRuntimeAvailable: Boolean,
        pmTilesBasemapPackReady: Boolean,
        offlineRoutePackReady: Boolean,
        gpsEnabled: Boolean,
        locationReadyForFieldUse: Boolean,
        routePointCount: Int,
        pmTilesReady: Boolean,
        amapReady: Boolean,
        pmTilesStyleAssetReadiness: MapLibrePmTilesStyleAssetReadiness
    ): List<TrailMapReadinessStep> {
        val baseSteps = listOf(
            TrailMapReadinessStep(
                label = "路线",
                value = if (routePointCount >= 2) "$routePointCount 点" else "缺少轨迹点",
                status = if (routePointCount >= 2) {
                    TrailMapReadinessStepStatus.READY
                } else {
                    TrailMapReadinessStepStatus.BLOCKED
                }
            ),
            TrailMapReadinessStep(
                label = "离线路线",
                value = if (offlineRoutePackReady) "已保存" else "待保存",
                status = if (offlineRoutePackReady) {
                    TrailMapReadinessStepStatus.READY
                } else {
                    TrailMapReadinessStepStatus.NEEDS_ACTION
                }
            ),
            TrailMapReadinessStep(
                label = "GPS",
                value = when {
                    locationReadyForFieldUse -> "已可靠"
                    gpsEnabled -> "校准中"
                    else -> "未开启"
                },
                status = if (locationReadyForFieldUse) {
                    TrailMapReadinessStepStatus.READY
                } else {
                    TrailMapReadinessStepStatus.NEEDS_ACTION
                }
            ),
            TrailMapReadinessStep(
                label = "离线底图",
                value = when {
                    pmTilesReady -> "已准备"
                    !mapLibreRuntimeAvailable -> "待接入"
                    !pmTilesBasemapPackReady -> "待准备"
                    else -> "待配置"
                },
                status = if (pmTilesReady) {
                    TrailMapReadinessStepStatus.READY
                } else {
                    TrailMapReadinessStepStatus.NEEDS_ACTION
                }
            )
        )

        if (routePointCount < 2 || !pmTilesReady) {
            return baseSteps
        }

        return baseSteps + TrailMapReadinessStep(
            label = "地图标注",
            value = if (pmTilesStyleAssetReadiness.readyForLabels) "已就绪" else "待补齐",
            status = if (pmTilesStyleAssetReadiness.readyForLabels) {
                TrailMapReadinessStepStatus.READY
            } else {
                TrailMapReadinessStepStatus.NEEDS_ACTION
            }
        )
    }
}
