package com.trailmate.app.core.model

data class AiGearAdvisorRequest(
    val route: ImportedRoute,
    val profile: BaselineProfile,
    val assessment: RouteAssessmentSummary,
    val ownedGear: List<GearItem>,
    val fallbackRecommendations: List<GearRecommendation>,
    val assessmentFingerprint: String,
    val guardrails: List<String>
)

data class AiGearAdvisorResponse(
    val assessmentFingerprint: String,
    val recommendations: List<GearRecommendation>
)

object AiGearAdvisorContract {
    fun buildRequest(
        route: ImportedRoute,
        profile: BaselineProfile,
        assessment: RouteAssessmentSummary,
        inventory: GearInventory,
        fallbackRecommendations: List<GearRecommendation>
    ): AiGearAdvisorRequest {
        require(route.readyForAssessment()) { "Route must be parsed before requesting AI gear advice." }

        return AiGearAdvisorRequest(
            route = route,
            profile = profile,
            assessment = assessment,
            ownedGear = inventory.items,
            fallbackRecommendations = fallbackRecommendations,
            assessmentFingerprint = assessment.fingerprint(),
            guardrails = listOf(
                "Do not change route assessment, match level, confidence, distance, ascent, or risks.",
                "Only return gear recommendations with category, status, and rationale.",
                "Use owned gear only to explain coverage; missing essentials must remain visible."
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
}
