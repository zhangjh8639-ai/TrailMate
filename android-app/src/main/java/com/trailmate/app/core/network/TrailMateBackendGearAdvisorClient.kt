package com.trailmate.app.core.network

import com.trailmate.app.core.model.AiGearAdvisorBackendClient
import com.trailmate.app.core.model.AiGearAdvisorBackendResult
import com.trailmate.app.core.model.AiGearAdvisorRequest

class TrailMateBackendGearAdvisorClient(
    private val planId: String,
    private val api: TrailMateGearAdviceApi
) : AiGearAdvisorBackendClient {
    override fun requestAdvice(request: AiGearAdvisorRequest): AiGearAdvisorBackendResult =
        when (val result = api.requestGearAdvice(planId = planId, request = request)) {
            is TrailMateApiResult.Success -> AiGearAdvisorBackendResult.Success(result.value)
            is TrailMateApiResult.Failure -> AiGearAdvisorBackendResult.Unavailable
        }
}
