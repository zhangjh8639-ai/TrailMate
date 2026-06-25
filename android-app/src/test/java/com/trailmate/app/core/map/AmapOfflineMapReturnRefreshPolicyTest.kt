package com.trailmate.app.core.map

import org.junit.Assert.assertEquals
import org.junit.Test

class AmapOfflineMapReturnRefreshPolicyTest {
    @Test
    fun refreshesDownloadedStatusAfterReturningFromOfflineMapManager() {
        val action = AmapOfflineMapReturnRefreshPolicy.resolve(
            pendingOfflineMapManagerReturn = true,
            amapSdkAvailable = true,
            amapPrivacyConsentAccepted = true
        )

        assertEquals(AmapOfflineMapReturnAction.REFRESH_DOWNLOADED_STATUS, action)
    }

    @Test
    fun doesNothingWhenOfflineMapManagerWasNotOpened() {
        val action = AmapOfflineMapReturnRefreshPolicy.resolve(
            pendingOfflineMapManagerReturn = false,
            amapSdkAvailable = true,
            amapPrivacyConsentAccepted = true
        )

        assertEquals(AmapOfflineMapReturnAction.NONE, action)
    }

    @Test
    fun doesNotRefreshBeforeAmapCanReadOfflineStatus() {
        val action = AmapOfflineMapReturnRefreshPolicy.resolve(
            pendingOfflineMapManagerReturn = true,
            amapSdkAvailable = true,
            amapPrivacyConsentAccepted = false
        )

        assertEquals(AmapOfflineMapReturnAction.NONE, action)
    }
}
