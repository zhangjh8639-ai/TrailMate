package com.trailmate.app.core.model

interface AiGearAdvisorBackendClient {
    fun requestAdvice(request: AiGearAdvisorRequest): AiGearAdvisorBackendResult
}

sealed interface AiGearAdvisorBackendResult {
    data class Success(val response: AiGearAdvisorResponse) : AiGearAdvisorBackendResult
    data object Timeout : AiGearAdvisorBackendResult
    data object Unavailable : AiGearAdvisorBackendResult
}

enum class AiGearAdvisorBackendStatus {
    SUCCESS,
    RETRY_AVAILABLE,
    STALE_RESPONSE,
    INVALID_RESPONSE
}

enum class AiGearAdvisorBackendFailureReason {
    TIMEOUT,
    UNAVAILABLE,
    THROWN,
    STALE_ASSESSMENT,
    INVALID_RESPONSE
}

data class AiGearAdvisorBackendAdvice(
    val request: AiGearAdvisorRequest,
    val presentation: AiGearAdvisorPresentation,
    val backendStatus: AiGearAdvisorBackendStatus,
    val backendReason: AiGearAdvisorBackendFailureReason?
)

class AiGearAdvisorBackendService(
    private val client: AiGearAdvisorBackendClient
) {
    fun advise(request: AiGearAdvisorRequest): AiGearAdvisorBackendAdvice {
        val result = try {
            client.requestAdvice(request)
        } catch (_: Exception) {
            return retryableFallback(
                request = request,
                reason = AiGearAdvisorBackendFailureReason.THROWN
            )
        }

        return when (result) {
            is AiGearAdvisorBackendResult.Success -> successAdvice(
                request = request,
                response = result.response
            )
            AiGearAdvisorBackendResult.Timeout -> retryableFallback(
                request = request,
                reason = AiGearAdvisorBackendFailureReason.TIMEOUT
            )
            AiGearAdvisorBackendResult.Unavailable -> retryableFallback(
                request = request,
                reason = AiGearAdvisorBackendFailureReason.UNAVAILABLE
            )
        }
    }

    private fun successAdvice(
        request: AiGearAdvisorRequest,
        response: AiGearAdvisorResponse
    ): AiGearAdvisorBackendAdvice {
        val presentation = AiGearAdvisorContract.resolvePresentation(
            request = request,
            response = response
        )
        val backendStatus = when {
            !presentation.isFallbackActive -> AiGearAdvisorBackendStatus.SUCCESS
            presentation.isStaleResponse -> AiGearAdvisorBackendStatus.STALE_RESPONSE
            else -> AiGearAdvisorBackendStatus.INVALID_RESPONSE
        }
        val backendReason = when (backendStatus) {
            AiGearAdvisorBackendStatus.SUCCESS -> null
            AiGearAdvisorBackendStatus.STALE_RESPONSE -> AiGearAdvisorBackendFailureReason.STALE_ASSESSMENT
            AiGearAdvisorBackendStatus.INVALID_RESPONSE -> AiGearAdvisorBackendFailureReason.INVALID_RESPONSE
            AiGearAdvisorBackendStatus.RETRY_AVAILABLE -> null
        }

        return AiGearAdvisorBackendAdvice(
            request = request,
            presentation = presentation,
            backendStatus = backendStatus,
            backendReason = backendReason
        )
    }

    private fun retryableFallback(
        request: AiGearAdvisorRequest,
        reason: AiGearAdvisorBackendFailureReason
    ): AiGearAdvisorBackendAdvice {
        val fallback = AiGearAdvisorContract.resolvePresentation(
            request = request,
            response = null
        )

        return AiGearAdvisorBackendAdvice(
            request = request,
            presentation = fallback.copy(
                caption = "AI service could not respond; retry is available. ${fallback.caption}"
            ),
            backendStatus = AiGearAdvisorBackendStatus.RETRY_AVAILABLE,
            backendReason = reason
        )
    }
}
