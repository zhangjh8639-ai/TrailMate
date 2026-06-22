package com.trailmate.app.core.model

object RouteGearAdvisorEngine {
    fun recommend(
        route: ImportedRoute,
        assessment: RouteAssessmentSummary
    ): List<GearRecommendation> {
        require(route.readyForAssessment()) { "Route must be parsed before recommending gear." }

        val longRoute = route.distanceKm >= 10.0
        val highAscent = route.ascentMeters >= 600
        val lateOrLong = upperDurationMinutes(assessment.estimatedDurationRange) >= 300
        val hasConcreteRouteRisk = assessment.risks.any { risk -> risk.isConcreteRouteRisk() }

        return listOf(
            GearRecommendation(
                category = "雨衣",
                status = GearStatus.CHECK,
                rationale = "出发前确认天气，并把防风防雨层放进包里。"
            ),
            GearRecommendation(
                category = "头灯",
                status = if (lateOrLong) GearStatus.CHECK else GearStatus.OPTIONAL,
                rationale = if (lateOrLong) {
                    "预计用时较长，需要确认照明和电池余量。"
                } else {
                    "短线白天可选；如果时间可能拖延，建议带上。"
                }
            ),
            GearRecommendation(
                category = "登山杖",
                status = if (highAscent || longRoute) GearStatus.MISSING else GearStatus.OPTIONAL,
                rationale = if (highAscent || longRoute) {
                    "距离或爬升偏高，登山杖能降低长上坡和下坡压力。"
                } else {
                    "这条路线较短、爬升较低，可按习惯选择。"
                }
            ),
            GearRecommendation(
                category = "保暖层",
                status = if (highAscent || lateOrLong || hasConcreteRouteRisk) GearStatus.MISSING else GearStatus.OPTIONAL,
                rationale = if (highAscent || lateOrLong || hasConcreteRouteRisk) {
                    "高强度、海拔或路线不确定性会让休息时体感变冷。"
                } else {
                    "短线且天气温和时可选。"
                }
            ),
            GearRecommendation(
                category = "备用水",
                status = if (longRoute || lateOrLong) GearStatus.MISSING else GearStatus.CHECK,
                rationale = if (longRoute || lateOrLong) {
                    "路线用时偏长，建议在常规饮水量外增加余量。"
                } else {
                    "按当天温度确认常规饮水量是否足够。"
                }
            )
        )
    }

    private fun String.isConcreteRouteRisk(): Boolean =
        startsWith("距离超过") ||
            startsWith("爬升超过") ||
            startsWith("GPX 点位偏少")

    private fun upperDurationMinutes(estimatedDurationRange: String): Int {
        val upperText = estimatedDurationRange.substringAfter("-", estimatedDurationRange).trim()
        val match = DurationPattern.matchEntire(upperText) ?: return 0
        val hours = match.groupValues[1].toInt()
        val minutes = match.groupValues[2].toInt()

        return hours * 60 + minutes
    }

    private val DurationPattern = Regex("""(\d+):([0-5]\d)""")
}
