package com.trailmate.app.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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

    @Test
    fun presentationUsesValidatedAiRecommendationsWhenFingerprintMatches() {
        val request = AiGearAdvisorContract.buildRequest(
            route = TrailMateSampleData.importedTargetRoute,
            profile = TrailMateSampleData.baselineProfile,
            assessment = assessment,
            inventory = inventory,
            fallbackRecommendations = fallback
        )
        val aiRecommendation = fallback.first().copy(
            rationale = "AI refined route-aware coverage."
        )
        val response = AiGearAdvisorResponse(
            assessmentFingerprint = request.assessmentFingerprint,
            recommendations = listOf(aiRecommendation)
        )

        val presentation = AiGearAdvisorContract.resolvePresentation(
            request = request,
            response = response
        )

        assertEquals("AI ready", presentation.statusLabel)
        assertFalse(presentation.isFallbackActive)
        assertFalse(presentation.isStaleResponse)
        assertEquals(listOf(aiRecommendation), presentation.recommendations)
    }

    @Test
    fun presentationAppliesCurrentInventoryToValidatedAiRecommendations() {
        val inventoryWithPoles = inventory.addBrandGear(
            category = "Trekking poles",
            brand = "Leki",
            model = "Makalu Lite",
            weightGrams = 480
        )
        val fallbackWithPoles = inventoryWithPoles.applyTo(
            RouteGearAdvisorEngine.recommend(
                route = TrailMateSampleData.importedTargetRoute,
                assessment = assessment
            )
        )
        val request = AiGearAdvisorContract.buildRequest(
            route = TrailMateSampleData.importedTargetRoute,
            profile = TrailMateSampleData.baselineProfile,
            assessment = assessment,
            inventory = inventoryWithPoles,
            fallbackRecommendations = fallbackWithPoles
        )
        val response = AiGearAdvisorResponse(
            assessmentFingerprint = request.assessmentFingerprint,
            recommendations = listOf(
                GearRecommendation(
                    category = "Trekking poles",
                    status = GearStatus.MISSING,
                    rationale = "AI route rationale before inventory refresh."
                )
            )
        )

        val presentation = AiGearAdvisorContract.resolvePresentation(
            request = request,
            response = response
        )

        val poles = presentation.recommendations.single()
        assertEquals("AI ready", presentation.statusLabel)
        assertEquals(GearStatus.COVERED, poles.status)
        assertTrue(poles.matchedGearItemId.orEmpty().contains("trekking-poles-leki-makalu-lite"))
    }

    @Test
    fun presentationMarksStaleResponseAndKeepsFallbackChecklist() {
        val request = AiGearAdvisorContract.buildRequest(
            route = TrailMateSampleData.importedTargetRoute,
            profile = TrailMateSampleData.baselineProfile,
            assessment = assessment,
            inventory = inventory,
            fallbackRecommendations = fallback
        )
        val staleResponse = AiGearAdvisorResponse(
            assessmentFingerprint = "different-route",
            recommendations = listOf(
                GearRecommendation(
                    category = "Avalanche beacon",
                    status = GearStatus.MISSING,
                    rationale = "Old winter route response."
                )
            )
        )

        val presentation = AiGearAdvisorContract.resolvePresentation(
            request = request,
            response = staleResponse
        )

        assertEquals("Stale response", presentation.statusLabel)
        assertTrue(presentation.isFallbackActive)
        assertTrue(presentation.isStaleResponse)
        assertEquals(fallback, presentation.recommendations)
        assertTrue(presentation.caption.contains("different route", ignoreCase = true))
        assertFalse(presentation.recommendations.any { it.category == "Avalanche beacon" })
    }

    @Test
    fun presentationFallsBackWhenMatchingAiResponseIsIncomplete() {
        val request = AiGearAdvisorContract.buildRequest(
            route = TrailMateSampleData.importedTargetRoute,
            profile = TrailMateSampleData.baselineProfile,
            assessment = assessment,
            inventory = inventory,
            fallbackRecommendations = fallback
        )
        val incompleteResponse = AiGearAdvisorResponse(
            assessmentFingerprint = request.assessmentFingerprint,
            recommendations = emptyList()
        )

        val presentation = AiGearAdvisorContract.resolvePresentation(
            request = request,
            response = incompleteResponse
        )

        assertEquals("Fallback active", presentation.statusLabel)
        assertTrue(presentation.isFallbackActive)
        assertFalse(presentation.isStaleResponse)
        assertEquals(fallback, presentation.recommendations)
        assertTrue(presentation.caption.contains("incomplete", ignoreCase = true))
    }
}
