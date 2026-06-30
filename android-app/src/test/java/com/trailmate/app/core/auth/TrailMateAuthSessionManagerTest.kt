package com.trailmate.app.core.auth

import com.trailmate.app.core.gpx.GpxImportQueue
import com.trailmate.app.core.map.AmapOfflineBaseMapTileProof
import com.trailmate.app.core.map.AmapPrivacyConsent
import com.trailmate.app.core.model.AiGearAdvisorResponse
import com.trailmate.app.core.model.BaselineProfile
import com.trailmate.app.core.model.HistoricalActivity
import com.trailmate.app.core.model.ImportedRoute
import com.trailmate.app.core.model.TrackRecordingState
import com.trailmate.app.core.network.TrailMateApiError
import com.trailmate.app.core.network.TrailMateApiResult
import com.trailmate.app.core.network.TrailMateAuthApi
import com.trailmate.app.core.network.TrailMateAuthProviderDto
import com.trailmate.app.core.network.TrailMateAuthSessionDto
import com.trailmate.app.core.network.TrailMateLogoutRequestDto
import com.trailmate.app.core.network.TrailMatePhoneCodeRequestDto
import com.trailmate.app.core.network.TrailMatePhoneCodeResponseDto
import com.trailmate.app.core.network.TrailMatePhoneLoginRequestDto
import com.trailmate.app.core.network.TrailMateRefreshSessionRequestDto
import com.trailmate.app.core.network.TrailMateWechatLoginRequestDto
import com.trailmate.app.core.persistence.TrailMateSessionRepository
import com.trailmate.app.core.persistence.TrailMateSnapshot
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant

class TrailMateAuthSessionManagerTest {
    @Test
    fun refreshSessionIfNeededKeepsCurrentSessionWhenExpiryIsNotNear() {
        val session = wechatSession(
            accessToken = "access-current",
            refreshToken = "refresh-current",
            expiresAt = "2026-06-22T12:10:00Z"
        )
        val repository = FakeSessionRepository(TrailMateSnapshot(authSession = session))
        val api = FakeAuthApi()
        val manager = sessionManager(repository, api, now = "2026-06-22T12:00:00Z")

        val result = manager.refreshSessionIfNeeded()

        assertEquals(
            TrailMateAuthSessionManagerResult.Active(session, refreshed = false),
            result
        )
        assertEquals(emptyList<TrailMateRefreshSessionRequestDto>(), api.refreshRequests)
        assertEquals(session, repository.snapshot.authSession)
    }

    @Test
    fun refreshSessionIfNeededRotatesNearExpirySessionAndSavesIt() {
        val oldSession = wechatSession(
            accessToken = "old-access",
            refreshToken = "old-refresh",
            expiresAt = "2026-06-22T12:04:00Z"
        )
        val newSession = wechatSession(
            accessToken = "new-access",
            refreshToken = "new-refresh",
            expiresAt = "2026-06-22T14:00:00Z"
        )
        val repository = FakeSessionRepository(TrailMateSnapshot(authSession = oldSession))
        val api = FakeAuthApi(
            refreshResult = TrailMateApiResult.Success(newSession.toDto())
        )
        val manager = sessionManager(repository, api, now = "2026-06-22T12:00:00Z")

        val result = manager.refreshSessionIfNeeded()

        assertEquals(listOf(TrailMateRefreshSessionRequestDto("old-refresh")), api.refreshRequests)
        assertEquals(
            TrailMateAuthSessionManagerResult.Active(newSession, refreshed = true),
            result
        )
        assertEquals(newSession, repository.snapshot.authSession)
    }

    @Test
    fun refreshSessionIfNeededClearsAuthSessionWhenRefreshTokenIsRejected() {
        val session = wechatSession(
            accessToken = "old-access",
            refreshToken = "rejected-refresh",
            expiresAt = "2026-06-22T12:00:00Z"
        )
        val repository = FakeSessionRepository(TrailMateSnapshot(authSession = session))
        val api = FakeAuthApi(
            refreshResult = TrailMateApiResult.Failure(
                TrailMateApiError(
                    status = 400,
                    code = "AUTH_INVALID_REQUEST",
                    message = "Invalid refresh token.",
                    traceId = "trace-1"
                )
            )
        )
        val manager = sessionManager(repository, api, now = "2026-06-22T12:00:00Z")

        val result = manager.refreshSessionIfNeeded()

        assertEquals(TrailMateAuthSessionManagerResult.SignedOut, result)
        assertEquals(null, repository.snapshot.authSession)
    }

    @Test
    fun logoutCurrentSessionRevokesBackendTokenAndClearsAuthSession() {
        val session = wechatSession(
            accessToken = "access",
            refreshToken = "refresh-to-revoke",
            expiresAt = "2026-06-22T14:00:00Z"
        )
        val repository = FakeSessionRepository(TrailMateSnapshot(authSession = session))
        val api = FakeAuthApi(logoutResult = TrailMateApiResult.Success(Unit))
        val manager = sessionManager(repository, api, now = "2026-06-22T12:00:00Z")

        val result = manager.logoutCurrentSession()

        assertEquals(listOf(TrailMateLogoutRequestDto("refresh-to-revoke")), api.logoutRequests)
        assertEquals(TrailMateAuthSessionManagerResult.SignedOut, result)
        assertEquals(null, repository.snapshot.authSession)
    }

    @Test
    fun logoutCurrentSessionKeepsAuthSessionWhenBackendLogoutFails() {
        val session = wechatSession(
            accessToken = "access",
            refreshToken = "refresh-to-revoke",
            expiresAt = "2026-06-22T14:00:00Z"
        )
        val repository = FakeSessionRepository(TrailMateSnapshot(authSession = session))
        val api = FakeAuthApi(
            logoutResult = TrailMateApiResult.Failure(
                TrailMateApiError(
                    status = 0,
                    code = "NETWORK_ERROR",
                    message = "Network request failed.",
                    traceId = null
                )
            )
        )
        val manager = sessionManager(repository, api, now = "2026-06-22T12:00:00Z")

        val result = manager.logoutCurrentSession()

        assertEquals(
            TrailMateAuthSessionManagerResult.Failure(
                code = "NETWORK_ERROR",
                message = "Network request failed.",
                traceId = null
            ),
            result
        )
        assertEquals(session, repository.snapshot.authSession)
    }

    private fun sessionManager(
        repository: FakeSessionRepository,
        api: FakeAuthApi,
        now: String
    ): TrailMateAuthSessionManager =
        TrailMateAuthSessionManager(
            repository = repository,
            authenticationService = TrailMateAuthenticationService(api),
            nowEpochMillis = { Instant.parse(now).toEpochMilli() }
        )

    private fun wechatSession(
        accessToken: String,
        refreshToken: String,
        expiresAt: String
    ): TrailMateAuthSession =
        TrailMateAuthSession(
            userId = "usr-wx",
            provider = TrailMateAuthProvider.WECHAT,
            accessToken = accessToken,
            refreshToken = refreshToken,
            expiresAt = expiresAt,
            phoneNumber = null,
            wechatOpenId = "openid",
            displayName = "张三"
        )

    private fun TrailMateAuthSession.toDto(): TrailMateAuthSessionDto =
        TrailMateAuthSessionDto(
            userId = userId,
            provider = TrailMateAuthProviderDto.WECHAT,
            accessToken = accessToken,
            refreshToken = refreshToken,
            expiresAt = expiresAt,
            wechatOpenId = wechatOpenId,
            displayName = displayName
        )

    private class FakeAuthApi(
        private val refreshResult: TrailMateApiResult<TrailMateAuthSessionDto> =
            TrailMateApiResult.Failure(TrailMateApiError(500, "NOT_STUBBED", "not stubbed", null)),
        private val logoutResult: TrailMateApiResult<Unit> =
            TrailMateApiResult.Failure(TrailMateApiError(500, "NOT_STUBBED", "not stubbed", null))
    ) : TrailMateAuthApi {
        val refreshRequests = mutableListOf<TrailMateRefreshSessionRequestDto>()
        val logoutRequests = mutableListOf<TrailMateLogoutRequestDto>()

        override fun requestPhoneCode(
            request: TrailMatePhoneCodeRequestDto
        ): TrailMateApiResult<TrailMatePhoneCodeResponseDto> =
            error("not used")

        override fun loginWithPhone(
            request: TrailMatePhoneLoginRequestDto
        ): TrailMateApiResult<TrailMateAuthSessionDto> =
            error("not used")

        override fun loginWithWechat(
            request: TrailMateWechatLoginRequestDto
        ): TrailMateApiResult<TrailMateAuthSessionDto> =
            error("not used")

        override fun refreshSession(
            request: TrailMateRefreshSessionRequestDto
        ): TrailMateApiResult<TrailMateAuthSessionDto> {
            refreshRequests += request
            return refreshResult
        }

        override fun logout(
            request: TrailMateLogoutRequestDto
        ): TrailMateApiResult<Unit> {
            logoutRequests += request
            return logoutResult
        }
    }

    private class FakeSessionRepository(
        initialSnapshot: TrailMateSnapshot
    ) : TrailMateSessionRepository {
        var snapshot: TrailMateSnapshot = initialSnapshot
            private set

        override fun loadSnapshot(): TrailMateSnapshot = snapshot

        override fun saveAuthSession(session: TrailMateAuthSession) {
            snapshot = snapshot.copy(authSession = session)
        }

        override fun clearAuthSession() {
            snapshot = snapshot.copy(authSession = null)
        }

        override fun saveProfile(profile: BaselineProfile) = Unit
        override fun saveImportedRoute(route: ImportedRoute) = Unit
        override fun saveHistoricalActivities(historicalActivities: List<HistoricalActivity>) = Unit
        override fun saveGpxImportQueue(queue: GpxImportQueue) = Unit
        override fun saveTrackRecording(trackRecording: TrackRecordingState) = Unit
        override fun saveAmapPrivacyConsent(consent: AmapPrivacyConsent) = Unit
        override fun saveOfflineRoutePackKeys(keys: Set<String>) = Unit
        override fun saveOfflineBaseMapTileProofs(proofs: List<AmapOfflineBaseMapTileProof>) = Unit
        override fun saveAiGearAdvisorResponse(response: AiGearAdvisorResponse?) = Unit

        override fun clearLocalData() {
            snapshot = TrailMateSnapshot.empty()
        }
    }
}
