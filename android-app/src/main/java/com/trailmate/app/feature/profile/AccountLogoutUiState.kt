package com.trailmate.app.feature.profile

data class AccountLogoutUiState(
    val isConfirmingLogout: Boolean = false
) {
    fun requestLogout(): AccountLogoutUiState =
        copy(isConfirmingLogout = true)

    fun cancelLogout(): AccountLogoutUiState =
        copy(isConfirmingLogout = false)

    fun confirmLogout(onLogout: () -> Unit): AccountLogoutUiState {
        if (isConfirmingLogout) {
            onLogout()
        }

        return copy(isConfirmingLogout = false)
    }
}
