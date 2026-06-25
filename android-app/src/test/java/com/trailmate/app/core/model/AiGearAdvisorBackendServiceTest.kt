package com.trailmate.app.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class AiGearAdvisorBackendServiceTest {
    private val assessment = RouteAssessmentEngine.assess(
        profile = TrailMateSampleData.baselineProfile,
        route = TrailMateSampleData.importedTargetRoute
    )
    private val fallback = RouteGearAdvisorEngine.recommend(
        route = TrailMateSampleData.importedTargetRoute,
        assessment = assessment
    )
    private val request = AiGearAdvisorContract.buildRequest(
        route = TrailMateSampleData.importedTargetRoute,
        profile = TrailMateSampleData.baselineProfile,
        assessment = assessment,
        fallbackRecommendations = fallback
    )

    @Test
    fun successfulBackendResponseReturnsValidatedCatalogReadyPresentation() {
        val backendClient = FakeBackendClient(
            result = AiGearAdvisorBackendResult.Success(
                AiGearAdvisorResponse(
                    assessmentFingerprint = request.assessmentFingerprint,
                    recommendations = listOf(
                        GearRecommendation(
                            category = "登山杖",
                            status = GearStatus.MISSING,
                            rationale = "AI route rationale before catalog matching."
                        )
                    )
                )
            )
        )
        val service = AiGearAdvisorBackendService(client = backendClient)

        val result = service.advise(request)

        val poles = result.presentation.recommendations.single()
        assertEquals(listOf(request), backendClient.requests)
        assertEquals(AiGearAdvisorBackendStatus.SUCCESS, result.backendStatus)
        assertEquals("AI 清单就绪", result.presentation.statusLabel)
        assertFalse(result.presentation.isFallbackActive)
        assertEquals(GearStatus.MISSING, poles.status)
        assertTrue(poles.matchedGearItemId.isNullOrBlank())
        assertTrue(result.presentation.caption.contains("服务端品牌库"))
    }

    @Test
    fun timeoutReturnsRetryableFallbackWithoutChangingAssessmentValues() {
        val service = AiGearAdvisorBackendService(
            client = FakeBackendClient(result = AiGearAdvisorBackendResult.Timeout)
        )

        val result = service.advise(request)

        assertEquals(AiGearAdvisorBackendStatus.RETRY_AVAILABLE, result.backendStatus)
        assertEquals(AiGearAdvisorBackendFailureReason.TIMEOUT, result.backendReason)
        assertEquals("规则清单就绪", result.presentation.statusLabel)
        assertTrue(result.presentation.isFallbackActive)
        assertFalse(result.presentation.isStaleResponse)
        assertEquals(fallback, result.presentation.recommendations)
        assertEquals(MatchLevel.CAUTION, request.assessment.matchLevel)
        assertEquals(ConfidenceLevel.LOW, request.assessment.confidenceLevel)
        assertTrue(result.presentation.caption.contains("retry", ignoreCase = true))
    }

    @Test
    fun unavailableReturnsRetryableFallbackWithoutChangingAssessmentValues() {
        val service = AiGearAdvisorBackendService(
            client = FakeBackendClient(result = AiGearAdvisorBackendResult.Unavailable)
        )

        val result = service.advise(request)

        assertEquals(AiGearAdvisorBackendStatus.RETRY_AVAILABLE, result.backendStatus)
        assertEquals(AiGearAdvisorBackendFailureReason.UNAVAILABLE, result.backendReason)
        assertTrue(result.presentation.isFallbackActive)
        assertEquals(fallback, result.presentation.recommendations)
        assertEquals(request.assessment.distanceKm, result.request.assessment.distanceKm, 0.0)
        assertEquals(request.assessment.ascentMeters, result.request.assessment.ascentMeters)
    }

    @Test
    fun thrownBackendFailureReturnsRetryableFallbackWithoutChangingAssessmentValues() {
        val service = AiGearAdvisorBackendService(
            client = object : AiGearAdvisorBackendClient {
                override fun requestAdvice(request: AiGearAdvisorRequest): AiGearAdvisorBackendResult {
                    throw IllegalStateException("backend exploded")
                }
            }
        )

        val result = service.advise(request)

        assertEquals(AiGearAdvisorBackendStatus.RETRY_AVAILABLE, result.backendStatus)
        assertEquals(AiGearAdvisorBackendFailureReason.THROWN, result.backendReason)
        assertTrue(result.presentation.isFallbackActive)
        assertEquals(fallback, result.presentation.recommendations)
        assertEquals(MatchLevel.CAUTION, result.request.assessment.matchLevel)
        assertEquals(ConfidenceLevel.LOW, result.request.assessment.confidenceLevel)
    }

    @Test
    fun ioBackendFailureReturnsRetryableFallbackWithoutEscaping() {
        val service = AiGearAdvisorBackendService(
            client = object : AiGearAdvisorBackendClient {
                override fun requestAdvice(request: AiGearAdvisorRequest): AiGearAdvisorBackendResult {
                    throw IOException("socket closed")
                }
            }
        )

        val result = service.advise(request)

        assertEquals(AiGearAdvisorBackendStatus.RETRY_AVAILABLE, result.backendStatus)
        assertEquals(AiGearAdvisorBackendFailureReason.THROWN, result.backendReason)
        assertTrue(result.presentation.isFallbackActive)
        assertEquals(fallback, result.presentation.recommendations)
    }

    @Test
    fun staleBackendResponseUsesStaleFallbackAndHidesStaleCategories() {
        val service = AiGearAdvisorBackendService(
            client = FakeBackendClient(
                result = AiGearAdvisorBackendResult.Success(
                    AiGearAdvisorResponse(
                        assessmentFingerprint = "different-route",
                        recommendations = listOf(
                            GearRecommendation(
                                category = "Avalanche beacon",
                                status = GearStatus.MISSING,
                                rationale = "Old winter route response."
                            )
                        )
                    )
                )
            )
        )

        val result = service.advise(request)

        assertEquals(AiGearAdvisorBackendStatus.STALE_RESPONSE, result.backendStatus)
        assertEquals(AiGearAdvisorBackendFailureReason.STALE_ASSESSMENT, result.backendReason)
        assertTrue(result.presentation.isFallbackActive)
        assertTrue(result.presentation.isStaleResponse)
        assertEquals(fallback, result.presentation.recommendations)
        assertFalse(result.presentation.recommendations.any { it.category == "Avalanche beacon" })
    }

    @Test
    fun invalidBackendSuccessIsNotTreatedAsAiSuccess() {
        val service = AiGearAdvisorBackendService(
            client = FakeBackendClient(
                result = AiGearAdvisorBackendResult.Success(
                    AiGearAdvisorResponse(
                        assessmentFingerprint = request.assessmentFingerprint,
                        recommendations = emptyList()
                    )
                )
            )
        )

        val result = service.advise(request)

        assertEquals(AiGearAdvisorBackendStatus.INVALID_RESPONSE, result.backendStatus)
        assertEquals(AiGearAdvisorBackendFailureReason.INVALID_RESPONSE, result.backendReason)
        assertTrue(result.presentation.isFallbackActive)
        assertFalse(result.presentation.isStaleResponse)
        assertEquals(fallback, result.presentation.recommendations)
        assertFalse(result.presentation.statusLabel.contains("AI 清单就绪"))
    }

    private class FakeBackendClient(
        private val result: AiGearAdvisorBackendResult
    ) : AiGearAdvisorBackendClient {
        val requests = mutableListOf<AiGearAdvisorRequest>()

        override fun requestAdvice(request: AiGearAdvisorRequest): AiGearAdvisorBackendResult {
            requests += request
            return result
        }
    }
}
