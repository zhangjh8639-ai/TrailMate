package com.trailmate.app.core.auth

import com.trailmate.app.core.model.TrailMateSampleData
import com.trailmate.app.core.network.TrailMateApiError
import com.trailmate.app.core.network.TrailMateApiResult
import com.trailmate.app.core.network.TrailMateOnboardingProfileDto
import com.trailmate.app.core.network.TrailMateUserProfileApi
import org.junit.Assert.assertEquals
import org.junit.Test

class TrailMateOnboardingProfileSyncerTest {
    @Test
    fun skipsSyncWhenNoAuthenticatedSessionExists() {
        val api = FakeUserProfileApi()
        val syncer = TrailMateOnboardingProfileSyncer(api)

        val result = syncer.sync(
            authSession = null,
            profile = TrailMateSampleData.baselineProfile
        )

        assertEquals(TrailMateOnboardingProfileSyncResult.SKIPPED_NO_SESSION, result)
        assertEquals(emptyList<String>(), api.userIds)
    }

    @Test
    fun syncsBaselineProfileForAuthenticatedUser() {
        val api = FakeUserProfileApi()
        val syncer = TrailMateOnboardingProfileSyncer(api)

        val result = syncer.sync(
            authSession = TrailMateAuthSession(
                userId = "usr-phone",
                provider = TrailMateAuthProvider.PHONE,
                accessToken = "access",
                refreshToken = "refresh",
                expiresAt = "2026-06-23T12:00:00Z",
                phoneNumber = "+8613800138000",
                wechatOpenId = null,
                displayName = null
            ),
            profile = TrailMateSampleData.baselineProfile
        )

        assertEquals(TrailMateOnboardingProfileSyncResult.SYNCED, result)
        assertEquals(listOf("usr-phone"), api.userIds)
        assertEquals(listOf(TrailMateSampleData.baselineProfile), api.profiles)
    }

    @Test
    fun reportsFailureWithoutClearingLocalProfile() {
        val api = FakeUserProfileApi(
            result = TrailMateApiResult.Failure(
                TrailMateApiError(
                    status = 0,
                    code = "NETWORK_ERROR",
                    message = "Network request failed.",
                    traceId = null
                )
            )
        )
        val syncer = TrailMateOnboardingProfileSyncer(api)

        val result = syncer.sync(
            authSession = TrailMateAuthSession.localWechatSession(nowEpochMillis = 1_000L),
            profile = TrailMateSampleData.baselineProfile
        )

        assertEquals(TrailMateOnboardingProfileSyncResult.FAILED, result)
        assertEquals(1, api.userIds.size)
    }

    private class FakeUserProfileApi(
        private val result: TrailMateApiResult<TrailMateOnboardingProfileDto> =
            TrailMateApiResult.Success(
                TrailMateOnboardingProfileDto.from(TrailMateSampleData.baselineProfile, userId = "usr-phone")
            )
    ) : TrailMateUserProfileApi {
        val userIds = mutableListOf<String>()
        val profiles = mutableListOf<com.trailmate.app.core.model.BaselineProfile>()

        override fun saveOnboardingProfile(
            userId: String,
            profile: com.trailmate.app.core.model.BaselineProfile
        ): TrailMateApiResult<TrailMateOnboardingProfileDto> {
            userIds += userId
            profiles += profile
            return result
        }
    }
}
