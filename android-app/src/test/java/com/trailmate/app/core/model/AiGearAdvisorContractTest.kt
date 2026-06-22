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

        assertEquals("龙井山脊", request.route.routeName)
        assertEquals(assessment.matchLevel, request.assessment.matchLevel)
        assertEquals(TrailMateSampleData.baselineProfile.commonPackWeightKg, request.profile.commonPackWeightKg)
        assertEquals(TrailMateSampleData.gearItems.size, request.ownedGear.size)
        assertEquals(fallback.size, request.fallbackRecommendations.size)
        assertTrue(request.guardrails.any { it.contains("不要改写路线评估") })
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

        assertEquals("AI 清单就绪", presentation.statusLabel)
        assertFalse(presentation.isFallbackActive)
        assertFalse(presentation.isStaleResponse)
        assertEquals(listOf(aiRecommendation), presentation.recommendations)
    }

    @Test
    fun presentationAppliesCurrentInventoryToValidatedAiRecommendations() {
        val inventoryWithPoles = inventory.addBrandGear(
            category = "登山杖",
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
                    category = "登山杖",
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
        assertEquals("AI 清单就绪", presentation.statusLabel)
        assertEquals(GearStatus.COVERED, poles.status)
        assertTrue(poles.matchedGearItemId.orEmpty().contains("leki-makalu-lite"))
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

        assertEquals("响应已过期", presentation.statusLabel)
        assertTrue(presentation.isFallbackActive)
        assertTrue(presentation.isStaleResponse)
        assertEquals(fallback, presentation.recommendations)
        assertTrue(presentation.caption.contains("另一条路线"))
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

        assertEquals("本地清单启用", presentation.statusLabel)
        assertTrue(presentation.isFallbackActive)
        assertFalse(presentation.isStaleResponse)
        assertEquals(fallback, presentation.recommendations)
        assertTrue(presentation.caption.contains("不完整"))
    }
}
