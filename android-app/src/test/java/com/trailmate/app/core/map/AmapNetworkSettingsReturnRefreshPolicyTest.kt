package com.trailmate.app.core.map

import org.junit.Assert.assertEquals
import org.junit.Test

class AmapNetworkSettingsReturnRefreshPolicyTest {
    @Test
    fun refreshesDownloadNetworkStatusAfterReturningFromNetworkSettings() {
        val action = AmapNetworkSettingsReturnRefreshPolicy.resolve(
            pendingNetworkSettingsReturn = true
        )

        assertEquals(AmapNetworkSettingsReturnAction.REFRESH_NETWORK_STATUS, action)
    }

    @Test
    fun doesNothingWhenNetworkSettingsWereNotOpened() {
        val action = AmapNetworkSettingsReturnRefreshPolicy.resolve(
            pendingNetworkSettingsReturn = false
        )

        assertEquals(AmapNetworkSettingsReturnAction.NONE, action)
    }

    @Test
    fun refreshesDownloadNetworkStatusWhenForegroundNetworkChanges() {
        val action = AmapDownloadNetworkStatusRefreshPolicy.resolve(
            routeVisible = true
        )

        assertEquals(AmapDownloadNetworkStatusRefreshAction.REFRESH_NETWORK_STATUS, action)
    }

    @Test
    fun doesNothingForForegroundNetworkChangesWhenRouteIsNotVisible() {
        val action = AmapDownloadNetworkStatusRefreshPolicy.resolve(
            routeVisible = false
        )

        assertEquals(AmapDownloadNetworkStatusRefreshAction.NONE, action)
    }
}
