package com.trailmate.app.core.map

enum class AmapNetworkSettingsReturnAction {
    NONE,
    REFRESH_NETWORK_STATUS
}

object AmapNetworkSettingsReturnRefreshPolicy {
    fun resolve(pendingNetworkSettingsReturn: Boolean): AmapNetworkSettingsReturnAction =
        if (pendingNetworkSettingsReturn) {
            AmapNetworkSettingsReturnAction.REFRESH_NETWORK_STATUS
        } else {
            AmapNetworkSettingsReturnAction.NONE
        }
}

enum class AmapDownloadNetworkStatusRefreshAction {
    NONE,
    REFRESH_NETWORK_STATUS
}

object AmapDownloadNetworkStatusRefreshPolicy {
    fun resolve(routeVisible: Boolean): AmapDownloadNetworkStatusRefreshAction =
        if (routeVisible) {
            AmapDownloadNetworkStatusRefreshAction.REFRESH_NETWORK_STATUS
        } else {
            AmapDownloadNetworkStatusRefreshAction.NONE
        }
}
