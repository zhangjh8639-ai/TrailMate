package com.trailmate.app.core.auth

import com.trailmate.app.core.persistence.TrailMateSessionRepository
import java.time.Instant

sealed interface TrailMateAuthSessionManagerResult {
    data class Active(
        val session: TrailMateAuthSession,
        val refreshed: Boolean
    ) : TrailMateAuthSessionManagerResult

    data object SignedOut : TrailMateAuthSessionManagerResult

    data class Failure(
        val code: String,
        val message: String,
        val traceId: String?
    ) : TrailMateAuthSessionManagerResult
}

class TrailMateAuthSessionManager(
    private val repository: TrailMateSessionRepository,
    private val authenticationService: TrailMateAuthenticationService,
    private val nowEpochMillis: () -> Long = { System.currentTimeMillis() },
    private val refreshWindowMillis: Long = DEFAULT_REFRESH_WINDOW_MILLIS
) {
    fun refreshSessionIfNeeded(): TrailMateAuthSessionManagerResult {
        val session = repository.loadSnapshot().authSession
            ?: return TrailMateAuthSessionManagerResult.SignedOut
        if (!session.shouldRefresh(nowEpochMillis(), refreshWindowMillis)) {
            return TrailMateAuthSessionManagerResult.Active(session, refreshed = false)
        }

        return when (val result = authenticationService.refreshSession(session.refreshToken)) {
            is TrailMateAuthActionResult.Success -> {
                repository.saveAuthSession(result.value)
                TrailMateAuthSessionManagerResult.Active(result.value, refreshed = true)
            }
            is TrailMateAuthActionResult.InvalidInput -> {
                repository.clearAuthSession()
                TrailMateAuthSessionManagerResult.SignedOut
            }
            is TrailMateAuthActionResult.Failure -> {
                repository.clearAuthSession()
                TrailMateAuthSessionManagerResult.SignedOut
            }
        }
    }

    fun logoutCurrentSession(): TrailMateAuthSessionManagerResult {
        val session = repository.loadSnapshot().authSession
            ?: return TrailMateAuthSessionManagerResult.SignedOut

        return when (val result = authenticationService.logout(session.refreshToken)) {
            is TrailMateAuthActionResult.Success -> {
                repository.clearAuthSession()
                TrailMateAuthSessionManagerResult.SignedOut
            }
            is TrailMateAuthActionResult.InvalidInput -> {
                repository.clearAuthSession()
                TrailMateAuthSessionManagerResult.SignedOut
            }
            is TrailMateAuthActionResult.Failure ->
                TrailMateAuthSessionManagerResult.Failure(
                    code = result.code,
                    message = result.message,
                    traceId = result.traceId
                )
        }
    }

    private fun TrailMateAuthSession.shouldRefresh(
        nowEpochMillis: Long,
        refreshWindowMillis: Long
    ): Boolean {
        val expiresAtEpochMillis = runCatching {
            Instant.parse(expiresAt).toEpochMilli()
        }.getOrNull() ?: return true
        return expiresAtEpochMillis - nowEpochMillis <= refreshWindowMillis
    }

    private companion object {
        const val DEFAULT_REFRESH_WINDOW_MILLIS = 5 * 60 * 1000L
    }
}
