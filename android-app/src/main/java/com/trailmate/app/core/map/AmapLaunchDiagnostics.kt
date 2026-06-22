package com.trailmate.app.core.map

enum class AmapLaunchDiagnosticStatus {
    READY,
    NEEDS_ACTION,
    MANUAL_CHECK
}

data class AmapLaunchDiagnosticItem(
    val label: String,
    val value: String,
    val status: AmapLaunchDiagnosticStatus
)

data class AmapLaunchDiagnostics(
    val title: String,
    val statusLabel: String,
    val caption: String,
    val packageName: String,
    val revealsApiKey: Boolean,
    val offlineMapActionLabel: String?,
    val items: List<AmapLaunchDiagnosticItem>,
    val networkSettingsActionLabel: String? = null,
    val targetOfflineBaseMapRegionLabel: String? = null,
    val targetOfflineBaseMapHint: String? = null,
    val offlineBaseMapPendingRegionCount: Int? = null,
    val offlineBaseMapPendingRegionLabels: List<String> = emptyList()
)

object AmapLaunchDiagnosticsEngine {
    fun build(
        packageName: String,
        packageSha1: String? = null,
        hasAmapKey: Boolean,
        amapSdkAvailable: Boolean,
        amapPrivacyConsentAccepted: Boolean,
        offlineMapActivityRegistered: Boolean = true,
        productionMapReady: Boolean,
        routePointCount: Int,
        gpsEnabled: Boolean,
        locationReadyForFieldUse: Boolean? = null,
        preciseLocationPermissionGranted: Boolean? = null,
        gpsProviderEnabled: Boolean? = null,
        offlineDownloadNetworkValidated: Boolean? = null,
        offlineBaseMapDownloadedRegionCount: Int? = null,
        offlineBaseMapPendingRegionCount: Int? = null,
        offlineBaseMapPendingRegionLabels: List<String> = emptyList(),
        offlineBaseMapCoversTargetRoute: Boolean = false,
        offlineBaseMapTilesVerifiedWithoutNetwork: Boolean = false,
        targetOfflineBaseMapRegionLabel: String? = null,
        targetOfflineBaseMapHint: String? = null
    ): AmapLaunchDiagnostics {
        val cleanedTargetOfflineBaseMapHint = targetOfflineBaseMapHint?.trim()?.takeIf { it.isNotEmpty() }
        val cleanedPendingRegionLabels = offlineBaseMapPendingRegionLabels
            .mapNotNull { label -> label.trim().takeIf { it.isNotEmpty() } }
        val hasRouteGeometry = routePointCount >= 2
        val preciseLocationReady = preciseLocationPermissionGranted ?: gpsEnabled
        val gpsProviderReady = gpsProviderEnabled ?: gpsEnabled
        val outdoorLocationRequested = preciseLocationReady && gpsProviderReady && gpsEnabled
        val outdoorLocationReady = locationReadyForFieldUse ?: outdoorLocationRequested
        val offlineMapEntryReady = hasAmapKey &&
            amapSdkAvailable &&
            amapPrivacyConsentAccepted &&
            offlineMapActivityRegistered
        val hasDownloadedOfflineBaseMapEvidence =
            offlineBaseMapDownloadedRegionCount != null &&
                offlineBaseMapDownloadedRegionCount > 0
        val hasTargetRouteOfflineBaseMapEvidence =
            hasDownloadedOfflineBaseMapEvidence &&
                offlineBaseMapCoversTargetRoute
        val shouldRepairDownloadNetworkBeforeOfflineManager =
            offlineMapEntryReady &&
                offlineDownloadNetworkValidated == false &&
                !hasTargetRouteOfflineBaseMapEvidence
        val hasPendingOfflineBaseMapDownload =
            offlineBaseMapPendingRegionCount != null &&
                offlineBaseMapPendingRegionCount > 0
        val offlineBaseMapReady = offlineMapEntryReady &&
            hasTargetRouteOfflineBaseMapEvidence
        val offlineBaseMapFieldVerified = offlineBaseMapReady && offlineBaseMapTilesVerifiedWithoutNetwork
        val onlineMapReady = hasAmapKey &&
            amapSdkAvailable &&
            amapPrivacyConsentAccepted &&
            productionMapReady &&
            hasRouteGeometry
        val readyForDeviceQa = onlineMapReady &&
            offlineBaseMapFieldVerified &&
            outdoorLocationReady
        return AmapLaunchDiagnostics(
            title = "高德上线检查",
            statusLabel = when {
                readyForDeviceQa -> "可真机验证"
                offlineBaseMapReady && !offlineBaseMapTilesVerifiedWithoutNetwork -> "待断网验证"
                shouldRepairDownloadNetworkBeforeOfflineManager -> "待下载网络"
                offlineMapEntryReady && !offlineBaseMapReady -> "待离线底图"
                !preciseLocationReady -> "待定位授权"
                !gpsProviderReady -> "待开启系统定位"
                !gpsEnabled -> "待定位校准"
                else -> "待补齐"
            },
            caption = buildCaption(
                hasAmapKey = hasAmapKey,
                amapSdkAvailable = amapSdkAvailable,
                amapPrivacyConsentAccepted = amapPrivacyConsentAccepted,
                offlineMapActivityRegistered = offlineMapActivityRegistered,
                offlineMapEntryReady = offlineMapEntryReady,
                offlineBaseMapDownloadedRegionCount = offlineBaseMapDownloadedRegionCount,
                offlineBaseMapPendingRegionCount = offlineBaseMapPendingRegionCount,
                offlineBaseMapPendingRegionLabels = cleanedPendingRegionLabels,
                offlineDownloadNetworkValidated = offlineDownloadNetworkValidated,
                offlineBaseMapCoversTargetRoute = offlineBaseMapCoversTargetRoute,
                offlineBaseMapTilesVerifiedWithoutNetwork = offlineBaseMapTilesVerifiedWithoutNetwork,
                hasRouteGeometry = hasRouteGeometry,
                preciseLocationReady = preciseLocationReady,
                gpsProviderReady = gpsProviderReady,
                gpsEnabled = gpsEnabled,
                readyForDeviceQa = readyForDeviceQa,
                targetOfflineBaseMapHint = cleanedTargetOfflineBaseMapHint
            ),
            packageName = packageName,
            revealsApiKey = false,
            offlineMapActionLabel = if (offlineMapEntryReady && !shouldRepairDownloadNetworkBeforeOfflineManager) {
                offlineMapActionLabel(targetOfflineBaseMapRegionLabel)
            } else {
                null
            },
            targetOfflineBaseMapRegionLabel = targetOfflineBaseMapRegionLabel?.trim()?.takeIf { it.isNotEmpty() },
            targetOfflineBaseMapHint = cleanedTargetOfflineBaseMapHint,
            offlineBaseMapPendingRegionCount = offlineBaseMapPendingRegionCount,
            offlineBaseMapPendingRegionLabels = cleanedPendingRegionLabels,
            items = listOf(
                AmapLaunchDiagnosticItem(
                    label = "Android Key",
                    value = if (hasAmapKey) "已注入" else "待配置",
                    status = if (hasAmapKey) {
                        AmapLaunchDiagnosticStatus.READY
                    } else {
                        AmapLaunchDiagnosticStatus.NEEDS_ACTION
                    }
                ),
                AmapLaunchDiagnosticItem(
                    label = "Package/SHA1",
                    value = when {
                        !hasAmapKey -> "待绑定"
                        packageSha1.isNullOrBlank() -> "控制台核验"
                        else -> packageSha1
                    },
                    status = if (hasAmapKey) {
                        AmapLaunchDiagnosticStatus.MANUAL_CHECK
                    } else {
                        AmapLaunchDiagnosticStatus.NEEDS_ACTION
                    }
                ),
                AmapLaunchDiagnosticItem(
                    label = "SDK",
                    value = if (amapSdkAvailable) "已接入" else "待接入",
                    status = if (amapSdkAvailable) {
                        AmapLaunchDiagnosticStatus.READY
                    } else {
                        AmapLaunchDiagnosticStatus.NEEDS_ACTION
                    }
                ),
                AmapLaunchDiagnosticItem(
                    label = "隐私授权",
                    value = if (amapPrivacyConsentAccepted) "已同意" else "待授权",
                    status = if (amapPrivacyConsentAccepted) {
                        AmapLaunchDiagnosticStatus.READY
                    } else {
                        AmapLaunchDiagnosticStatus.NEEDS_ACTION
                    }
                ),
                AmapLaunchDiagnosticItem(
                    label = "在线底图",
                    value = when {
                        onlineMapReady -> "可验证"
                        !hasRouteGeometry -> "缺路线"
                        else -> "待打开路线地图验证"
                    },
                    status = if (onlineMapReady) {
                        AmapLaunchDiagnosticStatus.READY
                    } else {
                        AmapLaunchDiagnosticStatus.NEEDS_ACTION
                    }
                ),
                AmapLaunchDiagnosticItem(
                    label = "离线底图",
                    value = when {
                        offlineBaseMapReady -> "已覆盖当前路线"
                        shouldRepairDownloadNetworkBeforeOfflineManager -> "待网络"
                        hasPendingOfflineBaseMapDownload ->
                            "下载中 $offlineBaseMapPendingRegionCount 个区域，待完成"
                        offlineBaseMapDownloadedRegionCount != null &&
                            offlineBaseMapDownloadedRegionCount > 0 ->
                            "已下载 $offlineBaseMapDownloadedRegionCount 个区域，待匹配当前路线"
                        offlineMapEntryReady && offlineBaseMapDownloadedRegionCount == 0 -> "未下载"
                        offlineMapEntryReady -> "管理器可打开"
                        !hasAmapKey -> "待配置"
                        !amapPrivacyConsentAccepted -> "待授权"
                        !amapSdkAvailable -> "待接入"
                        !offlineMapActivityRegistered -> "待注册"
                        else -> "待配置"
                    },
                    status = when {
                        offlineBaseMapFieldVerified -> AmapLaunchDiagnosticStatus.READY
                        offlineBaseMapReady -> AmapLaunchDiagnosticStatus.MANUAL_CHECK
                        hasPendingOfflineBaseMapDownload -> AmapLaunchDiagnosticStatus.MANUAL_CHECK
                        shouldRepairDownloadNetworkBeforeOfflineManager -> AmapLaunchDiagnosticStatus.NEEDS_ACTION
                        offlineMapEntryReady && offlineBaseMapDownloadedRegionCount == null ->
                            AmapLaunchDiagnosticStatus.MANUAL_CHECK
                        else -> AmapLaunchDiagnosticStatus.NEEDS_ACTION
                    }
                ),
                AmapLaunchDiagnosticItem(
                    label = "下载网络",
                    value = when {
                        offlineBaseMapReady -> "无需下载"
                        offlineDownloadNetworkValidated == true -> "已验证"
                        offlineDownloadNetworkValidated == false -> "未验证"
                        else -> "未检测"
                    },
                    status = when {
                        offlineBaseMapReady -> AmapLaunchDiagnosticStatus.READY
                        offlineDownloadNetworkValidated == true -> AmapLaunchDiagnosticStatus.READY
                        offlineDownloadNetworkValidated == false -> AmapLaunchDiagnosticStatus.NEEDS_ACTION
                        else -> AmapLaunchDiagnosticStatus.MANUAL_CHECK
                    }
                ),
                AmapLaunchDiagnosticItem(
                    label = "断网瓦片",
                    value = when {
                        offlineBaseMapFieldVerified -> "已断网验证"
                        offlineBaseMapReady -> "待断网验证"
                        offlineMapEntryReady -> "先下载离线底图"
                        else -> "待配置"
                    },
                    status = when {
                        offlineBaseMapFieldVerified -> AmapLaunchDiagnosticStatus.READY
                        offlineBaseMapReady -> AmapLaunchDiagnosticStatus.MANUAL_CHECK
                        else -> AmapLaunchDiagnosticStatus.NEEDS_ACTION
                    }
                ),
                AmapLaunchDiagnosticItem(
                    label = "精确定位",
                    value = when {
                        preciseLocationPermissionGranted == true -> "已授权"
                        preciseLocationPermissionGranted == false -> "待授权"
                        gpsEnabled -> "已授权"
                        else -> "未检测"
                    },
                    status = when {
                        preciseLocationReady -> AmapLaunchDiagnosticStatus.READY
                        preciseLocationPermissionGranted == null -> AmapLaunchDiagnosticStatus.MANUAL_CHECK
                        else -> AmapLaunchDiagnosticStatus.NEEDS_ACTION
                    }
                ),
                AmapLaunchDiagnosticItem(
                    label = "系统 GPS",
                    value = when {
                        gpsProviderEnabled == true -> "已开启"
                        preciseLocationPermissionGranted == false -> "待授权后检测"
                        gpsProviderEnabled == false -> "待开启"
                        gpsEnabled -> "已开启"
                        else -> "未检测"
                    },
                    status = when {
                        gpsProviderReady -> AmapLaunchDiagnosticStatus.READY
                        preciseLocationPermissionGranted == false -> AmapLaunchDiagnosticStatus.MANUAL_CHECK
                        gpsProviderEnabled == null -> AmapLaunchDiagnosticStatus.MANUAL_CHECK
                        else -> AmapLaunchDiagnosticStatus.NEEDS_ACTION
                    }
                ),
                AmapLaunchDiagnosticItem(
                    label = "定位校准",
                    value = when {
                        outdoorLocationReady -> "位置可靠"
                        gpsEnabled -> "等待可靠位置"
                        else -> "待开始"
                    },
                    status = if (outdoorLocationReady) {
                        AmapLaunchDiagnosticStatus.READY
                    } else {
                        AmapLaunchDiagnosticStatus.NEEDS_ACTION
                    }
                )
            ),
            networkSettingsActionLabel = if (shouldRepairDownloadNetworkBeforeOfflineManager) {
                "打开网络设置"
            } else {
                null
            }
        )
    }

    private fun buildCaption(
        hasAmapKey: Boolean,
        amapSdkAvailable: Boolean,
        amapPrivacyConsentAccepted: Boolean,
        offlineMapActivityRegistered: Boolean,
        offlineMapEntryReady: Boolean,
        offlineBaseMapDownloadedRegionCount: Int?,
        offlineBaseMapPendingRegionCount: Int?,
        offlineBaseMapPendingRegionLabels: List<String>,
        offlineDownloadNetworkValidated: Boolean?,
        offlineBaseMapCoversTargetRoute: Boolean,
        offlineBaseMapTilesVerifiedWithoutNetwork: Boolean,
        hasRouteGeometry: Boolean,
        preciseLocationReady: Boolean,
        gpsProviderReady: Boolean,
        gpsEnabled: Boolean,
        readyForDeviceQa: Boolean,
        targetOfflineBaseMapHint: String?
    ): String {
        val targetHintSuffix = offlineBaseMapTargetHintSuffix(targetOfflineBaseMapHint)
        return when {
            readyForDeviceQa ->
                "请用项目 Key、Package/SHA1 和网络在真机确认在线底图、离线底图、蓝点定位与路线覆盖。"
            !hasAmapKey ->
                "上线前需在高德控制台绑定 Package/SHA1，并通过 TRAILMATE_AMAP_API_KEY 注入 Android Key。"
            !amapSdkAvailable ->
                "当前检测不到高德地图 SDK，接入后再验证在线底图、蓝点定位和路线覆盖。"
            !amapPrivacyConsentAccepted ->
                "用户同意高德地图授权前不初始化 MapView；请先完成隐私授权再真机验证。"
            !offlineMapActivityRegistered ->
                "高德离线地图入口需在 Manifest 注册 OfflineMapActivity，注册后再开放离线地图管理器。"
            !hasRouteGeometry ->
                "当前路线缺少可绘制轨迹点，请先导入包含 trkpt 或 rtept 的 GPX。"
            offlineMapEntryReady &&
                (offlineBaseMapDownloadedRegionCount == null ||
                    offlineBaseMapDownloadedRegionCount <= 0 ||
                    !offlineBaseMapCoversTargetRoute) &&
                offlineDownloadNetworkValidated == false ->
                "当前设备网络未通过 Android 系统网络验证；请先修复 Wi-Fi/蜂窝网络，再下载目标区域离线底图。"
            offlineMapEntryReady && offlineBaseMapPendingRegionCount != null &&
                offlineBaseMapPendingRegionCount > 0 ->
                "已检测到高德离线底图下载任务（${
                    offlineBaseMapPendingRegionLabels.pendingOfflineBaseMapSummary(
                        offlineBaseMapPendingRegionCount
                    )
                }），但尚未完成；请打开高德离线底图管理查看等待、暂停或失败状态。"
            offlineMapEntryReady && offlineBaseMapDownloadedRegionCount == null ->
                "高德离线底图管理器可打开，但还未读取到已下载区域；请在真机下载后复核离线底图状态。$targetHintSuffix"
            offlineMapEntryReady && offlineBaseMapDownloadedRegionCount != null &&
                offlineBaseMapDownloadedRegionCount <= 0 ->
                "尚未检测到已下载的高德离线底图；出发前请下载目标区域离线底图。$targetHintSuffix"
            offlineMapEntryReady && offlineBaseMapDownloadedRegionCount != null &&
                offlineBaseMapDownloadedRegionCount > 0 && !offlineBaseMapCoversTargetRoute ->
                "已检测到高德离线底图区域，但还未确认覆盖当前路线；请下载或切换到目标路线所在区域。$targetHintSuffix"
            offlineMapEntryReady && offlineBaseMapDownloadedRegionCount != null &&
                offlineBaseMapDownloadedRegionCount > 0 &&
                offlineBaseMapCoversTargetRoute &&
                !offlineBaseMapTilesVerifiedWithoutNetwork ->
                "离线底图已覆盖当前路线；仍需在真机断网后确认底图瓦片可显示，再进入真机验证。"
            !preciseLocationReady ->
                "真机需授予精确定位权限；近似定位不能作为户外导航和轨迹记录的 GPS 证据。"
            !gpsProviderReady ->
                "Android 系统定位或 GPS Provider 未开启；请打开系统定位后返回 TrailMate 自动进入定位校准。"
            !gpsEnabled ->
                "定位尚未进入校准；请在路线页启动定位，确认蓝点和路线位置可靠后再做真机验证。"
            else ->
                "请复核高德 Key、Package/SHA1、SDK、隐私授权和网络状态。"
        }
    }

    private fun offlineBaseMapTargetHintSuffix(targetOfflineBaseMapHint: String?): String =
        targetOfflineBaseMapHint?.let {
            " 若目标城市未自动识别，请用${it}确认目标城市，再下载覆盖整条路线的离线底图。"
        } ?: ""

    private fun List<String>.pendingOfflineBaseMapSummary(count: Int): String =
        filter { it.isNotBlank() }
            .take(MAX_PENDING_REGION_LABELS)
            .joinToString("、")
            .ifBlank { "$count 个区域" }

    private fun offlineMapActionLabel(targetRegionLabel: String?): String {
        val cleanedLabel = targetRegionLabel?.trim()?.takeIf { it.isNotEmpty() }
        return if (cleanedLabel == null) {
            "打开高德离线底图管理"
        } else {
            "打开高德离线底图管理（$cleanedLabel）"
        }
    }

    private const val MAX_PENDING_REGION_LABELS = 3
}
