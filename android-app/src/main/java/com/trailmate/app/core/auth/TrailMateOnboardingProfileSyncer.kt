package com.trailmate.app.core.auth

import com.trailmate.app.core.model.BaselineProfile
import com.trailmate.app.core.network.TrailMateApiResult
import com.trailmate.app.core.network.TrailMateUserProfileApi

enum class TrailMateOnboardingProfileSyncResult {
    SYNCED,
    SKIPPED_NO_SESSION,
    FAILED
}

class TrailMateOnboardingProfileSyncer(
    private val userProfileApi: TrailMateUserProfileApi?
) {
    fun sync(
        authSession: TrailMateAuthSession?,
        profile: BaselineProfile
    ): TrailMateOnboardingProfileSyncResult {
        val api = userProfileApi ?: return TrailMateOnboardingProfileSyncResult.SKIPPED_NO_SESSION
        val session = authSession ?: return TrailMateOnboardingProfileSyncResult.SKIPPED_NO_SESSION

        return when (api.saveOnboardingProfile(userId = session.userId, profile = profile)) {
            is TrailMateApiResult.Success -> TrailMateOnboardingProfileSyncResult.SYNCED
            is TrailMateApiResult.Failure -> TrailMateOnboardingProfileSyncResult.FAILED
        }
    }
}
