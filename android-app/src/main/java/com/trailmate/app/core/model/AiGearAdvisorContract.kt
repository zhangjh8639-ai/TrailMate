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

    fun resolvePresentation(
        request: AiGearAdvisorRequest,
        response: AiGearAdvisorResponse?
    ): AiGearAdvisorPresentation {
        if (response == null) {
            return fallbackPresentation(
                request = request,
                statusLabel = "Fallback active",
                caption = fallbackCaption(request),
                isStaleResponse = false
            )
        }

        if (response.assessmentFingerprint != request.assessmentFingerprint) {
            return fallbackPresentation(
                request = request,
                statusLabel = "Stale response",
                caption = "Showing local fallback because the AI checklist belongs to a different route. " +
                    "Route assessment remains locked to ${request.assessment.matchLevel.name}.",
                isStaleResponse = true
            )
        }

        return try {
            val recommendations = GearInventory(request.ownedGear).applyTo(
                validateResponse(request = request, response = response)
            )
            AiGearAdvisorPresentation(
                statusLabel = "AI ready",
                caption = "${recommendations.size} AI checks validated against " +
                    "${request.assessment.matchLevel.name} assessment.",
                recommendations = recommendations,
                isFallbackActive = false,
                isStaleResponse = false
            )
        } catch (_: IllegalArgumentException) {
            fallbackPresentation(
                request = request,
                statusLabel = "Fallback active",
                caption = "Showing local fallback because the AI checklist was incomplete. " +
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
        "${request.fallbackRecommendations.size} checks prepared; " +
            "route assessment locked to ${request.assessment.matchLevel.name}."
}
