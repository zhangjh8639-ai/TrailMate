package com.trailmate.app.core.map

import android.content.Context
import android.os.Build
import com.trailmate.app.core.location.TrailMateLocationSnapshot
import com.trailmate.app.core.location.TrailMateLocationStatus

object TrailMateDeviceDiagnosticsReportFormatter {
    fun format(
        launchDiagnostics: AmapLaunchDiagnostics,
        deviceIdentity: TrailMateDeviceIdentity = TrailMateDeviceIdentity.unknown(),
        locationSnapshot: TrailMateLocationSnapshot,
        offlineDownloadDiagnostic: AmapOfflineDownloadQaDiagnostic? = null,
        pmTilesStyleAssetReadiness: MapLibrePmTilesStyleAssetReadiness? = null
    ): String =
        buildString {
            appendLine("TrailMate 真机诊断")
            appendLine("title=${launchDiagnostics.title}")
            appendLine("status=${launchDiagnostics.statusLabel}")
            appendLine("package=${launchDiagnostics.packageName}")
            appendLine("androidSdk=${deviceIdentity.androidSdkInt}")
            appendLine("manufacturer=${deviceIdentity.manufacturer}")
            appendLine("model=${deviceIdentity.model}")
            appendLine("device=${deviceIdentity.device}")
            appendLine("appVersion=${deviceIdentity.appVersionName}(${deviceIdentity.appVersionCode})")
            appendLine("revealsApiKey=${launchDiagnostics.revealsApiKey}")
            appendLine("caption=${launchDiagnostics.caption}")
            launchDiagnostics.nextActionLabel()?.let { action ->
                appendLine("launchNextAction=$action")
            }
            launchDiagnostics.offlineMapActionLabel?.let { action ->
                appendLine("offlineMapAction=$action")
            }
            launchDiagnostics.networkSettingsActionLabel?.let { action ->
                appendLine("networkSettingsAction=$action")
            }
            if ((launchDiagnostics.offlineBaseMapPendingRegionCount ?: 0) > 0) {
                appendLine("offlineBaseMapPendingRegionCount=${launchDiagnostics.offlineBaseMapPendingRegionCount}")
                launchDiagnostics.offlineBaseMapPendingRegionLabels.forEach { region ->
                    appendLine("offlineBaseMapPendingRegion=$region")
                }
            }
            if (launchDiagnostics.needsOnlineBaseMapVerification()) {
                appendLine("onlineBaseMapNextStep=$ONLINE_BASE_MAP_NEXT_STEP")
            }
            launchDiagnostics.targetOfflineBaseMapRegionLabel?.let { region ->
                appendLine("targetOfflineBaseMapRegion=$region")
                appendLine("offlineBaseMapNextStep=$OFFLINE_BASE_MAP_NEXT_STEP")
            }
            if (launchDiagnostics.targetOfflineBaseMapRegionLabel == null) {
                launchDiagnostics.targetOfflineBaseMapHint?.let { hint ->
                    appendLine("targetOfflineBaseMapHint=$hint")
                    appendLine("offlineBaseMapNextStep=$OFFLINE_BASE_MAP_HINT_NEXT_STEP")
                }
            }
            appendLine("offlineBaseMapReason=$OFFLINE_BASE_MAP_REASON")
            appendLine("items:")
            launchDiagnostics.items.forEach { item ->
                appendLine("- ${item.label}=${item.value} [${item.status.name}]")
            }
            appendLine("locationStatus=${locationSnapshot.status.name}")
            appendLine("locationAccuracy=${locationSnapshot.horizontalAccuracyMeters?.toInt()?.toString() ?: "unknown"}")
            appendLine("locationTimestamp=${locationSnapshot.timestampEpochMillis}")
            launchDiagnostics.locationRecoveryPlan(locationSnapshot)?.let { plan ->
                appendLine("locationRecoveryAction=${plan.action}")
                plan.steps.forEach { step ->
                    appendLine("locationRecoveryStep=$step")
                }
            }
            if (pmTilesStyleAssetReadiness != null) {
                appendLine("pmTilesStyleAssetStatus=${pmTilesStyleAssetReadiness.status.name}")
                appendLine("pmTilesLabelsReady=${pmTilesStyleAssetReadiness.readyForLabels}")
                appendLine("pmTilesStyleAssetCaption=${pmTilesStyleAssetReadiness.caption}")
                pmTilesStyleAssetReadiness.glyphsUrl?.let { glyphsUrl ->
                    appendLine("pmTilesGlyphsUrl=$glyphsUrl")
                }
                pmTilesStyleAssetReadiness.spriteUrl?.let { spriteUrl ->
                    appendLine("pmTilesSpriteUrl=$spriteUrl")
                }
            }
            if (offlineDownloadDiagnostic != null) {
                appendLine("offlineDownload=${offlineDownloadDiagnostic.statusLabel}")
                appendLine("offlineDownloadPassed=${offlineDownloadDiagnostic.passed}")
                appendLine("offlineDownloadSummary=${offlineDownloadDiagnostic.summary}")
                offlineDownloadDiagnostic.blockers.forEach { blocker ->
                    appendLine("blocker=$blocker")
                }
                appendLine("nextAction=${offlineDownloadDiagnostic.nextActionLabel}")
                appendLine("recoveryAction=${offlineDownloadDiagnostic.recoveryAction.name}")
                offlineDownloadDiagnostic.recoverySteps.forEach { step ->
                    appendLine("recoveryStep=$step")
                }
            }
        }.trimEnd()

    private fun AmapLaunchDiagnostics.nextActionLabel(): String? =
        networkSettingsActionLabel ?: offlineMapActionLabel

    private fun AmapLaunchDiagnostics.locationRecoveryPlan(
        snapshot: TrailMateLocationSnapshot
    ): LocationRecoveryPlan? {
        val preciseLocation = diagnosticItem("精确定位")
        val systemGps = diagnosticItem("系统 GPS")
        val calibration = diagnosticItem("定位校准")
        return when {
            snapshot.status == TrailMateLocationStatus.PERMISSION_REQUIRED ||
                preciseLocation.needsActionValue("待授权") ->
                LocationRecoveryPlan(
                    action = "REQUEST_PRECISE_LOCATION_PERMISSION",
                    steps = listOf(
                        "授予 Android 精确定位权限；仅大致位置不能用于户外导航和轨迹记录。",
                        "如果系统不再弹权限框，进入应用设置为 TrailMate 打开精确定位。"
                    )
                )

            snapshot.status == TrailMateLocationStatus.PROVIDER_DISABLED ||
                systemGps.needsActionValue("待开启") ->
                LocationRecoveryPlan(
                    action = "OPEN_SYSTEM_LOCATION_SETTINGS",
                    steps = listOf(
                        "打开 Android 系统定位/GPS 后返回 TrailMate；应用会继续定位校准。",
                        "如果仍显示待开启，检查省电模式或系统定位服务是否限制了 TrailMate。"
                    )
                )

            snapshot.status == TrailMateLocationStatus.SEARCHING ||
                calibration.needsActionValue("待开始") ->
                LocationRecoveryPlan(
                    action = "WAIT_FOR_FIRST_FIX",
                    steps = listOf(
                        "移动到开阔处并保持 TrailMate 在前台，等待首个 GPS fix。",
                        "不要用仅授权或请求中的状态作为出发、记录轨迹或安全分享证据。"
                    )
                )

            snapshot.status == TrailMateLocationStatus.LOW_ACCURACY ->
                LocationRecoveryPlan(
                    action = "WAIT_FOR_RELIABLE_FIX",
                    steps = listOf(
                        "等待定位精度稳定到户外阈值内，再开始徒步或记录轨迹。",
                        "远离高楼、树林密集处或山谷遮挡后重试定位。"
                    )
                )

            snapshot.status == TrailMateLocationStatus.UNAVAILABLE ->
                LocationRecoveryPlan(
                    action = "RETRY_LOCATION_SERVICE",
                    steps = listOf(
                        "停止定位后重试；如果仍失败，重启 TrailMate 或系统定位服务。",
                        "检查手机是否限制了 TrailMate 的定位权限、省电策略或前台服务。"
                    )
                )

            snapshot.status == TrailMateLocationStatus.DISABLED ->
                LocationRecoveryPlan(
                    action = "START_LOCATION_CALIBRATION",
                    steps = listOf(
                        "在路线页点击定位，让 TrailMate 开始户外定位校准。",
                        "看到可靠位置前，不要把路线状态视为可出发。"
                    )
                )

            else -> null
        }
    }

    private fun AmapLaunchDiagnostics.diagnosticItem(label: String): AmapLaunchDiagnosticItem? =
        items.firstOrNull { it.label == label }

    private fun AmapLaunchDiagnostics.needsOnlineBaseMapVerification(): Boolean =
        diagnosticItem("在线底图")?.let { item ->
            item.status == AmapLaunchDiagnosticStatus.NEEDS_ACTION &&
                item.value.contains("待打开路线地图验证")
        } ?: false

    private fun AmapLaunchDiagnosticItem?.needsActionValue(value: String): Boolean =
        this?.status == AmapLaunchDiagnosticStatus.NEEDS_ACTION && this.value.contains(value)

    private const val ONLINE_BASE_MAP_NEXT_STEP =
        "打开路线页地图，确认高德在线底图、道路与路线同时可见。"
    private const val OFFLINE_BASE_MAP_REASON =
        "GPX 只保存路线折线与检查点；目标区域离线底图用于在弱网或无网时保留道路、地名、水系、岔路等地图上下文，帮助判断撤退参照。"
    private const val OFFLINE_BASE_MAP_NEXT_STEP =
        "下载目标区域离线底图后，开启飞行模式确认底图瓦片可见。"
    private const val OFFLINE_BASE_MAP_HINT_NEXT_STEP =
        "先用路线中点确认目标城市，再下载覆盖整条路线的离线底图；下载后开启飞行模式确认底图瓦片可见。"

    private data class LocationRecoveryPlan(
        val action: String,
        val steps: List<String>
    )
}

data class TrailMateDeviceDiagnosticsReportAction(
    val label: String,
    val contentDescription: String
)

data class TrailMateDeviceIdentity(
    val androidSdkInt: Int,
    val manufacturer: String,
    val model: String,
    val device: String,
    val appVersionName: String,
    val appVersionCode: Long
) {
    companion object {
        fun unknown(): TrailMateDeviceIdentity =
            TrailMateDeviceIdentity(
                androidSdkInt = 0,
                manufacturer = "unknown",
                model = "unknown",
                device = "unknown",
                appVersionName = "unknown",
                appVersionCode = 0L
            )

        fun from(context: Context): TrailMateDeviceIdentity {
            val packageInfo = runCatching {
                context.packageManager.getPackageInfo(context.packageName, 0)
            }.getOrNull()
            return TrailMateDeviceIdentity(
                androidSdkInt = Build.VERSION.SDK_INT,
                manufacturer = Build.MANUFACTURER.orUnknown(),
                model = Build.MODEL.orUnknown(),
                device = Build.DEVICE.orUnknown(),
                appVersionName = packageInfo?.versionName?.orUnknown() ?: "unknown",
                appVersionCode = packageInfo?.longVersionCodeCompat() ?: 0L
            )
        }

        private fun String?.orUnknown(): String =
            this?.takeIf { it.isNotBlank() } ?: "unknown"

        private fun android.content.pm.PackageInfo.longVersionCodeCompat(): Long =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                longVersionCode
            } else {
                @Suppress("DEPRECATION")
                versionCode.toLong()
            }
    }
}

object TrailMateDeviceDiagnosticsReportActionPresenter {
    fun present(copied: Boolean): TrailMateDeviceDiagnosticsReportAction =
        if (copied) {
            TrailMateDeviceDiagnosticsReportAction(
                label = "诊断报告已复制",
                contentDescription = "TrailMate 真机诊断报告已复制"
            )
        } else {
            TrailMateDeviceDiagnosticsReportAction(
                label = "复制诊断报告",
                contentDescription = "复制 TrailMate 真机诊断报告"
            )
        }
}
