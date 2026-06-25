package com.trailmate.app.core.model

import java.util.Locale

object CapabilityProfileEngine {
    fun build(
        baselineProfile: BaselineProfile,
        historicalActivities: List<HistoricalActivity>
    ): CapabilityProfileSummary {
        val historicalProfile = HistoricalCapabilityProfileEngine.build(historicalActivities)
        if (historicalProfile == null) {
            return CapabilityProfileSummary(
                title = "问卷估算",
                value = baselineProfile.initialConfidence().displayLabel(),
                caption = "导入 3 条历史 GPX 后，可用真实距离和爬升校准能力画像。",
                confidenceLevel = ConfidenceLevel.LOW,
                evidenceLabel = "${historicalActivities.size}/${HistoricalCapabilityProfileEngine.REQUIRED_HISTORY_COUNT} GPX"
            )
        }

        val paceCopy = historicalProfile.averagePaceMinutesPerKm?.let { pace ->
            String.format(Locale.US, " 配速约 %.0f 分/公里。", pace)
        }.orEmpty()

        return CapabilityProfileSummary(
            title = "历史能力画像",
            value = String.format(
                Locale.US,
                "最长 %.1f km / +%d m",
                historicalProfile.stableDistanceKm,
                historicalProfile.stableAscentMeters.toInt()
            ),
            caption = String.format(
                Locale.US,
                "%d 条 GPX 平均 %.1f km / +%d m。%s",
                historicalProfile.activityCount,
                historicalProfile.averageDistanceKm,
                historicalProfile.averageAscentMeters,
                paceCopy
            ),
            confidenceLevel = historicalProfile.confidenceLevel,
            evidenceLabel = "${historicalProfile.activityCount}/${HistoricalCapabilityProfileEngine.REQUIRED_HISTORY_COUNT} GPX"
        )
    }
}

private fun ConfidenceLevel.displayLabel(): String =
    when (this) {
        ConfidenceLevel.LOW -> "低"
        ConfidenceLevel.MEDIUM -> "中"
        ConfidenceLevel.HIGH -> "高"
    }
