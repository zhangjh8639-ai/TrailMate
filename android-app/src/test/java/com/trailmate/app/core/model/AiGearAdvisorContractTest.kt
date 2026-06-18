package com.trailmate.app.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class AiGearAdvisorContractTest {
    private val assessment = RouteAssessmentEngine.assess(
        profile = TrailMateSampleData.baselineProfile,
        route = TrailMateSampleData.importedTargetRoute
    )
    private val inventory = GearInventory(TrailMateSampleData.gearItems)
    private val fallback = inventory.applyTo(
        RouteGearAdvisorEngine.recommend(
            route = TrailMateSampleData.importedTargetRoute,
            assessment = assessment
        )
    )

    @Test
    fun requestIncludesRouteProfileInventoryFallbackAndAssessmentGuardrail() {
        val request = AiGearAdvisorContract.buildRequest(
            route = TrailMateSampleData.importedTargetRoute,
            profile = TrailMateSampleData.baselineProfile,
            assessment = assessment,
            inventory = inventory,
            fallbackRecommendations = fallback
        )

        assertEquals("Longjing Ridge", request.route.routeName)
        assertEquals(assessment.matchLevel, request.assessment.matchLevel)
        assertEquals(TrailMateSampleData.baselineProfile.commonPackWeightKg, request.profile.commonPackWeightKg)
        assertEquals(TrailMateSampleData.gearItems.size, request.ownedGear.size)
        assertEquals(fallback.size, request.fallbackRecommendations.size)
        assertTrue(request.guardrails.any { it.contains("Do not change route assessment", ignoreCase = true) })
        assertTrue(request.assessmentFingerprint.isNotBlank())
    }

    @Test
    fun responseValidationRejectsStaleAssessmentFingerprint() {
        val request = AiGearAdvisorContract.buildRequest(
            route = TrailMateSampleData.importedTargetRoute,
            profile = TrailMateSampleData.baselineProfile,
            assessment = assessment,
            inventory = inventory,
            fallbackRecommendations = fallback
        )
        val response = AiGearAdvisorResponse(
            assessmentFingerprint = "different-route",
            recommendations = fallback
        )

        assertThrows(IllegalArgumentException::class.java) {
            AiGearAdvisorContract.validateResponse(request, response)
        }
    }

    @Test
    fun responseValidationRejectsBlankRecommendationFields() {
        val request = AiGearAdvisorContract.buildRequest(
            route = TrailMateSampleData.importedTargetRoute,
            profile = TrailMateSampleData.baselineProfile,
            assessment = assessment,
            inventory = inventory,
            fallbackRecommendations = fallback
        )
        val response = AiGearAdvisorResponse(
            assessmentFingerprint = request.assessmentFingerprint,
            recommendations = listOf(
                GearRecommendation(
                    category = " ",
                    status = GearStatus.MISSING,
                    rationale = " "
                )
            )
        )

        assertThrows(IllegalArgumentException::class.java) {
            AiGearAdvisorContract.validateResponse(request, response)
        }
    }

    @Test
    fun validResponseReturnsRecommendationsWithoutChangingAssessment() {
        val request = AiGearAdvisorContract.buildRequest(
            route = TrailMateSampleData.importedTargetRoute,
            profile = TrailMateSampleData.baselineProfile,
            assessment = assessment,
            inventory = inventory,
            fallbackRecommendations = fallback
        )
        val response = AiGearAdvisorResponse(
            assessmentFingerprint = request.assessmentFingerprint,
            recommendations = fallback
        )

        val validated = AiGearAdvisorContract.validateResponse(request, response)

        assertEquals(fallback, validated)
        assertEquals(MatchLevel.CAUTION, assessment.matchLevel)
        assertEquals(ConfidenceLevel.LOW, assessment.confidenceLevel)
    }
}
