package com.trailmate.app.core.model

data class AiGearAdvisorRequest(
    val route: ImportedRoute,
    val profile: BaselineProfile,
    val assessment: RouteAssessmentSummary,
    val fallbackRecommendations: List<GearRecommendation>,
    val assessmentFingerprint: String,
    val guardrails: List<String>
)

data class AiGearAdvisorResponse(
    val assessmentFingerprint: String,
    val recommendations: List<GearRecommendation>
)

data class AiGearAdvisorPresentation(
    val statusLabel: String,
    val caption: String,
    val recommendations: List<GearRecommendation>,
    val isFallbackActive: Boolean,
    val isStaleResponse: Boolean
)

object AiGearAdvisorContract {
    fun buildRequest(
        route: ImportedRoute,
        profile: BaselineProfile,
        assessment: RouteAssessmentSummary,
        fallbackRecommendations: List<GearRecommendation>
    ): AiGearAdvisorRequest {
        require(route.readyForAssessment()) { "Route must be parsed before requesting AI gear advice." }

        return AiGearAdvisorRequest(
            route = route,
            profile = profile,
            assessment = assessment,
            fallbackRecommendations = fallbackRecommendations,
            assessmentFingerprint = assessment.fingerprint(),
            guardrails = listOf(
                "不要改写路线评估、匹配等级、置信度、距离、爬升或风险。",
                "只返回装备类别、状态和理由。",
        "不要要求用户新增私有装备；缺口由服务端品牌库候选匹配。"
            )
        )
    }

    fun validateResponse(
        request: AiGearAdvisorRequest,
        response: AiGearAdvisorResponse
    ): List<GearRecommendation> {
        require(response.assessmentFingerprint == request.assessmentFingerprint) {
            "AI gear response does not match the current route assessment."
        }
        require(response.recommendations.isNotEmpty()) {
            "AI gear response must include at least one recommendation."
        }
        response.recommendations.forEach { recommendation ->
            require(recommendation.category.isNotBlank()) { "Recommendation category is required." }
            require(recommendation.rationale.isNotBlank()) { "Recommendation rationale is required." }
        }

        return response.recommendations
    }

    fun resolvePresentation(
        request: AiGearAdvisorRequest,
        response: AiGearAdvisorResponse?
    ): AiGearAdvisorPresentation {
        if (response == null) {
            return fallbackPresentation(
                request = request,
                statusLabel = "规则清单就绪",
                caption = fallbackCaption(request),
                isStaleResponse = false
            )
        }

        if (response.assessmentFingerprint != request.assessmentFingerprint) {
            return fallbackPresentation(
                request = request,
                statusLabel = "响应已过期",
                caption = "AI 清单属于另一条路线，当前展示路线规则清单，候选由服务端品牌库匹配。路线评估仍锁定为${request.assessment.matchLevel.displayLabel()}。",
                isStaleResponse = true
            )
        }

        return try {
            val recommendations = validateResponse(request = request, response = response)
            AiGearAdvisorPresentation(
                statusLabel = "AI 清单就绪",
            caption = "${recommendations.size} 条 AI 装备检查已按${request.assessment.matchLevel.displayLabel()}评估校验，候选由服务端品牌库匹配。",
                recommendations = recommendations,
                isFallbackActive = false,
                isStaleResponse = false
            )
        } catch (_: IllegalArgumentException) {
            fallbackPresentation(
                request = request,
                statusLabel = "规则清单就绪",
                caption = "AI 清单不完整，当前展示路线规则清单。" +
                    fallbackCaption(request),
                isStaleResponse = false
            )
        }
    }

    private fun RouteAssessmentSummary.fingerprint(): String =
        listOf(
            routeName,
            distanceKm,
            ascentMeters,
            matchLevel.name,
            confidenceLevel.name,
            estimatedDurationRange,
            risks.joinToString("|")
        ).joinToString("#")

    private fun fallbackPresentation(
        request: AiGearAdvisorRequest,
        statusLabel: String,
        caption: String,
        isStaleResponse: Boolean
    ): AiGearAdvisorPresentation =
        AiGearAdvisorPresentation(
            statusLabel = statusLabel,
            caption = caption,
            recommendations = request.fallbackRecommendations,
            isFallbackActive = true,
            isStaleResponse = isStaleResponse
        )

    private fun fallbackCaption(request: AiGearAdvisorRequest): String =
        "已准备 ${request.fallbackRecommendations.size} 条检查；路线评估锁定为${request.assessment.matchLevel.displayLabel()}。"
}

private fun MatchLevel.displayLabel(): String =
    when (this) {
        MatchLevel.RECOMMENDED -> "推荐"
        MatchLevel.CAUTION -> "谨慎尝试"
        MatchLevel.NOT_RECOMMENDED -> "不建议"
    }
