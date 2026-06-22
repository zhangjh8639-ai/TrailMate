package com.trailmate.app.core.map

enum class TrailMapProvider {
    LOCAL_GPX_PREVIEW,
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
        title = "地图状态待确认",
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
        offlineRoutePackReady: Boolean,
        gpsEnabled: Boolean,
        locationReadyForFieldUse: Boolean = gpsEnabled,
        routePointCount: Int
    ): TrailMapReadiness {
        val hasRouteGeometry = routePointCount >= 2
        val amapReady = hasAmapKey && amapSdkAvailable && amapPrivacyConsentAccepted
        val setupSteps = buildSetupSteps(
            hasAmapKey = hasAmapKey,
            amapSdkAvailable = amapSdkAvailable,
            amapPrivacyConsentAccepted = amapPrivacyConsentAccepted,
            offlineRoutePackReady = offlineRoutePackReady,
            gpsEnabled = gpsEnabled,
            locationReadyForFieldUse = locationReadyForFieldUse,
            routePointCount = routePointCount,
            amapReady = amapReady
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

        if (amapReady) {
            return TrailMapReadiness(
                provider = TrailMapProvider.AMAP_SDK,
                title = if (locationReadyForFieldUse) "在线轻导航" else "在线底图",
                caption = if (offlineRoutePackReady) {
                    "在线底图可用，离线路线包已保存。"
                } else {
                    "在线底图可用，建议出发前保存路线包。"
                },
                layerChips = baseLayers + "在线底图",
                actionLabel = if (offlineRoutePackReady) "继续轻导航" else "保存路线包",
                isProductionMapReady = false,
                setupHint = TrailMapSetupHint(
                    title = "在线底图已就绪",
                    caption = if (offlineRoutePackReady) {
                        "在线底图、地图服务同意和本地路线包已就绪；出发前仍需确认目标区域离线底图。"
                    } else {
                        "在线底图和地图服务同意已就绪；出发前建议保存本地路线包，并确认目标区域离线底图。"
                    },
                    statusLabel = "在线可用"
                ),
                setupSteps = setupSteps
            )
        }

        val fieldReady = offlineRoutePackReady && locationReadyForFieldUse
        val amapSetupCaption = when {
            gpsEnabled -> "定位正在校准，当前位置尚不能作为实走证据；当前使用本地 GPX 路线预览。"
            !hasAmapKey -> "在线底图暂不可用，当前使用本地 GPX 路线预览。"
            !amapPrivacyConsentAccepted -> "在线底图未在首次设置中启用，当前使用本地 GPX 路线预览。"
            !amapSdkAvailable -> "在线底图暂不可用，当前使用本地 GPX 路线预览。"
            else -> "在线底图暂不可用，当前使用本地 GPX 路线预览。"
        }
        return TrailMapReadiness(
            provider = TrailMapProvider.LOCAL_GPX_PREVIEW,
            title = when {
                fieldReady -> "实走轻导航"
                hasAmapKey -> "本地路线预览"
                else -> "本地路线预览"
            },
            caption = if (fieldReady) {
                "离线路线包已保存，当前位置可用于检查点推进；当前使用本地路线。"
            } else {
                amapSetupCaption
            },
            layerChips = baseLayers,
            actionLabel = when {
                fieldReady -> "继续轻导航"
                !hasAmapKey -> "使用本地路线"
                !amapPrivacyConsentAccepted -> "使用本地路线"
                !amapSdkAvailable -> "使用本地路线"
                else -> "使用本地路线"
            },
            isProductionMapReady = false,
            setupHint = buildSetupHint(
                hasAmapKey = hasAmapKey,
                amapSdkAvailable = amapSdkAvailable,
                amapPrivacyConsentAccepted = amapPrivacyConsentAccepted,
                fieldReady = fieldReady
            ),
            setupSteps = setupSteps
        )
    }

    private fun buildSetupHint(
        hasAmapKey: Boolean,
        amapSdkAvailable: Boolean,
        amapPrivacyConsentAccepted: Boolean,
        fieldReady: Boolean
    ): TrailMapSetupHint =
        when {
            fieldReady -> TrailMapSetupHint(
                title = "离线与定位已可用",
                caption = "当前可用本地 GPX 预览和实走轨迹记录；在线底图可用后会获得更完整地图体验。",
                statusLabel = "实走可用"
            )
            !hasAmapKey -> TrailMapSetupHint(
                title = "当前使用本地路线",
                caption = "可继续查看 GPX、检查点和记录轨迹；在线底图暂不可用。",
                statusLabel = "本地预览"
            )
            !amapPrivacyConsentAccepted -> TrailMapSetupHint(
                title = "当前使用本地路线",
                caption = "在线底图授权未在首次设置中启用；路线页继续使用本地 GPX 预览，避免实走中被授权流程打断。",
                statusLabel = "本地预览"
            )
            !amapSdkAvailable -> TrailMapSetupHint(
                title = "在线底图暂不可用",
                caption = "当前继续使用本地 GPX 预览和检查点；不影响路线评估与轨迹记录。",
                statusLabel = "本地预览"
            )
            else -> TrailMapSetupHint(
                title = "在线底图待确认",
                caption = "当前继续使用本地 GPX 预览，路线评估和轨迹记录不受影响。",
                statusLabel = "待确认"
            )
        }

    private fun buildSetupSteps(
        hasAmapKey: Boolean,
        amapSdkAvailable: Boolean,
        amapPrivacyConsentAccepted: Boolean,
        offlineRoutePackReady: Boolean,
        gpsEnabled: Boolean,
        locationReadyForFieldUse: Boolean,
        routePointCount: Int,
        amapReady: Boolean
    ): List<TrailMapReadinessStep> =
        listOf(
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
                label = "离线",
                value = if (offlineRoutePackReady) "已保存" else "待保存",
                status = if (offlineRoutePackReady) {
                    TrailMapReadinessStepStatus.READY
                } else {
                    TrailMapReadinessStepStatus.NEEDS_ACTION
                }
            ),
            TrailMapReadinessStep(
                label = "定位",
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
                label = "底图",
                value = when {
                    amapReady -> "在线可用"
                    !hasAmapKey -> "待配置"
                    !amapPrivacyConsentAccepted -> "待启用"
                    !amapSdkAvailable -> "待接入"
                    else -> "待配置"
                },
                status = if (amapReady) {
                    TrailMapReadinessStepStatus.READY
                } else {
                    TrailMapReadinessStepStatus.NEEDS_ACTION
                }
            )
        )
}
