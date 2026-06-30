package com.trailmate.app.core.network

import com.trailmate.app.core.model.AiGearAdvisorContract
import com.trailmate.app.core.model.AiGearAdvisorRequest
import com.trailmate.app.core.model.AiGearAdvisorResponse
import com.trailmate.app.core.model.GearRecommendation
import com.trailmate.app.core.model.GearStatus
import com.trailmate.app.core.model.RouteAssessmentEngine
import com.trailmate.app.core.model.RouteGearAdvisorEngine
import com.trailmate.app.core.model.TrailMateSampleData
import com.trailmate.app.core.model.AiGearAdvisorBackendResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TrailMateBackendGearAdvisorClientTest {
    private val assessment = RouteAssessmentEngine.assess(
        profile = TrailMateSampleData.baselineProfile,
        route = TrailMateSampleData.importedTargetRoute
    )
    private val request = AiGearAdvisorContract.buildRequest(
        route = TrailMateSampleData.importedTargetRoute,
        profile = TrailMateSampleData.baselineProfile,
        assessment = assessment,
        fallbackRecommendations = RouteGearAdvisorEngine.recommend(
            route = TrailMateSampleData.importedTargetRoute,
            assessment = assessment
        )
    )

    @Test
    fun successMapsUnifiedBackendApiResultToGearAdvisorSuccess() {
        val response = AiGearAdvisorResponse(
            assessmentFingerprint = request.assessmentFingerprint,
            recommendations = listOf(
                GearRecommendation(
                    category = "头灯",
                    status = GearStatus.CHECK,
                    rationale = "预计耗时较长，建议确认电量。"
                )
            )
        )
        val api = FakeGearAdviceApi(result = TrailMateApiResult.Success(response))
        val client = TrailMateBackendGearAdvisorClient(
            planId = "plan-123",
            api = api
        )

        val result = client.requestAdvice(request)

        assertEquals(listOf("plan-123" to request), api.calls)
        assertEquals(AiGearAdvisorBackendResult.Success(response), result)
    }

    @Test
    fun failureMapsUnifiedBackendApiErrorToRetryableUnavailable() {
        val api = FakeGearAdviceApi(
            result = TrailMateApiResult.Failure(
                TrailMateApiError(
                    status = 503,
                    code = "AI_SERVICE_UNAVAILABLE",
                    message = "AI service is temporarily unavailable.",
                    traceId = "trace-ai-1"
                )
            )
        )
        val client = TrailMateBackendGearAdvisorClient(
            planId = "plan-123",
            api = api
        )

        val result = client.requestAdvice(request)

        assertEquals(listOf("plan-123" to request), api.calls)
        assertTrue(result is AiGearAdvisorBackendResult.Unavailable)
    }

    @Test
    fun networkTimeoutMapsUnifiedBackendApiErrorToRetryableTimeout() {
        val api = FakeGearAdviceApi(
            result = TrailMateApiResult.Failure(
                TrailMateApiError(
                    status = 0,
                    code = "NETWORK_TIMEOUT",
                    message = "Timed out while requesting AI gear advice.",
                    traceId = null
                )
            )
        )
        val client = TrailMateBackendGearAdvisorClient(
            planId = "plan-123",
            api = api
        )

        val result = client.requestAdvice(request)

        assertEquals(listOf("plan-123" to request), api.calls)
        assertTrue(result is AiGearAdvisorBackendResult.Timeout)
    }

    private class FakeGearAdviceApi(
        private val result: TrailMateApiResult<AiGearAdvisorResponse>
    ) : TrailMateGearAdviceApi {
        val calls = mutableListOf<Pair<String, AiGearAdvisorRequest>>()

        override fun requestGearAdvice(
            planId: String,
            request: AiGearAdvisorRequest
        ): TrailMateApiResult<AiGearAdvisorResponse> {
            calls += planId to request
            return result
        }
    }
}
