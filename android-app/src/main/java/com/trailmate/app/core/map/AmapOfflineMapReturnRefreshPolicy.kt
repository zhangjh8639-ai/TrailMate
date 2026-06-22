package com.trailmate.app.core.map

enum class AmapOfflineMapReturnAction {
    NONE,
    REFRESH_DOWNLOADED_STATUS
}

object AmapOfflineMapReturnRefreshPolicy {
    fun resolve(
        pendingOfflineMapManagerReturn: Boolean,
        amapSdkAvailable: Boolean,
        amapPrivacyConsentAccepted: Boolean
    ): AmapOfflineMapReturnAction =
        if (pendingOfflineMapManagerReturn && amapSdkAvailable && amapPrivacyConsentAccepted) {
            AmapOfflineMapReturnAction.REFRESH_DOWNLOADED_STATUS
        } else {
            AmapOfflineMapReturnAction.NONE
        }
}
