package com.trailmate.app.core.map

enum class AmapOfflineMapReturnStatusAction {
    NONE,
    SHOW_NO_DOWNLOAD_DETECTED,
    CLEAR_RETURN_MESSAGE
}

data class AmapOfflineMapReturnStatusResult(
    val action: AmapOfflineMapReturnStatusAction,
    val message: String? = null
)

object AmapOfflineMapReturnStatusPolicy {
    fun resolve(
        returnedFromOfflineManager: Boolean,
        beforeStatus: AmapOfflineBaseMapStatus?,
        afterStatus: AmapOfflineBaseMapStatus?,
        targetRegionLabel: String?
    ): AmapOfflineMapReturnStatusResult {
        if (!returnedFromOfflineManager || afterStatus == null) {
            return AmapOfflineMapReturnStatusResult(AmapOfflineMapReturnStatusAction.NONE)
        }

        val beforeDownloadedCount = beforeStatus?.downloadedRegionCount ?: 0
        val beforePendingCount = beforeStatus?.pendingRegionCount ?: 0
        val afterDownloadedCount = afterStatus.downloadedRegionCount
        val afterPendingCount = afterStatus.pendingRegionCount

        if (afterDownloadedCount > beforeDownloadedCount || afterPendingCount > beforePendingCount) {
            return AmapOfflineMapReturnStatusResult(AmapOfflineMapReturnStatusAction.CLEAR_RETURN_MESSAGE)
        }

        val noOfflineMapEvidence = beforeDownloadedCount == 0 &&
            beforePendingCount == 0 &&
            afterDownloadedCount == 0 &&
            afterPendingCount == 0

        if (!noOfflineMapEvidence) {
            return AmapOfflineMapReturnStatusResult(AmapOfflineMapReturnStatusAction.NONE)
        }

        val targetLabel = targetRegionLabel
            ?.takeIf { it.isNotBlank() }
            ?: "目标区域"
        return AmapOfflineMapReturnStatusResult(
            action = AmapOfflineMapReturnStatusAction.SHOW_NO_DOWNLOAD_DETECTED,
            message = "未检测到离线底图下载任务；请在高德离线底图管理中选择$targetLabel，等待下载完成后返回 TrailMate 复核。"
        )
    }
}
