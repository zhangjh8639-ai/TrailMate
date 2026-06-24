package com.trailmate.app.core.map

data class AmapOfflineDownloadQaSnapshot(
    val targetCityName: String,
    val amapKeyConfigured: Boolean = true,
    val runtimePackageName: String = "",
    val runtimePackageSha1: String? = null,
    val catalogLoaded: Boolean,
    val targetCityResolved: Boolean,
    val resolvedCityName: String,
    val resolvedCityCode: String?,
    val resolvedAdcode: String?,
    val downloadRequestKind: String?,
    val downloadRequestValue: String?,
    val amapStorageDirectory: String,
    val initialDownloadedRegionCount: Int,
    val downloadedRegionCount: Int,
    val downloadedStateVerified: Boolean,
    val downloadedRegionCountIncreased: Boolean,
    val lastStatusLabel: String,
    val lastCompletePercent: Int,
    val lastCallbackCityName: String,
    val networkValidated: Boolean,
    val amapStorageWritable: Boolean
)

data class AmapOfflineDownloadQaDiagnostic(
    val passed: Boolean,
    val statusLabel: String,
    val summary: String,
    val blockers: List<String>,
    val nextActionLabel: String,
    val recoveryAction: AmapOfflineDownloadRecoveryAction,
    val recoverySteps: List<String>
)

enum class AmapOfflineDownloadRecoveryAction {
    KEEP_EVIDENCE,
    VERIFY_AMAP_KEY_BINDING,
    OPEN_NETWORK_SETTINGS,
    FIX_STORAGE,
    CHECK_TARGET_CITY,
    RETRY_CATALOG,
    RETRY_TARGET_CITY_DOWNLOAD
}

object AmapOfflineDownloadQaDiagnosticEngine {
    fun evaluate(snapshot: AmapOfflineDownloadQaSnapshot): AmapOfflineDownloadQaDiagnostic {
        val downloaded = snapshot.downloadedStateVerified || snapshot.downloadedRegionCountIncreased
        val blockers = buildList {
            if (!snapshot.amapKeyConfigured) {
                add("高德 Android Key 未注入")
            }
            if (snapshot.runtimePackageSha1.isNullOrBlank()) {
                add("无法读取安装包 SHA1")
            }
            if (!snapshot.catalogLoaded) {
                add("高德离线目录未加载")
            }
            if (!snapshot.targetCityResolved) {
                add("高德离线目录中未找到目标城市")
            }
            if (!snapshot.networkValidated) {
                add("设备网络未通过系统验证")
            }
            if (!snapshot.amapStorageWritable) {
                add("高德离线目录不可写")
            }
            if (snapshot.lastStatusLabel.contains("EXCEPTION_NETWORK_LOADING")) {
                add("高德 SDK 报告下载网络异常")
            }
            if (snapshot.lastStatusLabel.contains("EXCEPTION_AMAP")) {
                add("高德 SDK 报告服务或鉴权异常")
            }
            if (snapshot.lastStatusLabel.contains("EXCEPTION_SDCARD")) {
                add("高德 SDK 报告离线存储异常")
            }
            if (snapshot.lastStatusLabel.contains("START_DOWNLOAD_FAILED")) {
                add("高德 SDK 启动离线下载失败")
            }
            if (
                snapshot.catalogLoaded &&
                snapshot.targetCityResolved &&
                !downloaded &&
                snapshot.networkValidated &&
                snapshot.lastStatusLabel.contains("NO_CALLBACK")
            ) {
                add("高德 SDK 未返回离线下载回调")
            }
            if (
                snapshot.catalogLoaded &&
                snapshot.targetCityResolved &&
                !downloaded &&
                snapshot.networkValidated &&
                snapshot.lastStatusLabel.contains("CHECKUPDATES")
            ) {
                add("高德 SDK 停留在检查更新状态")
            }
            if (
                snapshot.catalogLoaded &&
                snapshot.targetCityResolved &&
                !downloaded &&
                snapshot.networkValidated &&
                !snapshot.lastStatusLabel.contains("NO_CALLBACK") &&
                !snapshot.lastStatusLabel.contains("CHECKUPDATES")
            ) {
                add("目标城市离线底图未完成下载")
            }
        }
        val recovery = resolveRecovery(snapshot, blockers)

        return AmapOfflineDownloadQaDiagnostic(
            passed = downloaded && blockers.isEmpty(),
            statusLabel = if (downloaded && blockers.isEmpty()) "离线底图已下载" else "离线底图未完成",
            summary = snapshot.toSummary(),
            blockers = blockers,
            nextActionLabel = recovery.nextActionLabel,
            recoveryAction = recovery.action,
            recoverySteps = recovery.steps
        )
    }

    private data class RecoveryPlan(
        val action: AmapOfflineDownloadRecoveryAction,
        val nextActionLabel: String,
        val steps: List<String>
    )

    private fun AmapOfflineDownloadQaSnapshot.toSummary(): String =
        "target=$targetCityName, catalogLoaded=$catalogLoaded, " +
            "amapKeyConfigured=$amapKeyConfigured, " +
            "package=$runtimePackageName, sha1=${runtimePackageSha1.orEmpty()}, " +
            "resolved=$resolvedCityName/code=$resolvedCityCode/adcode=$resolvedAdcode, " +
            "request=${downloadRequestKind.orEmpty()}:${downloadRequestValue.orEmpty()}, " +
            "amapStorage=$amapStorageDirectory, " +
            "regions=$initialDownloadedRegionCount->$downloadedRegionCount, " +
            "downloadedStateVerified=$downloadedStateVerified, " +
            "lastStatus=$lastStatusLabel, lastComplete=$lastCompletePercent, " +
            "lastCity=$lastCallbackCityName, networkValidated=$networkValidated, " +
            "amapStorageWritable=$amapStorageWritable"

    private fun resolveRecovery(
        snapshot: AmapOfflineDownloadQaSnapshot,
        blockers: List<String>
    ): RecoveryPlan =
        when {
            blockers.any { it.contains("Key") || it.contains("SHA1") || it.contains("鉴权") } ->
                RecoveryPlan(
                    action = AmapOfflineDownloadRecoveryAction.VERIFY_AMAP_KEY_BINDING,
                    nextActionLabel = "核对高德 Key 绑定",
                    steps = buildList {
                        add("在高德控制台确认 Android Key 已绑定包名 ${snapshot.runtimePackageName.ifBlank { "com.trailmate.app" }}。")
                        add(
                            "确认 SHA1 为 ${
                                snapshot.runtimePackageSha1
                                    ?.takeIf { it.isNotBlank() }
                                    ?: "诊断报告中的 Package/SHA1"
                            }。"
                        )
                        add("重新安装当前 APK 后复制 TrailMate 真机诊断报告复核；不要在报告中粘贴 Key 明文。")
                    }
                )

            blockers.any { it.contains("未找到目标城市") } ->
                RecoveryPlan(
                    action = AmapOfflineDownloadRecoveryAction.CHECK_TARGET_CITY,
                    nextActionLabel = "检查目标城市名称",
                    steps = listOf(
                        "确认路线反查出的城市为 ${snapshot.targetCityName}，并在高德离线目录中选择同一城市或上级省份。",
                        "如果路线跨市，优先下载覆盖全路线的省级离线底图。"
                    )
                )

            blockers.any { it.contains("网络") } &&
                !snapshot.networkValidated &&
                snapshot.amapStorageWritable ->
                RecoveryPlan(
                    action = AmapOfflineDownloadRecoveryAction.OPEN_NETWORK_SETTINGS,
                    nextActionLabel = "打开网络设置后重试下载",
                    steps = listOf(
                        "切换到稳定 Wi-Fi 或蜂窝网络，关闭需要网页登录的热点。",
                        "回到 TrailMate 后重新打开高德离线底图管理，下载目标城市 ${snapshot.resolvedCityName.ifBlank { snapshot.targetCityName }}。",
                        "下载完成后进入飞行模式，确认底图瓦片仍可显示。"
                    )
                )

            blockers.any { it.contains("目录不可写") || it.contains("存储异常") } &&
                snapshot.networkValidated &&
                !snapshot.amapStorageWritable ->
                RecoveryPlan(
                    action = AmapOfflineDownloadRecoveryAction.FIX_STORAGE,
                    nextActionLabel = "清理存储后重试下载",
                    steps = listOf(
                        "释放手机存储空间，并确认 TrailMate 可以写入 ${snapshot.amapStorageDirectory}。",
                        "重启 TrailMate 后重新打开高德离线底图管理。",
                        "下载完成后回到路线页复核离线底图覆盖当前路线。"
                    )
                )

            blockers.any { it.contains("网络") || it.contains("目录不可写") || it.contains("存储异常") } ->
                RecoveryPlan(
                    action = if (!snapshot.networkValidated) {
                        AmapOfflineDownloadRecoveryAction.OPEN_NETWORK_SETTINGS
                    } else {
                        AmapOfflineDownloadRecoveryAction.FIX_STORAGE
                    },
                    nextActionLabel = "修复网络或存储环境",
                    steps = listOf(
                        "先确认设备网络通过系统验证，再确认本机存储空间充足。",
                        "重新打开高德离线底图管理并下载目标区域。",
                        "下载后回到 TrailMate，复制诊断报告确认 blockers 已清空。"
                    )
                )

            blockers.any { it.contains("目录未加载") } ->
                RecoveryPlan(
                    action = AmapOfflineDownloadRecoveryAction.RETRY_CATALOG,
                    nextActionLabel = "重试加载离线目录",
                    steps = listOf(
                        "保持网络可用后重新进入高德离线底图管理。",
                        "如果目录仍无法加载，复制诊断报告核对 SDK 与网络状态。"
                    )
                )

            blockers.any { it.contains("未返回离线下载回调") || it.contains("检查更新状态") } &&
                snapshot.networkValidated &&
                snapshot.amapStorageWritable ->
                RecoveryPlan(
                    action = AmapOfflineDownloadRecoveryAction.VERIFY_AMAP_KEY_BINDING,
                    nextActionLabel = "核对高德 Key 权限",
                    steps = listOf(
                        "在高德控制台确认 Android Key 绑定包名 ${snapshot.runtimePackageName.ifBlank { "com.trailmate.app" }}。",
                        "确认 SHA1 为 ${snapshot.runtimePackageSha1?.takeIf { it.isNotBlank() } ?: "诊断报告中的 Package/SHA1"}。",
                        "确认该 Key 具备 Android SDK 离线地图或城市离线包接口权限；如果接口返回 infocode=10012 / INSUFFICIENT_PRIVILEGES，需要在高德控制台调整 Key 权限或重新申请正确类型 Key。",
                        "权限更新后重试离线底图；如果仍停留在 ${snapshot.lastStatusLabel} 且无回调，再切换网络并保留诊断报告联系高德排查离线下载服务。"
                    )
                )

            blockers.any { it.contains("检查更新") || it.contains("启动离线下载失败") } ->
                RecoveryPlan(
                    action = AmapOfflineDownloadRecoveryAction.RETRY_TARGET_CITY_DOWNLOAD,
                    nextActionLabel = "真机重试离线下载",
                    steps = listOf(
                        "退出高德离线底图管理后切换稳定网络，再重试目标城市 ${snapshot.resolvedCityName.ifBlank { snapshot.targetCityName }}。",
                        "优先使用 cityCode=${snapshot.resolvedCityCode.orEmpty().ifBlank { "目录城市编码" }} 的目标城市下载。",
                        "如果仍失败，复制诊断报告核对 lastStatus 与下载请求值。"
                    )
                )

            blockers.isNotEmpty() ->
                RecoveryPlan(
                    action = AmapOfflineDownloadRecoveryAction.RETRY_TARGET_CITY_DOWNLOAD,
                    nextActionLabel = "重试离线下载",
                    steps = listOf(
                        "按诊断 blocker 修复后重新下载目标区域离线底图。",
                        "下载完成后回到路线页确认离线底图覆盖当前路线。"
                    )
                )

            else ->
                RecoveryPlan(
                    action = AmapOfflineDownloadRecoveryAction.KEEP_EVIDENCE,
                    nextActionLabel = "保留离线底图证据",
                    steps = listOf(
                        "保留目标区域已下载和断网瓦片可见的截图。",
                        "出发前再次打开路线页确认离线底图仍覆盖当前路线。"
                    )
                )
        }
}
