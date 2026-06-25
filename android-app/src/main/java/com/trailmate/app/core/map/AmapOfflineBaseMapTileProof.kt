package com.trailmate.app.core.map

data class AmapOfflineBaseMapTileProof(
    val routeKey: String,
    val targetAdcode: String?,
    val targetCityName: String?,
    val verifiedAtEpochMillis: Long,
    val networkDisabled: Boolean,
    val tileVisible: Boolean
)

data class AmapOfflineBaseMapTileProofCaptureState(
    val canRecordProof: Boolean,
    val failureMessage: String?,
    val actionLabel: String
)

object AmapOfflineBaseMapTileProofCaptureEngine {
    fun evaluate(
        targetRegionKnown: Boolean,
        offlineBaseMapCoversTargetRoute: Boolean,
        networkUnavailable: Boolean,
        amapBaseMapRenderedInCurrentSession: Boolean
    ): AmapOfflineBaseMapTileProofCaptureState =
        when {
            !targetRegionKnown -> AmapOfflineBaseMapTileProofCaptureState(
                canRecordProof = false,
                failureMessage = "正在确认路线所属区域，稍后再试。",
                actionLabel = "等待路线区域"
            )
            !offlineBaseMapCoversTargetRoute -> AmapOfflineBaseMapTileProofCaptureState(
                canRecordProof = false,
                failureMessage = "请先下载覆盖当前路线的高德离线底图。",
                actionLabel = "先下载离线底图"
            )
            !networkUnavailable -> AmapOfflineBaseMapTileProofCaptureState(
                canRecordProof = false,
                failureMessage = "请先关闭蜂窝网络和 Wi-Fi，再确认底图是否仍可显示。",
                actionLabel = "关闭网络后验证"
            )
            !amapBaseMapRenderedInCurrentSession -> AmapOfflineBaseMapTileProofCaptureState(
                canRecordProof = false,
                failureMessage = "请先在断网状态下确认高德底图已加载并可见。",
                actionLabel = "确认底图可见后记录"
            )
            else -> AmapOfflineBaseMapTileProofCaptureState(
                canRecordProof = true,
                failureMessage = null,
                actionLabel = "我已断网并看到底图"
            )
        }
}

object AmapOfflineBaseMapTileProofEngine {
    fun recordProofOrNull(
        routeKey: String,
        targetRegion: AmapTargetRouteRegion,
        nowEpochMillis: Long,
        networkDisabled: Boolean,
        tileVisible: Boolean
    ): AmapOfflineBaseMapTileProof? {
        if (!networkDisabled || !tileVisible) {
            return null
        }

        return AmapOfflineBaseMapTileProof(
            routeKey = routeKey,
            targetAdcode = targetRegion.adcode,
            targetCityName = targetRegion.cityName ?: targetRegion.provinceName,
            verifiedAtEpochMillis = nowEpochMillis,
            networkDisabled = networkDisabled,
            tileVisible = tileVisible
        )
    }

    fun hasVerifiedProof(
        proofs: List<AmapOfflineBaseMapTileProof>,
        routeKey: String,
        targetRegion: AmapTargetRouteRegion?
    ): Boolean {
        if (targetRegion == null) {
            return false
        }

        return proofs.any { proof ->
            proof.routeKey == routeKey &&
                proof.matchesTargetRegion(targetRegion) &&
                proof.networkDisabled &&
                proof.tileVisible
        }
    }

    private fun AmapOfflineBaseMapTileProof.matchesTargetRegion(targetRegion: AmapTargetRouteRegion): Boolean {
        val targetAdcode = targetRegion.adcode
        if (!targetAdcode.isNullOrBlank()) {
            return this.targetAdcode == targetAdcode
        }

        val targetCityName = targetRegion.cityName ?: targetRegion.provinceName
        return !targetCityName.isNullOrBlank() && this.targetCityName == targetCityName
    }
}
