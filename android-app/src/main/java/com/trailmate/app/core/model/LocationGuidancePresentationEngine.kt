package com.trailmate.app.core.model

enum class LocationGuidanceTone {
    NEUTRAL,
    GOOD,
    WARNING,
    DANGER
}

data class LocationGuidancePresentation(
    val title: String,
    val statusLabel: String,
    val caption: String,
    val tone: LocationGuidanceTone
)

object LocationGuidancePresentationEngine {
    fun present(
        status: LocationBackedHikeStatus,
        caption: String
    ): LocationGuidancePresentation =
        when (status) {
            LocationBackedHikeStatus.WAITING -> LocationGuidancePresentation(
                title = "等待定位推进",
                statusLabel = "未开始",
                caption = caption,
                tone = LocationGuidanceTone.NEUTRAL
            )
            LocationBackedHikeStatus.ON_ROUTE -> LocationGuidancePresentation(
                title = "路线校验正常",
                statusLabel = "在线路上",
                caption = caption,
                tone = LocationGuidanceTone.GOOD
            )
            LocationBackedHikeStatus.LOW_ACCURACY -> LocationGuidancePresentation(
                title = "定位精度不足",
                statusLabel = "暂不推进",
                caption = caption,
                tone = LocationGuidanceTone.WARNING
            )
            LocationBackedHikeStatus.CHECK_ROUTE -> LocationGuidancePresentation(
                title = "疑似偏离路线",
                statusLabel = "请核对路线",
                caption = caption,
                tone = LocationGuidanceTone.DANGER
            )
            LocationBackedHikeStatus.FINISHED -> LocationGuidancePresentation(
                title = "路线已完成",
                statusLabel = "已完成",
                caption = caption,
                tone = LocationGuidanceTone.GOOD
            )
        }
}
